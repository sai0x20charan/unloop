package com.charan.unloop.core.di

import com.charan.unloop.data.repository.InstalledAppsRepository
import com.charan.unloop.data.repository.RoutineRepository
import com.charan.unloop.data.repository.SessionEventRepository
import com.charan.unloop.data.repository.SessionManagerRepository
import com.charan.unloop.data.repository.impl.DataStoreRepositoryImpl
import com.charan.unloop.data.repository.impl.InstalledAppsRepositoryImpl
import com.charan.unloop.data.repository.impl.RoutineRepositoryImpl
import com.charan.unloop.data.repository.impl.SessionEventRepositoryImpl
import com.charan.unloop.data.repository.impl.SessionManagerRepositoryImpl
import com.charan.unloop.data.repository.impl.SessionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindInstalledAppsRepository(
        installedAppsRepositoryImpl: InstalledAppsRepositoryImpl
    ): InstalledAppsRepository

    @Binds
    @Singleton
    abstract fun bindRoutineRepository(
        routineRepositoryImpl: RoutineRepositoryImpl
    ): RoutineRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        sessionRepositoryImpl: SessionRepositoryImpl
    ): com.charan.unloop.data.repository.SessionRepository

    @Binds
    @Singleton
    abstract fun bindDataStoreRepository(
        dataStoreRepositoryImpl: DataStoreRepositoryImpl
    ): com.charan.unloop.data.repository.DataStoreRepository

    @Binds
    @Singleton
    abstract fun bindSessionEventRepository(
        sessionEventRepositoryImpl: SessionEventRepositoryImpl
    ): SessionEventRepository

    @Binds
    @Singleton
    abstract fun bindSessionManagerRepository(
        sessionManagerRepositoryImpl: SessionManagerRepositoryImpl
    ): SessionManagerRepository
}
