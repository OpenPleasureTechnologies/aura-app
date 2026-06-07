package org.opt.aura

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.opt.aura.ui.screens.*
import org.opt.aura.ui.theme.AuraTheme
import org.opt.aura.viewmodel.BleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AuraTheme {
                AuraApp()
            }
        }
    }
}

@Composable
fun AuraApp() {
    var selectedTab by remember { mutableStateOf(0) }
    var connectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
    val tabs = listOf("Connect", "Feel", "Learn", "You")
    val viewModel: BleViewModel = viewModel()
    val connectionState by viewModel.connectionState.collectAsState()
    
    // Update connected device when state changes
    LaunchedEffect(connectionState.isConnected) {
        if (connectionState.isConnected) {
            selectedTab = 1 // Switch to Feel tab when connected
        }
    }
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> ConnectScreen(
                    onDeviceConnected = { device ->
                        connectedDevice = device
                        selectedTab = 1
                    }
                )
                1 -> FeelScreen(viewModel = viewModel)
                2 -> LearnScreen()
                3 -> YouScreen()
            }
        }
    }
}

@Composable
fun LearnScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("𖦹 Learn", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("AURA is learning...", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Every time you use AURA, it learns your unique patterns. " +
                    "After 3-5 sessions, it will start anticipating what feels good.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    progress = 0.2f,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Session 1 of 5 for baseline",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun YouScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("𖦹 You", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Your Privacy", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "✓ All data stays on this device\n" +
                    "✓ No account required\n" +
                    "✓ No cloud backup\n" +
                    "✓ You are in complete control",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("About AURA", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Version 0.1.0 Alpha\n" +
                    "MIT License · Open Source\n" +
                    "github.com/OPT/aura",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
