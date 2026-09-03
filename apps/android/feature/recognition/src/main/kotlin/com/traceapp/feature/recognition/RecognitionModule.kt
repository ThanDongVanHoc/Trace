package com.traceapp.feature.recognition

import com.traceapp.core.contracts.RecognitionApi
import com.traceapp.core.contracts.VisualEncoder
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RecognitionModule {
    @Binds
    @Singleton
    abstract fun bindVisualEncoder(implementation: OnDeviceVisualEngine): VisualEncoder

    @Binds
    @Singleton
    abstract fun bindRecognitionApi(implementation: OnDeviceVisualEngine): RecognitionApi
}
