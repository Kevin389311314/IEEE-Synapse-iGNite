package com.example.ui.history

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AnalysisResult
import com.example.data.model.AnalysisType
import com.example.data.model.RiskLevel
import com.example.ui.components.IndicatorCard
import com.example.ui.components.RiskGaugeMeter
import com.example.ui.components.SafetyActionsList
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDarkNavy
import com.example.ui.theme.CyberElectricBlue
import com.example.ui.theme.CyberHighRisk
import com.example.ui.theme.CyberHighRiskContainer
import com.example.ui.theme.CyberLowRisk
import com.example.ui.theme.CyberLowRiskContainer
import com.example.ui.theme.CyberMediumRisk
import com.example.ui.theme.CyberMediumRiskContainer
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import java.util.Date

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val filterState by viewModel.filterState.collectAsState()
    val historyItems by viewModel.historyList.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkNavy)
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = filterState.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search scan records...", color = CyberTextMuted) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = CyberElectricBlue)
            },
            trailingIcon = {
                if (filterState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = CyberTextMuted)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CyberNavyCard,
                unfocusedContainerColor = CyberNavyCard,
                focusedBorderColor = CyberElectricBlue,
                unfocusedBorderColor = CyberBorder,
                focusedTextColor = CyberTextPrimary,
                unfocusedTextColor = CyberTextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_search_field")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips and Clear All Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = filterState.riskFilter == null,
                    onClick = { viewModel.setRiskFilter(null) },
                    label = { Text("All", style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = CyberNavyCard,
                        selectedContainerColor = CyberElectricBlue.copy(alpha = 0.2f),
                        labelColor = CyberTextPrimary,
                        selectedLabelColor = CyberElectricBlue
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = filterState.riskFilter == null,
                        borderColor = CyberBorder,
                        selectedBorderColor = CyberElectricBlue
                    )
                )

                FilterChip(
                    selected = filterState.riskFilter == RiskLevel.HIGH,
                    onClick = { viewModel.setRiskFilter(if (filterState.riskFilter == RiskLevel.HIGH) null else RiskLevel.HIGH) },
                    label = { Text("High", style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = CyberNavyCard,
                        selectedContainerColor = CyberHighRiskContainer,
                        labelColor = CyberTextPrimary,
                        selectedLabelColor = CyberHighRisk
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = filterState.riskFilter == RiskLevel.HIGH,
                        borderColor = CyberBorder,
                        selectedBorderColor = CyberHighRisk
                    )
                )

                FilterChip(
                    selected = filterState.riskFilter == RiskLevel.MEDIUM,
                    onClick = { viewModel.setRiskFilter(if (filterState.riskFilter == RiskLevel.MEDIUM) null else RiskLevel.MEDIUM) },
                    label = { Text("Medium", style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = CyberNavyCard,
                        selectedContainerColor = CyberMediumRiskContainer,
                        labelColor = CyberTextPrimary,
                        selectedLabelColor = CyberMediumRisk
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = filterState.riskFilter == RiskLevel.MEDIUM,
                        borderColor = CyberBorder,
                        selectedBorderColor = CyberMediumRisk
                    )
                )

                FilterChip(
                    selected = filterState.riskFilter == RiskLevel.LOW,
                    onClick = { viewModel.setRiskFilter(if (filterState.riskFilter == RiskLevel.LOW) null else RiskLevel.LOW) },
                    label = { Text("Low", style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = CyberNavyCard,
                        selectedContainerColor = CyberLowRiskContainer,
                        labelColor = CyberTextPrimary,
                        selectedLabelColor = CyberLowRisk
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = filterState.riskFilter == RiskLevel.LOW,
                        borderColor = CyberBorder,
                        selectedBorderColor = CyberLowRisk
                    )
                )
            }

            if (historyItems.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.setShowClearDialog(true) },
                    modifier = Modifier.testTag("clear_all_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear All",
                        tint = CyberTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // History items list or empty state
        if (historyItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("history_empty_state"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CyberNavyCard)
                            .border(1.dp, CyberBorder, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = CyberElectricBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Analysis Records Found",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Scanned SMS messages, URLs, and screenshots will be saved here automatically with risk ratings and indicators.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = CyberTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("history_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyItems, key = { it.id }) { item ->
                    HistoryItemCard(
                        item = item,
                        onClick = { viewModel.selectDetail(item) },
                        onDelete = { viewModel.deleteItem(item.id) }
                    )
                }
            }
        }
    }

    // Detail dialog when tapped
    if (filterState.selectedDetail != null) {
        val detail = filterState.selectedDetail!!
        HistoryDetailDialog(
            result = detail,
            onDismiss = { viewModel.selectDetail(null) },
            onDelete = { viewModel.deleteItem(detail.id) }
        )
    }

    // Clear All Confirmation Dialog
    if (filterState.showClearDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowClearDialog(false) },
            title = {
                Text(
                    text = "Clear All Analysis History?",
                    color = CyberTextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = "This will permanently delete all local SQLite analysis records and threat logs. This action cannot be undone.",
                    color = CyberTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearAllHistory() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberHighRisk),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete All Records", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.setShowClearDialog(false) },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", color = CyberTextSecondary)
                }
            },
            containerColor = CyberNavySurface,
            modifier = Modifier.testTag("clear_history_dialog")
        )
    }
}

