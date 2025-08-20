package com.axesoft.treatment_manager.ui.di

import androidx.lifecycle.SavedStateHandle
import com.axesoft.treatment_manager.ui.destination.ConnectivityLabsDestination
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
internal class ConnectivityLabArgsModule {

    @Provides
    fun provideConnectivityLabArgs(savedStateHandle: SavedStateHandle): ConnectivityLabsDestination.ConnectivityLabArgs {
        return ConnectivityLabsDestination.getArguments(savedStateHandle)
    }
}
