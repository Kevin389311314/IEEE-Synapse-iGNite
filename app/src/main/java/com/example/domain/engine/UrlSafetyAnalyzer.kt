package com.example.domain.engine

import com.example.data.model.PhishingIndicator
import com.example.data.model.RiskLevel
import com.example.data.model.UrlBreakdown
import java.net.URI
import java.net.URLDecoder
import java.util.regex.Pattern

object UrlSafetyAnalyzer {

    private val IP_PATTERN = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")
    private val HEX_IP_PATTERN = Pattern.compile("^0x[0-9a-fA-F]+$")

    private val SUSPICIOUS_TLDS = setOf(
        "tk", "ml", "ga", "cf", "gq", "top", "xyz", "work", "click",
        "fit", "rest", "country", "vip", "icu", "buzz", "cam", "sbs",
        "surf", "monster", "cfd", "cyou", "quest", "beauty", "hair",
        "mom", "stream", "gdn", "date", "racing", "win"
    )

    private val POPULAR_BRANDS = listOf(
        "paypal", "apple", "google", "microsoft", "amazon", "netflix",
        "wellsfargo", "chase", "bankofamerica", "citi", "capitalone",
        "usps", "fedex", "dhl", "ups", "irs", "coinbase", "binance",
        "meta", "facebook", "instagram", "whatsapp", "telegram", "chime"
    )

    private val URL_SHORTENERS = setOf(
        "bit.ly", "tinyurl.com", "is.gd", "t.co", "cutt.ly", "ow.ly",
        "buff.ly", "rebrand.ly", "shorturl.at", "soo.gd", "v.gd", "rb.gy"
    )

    private val REDIRECT_PARAMS = setOf(
        "redirect", "url", "next", "dest", "destination", "target", "return", "link", "goto", "out"
    )

    data class UrlAnalysisResult(
        val breakdown: UrlBreakdown,
        val indicators: List<PhishingIndicator>,
        val riskScore: Int,
        val riskLevel: RiskLevel,
        val explanation: String,
        val safetyActions: List<String>
    )

