package com.example.localdrop.domain.usecase

import com.example.localdrop.domain.repository.NetworkDiscoveryRepository

class StartBroadcastingUseCase(private val repository : NetworkDiscoveryRepository){
    suspend operator fun invoke(id : String, port : Int){
        repository.startBroadcasting(id, port)
    }
}