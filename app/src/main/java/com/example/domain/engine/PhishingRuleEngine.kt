package com.example.domain.engine

import com.example.data.model.AnalysisResult
import com.example.data.model.AnalysisType
import com.example.data.model.PhishingIndicator
import com.example.data.model.RiskLevel
import java.util.regex.Pattern

object PhishingRuleEngine {

    private val URL_REGEX = Pattern.compile(
        "\\b(?:https?://|www\\.)[a-zA-Z0-9\\-\\._~:/?#\\[\\]@!$&'()*+,;=%]+\\b",
        Pattern.CASE_INSENSITIVE
    )

    // Rule definitions with patterns, weights, and categories
    private data class RulePattern(
        val id: String,
        val ruleName: String,
        val category: String,
        val severity: RiskLevel,
        val points: Int,
        val description: String,
        val regexes: List<Regex>
    )

    private val RULES: List<RulePattern> = listOf(
        RulePattern(
            id = "URGENCY_DEADLINE",
            ruleName = "Urgent Action & Deadline Pressure",
            category = "Psychological Pressure",
            severity = RiskLevel.HIGH,
            points = 30,
            description = "Creates artificial urgency or time-bounded deadlines to rush victims into bypassing rational judgment.",
            regexes = listOf(
                Regex("(?i)\\b(immediately|urgent|within\\s+\\d+\\s*(hours?|hrs?|mins?|minutes?)|act\\s+now|action\\s+required|expires?\\s+today|time\\s+sensitive)\\b"),
                Regex("(?i)\\b(final\\s+notice|last\\s+warning|within\\s+24\\s*h(ours?)?|immediate\\s+action)\\b")
            )
        ),
        RulePattern(
            id = "THREAT_CONSEQUENCE",
            ruleName = "Threats & Account Suspension",
            category = "Coercion & Intimidation",
            severity = RiskLevel.HIGH,
            points = 35,
            description = "Threatens punitive consequences like permanent account termination, legal action, or law enforcement.",
            regexes = listOf(
                Regex("(?i)\\b(account\\s+(?:(?:is|was|will\\s+be|has\\s+been)\\s+)?(suspended|locked|terminated|blocked|disabled|restricted))\\b"),
                Regex("(?i)\\b(legal\\s+action|police|arrest\\s+warrant|law\\s+enforcement|court\\s+order|fbi|irs\\s+agent)\\b"),
                Regex("(?i)\\b(unauthorized\\s+(access|transaction|login|activity)|security\\s+breach)\\b")
            )
        ),
        RulePattern(
            id = "CREDENTIAL_HARVESTING",
            ruleName = "Credential & Verification Request",
            category = "Credential Harvesting",
            severity = RiskLevel.HIGH,
            points = 40,
            description = "Solicits passwords, authentication PINs, one-time verification codes, or personal account access.",
            regexes = listOf(
                Regex("(?i)\\b(verify|confirm|update|validate|reset)\\s+(your|the)?\\s*(password|pin|credentials?|login|account|security\\s+info)\\b"),
                Regex("(?i)\\b(enter|send|provide|share)\\s+(your|the)?\\s*(otp|2fa|code|passcode|one-time\\s+code)\\b"),
                Regex("(?i)\\b(social\\s+security|ssn|date\\s+of\\s+birth|mother'?s\\s+maiden)\\b")
            )
        ),
        RulePattern(
            id = "DELIVERY_SMISHING",
            ruleName = "Parcel Delivery Scam Pretext",
            category = "Impersonation (Logistics)",
            severity = RiskLevel.HIGH,
            points = 35,
            description = "Falsely claims an undelivered package or missing postal address to bait link interaction or payment of 'redelivery fees'.",
            regexes = listOf(
                Regex("(?i)\\b(usps|fedex|ups|dhl|postal\\s+service|parcel)\\b.*\\b(cannot\\s+be\\s+delivered|on\\s+hold|incomplete\\s+address|redelivery|schedule\\s+delivery)\\b"),
                Regex("(?i)\\b(package|parcel|shipment)\\s+(tracking|#|notification|delay|failed)\\b"),
                Regex("(?i)\\b(update|confirm)\\s+address\\s+to\\s+receive\\b")
            )
        ),
        RulePattern(
            id = "BANK_IMPERSONATION",
            ruleName = "Financial Institution Spoofing",
            category = "Brand Impersonation",
            severity = RiskLevel.HIGH,
            points = 35,
            description = "Impersonates recognizable banks or credit card issuers to fabricate security emergencies.",
            regexes = listOf(
                Regex("(?i)\\b(wells\\s*fargo|chase|bank\\s+of\\s+america|citi(bank)?|capital\\s*one|paypal|chime)\\b.*\\b(security|fraud|locked|verify|alert|debit\\s+card)\\b"),
                Regex("(?i)\\b(your\\s+card\\s+has\\s+been\\s+(blocked|frozen|compromised))\\b")
            )
        ),
        RulePattern(
            id = "TECH_SUPPORT_REFUND",
            ruleName = "Fake Invoice & Tech Support Refund",
            category = "Financial Fraud",
            severity = RiskLevel.HIGH,
            points = 30,
            description = "Fabricates an unexpected renewal charge (e.g., Geek Squad, McAfee) with a phone number to call for a 'cancellation refund'.",
            regexes = listOf(
                Regex("(?i)\\b(geek\\s*squad|norton|mcafee|best\\s*buy)\\b.*\\b(renew(ed|al)?|subscription|charge|invoice|auto-debit)\\b"),
                Regex("(?i)\\b(call\\s+(us|support|helpdesk|our\\s+billing)|if\\s+you\\s+did\\s+not\\s+authorize)\\b.*\\b\\+?\\d{1,3}?[-.\\s]?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}\\b")
            )
        ),
        RulePattern(
            id = "FINANCIAL_WINDFALL_CRYPTO",
            ruleName = "Unsolicited Windfall or Crypto Lure",
            category = "Financial Lure",
            severity = RiskLevel.HIGH,
            points = 30,
            description = "Entices with unexpected financial prizes, lottery grants, inheritance, or cryptocurrency schemes.",
            regexes = listOf(
                Regex("(?i)\\b(won|selected\\s+for|claim|congratulations)\\s+.*\\b(\\$|usd|dollars|bitcoin|crypto|grant|lottery|prize)\\b"),
                Regex("(?i)\\b(gift\\s*cards?|itunes\\s*cards?|steam\\s*cards?|western\\s*union|moneygram)\\b"),
                Regex("(?i)\\b(earn\\s+\\$\\d+\\s*(per|a)?\\s*(day|hour|week)|remote\\s+job|work\\s+from\\s+home)\\b.*\\b(telegram|whatsapp)\\b")
            )
        ),
        RulePattern(
            id = "GENERIC_SALUTATION",
            ruleName = "Impersonal Generic Greeting",
            category = "Smishing Evasion",
            severity = RiskLevel.LOW,
            points = 15,
            description = "Uses vague greetings like 'Dear Customer' rather than your real name, common in mass phishing blasts.",
            regexes = listOf(
                Regex("(?i)\\b(dear\\s+(customer|client|user|member|cardholder|account\\s*holder)|attention\\s+(user|customer))\\b")
            )
        )
    )

