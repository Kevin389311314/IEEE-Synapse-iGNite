package com.example.ui.learn

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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDarkNavy
import com.example.ui.theme.CyberElectricBlue
import com.example.ui.theme.CyberHighRisk
import com.example.ui.theme.CyberLowRisk
import com.example.ui.theme.CyberMediumRisk
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@Composable
fun ThreatRadarScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkNavy)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("threat_radar_screen")
    ) {
        // Hero Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CyberNavySurface)
                .border(1.dp, CyberElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyberElectricBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CrisisAlert,
                            contentDescription = null,
                            tint = CyberElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "PhishLens Threat Radar",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                        )
                        Text(
                            text = "Scam Anatomy & Attack Vector Intelligence",
                            style = MaterialTheme.typography.bodySmall.copy(color = CyberElectricBlue)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Attackers continually evolve their psychological tactics and technical evasion tricks. Understanding these 5 core vectors keeps you and your organization resilient.",
                    style = MaterialTheme.typography.bodySmall.copy(color = CyberTextSecondary, lineHeight = 18.sp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "COMMON CYBER THREAT VECTORS",
            style = MaterialTheme.typography.labelSmall.copy(
                color = CyberElectricBlue,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        ThreatVectorCard(
            title = "1. Smishing (SMS Phishing)",
            icon = Icons.Default.Fingerprint,
            badge = "HIGH FREQUENCY",
            badgeColor = CyberHighRisk,
            description = "Unsolicited text messages pretending to be your bank, postal service (USPS/FedEx), or toll authority demanding urgent action. Attackers leverage small mobile screens to hide malicious destinations.",
            defense = "Never click links in SMS. Use verified shortcode 7726 to report spam to cellular carriers."
        )

        Spacer(modifier = Modifier.height(12.dp))

        ThreatVectorCard(
            title = "2. Punycode & Lookalike Domains",
            icon = Icons.Default.BugReport,
            badge = "TECHNICAL DECEPTION",
            badgeColor = CyberMediumRisk,
            description = "Attackers register domains with foreign Unicode characters (e.g. Cyrillic 'а') that render identically to English letters. The real browser destination is prefixed with 'xn--'.",
            defense = "Inspect the full ASCII URL. PhishLens flags Punycode markers immediately."
        )

        Spacer(modifier = Modifier.height(12.dp))

        ThreatVectorCard(
            title = "3. Fake Tech Support & Refund Invoices",
            icon = Icons.Default.Policy,
            badge = "FINANCIAL EXTORTION",
            badgeColor = CyberHighRisk,
            description = "Emails claiming your Geek Squad or antivirus renewed for $499. The goal is to prompt a panic phone call where scammers trick you into installing remote access tools (AnyDesk, TeamViewer).",
            defense = "Check your actual bank statement directly. Never call phone numbers listed in unverified emails."
        )

        Spacer(modifier = Modifier.height(12.dp))

        ThreatVectorCard(
            title = "4. Quishing (QR Code Scams)",
            icon = Icons.Default.QrCodeScanner,
            badge = "PHYSICAL-TO-DIGITAL",
            badgeColor = CyberMediumRisk,
            description = "Malicious QR stickers placed over legitimate parking meters, menus, or sent in emails to bypass corporate mail filters and redirect mobile users to credential harvesting sites.",
            defense = "Preview destination URLs before submitting login credentials or processing payments."
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Golden Rules Checklist
        Text(
            text = "THE ZERO-TRUST DEFENSE CHECKLIST",
            style = MaterialTheme.typography.labelSmall.copy(
                color = CyberLowRisk,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CyberNavyCard)
                .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            ChecklistRule(
                number = "1",
                title = "Out-of-Band Verification",
                body = "If an alert claims your account is locked, open a clean browser and type the official URL yourself."
            )
            Spacer(modifier = Modifier.height(10.dp))
            ChecklistRule(
                number = "2",
                title = "2FA Codes are Private",
                body = "No legitimate representative from Apple, Google, or your bank will ever ask you for your 6-digit OTP code."
            )
            Spacer(modifier = Modifier.height(10.dp))
            ChecklistRule(
                number = "3",
                title = "Beware of Urgent Time Clocks",
                body = "Scammers manufacture artificial panic ('within 24 hours') because logic breaks down under artificial rush."
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ThreatVectorCard(
    title: String,
    icon: ImageVector,
    badge: String,
    badgeColor: Color,
    description: String,
    defense: String
) {
    Column(
        modifier = Modifier
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = CyberElectricBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = CyberTextPrimary)
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(badgeColor.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall.copy(color = CyberTextSecondary, lineHeight = 18.sp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(CyberDarkNavy)
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = CyberLowRisk,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = defense,
                    style = MaterialTheme.typography.bodySmall.copy(color = CyberLowRisk, fontSize = 11.sp)
                )
            }
        }
    }
}

@Composable
private fun ChecklistRule(number: String, title: String, body: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(CyberLowRisk.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall.copy(color = CyberLowRisk, fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = CyberTextPrimary,
                    fontSize = 13.sp
                )
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall.copy(color = CyberTextSecondary, lineHeight = 16.sp)
            )
        }
    }
}
