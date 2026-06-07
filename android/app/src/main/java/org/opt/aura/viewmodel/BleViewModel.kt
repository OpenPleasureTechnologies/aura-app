package org.opt.aura.viewmodel

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*
import org.opt.aura.ble.BleScanner
import org.opt.aura.ble.BleDevice
import org.opt.aura.ble.BleConnector

class BleViewModel(application: Application) : AndroidViewModel(application) {
    
    private val scanner = BleScanner(application)
    private val connector = BleConnector(application)
    
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    
    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()
    
    val discoveredDevices: StateFlow<List<BleDevice>> = scanner.discoveredDevices
    val connectionState = connector.connectionState
    
    private var scanJob: kotlinx.coroutines.Job? = null
    private var currentDeviceAddress: String? = null
    
    fun startScan() {
        if (_isScanning.value) return
        
        scanJob = viewModelScope.launch {
            scanner.startScan().collect { started ->
                _isScanning.value = started
            }
        }
    }
    
    fun stopScan() {
        scanner.stopScan()
        _isScanning.value = false
        scanJob?.cancel()
    }
    
    fun connectToDevice(address: String, onConnected: (BluetoothDevice) -> Unit) {
        val device = BluetoothDevice.obtainDeviceAddress(address)?.let { 
            // Create BluetoothDevice instance
            // This is simplified - you'll need to get the actual device from scan results
        }
        
        _isConnecting.value = true
        currentDeviceAddress = address
        
        viewModelScope.launch {
            connector.connectionState.collect { state ->
                if (state.isConnected) {
                    _isConnecting.value = false
                    state.deviceName?.let { name ->
                        // Device connected successfully
                    }
                } else if (state.error != null && _isConnecting.value) {
                    _isConnecting.value = false
                }
            }
        }
        
        // TODO: Actually connect using the BluetoothDevice from scan results
        // connector.connect(bluetoothDevice)
    }
    
    fun cancelConnection() {
        _isConnecting.value = false
        connector.disconnect()
    }
    
    fun disconnectDevice() {
        connector.disconnect()
        currentDeviceAddress = null
    }
    
    fun sendIntensity(percent: Int): Boolean {
        return connector.sendIntensity(percent)
    }
    
    fun isBluetoothEnabled(): Boolean = scanner.isBluetoothEnabled()
    
    override fun onCleared() {
        super.onCleared()
        stopScan()
        connector.disconnect()
    }
}

// Extension helper
fun BluetoothDevice.obtainDeviceAddress(address: String): BluetoothDevice? {
    return try {
        // This is a placeholder - actual implementation needs the device from scan results
        null
    } catch (e: Exception) {
        null
    }
}
