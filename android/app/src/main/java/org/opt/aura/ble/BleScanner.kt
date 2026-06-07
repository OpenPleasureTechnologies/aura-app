package org.opt.aura.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.Context
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.UUID

data class BleDevice(
    val name: String?,
    val address: String,
    val rssi: Int,
    val serviceUuids: List<UUID> = emptyList()
)

class BleScanner(private val context: Context) {
    
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    
    private val _discoveredDevices = MutableStateFlow<List<BleDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BleDevice>> = _discoveredDevices.asStateFlow()
    
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                val device = result.device
                val name = device.name ?: "Unknown Device"
                val address = device.address
                val rssi = result.rssi
                
                // Extract service UUIDs from scan record
                val serviceUuids = result.scanRecord?.serviceUuids?.toList() ?: emptyList()
                
                val bleDevice = BleDevice(name, address, rssi, serviceUuids)
                
                // Update list without duplicates
                val currentList = _discoveredDevices.value.toMutableList()
                val existingIndex = currentList.indexOfFirst { it.address == address }
                if (existingIndex >= 0) {
                    currentList[existingIndex] = bleDevice
                } else {
                    currentList.add(bleDevice)
                }
                _discoveredDevices.value = currentList
            }
        }
        
        override fun onBatchScanResults(results: List<ScanResult>) {
            results.forEach { onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, it) }
        }
        
        override fun onScanFailed(errorCode: Int) {
            android.util.Log.e("BleScanner", "Scan failed with error: $errorCode")
        }
    }
    
    fun startScan(): Flow<Boolean> = callbackFlow {
        if (bluetoothAdapter?.isEnabled != true) {
            trySend(false)
            close()
            return@callbackFlow
        }
        
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        
        val scanFilters = listOf(
            ScanFilter.Builder().build() // Accept all devices
        )
        
        bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)
        trySend(true)
        
        awaitClose {
            bluetoothLeScanner?.stopScan(scanCallback)
        }
    }
    
    fun stopScan() {
        bluetoothLeScanner?.stopScan(scanCallback)
    }
    
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true
    
    fun requestBluetoothEnable(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothAdapter?.isLeEnabled == true
        } else {
            bluetoothAdapter?.isEnabled == true
        }
    }
}
