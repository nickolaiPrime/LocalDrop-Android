package com.example.localdrop

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.localdrop.presentation.viewmodel.DiscoveryUiState
import com.example.localdrop.presentation.viewmodel.MainViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import com.example.localdrop.domain.model.NetworkDevice

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme{
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ){
                    val viewModel : MainViewModel = koinViewModel()
                    val uiState by viewModel.uiState.collectAsState()

                    if(uiState.myDeviceName.isEmpty()){
                        LoginScreen(){
                            viewModel.initDevice(it)
                            viewModel.startScanning()
                        }
                    }
                    else{
                        if(uiState.selectedDevice == null){
                            MainDropScreen(
                                uiState = uiState,
                                onDeviceSelected = { device->
                                    Log.d("MainActivity", "Выбрано устройство: ${device.ipAddress}")
                                    viewModel.selectDevice(device)
                                }
                            )
                        }
                        else{
                            ChatScreen(
                                uiState = uiState,
                                onBackClick = { viewModel.goBackToDeviceList() },
                                onSendClick = { text ->
                                    Log.d("MainActivity", "Хотим отправить: $text")
                                }
                            ){}
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    onNameEntered : (String) -> Unit
){
    var inputName by remember { mutableStateOf("") }

    Column(){
        OutlinedTextField(
            value = inputName,
            onValueChange = {inputName = it}
        )

        Button(
            onClick = { onNameEntered(inputName) }
        ){
            Text(text = "Войти в сеть")
        }
    }
}

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

@Composable
fun ChatScreen(
    uiState : DiscoveryUiState,
    onBackClick: () -> Unit,
    onSendClick: (String) -> Unit
){
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
                Text(
                    text = message.text,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
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