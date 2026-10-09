package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PhishingIndicator
import com.example.data.model.RiskLevel
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberHighRisk
import com.example.ui.theme.CyberHighRiskContainer
import com.example.ui.theme.CyberLowRisk
import com.example.ui.theme.CyberLowRiskContainer
import com.example.ui.theme.CyberMediumRisk
import com.example.ui.theme.CyberMediumRiskContainer
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@Composable
fun IndicatorCard(
    indicator: PhishingIndicator,
    modifier: Modifier = Modifier
) {
    val (accentColor, containerColor, icon) = when (indicator.severity) {
        RiskLevel.HIGH -> Triple(CyberHighRisk, CyberHighRiskContainer, Icons.Default.Dangerous)
        RiskLevel.MEDIUM -> Triple(CyberMediumRisk, CyberMediumRiskContainer, Icons.Default.Warning)
        RiskLevel.LOW -> Triple(CyberLowRisk, CyberLowRiskContainer, Icons.Default.Info)
    }

    Column(
        modifier = modifier
            .testTag("indicator_card_${indicator.id}")
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CyberNavyCard)
            .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = indicator.severity.name,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = indicator.ruleName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = CyberTextPrimary
                        )
                    )
                    Text(
                        text = indicator.category,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CyberTextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Points badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(containerColor)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "+${indicator.points} pts",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = indicator.description,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = CyberTextSecondary,
                lineHeight = 18.sp
            )
        )

        if (indicator.evidence.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF090D1A))
                    .border(0.5.dp, Color(0xFF223254), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "MATCH: ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberTextMuted,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Text(
                        text = "\"${indicator.evidence}\"",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = accentColor,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}
