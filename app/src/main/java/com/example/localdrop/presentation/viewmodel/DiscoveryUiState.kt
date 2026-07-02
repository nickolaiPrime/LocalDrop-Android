package com.example.localdrop.presentation.viewmodel

import com.example.localdrop.domain.model.NetworkDevice
import com.example.localdrop.domain.model.TransferMessage

data class DiscoveryUiState(
    val myDeviceName : String = "",
    val isBroadcasting : Boolean = false,
    val isScanning : Boolean = true,
    val discoveredDevices : List<NetworkDevice> = emptyList(),
    val messages : List<TransferMessage> = emptyList(),
    val selectedDevice : NetworkDevice? = null
)