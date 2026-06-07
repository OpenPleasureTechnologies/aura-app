package org.opt.aura.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.opt.aura.viewmodel.BleViewModel
import kotlin.math.abs

@Composable
fun FeelScreen(viewModel: BleViewModel = viewModel()) {
    val connectionState by viewModel.connectionState.collectAsState()
    var intensity by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!connectionState.isConnected) {
            // Not connected state
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No device connected", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Go to Connect tab and pair your device",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            // Connected state - show controls
            Text(
                text = "Connected to ${connectionState.deviceName ?: "your device"}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Intensity display (circle that pulses)
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.2f + (intensity / 100f) * 0.6f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$intensity%",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Intensity slider
            Text("Intensity", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = intensity.toFloat(),
                onValueChange = { newValue ->
                    intensity = newValue.toInt()
                    viewModel.sendIntensity(intensity)
                },
                valueRange = 0f..100f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Pattern buttons
            Text("Patterns", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PatternButton("Wave", intensity) { viewModel.sendIntensity(intensity) }
                PatternButton("Pulse", intensity) { viewModel.sendIntensity(intensity) }
                PatternButton("Constant", intensity) { viewModel.sendIntensity(intensity) }
            }
        }
    }
}

@Composable
fun PatternButton(
    name: String,
    intensity: Int,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Text(name)
    }
}
