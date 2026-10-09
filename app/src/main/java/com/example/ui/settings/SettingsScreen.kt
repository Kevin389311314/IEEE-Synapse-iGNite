package com.example.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.ApiClient
import com.example.data.repository.AnalysisRepository
import com.example.ui.scanner.ScannerViewModel
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDarkNavy
import com.example.ui.theme.CyberElectricBlue
import com.example.ui.theme.CyberHighRisk
import com.example.ui.theme.CyberLowRisk
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    scannerViewModel: ScannerViewModel,
    repository: AnalysisRepository,
    modifier: Modifier = Modifier
) {
    val scannerState by scannerViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var backendUrlInput by remember { mutableStateOf(ApiClient.getBaseUrl()) }
    var isTestingHealth by remember { mutableStateOf(false) }
    var healthResult by remember { mutableStateOf<Boolean?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkNavy)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
        Text(
            text = "ENGINE CONFIGURATION",
            style = MaterialTheme.typography.labelSmall.copy(
                color = CyberElectricBlue,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Offline / Online Mode Toggle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CyberNavyCard)
                .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (scannerState.preferOnlineBackend) CyberElectricBlue.copy(alpha = 0.2f)
                                    else CyberLowRisk.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (scannerState.preferOnlineBackend) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (scannerState.preferOnlineBackend) CyberElectricBlue else CyberLowRisk,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (scannerState.preferOnlineBackend) "Hybrid Cloud Backend" else "Offline Safe Engine",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                            )
                            Text(
                                text = if (scannerState.preferOnlineBackend) "Query FastAPI & optional LLM" else "Air-gapped on-device heuristics only",
                                style = MaterialTheme.typography.bodySmall.copy(color = CyberTextSecondary)
                            )
                        }
                    }

                    Switch(
                        checked = scannerState.preferOnlineBackend,
                        onCheckedChange = { scannerViewModel.toggleBackendMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberElectricBlue,
                            checkedTrackColor = CyberNavySurface,
                            uncheckedThumbColor = CyberTextMuted,
                            uncheckedTrackColor = CyberBorder
                        ),
                        modifier = Modifier.testTag("backend_mode_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberDarkNavy)
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (scannerState.preferOnlineBackend)
                            "Hybrid mode sends requests to your Python FastAPI backend server. If the backend is offline, PhishLens automatically falls back to local rules without interrupting scans."
                        else
                            "Offline mode runs 100% locally on your Android device. Zero bytes leave your device, guaranteeing absolute privacy for sensitive emails and messages.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CyberTextSecondary,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Backend Server Endpoint Settings
        Text(
            text = "FASTAPI BACKEND URL",
            style = MaterialTheme.typography.labelSmall.copy(
                color = CyberElectricBlue,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CyberNavyCard)
                .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column {
                OutlinedTextField(
                    value = backendUrlInput,
                    onValueChange = { backendUrlInput = it },
                    label = { Text("Server Base URL", color = CyberTextMuted) },
                    placeholder = { Text("http://10.0.2.2:8000/", color = CyberTextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Dns, contentDescription = null, tint = CyberElectricBlue)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CyberDarkNavy,
                        unfocusedContainerColor = CyberDarkNavy,
                        focusedBorderColor = CyberElectricBlue,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = CyberTextPrimary,
                        unfocusedTextColor = CyberTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("backend_url_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Tip: 'http://10.0.2.2:8000/' maps to 'localhost:8000' inside the Android emulator.",
                    style = MaterialTheme.typography.bodySmall.copy(color = CyberTextMuted, fontSize = 11.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            ApiClient.setBaseUrl(backendUrlInput.trim())
                            isTestingHealth = true
                            healthResult = null
                            coroutineScope.launch {
                                val ok = repository.testBackendConnection()
                                isTestingHealth = false
                                healthResult = ok
                                scannerViewModel.checkBackendHealth()
                                Toast.makeText(
                                    context,
                                    if (ok) "Backend connected successfully!" else "Backend unreachable. Offline mode active.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberElectricBlue,
                            contentColor = CyberDarkNavy
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_backend_button")
                    ) {
                        if (isTestingHealth) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = CyberDarkNavy,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Testing...", style = MaterialTheme.typography.labelSmall)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save & Ping", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            backendUrlInput = "http://10.0.2.2:8000/"
                            ApiClient.setBaseUrl(backendUrlInput)
                            Toast.makeText(context, "Reset to default emulator URL", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Text("Reset Default", style = MaterialTheme.typography.labelSmall, color = CyberTextSecondary)
                    }
                }

                if (healthResult != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val ok = healthResult == true
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (ok) CyberLowRisk.copy(alpha = 0.15f) else CyberHighRisk.copy(alpha = 0.15f))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (ok) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (ok) CyberLowRisk else CyberHighRisk,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (ok) "Status 200 OK: FastAPI server reachable" else "Backend unreachable. Local offline engine active.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (ok) CyberLowRisk else CyberHighRisk,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Hackathon Project Metadata
        Text(
            text = "HACKATHON SPECIFICATIONS",
            style = MaterialTheme.typography.labelSmall.copy(
                color = CyberElectricBlue,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CyberNavyCard)
                .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = CyberElectricBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PhishLens v1.0",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = CyberTextPrimary)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Built for Cybersecurity Hackathon 2026. Designed with defense-in-depth: native on-device heuristics, zero-trust URL deconstruction, and explainable risk scoring.",
                    style = MaterialTheme.typography.bodySmall.copy(color = CyberTextSecondary, lineHeight = 18.sp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "• Architecture: Jetpack Compose + MVVM + Room SQLite\n• Backend: Python FastAPI + SQLite\n• Threat Rules: 8 Core Pattern Classes + 10 URL Anomaly Filters\n• Scoring: 0–100 Weighted Risk Normalization",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CyberTextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 18.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
