package com.example.ui.scanner

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AnalysisType
import com.example.data.model.RiskLevel
import com.example.ui.components.DemoChipsRow
import com.example.ui.components.IndicatorCard
import com.example.ui.components.RiskGaugeMeter
import com.example.ui.components.SafetyActionsList
import com.example.ui.components.UrlBreakdownCard
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDarkNavy
import com.example.ui.theme.CyberElectricBlue
import com.example.ui.theme.CyberHighRisk
import com.example.ui.theme.CyberHighRiskContainer
import com.example.ui.theme.CyberLowRisk
import com.example.ui.theme.CyberLowRiskContainer
import com.example.ui.theme.CyberMediumRisk
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.CyberNavySurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Zero-permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onScreenshotSelected(context, uri)
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkNavy)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Mode Selector Tab Row
        val tabs = listOf(
            Triple(AnalysisType.TEXT, "Text / SMS", Icons.Default.Message),
            Triple(AnalysisType.URL, "URL Inspect", Icons.Default.Link),
            Triple(AnalysisType.SCREENSHOT, "Screenshot", Icons.Default.PhotoCamera)
        )

        TabRow(
            selectedTabIndex = tabs.indexOfFirst { it.first == uiState.selectedType }.coerceAtLeast(0),
            containerColor = CyberNavySurface,
            contentColor = CyberElectricBlue,
            indicator = { tabPositions ->
                val selectedIndex = tabs.indexOfFirst { it.first == uiState.selectedType }.coerceAtLeast(0)
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    color = CyberElectricBlue,
                    height = 3.dp
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
        ) {
            tabs.forEach { (type, label, icon) ->
                val selected = uiState.selectedType == type
                Tab(
                    selected = selected,
                    onClick = { viewModel.selectType(type) },
                    text = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) CyberElectricBlue else CyberTextSecondary
                            )
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (selected) CyberElectricBlue else CyberTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("tab_${type.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Demo Examples
        Text(
            text = "HACKATHON DEMO EXAMPLES (1-TAP LOAD):",
            style = MaterialTheme.typography.labelSmall.copy(
                color = CyberElectricBlue,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        DemoChipsRow(
            onSelectSample = { sample ->
                viewModel.loadDemoSample(sample)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Screenshot picker banner if in SCREENSHOT mode
        if (uiState.selectedType == AnalysisType.SCREENSHOT) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberNavySurfaceVariant)
                    .border(1.dp, CyberElectricBlue.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Optical Scan & Screenshot OCR",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                            )
                            Text(
                                text = "Extract text from messages, emails, or fake receipts",
                                style = MaterialTheme.typography.bodySmall.copy(color = CyberTextSecondary)
                            )
                        }
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberElectricBlue,
                                contentColor = Color(0xFF001F2A)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("pick_screenshot_button")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select Image", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    if (uiState.selectedImageUri != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = uiState.selectedImageUri,
                                contentDescription = "Screenshot Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    if (uiState.isOcrProcessing) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = CyberElectricBlue,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Extracting text with on-device OCR...",
                                style = MaterialTheme.typography.bodySmall.copy(color = CyberElectricBlue)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Input Field
        val placeholderText = when (uiState.selectedType) {
            AnalysisType.TEXT -> "Paste suspicious SMS message, email body, or threat text..."
            AnalysisType.URL -> "Enter suspicious link (e.g. http://wellsfargo-verify.tk/login)..."
            AnalysisType.SCREENSHOT -> "OCR extracted text will appear here (or type manually)..."
        }

        OutlinedTextField(
            value = uiState.inputText,
            onValueChange = { viewModel.updateInputText(it) },
            label = {
                Text(
                    text = when (uiState.selectedType) {
                        AnalysisType.TEXT -> "Message / Email Content"
                        AnalysisType.URL -> "Suspicious URL Target"
                        AnalysisType.SCREENSHOT -> "Extracted Text for Inspection"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(color = CyberElectricBlue)
                )
            },
            placeholder = {
                Text(
                    text = placeholderText,
                    style = MaterialTheme.typography.bodyMedium.copy(color = CyberTextMuted)
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (uiState.inputText.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearInput() },
                            modifier = Modifier.testTag("clear_input_button")
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = CyberTextMuted)
                        }
                    }
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                            if (clip.isNotBlank()) {
                                viewModel.updateInputText(clip)
                                Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("paste_input_button")
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = CyberElectricBlue)
                    }
                }
            },
            minLines = if (uiState.selectedType == AnalysisType.URL) 2 else 4,
            maxLines = 8,
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
                .testTag("scanner_input_field")
        )

        // Error message if any
        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberHighRiskContainer)
                    .border(1.dp, CyberHighRisk.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = uiState.errorMessage ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(color = CyberHighRisk)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main CTA Analyze Button
        Button(
            onClick = { viewModel.analyze() },
            enabled = !uiState.isAnalyzing && uiState.inputText.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberElectricBlue,
                contentColor = Color(0xFF001F2A),
                disabledContainerColor = CyberNavySurfaceVariant,
                disabledContentColor = CyberTextMuted
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("analyze_button")
        ) {
            if (uiState.isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color(0xFF001F2A),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "RUNNING HEURISTICS & ANOMALY SCAN...",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Shield",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ANALYZE FOR PHISHING & SCAMS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Analysis Result Section
        AnimatedVisibility(
            visible = uiState.currentResult != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val result = uiState.currentResult
            if (result != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("analysis_result_container")
                ) {
                    // Risk Score & Badge Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        CyberNavyCard,
                                        CyberDarkNavy
                                    )
                                )
                            )
                            .border(
                                1.5.dp,
                                when (result.riskLevel) {
                                    RiskLevel.HIGH -> CyberHighRisk
                                    RiskLevel.MEDIUM -> CyberMediumRisk
                                    RiskLevel.LOW -> CyberLowRisk
                                },
                                RoundedCornerShape(16.dp)
                            )
                            .padding(18.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (result.isOffline) Color(0xFF1E293B) else Color(0xFF0C4A6E))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (result.isOffline) "OFFLINE SAFE ENGINE" else "HYBRID CLOUD AI",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (result.isOffline) CyberElectricBlue else Color(0xFF38BDF8),
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp
                                        )
                                    )
                                }

                                Text(
                                    text = "${result.indicators.size} SIGNALS FOUND",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = CyberTextMuted,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            RiskGaugeMeter(score = result.riskScore, riskLevel = result.riskLevel)

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
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = CyberTextSecondary,
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // URL Anatomy Breakdown if URL exists
                    if (result.urlBreakdown != null) {
                        UrlBreakdownCard(breakdown = result.urlBreakdown)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Threat Indicators list
                    if (result.indicators.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = CyberElectricBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DETECTED THREAT INDICATORS (${result.indicators.size}):",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CyberElectricBlue,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        result.indicators.forEach { ind ->
                            IndicatorCard(indicator = ind)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Recommended Safety Playbook
                    SafetyActionsList(actions = result.safetyActions)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons (Share report, Clear)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val shareText = buildString {
                                    appendLine("🛡️ PhishLens Security Scan Report")
                                    appendLine("Target: ${result.rawInput.take(60)}...")
                                    appendLine("Threat Level: ${result.riskLevel.name} (${result.riskScore}/100)")
                                    appendLine("Summary: ${result.explanation}")
                                    appendLine()
                                    appendLine("Key Actions:")
                                    result.safetyActions.take(3).forEach { appendLine("• $it") }
                                    appendLine()
                                    appendLine("Scanned with PhishLens Scam Detector")
                                }
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share PhishLens Report")
                                context.startActivity(shareIntent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberNavyCard,
                                contentColor = CyberElectricBlue
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(CyberBorder)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_report_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Findings", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = { viewModel.clearInput() },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CyberTextSecondary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("scan_another_button")
                        ) {
                            Text("New Scan", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}
