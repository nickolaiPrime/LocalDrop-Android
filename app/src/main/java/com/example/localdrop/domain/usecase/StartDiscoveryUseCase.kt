package com.example.localdrop.domain.usecase

import com.example.localdrop.domain.model.NetworkDevice
import com.example.localdrop.domain.repository.NetworkDiscoveryRepository
import kotlinx.coroutines.flow.Flow

class StartDiscoveryUseCase(private val repository: NetworkDiscoveryRepository){
    operator fun invoke(myDeviceName : String) : Flow<List<NetworkDevice>> {
        return repository.startDiscovery(myDeviceName)
    }
}