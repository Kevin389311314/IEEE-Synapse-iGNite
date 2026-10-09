package com.example.domain.engine

import com.example.data.model.AnalysisType
import com.example.data.model.RiskLevel

data class DemoSample(
    val id: String,
    val title: String,
    val type: AnalysisType,
    val expectedRisk: RiskLevel,
    val content: String,
    val description: String
)

object DemoDataProvider {

    val SAMPLES = listOf(
        DemoSample(
            id = "demo_bank_urgent",
            title = "Wells Fargo Bank Alert",
            type = AnalysisType.TEXT,
            expectedRisk = RiskLevel.HIGH,
            content = "URGENT: Wells Fargo security alert. Your debit card has been locked due to unauthorized access. Confirm your login and verify credentials immediately within 24 hours at http://wellsfargo-verify.security-auth.xyz/login to prevent account termination.",
            description = "Simulates urgent smishing with brand spoofing, deadline panic, and credential harvest."
        ),
        DemoSample(
            id = "demo_usps_delivery",
            title = "USPS Package Delivery Smish",
            type = AnalysisType.TEXT,
            expectedRisk = RiskLevel.HIGH,
            content = "USPS: Your package #US984210 cannot be delivered due to incomplete address. Update address and pay $1.50 redelivery fee within 12h: http://192.168.1.100/usps-track",
            description = "Classic logistics smishing lure leveraging raw IP link and bogus fee."
        ),
        DemoSample(
            id = "demo_geeksquad_refund",
            title = "Geek Squad Fake Invoice",
            type = AnalysisType.TEXT,
            expectedRisk = RiskLevel.HIGH,
            content = "Geek Squad Total Protection: Your subscription auto-renewed for $499.00 today. If you did not authorize this charge, call our billing support immediately at +1-800-555-0199 for an instant refund.",
            description = "Tech support refund fraud using intimidation and fake call-center numbers."
        ),
        DemoSample(
            id = "demo_url_subdomain_spoof",
            title = "Apple ID Deceptive URL",
            type = AnalysisType.URL,
            expectedRisk = RiskLevel.HIGH,
            content = "http://login.appleid.apple.com.secure-portal-verify.top/auth/signin",
            description = "Deep subdomain brand nesting aiming to fool mobile browser address bars."
        ),
        DemoSample(
            id = "demo_url_ip_banking",
            title = "Raw IP Host URL",
            type = AnalysisType.URL,
            expectedRisk = RiskLevel.HIGH,
            content = "http://45.33.32.156:8080/secure/chase-banking/login",
            description = "Unencrypted raw IP address with non-standard port impersonating Chase banking."
        ),
        DemoSample(
            id = "demo_promo_survey",
            title = "Urgent Reward Survey",
            type = AnalysisType.TEXT,
            expectedRisk = RiskLevel.MEDIUM,
            content = "Congratulations! Final notice: You have been selected for a $100 reward voucher. Action required: complete the 2-minute survey at http://claim-voucher-now.click before midnight.",
            description = "Medium risk survey scam with time pressure and disposable .click domain."
        ),
        DemoSample(
            id = "demo_safe_appointment",
            title = "Safe Clinic Appointment",
            type = AnalysisType.TEXT,
            expectedRisk = RiskLevel.LOW,
            content = "Your appointment with Dr. Evans is confirmed for Thursday, Oct 15 at 3:00 PM at West Medical Suite 400. Reply C to confirm or call 555-0142 to reschedule.",
            description = "Normal transactional SMS with no credential harvesting or coercive pressure."
        ),
        DemoSample(
            id = "demo_safe_github_url",
            title = "Legitimate GitHub URL",
            type = AnalysisType.URL,
            expectedRisk = RiskLevel.LOW,
            content = "https://github.com/security/advisories",
            description = "Authentic HTTPS repository URL on an established high-reputation domain."
        )
    )
}
