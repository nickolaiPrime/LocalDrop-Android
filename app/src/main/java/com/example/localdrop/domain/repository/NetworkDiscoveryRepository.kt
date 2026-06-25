package com.example.localdrop.domain.repository

import com.example.localdrop.domain.model.NetworkDevice
import kotlinx.coroutines.flow.Flow

interface NetworkDiscoveryRepository{
    suspend fun startBroadcasting(id : String)
    suspend fun stopBroadcasting()

    fun startDiscovery() : Flow<List<NetworkDevice>>
    suspend fun stopDiscovery()
}