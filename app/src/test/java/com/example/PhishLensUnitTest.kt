package com.example

import com.example.data.model.RiskLevel
import com.example.domain.engine.PhishingRuleEngine
import com.example.domain.engine.UrlSafetyAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhishLensUnitTest {

    @Test
    fun testRawIpUrlDetection() {
        val result = UrlSafetyAnalyzer.analyze("http://192.168.1.1/login")
        assertTrue("Should detect IP host", result.breakdown.isIpAddress)
        assertTrue("Risk score should be high for raw IP login", result.riskScore >= 60)
        assertEquals(RiskLevel.HIGH, result.riskLevel)
        assertTrue(result.indicators.any { it.id == "URL_IP_HOST" })
    }

    @Test
    fun testPunycodeDeceptionDetection() {
        val result = UrlSafetyAnalyzer.analyze("https://xn--pple-43d.com/auth")
        assertTrue("Should detect punycode", result.breakdown.isPunycode)
        assertTrue(result.indicators.any { it.id == "URL_PUNYCODE" })
    }

    @Test
    fun testSubdomainBrandSpoofing() {
        val result = UrlSafetyAnalyzer.analyze("http://paypal.com.account-update.xyz/verify")
        assertTrue("Should flag brand in subdomain", result.breakdown.hasBrandKeywordInSubdomain)
        assertTrue(result.indicators.any { it.id == "URL_BRAND_SPOOF_SUBDOMAIN" })
        assertEquals(RiskLevel.HIGH, result.riskLevel)
    }

    @Test
    fun testCleanDomainInspection() {
        val result = UrlSafetyAnalyzer.analyze("https://github.com/torvalds/linux")
        assertFalse(result.breakdown.isIpAddress)
        assertFalse(result.breakdown.isPunycode)
        assertFalse(result.breakdown.isSuspiciousTld)
        assertTrue("Clean repo should have low risk score", result.riskScore < 30)
        assertEquals(RiskLevel.LOW, result.riskLevel)
    }

    @Test
    fun testUrgentSmishingAnalysis() {
        val text = "URGENT: Wells Fargo alert. Your account is suspended. Verify your password immediately within 24 hours at http://wellsfargo-verify.tk/login"
        val result = PhishingRuleEngine.analyzeText(text)
        assertTrue("Risk score should be high", result.riskScore >= 60)
        assertEquals(RiskLevel.HIGH, result.riskLevel)
        assertTrue(result.indicators.any { it.id == "URGENCY_DEADLINE" })
        assertTrue(result.indicators.any { it.id == "THREAT_CONSEQUENCE" })
        assertTrue(result.indicators.any { it.id == "CREDENTIAL_HARVESTING" })
    }

    @Test
    fun testBenignMessageAnalysis() {
        val text = "Your doctor appointment is confirmed for Tuesday at 3:00 PM. Reply STOP to cancel."
        val result = PhishingRuleEngine.analyzeText(text)
        assertTrue("Benign message should have low score", result.riskScore < 30)
        assertEquals(RiskLevel.LOW, result.riskLevel)
    }

    @Test
    fun testRiskLevelScoreMapping() {
        assertEquals(RiskLevel.LOW, RiskLevel.fromScore(0))
        assertEquals(RiskLevel.LOW, RiskLevel.fromScore(29))
        assertEquals(RiskLevel.MEDIUM, RiskLevel.fromScore(30))
        assertEquals(RiskLevel.MEDIUM, RiskLevel.fromScore(59))
        assertEquals(RiskLevel.HIGH, RiskLevel.fromScore(60))
        assertEquals(RiskLevel.HIGH, RiskLevel.fromScore(100))
    }
}
