package com.example.localdrop.domain.usecase

import com.example.localdrop.domain.model.TransferMessage
import com.example.localdrop.domain.repository.P2pConnectionRepository
import kotlinx.coroutines.flow.Flow

class StartServerUseCase(private val repository : P2pConnectionRepository){
    suspend operator fun invoke() : Int {
        return repository.startServer()
    }
}