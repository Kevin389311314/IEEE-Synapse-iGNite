package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.RiskLevel
import com.example.domain.engine.DemoDataProvider
import com.example.domain.engine.DemoSample
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberElectricBlue
import com.example.ui.theme.CyberHighRisk
import com.example.ui.theme.CyberLowRisk
import com.example.ui.theme.CyberMediumRisk
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberTextPrimary

@Composable
fun DemoChipsRow(
    onSelectSample: (DemoSample) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .testTag("demo_chips_row")
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DemoDataProvider.SAMPLES.forEach { sample ->
            val iconPrefix = when (sample.expectedRisk) {
                RiskLevel.HIGH -> "🚨"
                RiskLevel.MEDIUM -> "⚠️"
                RiskLevel.LOW -> "🛡️"
            }
            val accentColor = when (sample.expectedRisk) {
                RiskLevel.HIGH -> CyberHighRisk
                RiskLevel.MEDIUM -> CyberMediumRisk
                RiskLevel.LOW -> CyberLowRisk
            }

            FilterChip(
                selected = false,
                onClick = { onSelectSample(sample) },
                label = {
                    Text(
                        text = "$iconPrefix ${sample.title}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberTextPrimary
                        )
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CyberNavyCard,
                    labelColor = CyberTextPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = false,
                    borderColor = CyberBorder,
                    selectedBorderColor = accentColor
                ),
                modifier = Modifier.testTag("demo_chip_${sample.id}")
            )
        }
    }
}
