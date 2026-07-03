package com.example.localdrop.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.localdrop.presentation.viewmodel.DiscoveryUiState

@Composable
fun ChatScreen(
    uiState : DiscoveryUiState,
    onBackClick: () -> Unit,
    onSendClick: (String) -> Unit
){
    BackHandler(
        enabled = true,
        onBack = onBackClick
    )
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){

        Button(
            onClick = onBackClick
        ){
            Text(text = "Назад")
        }

        Text(
            text = "Чат с устройством: ${uiState.selectedDevice?.ipAddress}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ){
            items(uiState.messages){ message ->
                val alignment = if (message.isFromMe) Alignment.CenterEnd else Alignment.CenterStart
                val color = if (message.isFromMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = alignment
                ){
                    Card(
                        colors = CardDefaults.cardColors(containerColor = color)
                    ){
                        Text(
                            text = message.text,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            OutlinedTextField(
                value = inputText,
                onValueChange = {inputText = it},
                modifier = Modifier.weight(1f),
                placeholder = { Text(text = "Введите сообщение...") }
            )

            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )

            Button(
                onClick = {
                    if(inputText.isNotBlank()){
                        onSendClick(inputText)
                        inputText = ""
                    }
                }
            ){
                Text(text = "Отправить")
            }
        }
    }

}