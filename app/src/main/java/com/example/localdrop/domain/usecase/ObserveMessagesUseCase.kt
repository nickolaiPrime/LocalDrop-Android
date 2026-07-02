package com.example.localdrop.domain.usecase

import com.example.localdrop.domain.model.TransferMessage
import com.example.localdrop.domain.repository.P2pConnectionRepository
import kotlinx.coroutines.flow.Flow

class ObserveMessagesUseCase(private val repository: P2pConnectionRepository) {
    operator fun invoke() : Flow<TransferMessage> {
        return repository.observeMessages()
    }
}