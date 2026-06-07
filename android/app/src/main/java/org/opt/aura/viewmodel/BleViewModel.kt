package org.opt.aura.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*
import org.opt.aura.ble.BleScanner
import org.opt.aura.ble.BleDevice

class BleViewModel(application: Application) : AndroidViewModel(application) {
    
    private val scanner = BleScanner(application)
    
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    
    val discoveredDevices: StateFlow<List<BleDevice>> = scanner.discoveredDevices
    
    private var scanJob: kotlinx.coroutines.Job? = null
    
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
    
    fun isBluetoothEnabled(): Boolean = scanner.isBluetoothEnabled()
    
    override fun onCleared() {
        super.onCleared()
        stopScan()
    }
}
