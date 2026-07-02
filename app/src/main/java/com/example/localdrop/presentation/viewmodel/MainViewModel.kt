package com.example.localdrop.presentation.viewmodel

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localdrop.domain.model.NetworkDevice
import com.example.localdrop.domain.model.TransferMessage
import com.example.localdrop.domain.usecase.ObserveMessagesUseCase
import com.example.localdrop.domain.usecase.SendMessageUseCase
import com.example.localdrop.domain.usecase.StartBroadcastingUseCase
import com.example.localdrop.domain.usecase.StartDiscoveryUseCase
import com.example.localdrop.domain.usecase.StartServerUseCase
import com.example.localdrop.domain.usecase.StopBroadcastingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val startBroadcastingUseCase: StartBroadcastingUseCase,
    private val stopBroadcastingUseCase: StopBroadcastingUseCase,
    private val startServerUseCase: StartServerUseCase,
    private val startDiscoveryUseCase: StartDiscoveryUseCase,
    private val observeMessagesUseCase: ObserveMessagesUseCase
) : ViewModel(){
    private val _uiState = MutableStateFlow(DiscoveryUiState())
    val uiState : StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    private val myId = java.util.UUID.randomUUID().toString()

    fun initDevice(name : String){
        val fullNetworkName = "$name:$myId"
        _uiState.value = _uiState.value.copy(myDeviceName = fullNetworkName)
        viewModelScope.launch {
            val port = startServerUseCase()
            startBroadcastingUseCase(id = fullNetworkName, port = port)
            observeMessagesUseCase().collect{ message ->
                _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + message)
            }
        }
    }

    fun startScanning(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            startDiscoveryUseCase(_uiState.value.myDeviceName).collect { devicesList ->
                _uiState.value = _uiState.value.copy(discoveredDevices = devicesList)
            }
        }
    }

    fun sendText(device : NetworkDevice, text : String){
        viewModelScope.launch {
            sendMessageUseCase(device, text)
            val message = TransferMessage(text = text, isFromMe = true, timestamp = System.currentTimeMillis())
            _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + message)
        }
    }
}