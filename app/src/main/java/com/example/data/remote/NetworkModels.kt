package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TextAnalyzeRequest(
    @Json(name = "text") val text: String,
    @Json(name = "include_llm") val includeLlm: Boolean = false
)

@JsonClass(generateAdapter = true)
data class UrlAnalyzeRequest(
    @Json(name = "url") val url: String,
    @Json(name = "include_llm") val includeLlm: Boolean = false
)

@JsonClass(generateAdapter = true)
data class NetworkIndicator(
    @Json(name = "id") val id: String,
    @Json(name = "rule_name") val ruleName: String,
    @Json(name = "category") val category: String,
    @Json(name = "severity") val severity: String,
    @Json(name = "description") val description: String,
    @Json(name = "evidence") val evidence: String,
    @Json(name = "points") val points: Int
)

@JsonClass(generateAdapter = true)
data class NetworkAnalyzeResponse(
    @Json(name = "risk_score") val riskScore: Int,
    @Json(name = "risk_level") val riskLevel: String,
    @Json(name = "title") val title: String,
    @Json(name = "explanation") val explanation: String,
    @Json(name = "indicators") val indicators: List<NetworkIndicator>,
    @Json(name = "safety_actions") val safetyActions: List<String>,
    @Json(name = "engine") val engine: String = "FastAPI Backend"
)

@JsonClass(generateAdapter = true)
data class HealthResponse(
    @Json(name = "status") val status: String,
    @Json(name = "version") val version: String,
    @Json(name = "llm_enabled") val llmEnabled: Boolean = false
)
