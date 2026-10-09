import re
import os
from urllib.parse import urlparse, parse_qs
from typing import List, Dict, Any, Tuple
from models import IndicatorModel, AnalyzeResponse

SUSPICIOUS_TLDS = {
    "tk", "ml", "ga", "cf", "gq", "top", "xyz", "work", "click",
    "fit", "rest", "country", "vip", "icu", "buzz", "cam", "sbs",
    "surf", "monster", "cfd", "cyou", "quest", "beauty"
}

POPULAR_BRANDS = [
    "paypal", "apple", "google", "microsoft", "amazon", "netflix",
    "wellsfargo", "chase", "bankofamerica", "citi", "capitalone",
    "usps", "fedex", "dhl", "ups", "irs", "coinbase", "binance"
]

SHORTENERS = {"bit.ly", "tinyurl.com", "is.gd", "t.co", "cutt.ly", "ow.ly", "shorturl.at"}
REDIRECT_PARAMS = {"redirect", "url", "next", "dest", "target", "return", "goto", "out"}

IP_REGEX = re.compile(r"^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$")

TEXT_RULES = [
    {
        "id": "URGENCY_DEADLINE",
        "name": "Urgent Action & Artificial Deadline",
        "category": "Psychological Pressure",
        "severity": "HIGH",
        "points": 30,
        "description": "Manufactures urgency to rush the victim into bypassing security precautions.",
        "patterns": [
            r"(?i)\b(immediately|urgent|within\s+\d+\s*(hours?|hrs?|mins?)|act\s+now|action\s+required|expires?\s+today)\b",
            r"(?i)\b(final\s+notice|last\s+warning|within\s+24\s*h(ours?)?)\b"
        ]
    },
    {
        "id": "THREAT_CONSEQUENCE",
        "name": "Threat of Account Suspension or Arrest",
        "category": "Coercion & Intimidation",
        "severity": "HIGH",
        "points": 35,
        "description": "Threatens punitive actions like account locking, legal summons, or police intervention.",
        "patterns": [
            r"(?i)\b(account\s+(?:(?:is|was|will\s+be|has\s+been)\s+)?(suspended|locked|terminated|blocked|disabled|restricted))\b",
            r"(?i)\b(legal\s+action|police|arrest\s+warrant|law\s+enforcement|irs\s+agent)\b",
            r"(?i)\b(unauthorized\s+(access|transaction|login|activity))\b"
        ]
    },
    {
        "id": "CREDENTIAL_HARVESTING",
        "name": "Credential & OTP Verification Bait",
        "category": "Credential Harvesting",
        "severity": "HIGH",
        "points": 40,
        "description": "Directly requests login passwords, PINs, or 2FA authentication codes.",
        "patterns": [
            r"(?i)\b(verify|confirm|update|validate|reset)\s+(your|the)?\s*(password|pin|credentials?|login|account)\b",
            r"(?i)\b(enter|send|provide|share)\s+(your|the)?\s*(otp|2fa|code|passcode)\b",
            r"(?i)\b(social\s+security|ssn|date\s+of\s+birth)\b"
        ]
    },
    {
        "id": "DELIVERY_SMISHING",
        "name": "Package Delivery Scam Pretext",
        "category": "Impersonation (Logistics)",
        "severity": "HIGH",
        "points": 35,
        "description": "Baiting incomplete package delivery and requesting address or redelivery fees.",
        "patterns": [
            r"(?i)\b(usps|fedex|ups|dhl|postal\s+service)\b.*(cannot\s+be\s+delivered|on\s+hold|incomplete\s+address|redelivery)\b",
            r"(?i)\b(package|parcel|shipment)\s+(tracking|#|notification|delay)\b"
        ]
    },
    {
        "id": "BANK_IMPERSONATION",
        "name": "Financial Institution Smishing",
        "category": "Brand Impersonation",
        "severity": "HIGH",
        "points": 35,
        "description": "Impersonates recognizable banks to simulate fraudulent transactions.",
        "patterns": [
            r"(?i)\b(wells\s*fargo|chase|bank\s+of\s+america|citi|capital\s*one|paypal)\b.*(security|fraud|locked|verify|alert)\b"
        ]
    },
    {
        "id": "TECH_SUPPORT_REFUND",
        "name": "Fake Auto-Renewal Invoice Scam",
        "category": "Financial Fraud",
        "severity": "HIGH",
        "points": 30,
        "description": "Claims an unauthorized renewal charge (e.g. Geek Squad) with a number to call for a refund.",
        "patterns": [
            r"(?i)\b(geek\s*squad|norton|mcafee)\b.*(renew(ed|al)?|subscription|charge|invoice)\b",
            r"(?i)\b(call\s+(us|support)|if\s+you\s+did\s+not\s+authorize)\b"
        ]
    }
]

