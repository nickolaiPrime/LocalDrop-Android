package com.example.localdrop.domain.model

data class TransferMessage(
    val text : String,
    val isFromMe : Boolean,
    val timestamp : Long,
    val dialogKey : String?
)