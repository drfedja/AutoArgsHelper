package com.axesoft.treatment_manager.data.di

import android.content.Context
import com.axesoft.treatment_manager.data.data.LoopbackRepositoryImpl
import com.axesoft.treatment_manager.data.data.PermissionRepositoryImpl
import com.axesoft.treatment_manager.data.data.WebSocketRepositoryImpl
import com.axesoft.treatment_manager.data.data.WifiP2pManagerWrapperImpl
import com.axesoft.treatment_manager.repository.LoopbackRepository
import com.axesoft.treatment_manager.repository.PermissionRepository
import com.axesoft.treatment_manager.repository.WebSocketRepository
import com.axesoft.treatment_manager.repository.WifiP2pManagerWrapper
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
internal abstract class LoopbackModule {

    @Binds
    abstract fun bindLoopbackRepository(
        impl: LoopbackRepositoryImpl
    ): LoopbackRepository
}

@Module
@InstallIn(SingletonComponent::class)
internal object WifiModule {

    @Provides
    fun provideWifiP2pManagerWrapper(
        @ApplicationContext context: Context
    ): WifiP2pManagerWrapper = WifiP2pManagerWrapperImpl(context)
}

@Module
@InstallIn(SingletonComponent::class)
internal object PermissionRepositoryModule {

    @Provides
    fun providePermissionRepository(
        @ApplicationContext context: Context
    ): PermissionRepository = PermissionRepositoryImpl(context)
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideWebSocketRepository(): WebSocketRepository = WebSocketRepositoryImpl()
}