def analyze_url_heuristics(url_str: str) -> AnalyzeResponse:
    clean_url = url_str.strip()
    if not clean_url.startswith("http://") and not clean_url.startswith("https://"):
        clean_url = f"https://{clean_url}"

    parsed = urlparse(clean_url)
    host = (parsed.hostname or "").lower()
    path = parsed.path or ""
    query_params = parse_qs(parsed.query)

    indicators: List[IndicatorModel] = []
    score = 0

    parts = [p for p in host.split(".") if p]
    tld = parts[-1] if parts else ""
    registered_domain = ".".join(parts[-2:]) if len(parts) >= 2 else host
    subdomains = parts[:-2] if len(parts) > 2 else []

    # 1. Raw IP address host
    if IP_REGEX.match(host):
        indicators.append(IndicatorModel(
            id="URL_IP_HOST",
            rule_name="Raw IP Address Host",
            category="Suspicious URL Structure",
            severity="HIGH",
            description="URL uses a raw IP address instead of a standard domain. Legitimate companies almost never direct users to raw IPs.",
            evidence=host,
            points=45
        ))
        score += 45

    # 2. Punycode (IDN homoglyph)
    if "xn--" in host:
        indicators.append(IndicatorModel(
            id="URL_PUNYCODE",
            rule_name="Punycode Homoglyph Deception",
            category="Domain Spoofing",
            severity="HIGH",
            description="URL uses internationalized Punycode to visually mimic familiar letters.",
            evidence=host,
            points=40
        ))
        score += 40

    # 3. Disposable suspicious TLD
    if tld in SUSPICIOUS_TLDS:
        indicators.append(IndicatorModel(
            id="URL_SUSPICIOUS_TLD",
            rule_name="High-Risk Disposable TLD",
            category="Domain Reputation",
            severity="HIGH",
            description=f"Domain uses '.{tld}', an extension frequently tied to throwaway scam infrastructure.",
            evidence=f".{tld}",
            points=35
        ))
        score += 35

    # 4. Brand in subdomain
    subdomain_str = ".".join(subdomains)
    for brand in POPULAR_BRANDS:
        if brand in subdomain_str and not registered_domain.startswith(brand):
            indicators.append(IndicatorModel(
                id="URL_BRAND_SPOOF_SUBDOMAIN",
                rule_name="Subdomain Brand Spoofing",
                category="Brand Impersonation",
                severity="HIGH",
                description=f"Brand '{brand}' appears in subdomains ({subdomain_str}), but destination registered domain is '{registered_domain}'.",
                evidence=f"{subdomain_str} on {registered_domain}",
                points=45
            ))
            score += 45
            break

    # 5. Excessive subdomains
    if len(subdomains) >= 3:
        indicators.append(IndicatorModel(
            id="URL_EXCESSIVE_SUBDOMAINS",
            rule_name="Excessive Subdomain Levels",
            category="Obfuscation & Evasion",
            severity="MEDIUM",
            description=f"Host contains {len(subdomains)} subdomain layers to disguise the true destination domain.",
            evidence=host,
            points=25
        ))
        score += 25

    # 6. Hyphen stuffing
    if host.count("-") >= 3:
        indicators.append(IndicatorModel(
            id="URL_EXCESSIVE_HYPHENS",
            rule_name="Domain Hyphen Stuffing",
            category="Suspicious URL Structure",
            severity="MEDIUM",
            description="High count of hyphens often indicates keyword stuffing (e.g. 'verify-account-security').",
            evidence=host,
            points=20
        ))
        score += 20

    # 7. URL shortener
    if host in SHORTENERS:
        indicators.append(IndicatorModel(
            id="URL_SHORTENER_MASK",
            rule_name="URL Shortener Masking",
            category="Obfuscation & Evasion",
            severity="MEDIUM",
            description="URL is shortened, hiding the true destination link.",
            evidence=host,
            points=20
        ))
        score += 20

    # 8. Open redirect
    for param, vals in query_params.items():
        if param.lower() in REDIRECT_PARAMS:
            for v in vals:
                if v.startswith("http://") or v.startswith("https://") or "//" in v:
                    indicators.append(IndicatorModel(
                        id="URL_OPEN_REDIRECT",
                        rule_name="Open Redirect Parameter",
                        category="Exploitation Technique",
                        severity="HIGH",
                        description=f"Parameter '{param}' contains external redirect target '{v}'.",
                        evidence=f"{param}={v}",
                        points=35
                    ))
                    score += 35

    final_score = min(max(score, 0), 100)
    risk_level = "HIGH" if final_score >= 60 else "MEDIUM" if final_score >= 30 else "LOW"

    title = "High Risk Deceptive URL" if risk_level == "HIGH" else "Suspicious URL Warning" if risk_level == "MEDIUM" else "Low Risk URL Structure"

    explanation = (
        "CRITICAL RISK: Multiple high-confidence deceptive markers detected in URL structure. DO NOT open or enter credentials."
        if risk_level == "HIGH"
        else "ELEVATED RISK: Suspicious structural attributes detected. Exercise caution and verify independently."
        if risk_level == "MEDIUM"
        else "LOW RISK: No prominent phishing or spoofing patterns were found. Verify TLS certificate on visiting."
    )

    safety_actions = [
        "DO NOT click or open this link.",
        "Never submit passwords, OTP tokens, or payment card numbers.",
        "Check domain spelling carefully in an independent browser tab."
    ] if final_score >= 50 else [
        "Verify destination matches official company web portal.",
        "Ensure HTTPS padlock is present and certificate is valid."
    ]

    return AnalyzeResponse(
        risk_score=final_score,
        risk_level=risk_level,
        title=title,
        explanation=explanation,
        indicators=indicators,
        safety_actions=safety_actions,
        engine="FastAPI Rule Engine"
    )

