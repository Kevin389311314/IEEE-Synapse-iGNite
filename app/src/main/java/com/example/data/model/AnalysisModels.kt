package com.example.data.model

enum class AnalysisType {
    TEXT,
    URL,
    SCREENSHOT
}

enum class RiskLevel(val label: String) {
    LOW("LOW RISK"),
    MEDIUM("MEDIUM RISK"),
    HIGH("HIGH RISK");

    companion object {
        fun fromScore(score: Int): RiskLevel {
            return when {
                score < 30 -> LOW
                score < 60 -> MEDIUM
                else -> HIGH
            }
        }
    }
}

data class PhishingIndicator(
    val id: String,
    val ruleName: String,
    val category: String,
    val severity: RiskLevel,
    val description: String,
    val evidence: String,
    val points: Int
)

data class UrlBreakdown(
    val originalUrl: String,
    val protocol: String,
    val host: String,
    val registeredDomain: String,
    val subdomains: List<String>,
    val tld: String,
    val path: String,
    val queryParams: Map<String, String>,
    val isIpAddress: Boolean,
    val isPunycode: Boolean,
    val isSuspiciousTld: Boolean,
    val hasBrandKeywordInSubdomain: Boolean,
    val isShortener: Boolean,
    val hasOpenRedirect: Boolean
)

data class AnalysisResult(
    val id: Long = 0,
    val type: AnalysisType,
    val rawInput: String,
    val title: String,
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val explanation: String,
    val indicators: List<PhishingIndicator>,
    val safetyActions: List<String>,
    val urlBreakdown: UrlBreakdown? = null,
    val isOffline: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
