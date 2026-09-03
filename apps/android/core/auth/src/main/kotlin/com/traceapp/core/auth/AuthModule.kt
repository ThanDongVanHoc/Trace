package com.traceapp.core.auth

import com.traceapp.core.contracts.AccountSession
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalAuthBindings {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(implementation: LocalAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAccountSession(implementation: LocalAuthRepository): AccountSession
}
