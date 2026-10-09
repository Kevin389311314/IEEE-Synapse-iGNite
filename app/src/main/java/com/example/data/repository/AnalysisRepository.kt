package com.example.data.repository

import com.example.data.local.AnalysisDao
import com.example.data.local.AnalysisEntity
import com.example.data.model.AnalysisResult
import com.example.data.model.AnalysisType
import com.example.data.model.PhishingIndicator
import com.example.data.model.RiskLevel
import com.example.data.remote.ApiClient
import com.example.data.remote.TextAnalyzeRequest
import com.example.data.remote.UrlAnalyzeRequest
import com.example.domain.engine.PhishingRuleEngine
import com.example.domain.engine.UrlSafetyAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AnalysisRepository(
    private val dao: AnalysisDao
) {
    val allHistory: Flow<List<AnalysisResult>> = dao.getAllHistory().map { list ->
        list.map { it.toDomainModel() }
    }

    suspend fun getById(id: Long): AnalysisResult? = withContext(Dispatchers.IO) {
        dao.getById(id)?.toDomainModel()
    }

    suspend fun saveAnalysis(result: AnalysisResult): Long = withContext(Dispatchers.IO) {
        val entity = AnalysisEntity.fromDomain(result)
        dao.insert(entity)
    }

    suspend fun deleteAnalysis(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        dao.clearAll()
    }

    suspend fun analyzeText(
        text: String,
        type: AnalysisType = AnalysisType.TEXT,
        preferRemote: Boolean = false,
        useLlm: Boolean = false
    ): AnalysisResult = withContext(Dispatchers.Default) {
        if (preferRemote) {
            try {
                val service = ApiClient.getService()
                val response = service.analyzeText(
                    TextAnalyzeRequest(text = text, includeLlm = useLlm)
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val indicators = body.indicators.map { netInd ->
                        PhishingIndicator(
                            id = netInd.id,
                            ruleName = netInd.ruleName,
                            category = netInd.category,
                            severity = try { RiskLevel.valueOf(netInd.severity) } catch (_: Exception) { RiskLevel.LOW },
                            description = netInd.description,
                            evidence = netInd.evidence,
                            points = netInd.points
                        )
                    }
                    return@withContext AnalysisResult(
                        type = type,
                        rawInput = text,
                        title = body.title,
                        riskScore = body.riskScore,
                        riskLevel = try { RiskLevel.valueOf(body.riskLevel) } catch (_: Exception) { RiskLevel.LOW },
                        explanation = body.explanation,
                        indicators = indicators,
                        safetyActions = body.safetyActions,
                        isOffline = false,
                        timestamp = System.currentTimeMillis()
                    )
                }
            } catch (_: Exception) {
                // Network failed or offline: seamless fallback to local rule engine!
            }
        }

        // Local Rule Engine (100% offline)
        PhishingRuleEngine.analyzeText(text, type = type)
    }

    suspend fun analyzeUrl(
        url: String,
        preferRemote: Boolean = false,
        useLlm: Boolean = false
    ): AnalysisResult = withContext(Dispatchers.Default) {
        if (preferRemote) {
            try {
                val service = ApiClient.getService()
                val response = service.analyzeUrl(
                    UrlAnalyzeRequest(url = url, includeLlm = useLlm)
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val localInspection = UrlSafetyAnalyzer.analyze(url)
                    val indicators = body.indicators.map { netInd ->
                        PhishingIndicator(
                            id = netInd.id,
                            ruleName = netInd.ruleName,
                            category = netInd.category,
                            severity = try { RiskLevel.valueOf(netInd.severity) } catch (_: Exception) { RiskLevel.LOW },
                            description = netInd.description,
                            evidence = netInd.evidence,
                            points = netInd.points
                        )
                    }
                    return@withContext AnalysisResult(
                        type = AnalysisType.URL,
                        rawInput = url,
                        title = body.title,
                        riskScore = body.riskScore,
                        riskLevel = try { RiskLevel.valueOf(body.riskLevel) } catch (_: Exception) { RiskLevel.LOW },
                        explanation = body.explanation,
                        indicators = indicators,
                        safetyActions = body.safetyActions,
                        urlBreakdown = localInspection.breakdown,
                        isOffline = false,
                        timestamp = System.currentTimeMillis()
                    )
                }
            } catch (_: Exception) {
                // Fallback to local
            }
        }

        val localResult = UrlSafetyAnalyzer.analyze(url)
        AnalysisResult(
            type = AnalysisType.URL,
            rawInput = url,
            title = when (localResult.riskLevel) {
                RiskLevel.HIGH -> "High Risk Deceptive URL"
                RiskLevel.MEDIUM -> "Suspicious URL Warning"
                RiskLevel.LOW -> "Low Risk URL Structure"
            },
            riskScore = localResult.riskScore,
            riskLevel = localResult.riskLevel,
            explanation = localResult.explanation,
            indicators = localResult.indicators,
            safetyActions = localResult.safetyActions,
            urlBreakdown = localResult.breakdown,
            isOffline = true,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun testBackendConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.getService().checkHealth()
            response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }
}
