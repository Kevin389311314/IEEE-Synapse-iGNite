package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AnalysisResult
import com.example.data.model.AnalysisType
import com.example.data.model.PhishingIndicator
import com.example.data.model.RiskLevel
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "analysis_history")
data class AnalysisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // TEXT, URL, SCREENSHOT
    val rawInput: String,
    val title: String,
    val riskScore: Int,
    val riskLevel: String, // LOW, MEDIUM, HIGH
    val explanation: String,
    val indicatorsJson: String,
    val safetyActionsJson: String,
    val isOffline: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): AnalysisResult {
        val indicatorsList = mutableListOf<PhishingIndicator>()
        try {
            val jsonArray = JSONArray(indicatorsJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                indicatorsList.add(
                    PhishingIndicator(
                        id = obj.optString("id", ""),
                        ruleName = obj.optString("ruleName", ""),
                        category = obj.optString("category", ""),
                        severity = try {
                            RiskLevel.valueOf(obj.optString("severity", "LOW"))
                        } catch (e: Exception) {
                            RiskLevel.LOW
                        },
                        description = obj.optString("description", ""),
                        evidence = obj.optString("evidence", ""),
                        points = obj.optInt("points", 0)
                    )
                )
            }
        } catch (_: Exception) {}

        val actionsList = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(safetyActionsJson)
            for (i in 0 until jsonArray.length()) {
                actionsList.add(jsonArray.getString(i))
            }
        } catch (_: Exception) {}

        return AnalysisResult(
            id = id,
            type = try { AnalysisType.valueOf(type) } catch (_: Exception) { AnalysisType.TEXT },
            rawInput = rawInput,
            title = title,
            riskScore = riskScore,
            riskLevel = try { RiskLevel.valueOf(riskLevel) } catch (_: Exception) { RiskLevel.LOW },
            explanation = explanation,
            indicators = indicatorsList,
            safetyActions = actionsList,
            isOffline = isOffline,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromDomain(domain: AnalysisResult): AnalysisEntity {
            val indicatorsArray = JSONArray()
            domain.indicators.forEach { indicator ->
                val obj = JSONObject()
                obj.put("id", indicator.id)
                obj.put("ruleName", indicator.ruleName)
                obj.put("category", indicator.category)
                obj.put("severity", indicator.severity.name)
                obj.put("description", indicator.description)
                obj.put("evidence", indicator.evidence)
                obj.put("points", indicator.points)
                indicatorsArray.put(obj)
            }

            val actionsArray = JSONArray()
            domain.safetyActions.forEach { action ->
                actionsArray.put(action)
            }

            return AnalysisEntity(
                id = domain.id,
                type = domain.type.name,
                rawInput = domain.rawInput,
                title = domain.title,
                riskScore = domain.riskScore,
                riskLevel = domain.riskLevel.name,
                explanation = domain.explanation,
                indicatorsJson = indicatorsArray.toString(),
                safetyActionsJson = actionsArray.toString(),
                isOffline = domain.isOffline,
                timestamp = domain.timestamp
            )
        }
    }
}
