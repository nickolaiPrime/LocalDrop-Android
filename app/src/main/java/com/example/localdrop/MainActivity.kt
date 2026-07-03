package com.example.localdrop

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
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
import com.example.localdrop.presentation.screens.ChatScreen
import com.example.localdrop.presentation.screens.LoginScreen
import com.example.localdrop.presentation.screens.MainDropScreen

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

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding()
                    ) {
                        if (uiState.myDeviceName.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                LoginScreen { name ->
                                    viewModel.initDevice(name)
                                    viewModel.startScanning()
                                }
                            }
                        } else {
                            if (uiState.selectedDevice == null) {
                                MainDropScreen(
                                    uiState = uiState,
                                    onDeviceSelected = { device ->
                                        Log.d("MainActivity", "Выбрано устройство: ${device.ipAddress}")
                                        viewModel.selectDevice(device)
                                    }
                                )
                            } else {
                                ChatScreen(
                                    uiState = uiState,
                                    onBackClick = { viewModel.goBackToDeviceList() },
                                    onSendClick = { text ->
                                        val target = uiState.selectedDevice
                                        if (target != null) {
                                            viewModel.sendText(target, text)
                                        }
                                    }
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}