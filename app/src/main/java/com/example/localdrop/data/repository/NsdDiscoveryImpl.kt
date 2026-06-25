package com.example.localdrop.data.repository

import android.content.Context
import com.example.localdrop.domain.model.NetworkDevice
import com.example.localdrop.domain.repository.NetworkDiscoveryRepository
import kotlinx.coroutines.flow.Flow
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class NsdDiscoveryImpl(private val context : Context) : NetworkDiscoveryRepository{
    private val registrationListener = object : NsdManager.RegistrationListener{
        override fun onServiceRegistered(nsdServiceInfo: NsdServiceInfo?) {
            Log.d("NSD_TAG", "Сервис успешно зарегистрирован")
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
            Log.d("Ошибка регистрации : $errorCode", "Вещание остановлено")
        }

        override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) {
            Log.d("NSD_TAG", "Сервис успешно разарегистрирован")
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
            Log.d("NSD_TAG", "Сервис не смог разрегестрироваться")
        }
    }
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager


    override suspend fun startBroadcasting(id: String) {
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = id
            serviceType = "_localDrop._tcp."
            port = 7777
        }
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    override suspend fun stopBroadcasting() {
        nsdManager.unregisterService(registrationListener)
    }

    override fun startDiscovery(): Flow<List<NetworkDevice>> {
        return callbackFlow {
            val discoveredDevices = mutableSetOf<NetworkDevice>()
            val discoveryListener = object : NsdManager.DiscoveryListener{
                override fun onDiscoveryStarted(message: String?) {
                    Log.d("NSD_TAG", message?: "")
                }

                override fun onDiscoveryStopped(message: String?) {
                    Log.d("NSD_TAG", message?: "")
                }
                @Suppress("DEPRECATION")
                override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                    nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                            Log.d("NSD_TAG", "Не удалось разрешить адрес: $errorCode")
                        }

                        override fun onServiceResolved(resolvedInfo: NsdServiceInfo) {
                            val ip = resolvedInfo.host?.hostAddress ?: ""

                            val device = NetworkDevice(
                                name = resolvedInfo.serviceName,
                                ipAddress = ip,
                                port = resolvedInfo.port
                            )

                            discoveredDevices.add(device)
                            trySend(discoveredDevices.toList())
                        }
                    })
                }

                override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                    discoveredDevices.removeAll { it.name == serviceInfo.serviceName}
                    trySend(discoveredDevices.toList())
                }

                override fun onStartDiscoveryFailed(message: String?, errorCode: Int) {
                    Log.d("$errorCode", message?: "")
                }

                override fun onStopDiscoveryFailed(message: String?, errorCode : Int) {
                    Log.d("$errorCode", message?: "")
                }

            }
            nsdManager.discoverServices("_localDrop._tcp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
            awaitClose { nsdManager.stopServiceDiscovery(discoveryListener)}
        }
    }

    override suspend fun stopDiscovery() {
        TODO("Not yet implemented")
    }
}