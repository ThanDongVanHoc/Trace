package com.traceapp.feature.enrollment

import com.traceapp.core.contracts.EnrollmentApi
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EnrollmentModule {
    @Binds
    @Singleton
    abstract fun bindEnrollmentApi(implementation: EnrollmentService): EnrollmentApi
}
