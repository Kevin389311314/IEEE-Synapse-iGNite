# 🏆 PhishLens Hackathon Demo Script (3-Minute Presentation)

**Presenter Roles**: Cybersecurity Engineer / Mobile Lead  
**Audience**: Hackathon Judges & Technical Reviewers  

---

## ⏱️ Minute 0:00 – The Hook & The Problem
> *"Judges, over 3.4 billion phishing emails and SMS attacks are blasted every single day. Attackers don't hack systems anymore—they hack human psychology with urgency, deadline pressure, and deceptive mobile links. Current tools either require forwarding private emails to a third-party cloud, or they just show a vague binary 'safe/unsafe' warning with zero explanation.*
>
> *We built **PhishLens**—an explainable, transparent AI & rule-based phishing detector that operates seamlessly even in 100% air-gapped offline environments."*

---

## ⏱️ Minute 0:45 – Live Demo 1: High-Risk Bank Smishing (1-Tap)
1. Open **PhishLens** on the Android device/emulator.
2. Tap the quick demo chip: **"🚨 Wells Fargo Bank Alert"**.
3. Point out the input text loaded:
   `"URGENT: Wells Fargo security alert. Your debit card has been locked due to unauthorized access..."`
4. Tap **"ANALYZE FOR PHISHING & SCAMS"**.
5. Show the animated gauge and results:
   - **Score: 95/100 (HIGH RISK)**
   - **Indicators Found**:
     - *Urgent Action & Artificial Deadline (+30 pts)*
     - *Threat of Account Suspension (+35 pts)*
     - *Credential & Verification Request (+40 pts)*
     - *Embedded Link: High-Risk Disposable TLD (.xyz)*
   - **Explainability**: Highlight that each card displays the exact regex match evidence (`"immediately within 24 hours"`).
   - **Actionable Safety Playbook**: Shows real security steps (e.g., *"Report SMS to 7726"*, *"Call verified bank number on physical card"*).

---

## ⏱️ Minute 1:30 – Live Demo 2: Safe URL Deconstruction (Zero-Click Defense)
1. Switch to the **URL Inspect** tab.
2. Tap **"🚨 Apple ID Deceptive URL"**:
   `http://login.appleid.apple.com.secure-portal-verify.top/auth/signin`
3. Tap **"ANALYZE RISK NOW"**.
4. Show the **URL Structural Deconstruction Card**:
   - Registered apex domain highlighted: `secure-portal-verify.top` (NOT `apple.com`).
   - Flags: **Brand in Subdomain**, **Unencrypted HTTP Protocol**, **Disposable .top TLD**.
   - Explain: *"Notice we never send a web request to the malicious attacker server. We deconstruct the URL mathematically in memory, with a safe 'Copy Domain Only' feature to avoid drive-by downloads."*

---

## ⏱️ Minute 2:15 – Live Demo 3: Local SQLite History & Offline Privacy
1. Switch to the **History** tab.
2. Show that all scans are persisted locally in SQLite with Room.
3. Test the search filter and risk chips (filter by *High* or *Medium*).
4. Tap any record to open the complete retrospective report.
5. Demonstrate single-record deletion and batch clear.
6. Switch to **Settings** and show the **Offline Safe Engine** toggle:
   - *"If a user is reviewing confidential corporate emails or personal banking SMS, zero network bytes leave their phone. It is truly private by design."*

---

## ⏱️ Minute 2:45 – The Wrap & Engineering Polish
> *"In summary, PhishLens delivers:
> 1. Native Jetpack Compose cybersecurity interface.
> 2. On-device Room persistence and ML Kit OCR.
> 3. Dual-engine architecture: local offline Kotlin heuristics + Python FastAPI backend with SQLite.
> 4. Zero-trust explainability that educates users instead of leaving them in the dark.
>
> Thank you, judges! We're ready for your questions."*
