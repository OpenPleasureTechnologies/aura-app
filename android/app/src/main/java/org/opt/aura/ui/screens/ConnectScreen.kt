package org.opt.aura.ui.screens

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import org.opt.aura.ble.BleDevice
import org.opt.aura.viewmodel.BleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectScreen(
    onDeviceConnected: (BluetoothDevice) -> Unit = {},
    viewModel: BleViewModel = viewModel()
) {
    val context = LocalContext.current
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val isConnecting by viewModel.isConnecting.collectAsState()
    
    // Permission launchers
    val bluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { 
        if (viewModel.isBluetoothEnabled()) {
            viewModel.startScan()
        }
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted && viewModel.isBluetoothEnabled()) {
            viewModel.startScan()
        }
    }
    
    // Check and request permissions
    LaunchedEffect(Unit) {
        val neededPermissions = mutableListOf<String>()
        
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        
        if (neededPermissions.isNotEmpty()) {
            permissionLauncher.launch(neededPermissions.toTypedArray())
        } else if (viewModel.isBluetoothEnabled()) {
            viewModel.startScan()
        }
    }
    
    // Show connection dialog
    if (isConnecting) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Connecting...") },
            text = { 
                Column {
                    Text(connectionState.deviceName ?: "Device")
                    LinearProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                    connectionState.error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.cancelConnection() }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("𖦹 Connect", color = MaterialTheme.colorScheme.onBackground) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    if (isScanning) {
                        Button(onClick = { viewModel.stopScan() }) {
                            Text("Stop")
                        }
                    } else {
                        Button(onClick = { 
                            if (viewModel.isBluetoothEnabled()) {
                                viewModel.startScan() 
                            } else {
                                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                bluetoothLauncher.launch(intent)
                            }
                        }) {
                            Text("Scan")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!viewModel.isBluetoothEnabled()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bluetooth is off")
                        Button(
                            onClick = {
                                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                bluetoothLauncher.launch(intent)
                            }
                        ) {
                            Text("Enable")
                        }
                    }
                }
            }
            
            if (isScanning) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    text = "Scanning for devices...",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Show connected device status
            if (connectionState.isConnected) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Connected to", style = MaterialTheme.typography.labelSmall)
                            Text(
                                connectionState.deviceName ?: "Device",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Button(onClick = { viewModel.disconnectDevice() }) {
                            Text("Disconnect")
                        }
                    }
                }
            }
            
            // Device list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (discoveredDevices.isNotEmpty()) {
                    item {
                        Text(
                            text = "Found ${discoveredDevices.size} devices",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
                
                items(discoveredDevices) { device ->
                    DeviceCard(
                        device = device,
                        isConnecting = isConnecting,
                        onConnect = { 
                            viewModel.connectToDevice(device.address) { bluetoothDevice ->
                                onDeviceConnected(bluetoothDevice)
                            }
                        }
                    )
                }
                
                if (discoveredDevices.isEmpty() && !isScanning) {
                    item {
                        Text(
                            text = "No devices found. Make sure your device is in pairing mode.",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceCard(
    device: BleDevice, 
    isConnecting: Boolean,
    onConnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = device.name ?: "Unknown Device",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Signal: ${device.rssi} dBm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (isPleasureDevice(device)) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "𖦹 Pleasure device",
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Button(
                onClick = onConnect,
                enabled = !isConnecting
            ) {
                Text(if (isConnecting) "..." else "Connect")
            }
        }
    }
}

fun isPleasureDevice(device: BleDevice): Boolean {
    val knownUuids = listOf(
        "0000fff0", "0000180d", "0000fe51", "0000ffe0",
        "0000fdd3", "0000fff6"
    )
    
    return device.serviceUuids.any { uuid ->
        knownUuids.any { known -> uuid.toString().startsWith(known, ignoreCase = true) }
    } || device.name?.let { name ->
        listOf("Lovense", "We-Vibe", "Kiiroo", "LELO", "Lush", "Max", "Nora", "Hush", "Sync", "Chorus", "Moxie", "Onyx", "Pearl")
            .any { name.contains(it, ignoreCase = true) }
    } == true
}
