package com.example.localdrop.domain.usecase

import com.example.localdrop.domain.repository.NetworkDiscoveryRepository

class StopBroadcastingUseCase(private val repository: NetworkDiscoveryRepository){
    suspend operator fun invoke(){
        repository.stopBroadcasting()
    }
}