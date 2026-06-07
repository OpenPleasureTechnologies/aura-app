package org.opt.aura.ble

import android.bluetooth.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

data class ConnectionState(
    val isConnected: Boolean = false,
    val deviceName: String? = null,
    val error: String? = null
)

class BleConnector(private val context: Context) {
    
    private var bluetoothGatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    
    private val _connectionState = MutableStateFlow(ConnectionState())
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val mainHandler = Handler(Looper.getMainLooper())
    private var connectionTimeoutJob: Job? = null
    
    // Known device configurations
    private val deviceConfigs = mapOf(
        "Lovense" to DeviceConfig(
            serviceUuid = "0000fff0-0000-1000-8000-00805f9b34fb",
            writeCharUuid = "0000fff2-0000-1000-8000-00805f9b34fb",
            intensityMin = 0,
            intensityMax = 20
        ),
        "We-Vibe" to DeviceConfig(
            serviceUuid = "0000180d-0000-1000-8000-00805f9b34fb",
            writeCharUuid = "00002a37-0000-1000-8000-00805f9b34fb",
            intensityMin = 0,
            intensityMax = 100
        ),
        "Kiiroo" to DeviceConfig(
            serviceUuid = "0000fe51-0000-1000-8000-00805f9b34fb",
            writeCharUuid = "0000fe52-0000-1000-8000-00805f9b34fb",
            intensityMin = 0,
            intensityMax = 99
        ),
        "LELO" to DeviceConfig(
            serviceUuid = "0000ffe0-0000-1000-8000-00805f9b34fb",
            writeCharUuid = "0000ffe1-0000-1000-8000-00805f9b34fb",
            intensityMin = 0,
            intensityMax = 10
        )
    )
    
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectionState.update { 
                        it.copy(isConnected = true, error = null) 
                    }
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    resetConnection()
                    _connectionState.update { 
                        it.copy(isConnected = false, error = "Disconnected") 
                    }
                }
                else -> {
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        _connectionState.update { 
                            it.copy(error = "Connection failed: $status") 
                        }
                    }
                }
            }
        }
        
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                findAndConfigureWriteCharacteristic(gatt)
            } else {
                _connectionState.update { 
                    it.copy(error = "Service discovery failed") 
                }
            }
        }
        
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                android.util.Log.e("BleConnector", "Write failed: $status")
            }
        }
    }
    
    private fun findAndConfigureWriteCharacteristic(gatt: BluetoothGatt) {
        var found = false
        
        for (service in gatt.services) {
            val config = findDeviceConfig(service.uuid.toString())
            if (config != null) {
                val characteristic = service.getCharacteristic(UUID.fromString(config.writeCharUuid))
                if (characteristic != null) {
                    writeCharacteristic = characteristic
                    found = true
                    android.util.Log.d("BleConnector", "Found write characteristic for ${config.serviceUuid}")
                    break
                }
            }
        }
        
        // Fallback: look for any characteristic with WRITE property
        if (!found) {
            for (service in gatt.services) {
                for (characteristic in service.characteristics) {
                    if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) {
                        writeCharacteristic = characteristic
                        found = true
                        android.util.Log.d("BleConnector", "Using fallback write characteristic")
                        break
                    }
                }
                if (found) break
            }
        }
        
        if (!found) {
            _connectionState.update { 
                it.copy(error = "No write characteristic found") 
            }
        }
    }
    
    private fun findDeviceConfig(serviceUuid: String): DeviceConfig? {
        return deviceConfigs.values.find { config ->
            serviceUuid.equals(config.serviceUuid, ignoreCase = true)
        }
    }
    
    fun connect(device: BluetoothDevice) {
        resetConnection()
        
        connectionTimeoutJob = CoroutineScope(Dispatchers.IO).launch {
            delay(30000) // 30 second timeout
            if (_connectionState.value.isConnected == false) {
                _connectionState.update { 
                    it.copy(error = "Connection timeout") 
                }
                bluetoothGatt?.close()
                bluetoothGatt = null
            }
        }
        
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
        _connectionState.update { 
            it.copy(deviceName = device.name, isConnected = false, error = null) 
        }
    }
    
    fun disconnect() {
        bluetoothGatt?.disconnect()
        resetConnection()
    }
    
    private fun resetConnection() {
        bluetoothGatt?.close()
        bluetoothGatt = null
        writeCharacteristic = null
        connectionTimeoutJob?.cancel()
    }
    
    fun sendIntensity(percent: Int): Boolean {
        // percent should be 0-100
        val clampedPercent = percent.coerceIn(0, 100)
        
        val characteristic = writeCharacteristic
        if (characteristic == null) {
            android.util.Log.e("BleConnector", "No write characteristic available")
            return false
        }
        
        // Lovense command format
        val command = buildLovenseCommand(clampedPercent)
        
        characteristic.value = command
        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        
        return try {
            bluetoothGatt?.writeCharacteristic(characteristic) == true
        } catch (e: SecurityException) {
            android.util.Log.e("BleConnector", "Write permission error", e)
            false
        }
    }
    
    private fun buildLovenseCommand(percent: Int): ByteArray {
        // Lovense protocol: "Vibrate:XX;" where XX is 0-20
        val lovenseValue = (percent * 20 / 100).coerceIn(0, 20)
        return "Vibrate:$lovenseValue;".toByteArray(Charsets.UTF_8)
    }
    
    fun sendPattern(pattern: String, intensity: Int): Boolean {
        // For pattern commands like "Vibrate:10;"
        return sendIntensity(intensity)
    }
    
    fun isConnected(): Boolean = _connectionState.value.isConnected
}

data class DeviceConfig(
    val serviceUuid: String,
    val writeCharUuid: String,
    val intensityMin: Int,
    val intensityMax: Int
)