@Composable
private fun HistoryItemCard(
    item: AnalysisResult,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (accentColor, containerColor) = when (item.riskLevel) {
        RiskLevel.HIGH -> Pair(CyberHighRisk, CyberHighRiskContainer)
        RiskLevel.MEDIUM -> Pair(CyberMediumRisk, CyberMediumRiskContainer)
        RiskLevel.LOW -> Pair(CyberLowRisk, CyberLowRiskContainer)
    }

    val typeIcon = when (item.type) {
        AnalysisType.TEXT -> Icons.Default.Message
        AnalysisType.URL -> Icons.Default.Link
        AnalysisType.SCREENSHOT -> Icons.Default.PhotoCamera
    }

    val formattedDate = DateFormat.format("MMM dd, yyyy  HH:mm", Date(item.timestamp)).toString()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CyberNavyCard)
            .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp)
            .testTag("history_item_${item.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = item.type.name,
                        tint = CyberElectricBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CyberTextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    )
                }

                // Risk Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(containerColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${item.riskLevel.name} (${item.riskScore}/100)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = CyberTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.rawInput,
                style = MaterialTheme.typography.bodySmall.copy(color = CyberTextSecondary),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.indicators.size} indicators detected",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CyberElectricBlue,
                        fontSize = 11.sp
                    )
                )

                IconButton(
                    onClick = { onDelete() },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("delete_item_button_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Record",
                        tint = CyberTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryDetailDialog(
    result: AnalysisResult,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = CyberNavySurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Historical Threat Report",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyberElectricBlue
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Clear, contentDescription = "Close", tint = CyberTextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                RiskGaugeMeter(
                    score = result.riskScore,
                    riskLevel = result.riskLevel,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = result.explanation,
                    style = MaterialTheme.typography.bodyMedium.copy(color = CyberTextSecondary)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Original Input Excerpt
                Text(
                    text = "ORIGINAL INPUT TARGET:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CyberTextMuted,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberDarkNavy)
                        .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = result.rawInput,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CyberTextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (result.indicators.isNotEmpty()) {
                    Text(
                        text = "DETECTED THREAT INDICATORS (${result.indicators.size}):",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberElectricBlue,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    result.indicators.forEach { ind ->
                        IndicatorCard(indicator = ind)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                SafetyActionsList(actions = result.safetyActions)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberHighRisk),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Record")
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close", color = CyberTextPrimary)
                    }
                }
            }
        }
    }
}
