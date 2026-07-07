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
import java.net.NetworkInterface
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class NsdDiscoveryImpl(private val context : Context) : NetworkDiscoveryRepository{

    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
    val multicastLock = wifiManager.createMulticastLock("LocalDropLock").apply {
        setReferenceCounted(false)
    }
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


    override suspend fun startBroadcasting(id: String, port : Int) {
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = id
            serviceType = "_localdrop._tcp."
            this.port = port
        }
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    override suspend fun stopBroadcasting() {
        nsdManager.unregisterService(registrationListener)
    }

    override fun startDiscovery(myDeviceName : String): Flow<List<NetworkDevice>> {
        return callbackFlow {

            val discoveredDevices = ConcurrentHashMap<String, NetworkDevice>()
            val myCurrentIp = getLocalIpAddress()

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

                            if(ip.isNotEmpty() && ip == myCurrentIp){
                                Log.d("NSD_TAG", "Фильтр собственного IP: $ip")
                                return
                            }


                            val device = NetworkDevice(
                                name = resolvedInfo.serviceName,
                                ipAddress = ip,
                                port = resolvedInfo.port
                            )

                            discoveredDevices[resolvedInfo.serviceName] = device
                            trySend(discoveredDevices.values.toList())
                        }
                    })
                }

                override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                    discoveredDevices.remove(serviceInfo.serviceName)
                    trySend(discoveredDevices.values.toList())
                }

                override fun onStartDiscoveryFailed(message: String?, errorCode: Int) {
                    Log.d("$errorCode", message?: "")
                }

                override fun onStopDiscoveryFailed(message: String?, errorCode : Int) {
                    Log.d("$errorCode", message?: "")
                }

            }
            multicastLock.acquire()
            nsdManager.discoverServices("_localdrop._tcp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
            awaitClose {
                nsdManager.stopServiceDiscovery(discoveryListener)
                if (multicastLock.isHeld) multicastLock.release()
            }
        }
    }

    override suspend fun stopDiscovery() {
        TODO("Not yet implemented")
    }

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                val addresses = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress) {
                        val sAddr = address.hostAddress
                        val isIPv4 = sAddr.indexOf(':') < 0 // Если нет двоеточий, это IPv4
                        if (isIPv4) return sAddr
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("NSD_TAG", "Не удалось получить собственный IP: ${e.message}")
        }
        return ""
    }
}