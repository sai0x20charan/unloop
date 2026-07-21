package com.charan.stepaway.core.di

import com.charan.stepaway.data.repository.InstalledAppsRepository
import com.charan.stepaway.data.repository.RoutineRepository
import com.charan.stepaway.data.repository.impl.InstalledAppsRepositoryImpl
import com.charan.stepaway.data.repository.impl.RoutineRepositoryImpl
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
}
