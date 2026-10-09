package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.CrisisAlert
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.AnalysisRepository
import com.example.ui.history.HistoryScreen
import com.example.ui.history.HistoryViewModel
import com.example.ui.learn.ThreatRadarScreen
import com.example.ui.scanner.ScannerScreen
import com.example.ui.scanner.ScannerViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDarkNavy
import com.example.ui.theme.CyberElectricBlue
import com.example.ui.theme.CyberLowRisk
import com.example.ui.theme.CyberLowRiskContainer
import com.example.ui.theme.CyberMediumRisk
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

enum class NavDestination(
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    SCANNER("Scanner", Icons.Filled.Shield, Icons.Outlined.Shield),
    HISTORY("History", Icons.Filled.History, Icons.Outlined.History),
    RADAR("Radar", Icons.Filled.CrisisAlert, Icons.Outlined.CrisisAlert),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainScreen(
    scannerViewModel: ScannerViewModel,
    historyViewModel: HistoryViewModel,
    repository: AnalysisRepository
) {
    var currentDestination by remember { mutableStateOf(NavDestination.SCANNER) }
    val scannerState by scannerViewModel.uiState.collectAsState()

    // Handle Android system back button
    BackHandler(enabled = currentDestination != NavDestination.SCANNER) {
        currentDestination = NavDestination.SCANNER
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDarkNavy),
        topBar = {
            CyberAppHeader(
                isOffline = !scannerState.preferOnlineBackend,
                isBackendConnected = scannerState.isBackendConnected,
                onStatusClick = { currentDestination = NavDestination.SETTINGS }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CyberNavySurface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = CyberBorder,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .testTag("bottom_navigation_bar")
            ) {
                NavDestination.entries.forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = destination.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberElectricBlue,
                            selectedTextColor = CyberElectricBlue,
                            indicatorColor = CyberElectricBlue.copy(alpha = 0.15f),
                            unselectedIconColor = CyberTextMuted,
                            unselectedTextColor = CyberTextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                NavDestination.SCANNER -> ScannerScreen(viewModel = scannerViewModel)
                NavDestination.HISTORY -> HistoryScreen(viewModel = historyViewModel)
                NavDestination.RADAR -> ThreatRadarScreen()
                NavDestination.SETTINGS -> SettingsScreen(
                    scannerViewModel = scannerViewModel,
                    repository = repository
                )
            }
        }
    }
}

@Composable
private fun CyberAppHeader(
    isOffline: Boolean,
    isBackendConnected: Boolean,
    onStatusClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .background(CyberNavySurface)
            .border(width = 0.5.dp, color = CyberBorder)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberDarkNavy)
                        .border(1.dp, CyberElectricBlue.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.phishlens_icon),
                        contentDescription = "PhishLens Logo",
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PhishLens",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary,
                                fontSize = 17.sp,
                                letterSpacing = 0.3.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberElectricBlue.copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "HACKATHON",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CyberElectricBlue,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                    Text(
                        text = "AI & Rule-Based Scam Guard",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CyberTextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Engine status indicator pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isOffline) CyberLowRiskContainer
                        else if (isBackendConnected) Color(0xFF07381C)
                        else Color(0xFF3B2807)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isOffline) CyberLowRisk.copy(alpha = 0.5f)
                        else if (isBackendConnected) CyberLowRisk.copy(alpha = 0.5f)
                        else CyberMediumRisk.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onStatusClick() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("status_indicator_pill")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (isOffline) CyberLowRisk
                                else if (isBackendConnected) CyberLowRisk
                                else CyberMediumRisk
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOffline) "OFFLINE SAFE"
                        else if (isBackendConnected) "ONLINE"
                        else "FALLBACK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isOffline) CyberLowRisk
                            else if (isBackendConnected) CyberLowRisk
                            else CyberMediumRisk,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}
