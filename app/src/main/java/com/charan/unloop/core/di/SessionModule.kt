package com.charan.unloop.core.di

import com.charan.unloop.session.SessionController
import com.charan.unloop.session.SessionManager
import com.charan.unloop.session.impl.SessionControllerImpl
import com.charan.unloop.session.impl.SessionManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {

    @Binds
    @Singleton
    abstract fun bindSessionController(
        sessionControllerImpl: SessionControllerImpl
    ): SessionController

    @Binds
    @Singleton
    abstract fun bindSessionManager(
        sessionManagerImpl: SessionManagerImpl
    ): SessionManager
}
