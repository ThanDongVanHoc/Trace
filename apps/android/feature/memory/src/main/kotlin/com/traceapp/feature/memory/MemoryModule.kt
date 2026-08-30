package com.traceapp.feature.memory

import com.traceapp.core.contracts.MemoryApi
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MemoryModule {
    @Binds
    @Singleton
    abstract fun bindMemoryApi(implementation: MemoryService): MemoryApi
}
