package com.example.localdrop.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.localdrop.domain.model.NetworkDevice
import com.example.localdrop.presentation.viewmodel.DiscoveryUiState

@Composable
fun MainDropScreen(
    uiState : DiscoveryUiState,
    onDeviceSelected : (NetworkDevice) -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        Card(
            modifier = Modifier
                .fillMaxWidth()
        ){
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ){
                Text(text = "Вы в сети как ${uiState.myDeviceName.substringBefore(":")}")
                if(uiState.isScanning){
                    Text(text = "Сканирование активно...")
                }
                else{
                    Text(text = "Ожидание...")
                }
            }
        }

        Spacer(
            modifier = Modifier
                .height(16.dp)
        )

        Text(text = "Устройства поблизости:")

        Spacer(
            modifier = Modifier
                .height(8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            items(uiState.discoveredDevices) { device ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onDeviceSelected(device) }
                ){
                    Text(text = "Устройство : ${device.ipAddress}:${device.port}", Modifier.padding(16.dp))
                }
            }
        }
    }
}