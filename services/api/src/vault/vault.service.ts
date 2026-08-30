import { BadRequestException, Inject, Injectable } from '@nestjs/common';
import { VAULT_ENGINE, type VaultEngine } from './vault-engine.js';
import type {
  OpenedVaultResponseDto,
  OpenVaultDto,
  SealedVaultResponseDto,
  SealVaultDto,
} from './vault.dto.js';

@Injectable()
export class VaultService {
  constructor(
    @Inject(VAULT_ENGINE)
    private readonly engine: VaultEngine,
  ) {}

  async seal(dto: SealVaultDto): Promise<SealedVaultResponseDto> {
    const plaintext = Buffer.from(dto.plaintextBase64, 'base64');
    this.assertPayloadSize(plaintext);
    const result = await this.engine.seal({
      plaintext,
      associatedData: Buffer.from(dto.associatedData, 'utf8'),
    });
    return {
      algorithm: result.algorithm,
      keyVersion: result.keyVersion,
      nonceBase64: result.nonce.toString('base64'),
      ciphertextBase64: result.ciphertext.toString('base64'),
      authenticationTagBase64: result.authenticationTag.toString('base64'),
      warnings: [...result.warnings],
    };
  }

  async open(dto: OpenVaultDto): Promise<OpenedVaultResponseDto> {
    const nonce = Buffer.from(dto.nonceBase64, 'base64');
    const ciphertext = Buffer.from(dto.ciphertextBase64, 'base64');
    const authenticationTag = Buffer.from(
      dto.authenticationTagBase64,
      'base64',
    );
    this.assertPayloadSize(ciphertext);
    if (nonce.length !== 12 || authenticationTag.length !== 16) {
      throw new BadRequestException({
        code: 'CRYPTO_FAILURE',
        message: 'AES-GCM requires a 12-byte nonce and 16-byte tag',
      });
    }
    const plaintext = await this.engine.open({
      nonce,
      ciphertext,
      authenticationTag,
      associatedData: Buffer.from(dto.associatedData, 'utf8'),
      keyVersion: dto.keyVersion,
    });
    return {
      plaintextBase64: plaintext.toString('base64'),
      keyVersion: dto.keyVersion,
    };
  }

  private assertPayloadSize(value: Buffer): void {
    if (value.length === 0 || value.length > MAX_PAYLOAD_SIZE_BYTES) {
      throw new BadRequestException(
        'Vault payload must be between 1 B and 1 MiB',
      );
    }
  }
}

const MAX_PAYLOAD_SIZE_BYTES = 1024 * 1024;
