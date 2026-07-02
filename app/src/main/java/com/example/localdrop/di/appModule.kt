package com.example.localdrop.di

import android.content.Context
import android.net.nsd.NsdManager
import com.example.localdrop.data.repository.NsdDiscoveryImpl
import com.example.localdrop.data.repository.P2pConnectionImpl
import com.example.localdrop.domain.repository.NetworkDiscoveryRepository
import com.example.localdrop.domain.repository.P2pConnectionRepository
import com.example.localdrop.domain.usecase.ObserveMessagesUseCase
import com.example.localdrop.domain.usecase.SendMessageUseCase
import com.example.localdrop.domain.usecase.StartBroadcastingUseCase
import com.example.localdrop.domain.usecase.StartDiscoveryUseCase
import com.example.localdrop.domain.usecase.StartServerUseCase
import com.example.localdrop.domain.usecase.StopBroadcastingUseCase
import com.example.localdrop.presentation.viewmodel.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.plugin.module.dsl.viewModel
import kotlin.coroutines.EmptyCoroutineContext.get

val appModule = module{
    single<NsdManager>{
        androidContext().getSystemService(Context.NSD_SERVICE) as NsdManager
    }

    single<NetworkDiscoveryRepository>{ NsdDiscoveryImpl(get()) }
    single<P2pConnectionRepository>{ P2pConnectionImpl() }

    factory { StartBroadcastingUseCase(get()) }
    factory { SendMessageUseCase(get()) }
    factory { StartDiscoveryUseCase(get()) }
    factory { StartServerUseCase(get()) }
    factory { StopBroadcastingUseCase(get()) }
    factory { ObserveMessagesUseCase(get()) }

    viewModel{ MainViewModel(get(),get(),get(),get(), get()) }
}