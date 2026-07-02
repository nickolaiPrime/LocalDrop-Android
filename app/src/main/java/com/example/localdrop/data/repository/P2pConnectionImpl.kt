package com.example.localdrop.data.repository

import android.util.Log
import com.example.localdrop.domain.model.NetworkDevice
import com.example.localdrop.domain.model.TransferMessage
import com.example.localdrop.domain.repository.P2pConnectionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket

class P2pConnectionImpl : P2pConnectionRepository {

    private var serverSocket : ServerSocket? = null
    private var clientSocket : Socket? = null
    private val messageFlow = MutableSharedFlow<TransferMessage>()

    override suspend fun startServer(): Int {
        val socket = ServerSocket(0)
        serverSocket = socket
        Thread {
            while (!socket.isClosed){
                val clientSocket = socket.accept()
                Thread {
                    clientSocket.soTimeout = 5000
                    try {
                        val inputStream = DataInputStream(clientSocket.getInputStream())
                        val messageBytes = ByteArray(inputStream.readInt())
                        inputStream.readFully(messageBytes)
                        val text = String(messageBytes, Charsets.UTF_8)

                        val message = TransferMessage(text = text, isFromMe = false, timestamp = System.currentTimeMillis())
                        messageFlow.tryEmit(message)
                    }
                    catch(e: Exception){
                        Log.d("P2P_SERVER", "Сервер остановлен или произошла ошибка:${e.message}")
                    }
                    finally {
                        try {
                            clientSocket.close()
                        }
                        catch(e: Exception){
                            Log.d("P2P_SERVER", "Сервер остановлен или произошла ошибка:${e.message}")
                        }
                    }
                }.start()
            }
        }.start()
        return socket.localPort
    }

    override fun observeMessages(): Flow<TransferMessage> {
        return messageFlow.asSharedFlow()
    }

    override suspend fun stopServer() {
        serverSocket?.close()
    }

    override suspend fun sendMessage(
        targetDevice: NetworkDevice,
        text: String
    ) {
        withContext(Dispatchers.IO){
            try {
                val socket = Socket(targetDevice.ipAddress, targetDevice.port)
                val bytes = text.toByteArray(Charsets.UTF_8)
                val outputStream = DataOutputStream(socket.getOutputStream())
                outputStream.writeInt(bytes.size)
                outputStream.write(bytes)
                outputStream.flush()
                socket.close()
            }
            catch (e: Exception){
                throw e
            }
        }
    }
}