    fun analyze(inputUrl: String): UrlAnalysisResult {
        var cleanInput = inputUrl.trim()
        if (!cleanInput.startsWith("http://") && !cleanInput.startsWith("https://")) {
            cleanInput = "https://$cleanInput"
        }

        val indicators = mutableListOf<PhishingIndicator>()
        var score = 0

        var protocol = ""
        var host = ""
        var path = ""
        val queryMap = mutableMapOf<String, String>()

        try {
            val uri = URI(cleanInput)
            protocol = uri.scheme?.lowercase() ?: "unknown"
            host = uri.host?.lowercase() ?: ""
            path = uri.path ?: ""
            uri.query?.let { q ->
                q.split("&").forEach { param ->
                    val parts = param.split("=", limit = 2)
                    if (parts.size == 2) {
                        queryMap[URLDecoder.decode(parts[0], "UTF-8")] = URLDecoder.decode(parts[1], "UTF-8")
                    } else if (parts.isNotEmpty()) {
                        queryMap[parts[0]] = ""
                    }
                }
            }
        } catch (_: Exception) {
            host = cleanInput.removePrefix("https://").removePrefix("http://").substringBefore("/").substringBefore("?")
            protocol = if (cleanInput.startsWith("http://")) "http" else "https"
        }

        // Host parsing & subdomain deconstruction
        val hostParts = host.split(".").filter { it.isNotEmpty() }
        val tld = if (hostParts.isNotEmpty()) hostParts.last().lowercase() else ""
        val registeredDomain = if (hostParts.size >= 2) {
            "${hostParts[hostParts.size - 2]}.${hostParts.last()}"
        } else {
            host
        }
        val subdomains = if (hostParts.size > 2) {
            hostParts.subList(0, hostParts.size - 2)
        } else {
            emptyList()
        }

        val isIp = IP_PATTERN.matcher(host).matches() || HEX_IP_PATTERN.matcher(host).matches()
        val isPunycode = host.contains("xn--")
        val isSuspiciousTld = SUSPICIOUS_TLDS.contains(tld)
        val isShortener = URL_SHORTENERS.contains(host)

        // Check 1: Raw IP Address Host
        if (isIp) {
            indicators.add(
                PhishingIndicator(
                    id = "URL_IP_HOST",
                    ruleName = "Raw IP Address Host",
                    category = "Suspicious URL Structure",
                    severity = RiskLevel.HIGH,
                    description = "URL points directly to an IP address instead of a recognized domain name. Legitimate services virtually never use raw IP hosts.",
                    evidence = host,
                    points = 45
                )
            )
            score += 45
        }

        // Check 2: Punycode / IDN Homoglyph Attack
        if (isPunycode) {
            indicators.add(
                PhishingIndicator(
                    id = "URL_PUNYCODE",
                    ruleName = "Punycode Homoglyph Deception",
                    category = "Domain Spoofing",
                    severity = RiskLevel.HIGH,
                    description = "URL uses internationalized Punycode ('xn--'), which attackers employ to impersonate recognizable brand characters with foreign Unicode lookalikes.",
                    evidence = host,
                    points = 40
                )
            )
            score += 40
        }

        // Check 3: Suspicious Disposable TLD
        if (isSuspiciousTld) {
            indicators.add(
                PhishingIndicator(
                    id = "URL_SUSPICIOUS_TLD",
                    ruleName = "High-Risk Disposable TLD",
                    category = "Domain Reputation",
                    severity = RiskLevel.HIGH,
                    description = "Domain uses '.$tld', a top-level domain frequently associated with throwaway phishing campaigns due to low-cost or free registration.",
                    evidence = ".$tld",
                    points = 35
                )
            )
            score += 35
        }

        // Check 4: Brand Spoofing in Subdomain or Path
        var hasBrandInSubdomain = false
        val subdomainString = subdomains.joinToString(".")
        POPULAR_BRANDS.forEach { brand ->
            if (subdomainString.contains(brand) && !registeredDomain.startsWith(brand)) {
                hasBrandInSubdomain = true
                indicators.add(
                    PhishingIndicator(
                        id = "URL_BRAND_SPOOF_SUBDOMAIN",
                        ruleName = "Subdomain Brand Spoofing",
                        category = "Brand Impersonation",
                        severity = RiskLevel.HIGH,
                        description = "The brand '$brand' appears in the subdomain prefix ($subdomainString) while the actual registered destination domain is '$registeredDomain'. This is a classic deception tactic.",
                        evidence = "$subdomainString on $registeredDomain",
                        points = 45
                    )
                )
                score += 45
            }
        }

        // Check 5: Excessive Subdomains / Deep Hierarchy
        if (subdomains.size >= 3) {
            indicators.add(
                PhishingIndicator(
                    id = "URL_EXCESSIVE_SUBDOMAINS",
                    ruleName = "Excessive Subdomain Levels",
                    category = "Obfuscation & Evasion",
                    severity = RiskLevel.MEDIUM,
                    description = "Domain contains ${subdomains.size} nested subdomain layers. Attackers use deep nesting to push the real domain off-screen on mobile devices.",
                    evidence = host,
                    points = 25
                )
            )
            score += 25
        }

        // Check 6: Hyphenation Stuffing in Host
        val hyphenCount = host.count { it == '-' }
        if (hyphenCount >= 3) {
            indicators.add(
                PhishingIndicator(
                    id = "URL_EXCESSIVE_HYPHENS",
                    ruleName = "Domain Hyphen Stuffing",
                    category = "Suspicious URL Structure",
                    severity = RiskLevel.MEDIUM,
                    description = "Domain name contains $hyphenCount hyphens. Legitimate brands maintain clean domains, whereas scammers combine keyword strings (e.g. 'verify-account-security').",
                    evidence = host,
                    points = 20
                )
            )
            score += 20
        }

        // Check 7: Credential / Security Keywords in Host or Path
        val sensitiveKeywords = listOf("login", "signin", "verify", "account", "security", "update", "banking", "wallet", "recover", "authenticate")
        val foundKeywords = sensitiveKeywords.filter { host.contains(it) || path.contains(it) }
        if (foundKeywords.isNotEmpty()) {
            val isKnownLegitDomain = isLikelyLegitimateHost(host)
            if (!isKnownLegitDomain) {
                indicators.add(
                    PhishingIndicator(
                        id = "URL_CREDENTIAL_LURE_PATH",
                        ruleName = "Credential Harvesting Keyword in URL",
                        category = "Credential Harvesting",
                        severity = RiskLevel.MEDIUM,
                        description = "URL incorporates sensitive security keywords (${foundKeywords.joinToString(", ")}) on an unverified domain.",
                        evidence = foundKeywords.joinToString(", "),
                        points = 25
                    )
                )
                score += 25
            }
        }

        // Check 8: Insecure Protocol (HTTP) on sensitive keywords
        if (protocol == "http" && foundKeywords.isNotEmpty()) {
            indicators.add(
                PhishingIndicator(
                    id = "URL_INSECURE_HTTP",
                    ruleName = "Unencrypted HTTP on Authentication Keyword",
                    category = "Security Standards",
                    severity = RiskLevel.HIGH,
                    description = "URL uses unencrypted 'http://' while referencing sensitive actions. All authentic financial and login services strictly mandate HTTPS.",
                    evidence = "$protocol://$host",
                    points = 30
                )
            )
            score += 30
        }

        // Check 9: URL Shortener Masking
        if (isShortener) {
            indicators.add(
                PhishingIndicator(
                    id = "URL_SHORTENER_MASK",
                    ruleName = "URL Shortener / Destination Hidden",
                    category = "Obfuscation & Evasion",
                    severity = RiskLevel.MEDIUM,
                    description = "URL is processed through a shortening service ($host), concealing the actual web destination from view.",
                    evidence = host,
                    points = 20
                )
            )
            score += 20
        }

        // Check 10: Open Redirect Vector
        var hasOpenRedirect = false
        queryMap.forEach { (k, v) ->
            if (REDIRECT_PARAMS.contains(k.lowercase()) && (v.startsWith("http://") || v.startsWith("https://") || v.contains("//"))) {
                hasOpenRedirect = true
                indicators.add(
                    PhishingIndicator(
                        id = "URL_OPEN_REDIRECT",
                        ruleName = "Open Redirect Vector in Query",
                        category = "Exploitation Technique",
                        severity = RiskLevel.HIGH,
                        description = "URL parameters contain a secondary redirection target ('$k=$v'). Attackers abuse open redirects to bypass perimeter URL filters.",
                        evidence = "$k=$v",
                        points = 35
                    )
                )
                score += 35
            }
        }

        // Clamp score 0..100
        val finalScore = score.coerceIn(0, 100)
        val riskLevel = RiskLevel.fromScore(finalScore)

        val explanation = when (riskLevel) {
            RiskLevel.HIGH -> "CRITICAL THREAT: This URL exhibits severe indicators of phishing, domain spoofing, or deceptive redirection. Under no circumstances should you visit this destination or submit credentials."
            RiskLevel.MEDIUM -> "ELEVATED RISK: This URL exhibits multiple structural anomalies or masking techniques. Exercise extreme caution and verify the legitimate host through an independent browser tab."
            RiskLevel.LOW -> "LOW RISK: No prominent phishing or spoofing patterns were detected in the URL structure. Always verify the HTTPS security padlock and certificate."
        }

        val safetyActions = mutableListOf<String>()
        if (finalScore >= 60) {
            safetyActions.add("DO NOT click or open this link in any browser.")
            safetyActions.add("DO NOT enter login passwords, SMS codes, or payment card details.")
            safetyActions.add("If received via message, report the sender as spam/phishing.")
            safetyActions.add("Navigate manually to the official website by typing the verified URL yourself.")
        } else if (finalScore >= 30) {
            safetyActions.add("Inspect the registered domain carefully before interacting.")
            safetyActions.add("Look up the service in your official app or search engine instead of following links.")
            safetyActions.add("Never download unexpected APKs, profiles, or executable files.")
        } else {
            safetyActions.add("Verify that the destination matches the company's official public domain.")
            safetyActions.add("Ensure your browser displays a valid TLS/SSL security certificate.")
        }

        val breakdown = UrlBreakdown(
            originalUrl = cleanInput,
            protocol = protocol,
            host = host,
            registeredDomain = registeredDomain,
            subdomains = subdomains,
            tld = tld,
            path = path,
            queryParams = queryMap,
            isIpAddress = isIp,
            isPunycode = isPunycode,
            isSuspiciousTld = isSuspiciousTld,
            hasBrandKeywordInSubdomain = hasBrandInSubdomain,
            isShortener = isShortener,
            hasOpenRedirect = hasOpenRedirect
        )

        return UrlAnalysisResult(
            breakdown = breakdown,
            indicators = indicators,
            riskScore = finalScore,
            riskLevel = riskLevel,
            explanation = explanation,
            safetyActions = safetyActions
        )
    }

    private fun isLikelyLegitimateHost(host: String): Boolean {
        val trustedDomains = setOf(
            "google.com", "apple.com", "microsoft.com", "amazon.com", "github.com",
            "paypal.com", "chase.com", "wellsfargo.com", "bankofamerica.com", "netflix.com"
        )
        return trustedDomains.any { host == it || host.endsWith(".$it") }
    }
}
