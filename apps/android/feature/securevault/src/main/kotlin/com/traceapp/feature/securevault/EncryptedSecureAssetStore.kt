package com.traceapp.feature.securevault

import android.content.Context
import com.traceapp.core.contracts.AccountSession
import com.traceapp.core.contracts.SecureAsset
import com.traceapp.core.contracts.SecureAssetStore
import com.traceapp.core.contracts.SecureAssetType
import com.traceapp.core.contracts.TraceError
import com.traceapp.core.contracts.TraceErrorCode
import com.traceapp.core.contracts.TraceResult
import com.traceapp.core.database.SecureAssetDao
import com.traceapp.core.database.SecureAssetEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class EncryptedSecureAssetStore @Inject constructor(
    @ApplicationContext context: Context,
    private val accountSession: AccountSession,
    private val assetDao: SecureAssetDao,
    private val vault: CryptoVault,
) : SecureAssetStore {
    private val assetDirectory = File(context.noBackupFilesDir, ASSET_DIRECTORY).apply { mkdirs() }

    override suspend fun write(
        ownerRecordId: String,
        type: SecureAssetType,
        plaintext: ByteArray,
        mimeType: String,
    ): TraceResult<SecureAsset> = withContext(Dispatchers.IO) {
        val accountId = accountSession.currentAccountId()
            ?: return@withContext unauthorized()
        if (ownerRecordId.isBlank() || plaintext.isEmpty() || mimeType.isBlank()) {
            return@withContext invalid("Secure asset input is incomplete")
        }
        val assetId = UUID.randomUUID().toString()
        val context = assetContext(accountId, ownerRecordId, assetId, type, mimeType)
        var finalFile: File? = null
        try {
            val envelope = vault.seal(plaintext, context)
            val target = checkedAssetFile("$assetId.bin")
            val temporary = File.createTempFile("asset-", ".tmp", assetDirectory)
            try {
                FileOutputStream(temporary).use { output ->
                    output.write(envelope.cipherTextAndTag)
                    output.fd.sync()
                }
                check(temporary.renameTo(target)) { "Could not publish encrypted asset" }
                finalFile = target
            } finally {
                temporary.delete()
            }
            val now = System.currentTimeMillis()
            assetDao.insert(
                SecureAssetEntity(
                    assetId = assetId,
                    accountId = accountId,
                    ownerRecordId = ownerRecordId,
                    type = type.name,
                    relativePath = target.name,
                    nonce = envelope.nonce,
                    envelopeVersion = envelope.envelopeVersion,
                    algorithm = envelope.algorithm,
                    keyId = envelope.keyId,
                    mimeType = mimeType,
                    createdAtEpochMillis = now,
                ),
            )
            TraceResult.Success(SecureAsset(assetId, ownerRecordId, type, mimeType, now))
        } catch (failure: Exception) {
            finalFile?.delete()
            failure.toTraceFailure("Could not encrypt and store asset")
        }
    }

    override suspend fun read(
        assetId: String,
        expectedOwnerRecordId: String,
        expectedType: SecureAssetType,
    ): TraceResult<ByteArray> = withContext(Dispatchers.IO) {
        val accountId = accountSession.currentAccountId()
            ?: return@withContext unauthorized()
        try {
            val entity = assetDao.get(accountId, assetId)
                ?: return@withContext notFound("Secure asset was not found")
            if (entity.ownerRecordId != expectedOwnerRecordId || entity.type != expectedType.name) {
                return@withContext cryptoFailure("Secure asset context does not match")
            }
            val encrypted = checkedAssetFile(entity.relativePath).readBytes()
            val envelope = VaultEnvelope(
                envelopeVersion = entity.envelopeVersion,
                algorithm = entity.algorithm,
                keyId = entity.keyId,
                nonce = entity.nonce,
                cipherTextAndTag = encrypted,
            )
            TraceResult.Success(
                vault.open(
                    envelope,
                    assetContext(
                        accountId,
                        expectedOwnerRecordId,
                        assetId,
                        expectedType,
                        entity.mimeType,
                    ),
                ),
            )
        } catch (failure: Exception) {
            failure.toTraceFailure("Could not authenticate secure asset")
        }
    }

    override suspend fun delete(assetId: String): TraceResult<Unit> = withContext(Dispatchers.IO) {
        val accountId = accountSession.currentAccountId()
            ?: return@withContext unauthorized()
        try {
            val entity = assetDao.get(accountId, assetId)
            assetDao.delete(accountId, assetId)
            entity?.let { checkedAssetFile(it.relativePath).delete() }
            TraceResult.Success(Unit)
        } catch (failure: Exception) {
            failure.toTraceFailure("Could not delete secure asset")
        }
    }

    private fun checkedAssetFile(relativePath: String): File {
        require(FILE_NAME.matches(relativePath))
        val directory = assetDirectory.canonicalFile
        val candidate = File(directory, relativePath).canonicalFile
        require(candidate.parentFile == directory)
        return candidate
    }

    private fun assetContext(
        accountId: String,
        ownerRecordId: String,
        assetId: String,
        type: SecureAssetType,
        mimeType: String,
    ) = VaultContext(
        accountId = accountId,
        recordId = "$ownerRecordId:$assetId",
        field = "ASSET:${type.name}:$mimeType",
    )

    private companion object {
        const val ASSET_DIRECTORY = "secure-assets-v1"
        val FILE_NAME = Regex("[0-9a-fA-F-]{36}\\.bin")
    }
}

internal fun Throwable.toTraceFailure(message: String): TraceResult.Failure = when (this) {
    is VaultException -> cryptoFailure(message, this)
    else -> TraceResult.Failure(TraceError(TraceErrorCode.STORAGE_FAILURE, message, this))
}

internal fun unauthorized() = TraceResult.Failure(
    TraceError(TraceErrorCode.UNAUTHORIZED, "Bạn cần đăng nhập trên thiết bị."),
)

internal fun invalid(message: String) = TraceResult.Failure(
    TraceError(TraceErrorCode.INVALID_INPUT, message),
)

internal fun notFound(message: String) = TraceResult.Failure(
    TraceError(TraceErrorCode.NOT_FOUND, message),
)

internal fun cryptoFailure(message: String, cause: Throwable? = null) = TraceResult.Failure(
    TraceError(TraceErrorCode.CRYPTO_FAILURE, message, cause),
)
