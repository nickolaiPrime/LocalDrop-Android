package com.example.localdrop.data.repository

import android.util.Log
import com.example.localdrop.domain.model.NetworkDevice
import com.example.localdrop.domain.model.TransferMessage
import com.example.localdrop.domain.repository.P2pConnectionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket

class P2pConnectionImpl(private val externalScope: CoroutineScope) : P2pConnectionRepository {

    private var serverSocket : ServerSocket? = null
    private val messageFlow = MutableSharedFlow<TransferMessage>()

    override suspend fun startServer(): Int = withContext(Dispatchers.IO) {
        val socket = ServerSocket(0)
        serverSocket = socket
        externalScope.launch(Dispatchers.IO) {
            try {
                while (isActive && !socket.isClosed) {
                    val clientSocket = socket.accept()

                    launch {
                        clientSocket.soTimeout = 5000
                        try {
                            val inputStream = DataInputStream(clientSocket.getInputStream())

                            val length = inputStream.readInt()
                            if (length > 0) {
                                val messageBytes = ByteArray(length)
                                inputStream.readFully(messageBytes)
                                val text = String(messageBytes, Charsets.UTF_8)

                                val message = TransferMessage(
                                    text = text,
                                    isFromMe = false,
                                    timestamp = System.currentTimeMillis(),
                                    dialogKey = clientSocket.inetAddress.hostAddress.removePrefix("/")
                                )
                                messageFlow.emit(message)
                            }
                        } catch (e: Exception) {
                            Log.e("P2P_SERVER", "Ошибка чтения данных клиента: ${e.message}")
                        } finally {
                            try {
                                clientSocket.close()
                            } catch (e: Exception) {
                                Log.e("P2P_SERVER", "Ошибка закрытия клиентского сокета: ${e.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("P2P_SERVER", "Серверный сокет закрыт или произошла ошибка: ${e.message}")
            }
        }

        return@withContext socket.localPort
    }

    override fun observeMessages(): Flow<TransferMessage> {
        return messageFlow.asSharedFlow()
    }

    override suspend fun stopServer() {
        withContext(Dispatchers.IO) {
            serverSocket?.close()
            serverSocket = null
        }
    }

    override suspend fun sendMessage(targetDevice: NetworkDevice, text: String) {
        withContext(Dispatchers.IO) {
            var socket: Socket? = null
            try {
                socket = Socket()
                socket.connect(java.net.InetSocketAddress(targetDevice.ipAddress, targetDevice.port), 2000)

                val bytes = text.toByteArray(Charsets.UTF_8)
                val outputStream = DataOutputStream(socket.getOutputStream())
                outputStream.writeInt(bytes.size)
                outputStream.write(bytes)
                outputStream.flush()

                val message = TransferMessage(text = text, isFromMe = true, timestamp = System.currentTimeMillis(), dialogKey = targetDevice.name)
                messageFlow.emit(message)
            } catch (e: Exception) {
                Log.e("P2P_SERVER", "Ошибка отправки сообщения на ${targetDevice.ipAddress}:${targetDevice.port}: ${e.message}")
            } finally {
                try {
                    socket?.close()
                } catch (e: Exception) {
                    Log.e("P2P_SERVER", "Ошибка закрытия сокета отправки: ${e.message}")
                }
            }
        }
    }
}