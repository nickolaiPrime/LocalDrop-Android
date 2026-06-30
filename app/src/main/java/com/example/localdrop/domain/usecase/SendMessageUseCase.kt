package com.example.localdrop.domain.usecase

import com.example.localdrop.domain.model.NetworkDevice
import com.example.localdrop.domain.repository.P2pConnectionRepository

class SendMessageUseCase(private val repository : P2pConnectionRepository){
    suspend operator fun invoke(targetDevice : NetworkDevice, text : String){
        repository.sendMessage(targetDevice, text)
    }
}