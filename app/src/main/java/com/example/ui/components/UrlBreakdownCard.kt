package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UrlBreakdown
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberElectricBlue
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UrlBreakdownCard(
    breakdown: UrlBreakdown,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .testTag("url_breakdown_card")
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CyberNavyCard)
            .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "URL Anatomy",
                    tint = CyberElectricBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "URL Structural Deconstruction",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )
                )
            }

            // Protocol security badge
            val isHttps = breakdown.protocol == "https"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isHttps) CyberLowRiskContainer else CyberHighRiskContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isHttps) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = breakdown.protocol,
                        tint = if (isHttps) CyberLowRisk else CyberHighRisk,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = breakdown.protocol.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isHttps) CyberLowRisk else CyberHighRisk,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Registered Destination Domain
        Text(
            text = "ACTUAL REGISTERED DOMAIN",
            style = MaterialTheme.typography.labelSmall.copy(
                color = CyberTextMuted,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF090D1A))
                .border(1.dp, CyberElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = breakdown.registeredDomain.ifEmpty { breakdown.host },
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyberElectricBlue
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Subdomain & TLD details
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SUBDOMAINS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CyberTextMuted,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp
                    )
                )
                Text(
                    text = if (breakdown.subdomains.isNotEmpty()) breakdown.subdomains.joinToString(".") else "None (apex)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = CyberTextSecondary
                    )
                )
            }
            Column(modifier = Modifier.weight(0.6f)) {
                Text(
                    text = "TLD EXTENSION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CyberTextMuted,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp
                    )
                )
                Text(
                    text = ".${breakdown.tld}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = if (breakdown.isSuspiciousTld) CyberHighRisk else CyberTextSecondary,
                        fontWeight = if (breakdown.isSuspiciousTld) FontWeight.Bold else FontWeight.Normal
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Anomaly Tags
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (breakdown.isIpAddress) {
                AnomalyChip("Raw IP Host", CyberHighRisk, CyberHighRiskContainer)
            }
            if (breakdown.isPunycode) {
                AnomalyChip("Punycode (xn--)", CyberHighRisk, CyberHighRiskContainer)
            }
            if (breakdown.isSuspiciousTld) {
                AnomalyChip("Suspicious TLD", CyberHighRisk, CyberHighRiskContainer)
            }
            if (breakdown.hasBrandKeywordInSubdomain) {
                AnomalyChip("Brand in Subdomain", CyberHighRisk, CyberHighRiskContainer)
            }
            if (breakdown.isShortener) {
                AnomalyChip("URL Shortener", CyberMediumRisk, CyberMediumRiskContainer)
            }
            if (breakdown.hasOpenRedirect) {
                AnomalyChip("Open Redirect", CyberHighRisk, CyberHighRiskContainer)
            }
            if (!breakdown.isIpAddress && !breakdown.isPunycode && !breakdown.isSuspiciousTld && !breakdown.hasBrandKeywordInSubdomain) {
                AnomalyChip("Standard DNS Host", CyberLowRisk, CyberLowRiskContainer)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Safe Action: Copy Domain Only
        OutlinedButton(
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Domain", breakdown.registeredDomain.ifEmpty { breakdown.host })
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied domain to clipboard (safe)", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("copy_domain_button"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = CyberElectricBlue
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(CyberBorder)
            )
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy Domain",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Safe Copy Domain Only (Do Not Visit)",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)
            )
        }
    }
}

@Composable
private fun AnomalyChip(text: String, textColor: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        )
    }
}
