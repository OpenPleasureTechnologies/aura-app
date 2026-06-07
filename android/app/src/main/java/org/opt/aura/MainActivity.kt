package org.opt.aura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.opt.aura.ui.theme.AuraTheme

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
    val tabs = listOf("Connect", "Feel", "Learn", "You")
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> ConnectScreen()
                1 -> FeelScreen()
                2 -> LearnScreen()
                3 -> YouScreen()
            }
        }
    }
}

@Composable
fun ConnectScreen() {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Connect your pleasure", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* TODO: Start BLE scan */ }) {
            Text("Scan for devices")
        }
        Text("No watch connected", modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
fun FeelScreen() {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Feel", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Connect a device first 💜")
    }
}

@Composable
fun LearnScreen() {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Learn", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("AURA is watching. AURA is learning. AURA sees you.")
    }
}

@Composable
fun YouScreen() {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("You", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Your data never leaves this device. Ever.")
    }
}