def analyze_text_heuristics(text: str) -> AnalyzeResponse:
    trimmed = text.strip()
    indicators: List[IndicatorModel] = []
    score = 0

    for rule in TEXT_RULES:
        for pat in rule["patterns"]:
            match = re.search(pat, trimmed)
            if match:
                indicators.append(IndicatorModel(
                    id=rule["id"],
                    rule_name=rule["name"],
                    category=rule["category"],
                    severity=rule["severity"],
                    description=rule["description"],
                    evidence=match.group(0),
                    points=rule["points"]
                ))
                score += rule["points"]
                break

    # Embedded URL check
    url_matches = re.findall(r"\b(?:https?://|www\.)[^\s]+\b", trimmed)
    for u in url_matches[:2]:
        url_resp = analyze_url_heuristics(u)
        for u_ind in url_resp.indicators:
            indicators.append(IndicatorModel(
                id=f"LINK_{u_ind.id}",
                rule_name=f"Embedded Link: {u_ind.rule_name}",
                category=f"Link ({u_ind.category})",
                severity=u_ind.severity,
                description=u_ind.description,
                evidence=f"{u_ind.evidence} (in {u})",
                points=u_ind.points
            ))
            score += int(u_ind.points * 0.9)

    final_score = min(max(score, 0), 100)
    risk_level = "HIGH" if final_score >= 60 else "MEDIUM" if final_score >= 30 else "LOW"

    title = "High Phishing Threat Detected" if risk_level == "HIGH" else "Suspicious Scam Warning" if risk_level == "MEDIUM" else "Low Phishing Indicators"

    explanation = (
        f"CRITICAL THREAT: {len(indicators)} danger markers found, including urgent deadlines, credential harvesting, or deceptive links."
        if risk_level == "HIGH"
        else f"CAUTION: {len(indicators)} suspicious signs detected common to smishing blasts."
        if risk_level == "MEDIUM"
        else "SAFE PROFILE: No major social engineering triggers or malicious links detected."
    )

    safety_actions = [
        "DO NOT click any links inside the message.",
        "Never reply with 2FA / OTP one-time passcodes.",
        "Report SMS to 7726 (SPAM) to inform carrier networks.",
        "Call the institution directly via the official phone number on your card or website."
    ] if final_score >= 50 else [
        "Verify sender identity through verified contact channels.",
        "Do not send unexpected wire transfers or gift cards."
    ]

    return AnalyzeResponse(
        risk_score=final_score,
        risk_level=risk_level,
        title=title,
        explanation=explanation,
        indicators=indicators,
        safety_actions=safety_actions,
        engine="FastAPI Rule Engine"
    )
