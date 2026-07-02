package com.example.localdrop.domain.repository

import com.example.localdrop.domain.model.NetworkDevice
import kotlinx.coroutines.flow.Flow

interface NetworkDiscoveryRepository{
    suspend fun startBroadcasting(id : String, port : Int)
    suspend fun stopBroadcasting()

    fun startDiscovery(myDeviceName : String) : Flow<List<NetworkDevice>>
    suspend fun stopDiscovery()
}