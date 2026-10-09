package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RiskLevel
import com.example.ui.theme.CyberHighRisk
import com.example.ui.theme.CyberHighRiskContainer
import com.example.ui.theme.CyberLowRisk
import com.example.ui.theme.CyberLowRiskContainer
import com.example.ui.theme.CyberMediumRisk
import com.example.ui.theme.CyberMediumRiskContainer
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary

@Composable
fun RiskGaugeMeter(
    score: Int,
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "score_progress"
    )

    val (accentColor, containerColor) = when (riskLevel) {
        RiskLevel.HIGH -> Pair(CyberHighRisk, CyberHighRiskContainer)
        RiskLevel.MEDIUM -> Pair(CyberMediumRisk, CyberMediumRiskContainer)
        RiskLevel.LOW -> Pair(CyberLowRisk, CyberLowRiskContainer)
    }

    Box(
        modifier = modifier
            .testTag("risk_gauge_meter")
            .size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(150.dp)) {
            val strokeWidth = 12.dp.toPx()
            // Background arc (full 260 degrees)
            drawArc(
                color = Color(0xFF1E293B),
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Dynamic progress sweep
            val sweep = 260f * animatedProgress
            if (sweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(
                            accentColor.copy(alpha = 0.7f),
                            accentColor
                        )
                    ),
                    startAngle = 140f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Center Content: Score number and Risk Level
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = CyberTextPrimary
                )
            )
            Text(
                text = "/ 100",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = CyberTextMuted,
                    fontSize = 11.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(containerColor)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = riskLevel.name,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}