    fun analyzeText(rawText: String, type: AnalysisType = AnalysisType.TEXT): AnalysisResult {
        val trimmed = rawText.trim()
        val indicators = mutableListOf<PhishingIndicator>()
        var score = 0

        // 1. Scan text rules
        RULES.forEach { rule ->
            for (regex in rule.regexes) {
                val match = regex.find(trimmed)
                if (match != null) {
                    val snippet = match.value
                    indicators.add(
                        PhishingIndicator(
                            id = rule.id,
                            ruleName = rule.ruleName,
                            category = rule.category,
                            severity = rule.severity,
                            description = rule.description,
                            evidence = snippet,
                            points = rule.points
                        )
                    )
                    score += rule.points
                    break // Avoid duplicate triggers for same rule ID
                }
            }
        }

        // 2. Scan for URLs inside text
        val urlMatcher = URL_REGEX.matcher(trimmed)
        val extractedUrls = mutableListOf<String>()
        while (urlMatcher.find()) {
            extractedUrls.add(urlMatcher.group())
        }

        var primaryUrlBreakdown: com.example.data.model.UrlBreakdown? = null

        if (extractedUrls.isNotEmpty()) {
            extractedUrls.take(3).forEach { url ->
                val urlResult = UrlSafetyAnalyzer.analyze(url)
                if (primaryUrlBreakdown == null) {
                    primaryUrlBreakdown = urlResult.breakdown
                }

                // Add indicators from URL
                urlResult.indicators.forEach { urlInd ->
                    indicators.add(
                        PhishingIndicator(
                            id = "LINK_${urlInd.id}",
                            ruleName = "Embedded Link: ${urlInd.ruleName}",
                            category = "Suspicious Link (${urlInd.category})",
                            severity = urlInd.severity,
                            description = urlInd.description,
                            evidence = "${urlInd.evidence} (found in URL: $url)",
                            points = urlInd.points
                        )
                    )
                    score += (urlInd.points * 0.9).toInt()
                }
            }
        }

        // 3. Shouting / Uppercase check
        val letters = trimmed.filter { it.isLetter() }
        if (letters.length >= 20) {
            val upperCount = letters.count { it.isUpperCase() }
            val ratio = upperCount.toFloat() / letters.length
            if (ratio > 0.45f) {
                indicators.add(
                    PhishingIndicator(
                        id = "AGGRESSIVE_CAPS",
                        ruleName = "Excessive Capitalization / Pressure",
                        category = "Psychological Pressure",
                        severity = RiskLevel.MEDIUM,
                        description = "Message employs aggressive capitalization to induce panic and urgency.",
                        evidence = "${(ratio * 100).toInt()}% uppercase characters",
                        points = 15
                    )
                )
                score += 15
            }
        }

        // Clamp 0..100
        val finalScore = score.coerceIn(0, 100)
        val riskLevel = RiskLevel.fromScore(finalScore)

        val title = when (riskLevel) {
            RiskLevel.HIGH -> "High Phishing Threat Detected"
            RiskLevel.MEDIUM -> "Suspicious Scam Warning"
            RiskLevel.LOW -> "Low Phishing Indicators"
        }

        val explanation = when (riskLevel) {
            RiskLevel.HIGH -> "CRITICAL RISK: Multiple high-confidence phishing indicators detected (${indicators.size} signs), including coercive pressure, credential harvesting, or deceptive links. DO NOT reply, click links, or provide information."
            RiskLevel.MEDIUM -> "CAUTION: This message contains suspicious elements (${indicators.size} warning signs) commonly associated with smishing campaigns and unverified promotions. Exercise heightened vigilance."
            RiskLevel.LOW -> "SAFE PROFILE: No high-risk coercive threats, credential lures, or known spoofing markers were detected. Always remain aware of social engineering."
        }

        val safetyActions = mutableListOf<String>()
        if (finalScore >= 60) {
            safetyActions.add("DO NOT reply or send any verification codes (2FA / OTP).")
            safetyActions.add("DO NOT click any links inside the message.")
            safetyActions.add("Forward SMS to '7726' (SPAM) to report it to your telecom carrier.")
            safetyActions.add("Verify alerts independently by logging in directly via the official app or website.")
            safetyActions.add("Block the sender number or email address.")
        } else if (finalScore >= 30) {
            safetyActions.add("Inspect any attached link domain carefully before clicking.")
            safetyActions.add("Never send payments or gift cards in response to unexpected messages.")
            safetyActions.add("Contact the purported organization using a publicly verified phone number.")
        } else {
            safetyActions.add("Standard safety: Ensure sender identity matches official communication channels.")
            safetyActions.add("Never share account credentials over unsolicited text.")
        }

        return AnalysisResult(
            type = type,
            rawInput = rawText,
            title = title,
            riskScore = finalScore,
            riskLevel = riskLevel,
            explanation = explanation,
            indicators = indicators,
            safetyActions = safetyActions,
            urlBreakdown = primaryUrlBreakdown,
            isOffline = true,
            timestamp = System.currentTimeMillis()
        )
    }
}
