# PhishLens – AI Phishing & Scam Detector 🛡️

PhishLens is a cybersecurity defense application built with **Jetpack Compose (Kotlin)** on Android, a **Web Edition (HTML5/CSS3/ES6)**, and a companion **Python FastAPI** backend with **SQLite**. It empowers everyday users and cybersecurity analysts to detect and deconstruct suspicious SMS messages, emails, phishing URLs, and screenshots—**both online and completely offline (air-gapped)**.

---

## 🌟 Available Form Factors
1. **Native Android App (APK)**: Running on Android devices and the AI Studio streaming emulator with Jetpack Compose, on-device Room SQLite database, and ML Kit OCR.
2. **Web Edition (Website)**: Standalone web application located in `/web` and served directly through FastAPI at `http://localhost:8000/`. Runs on all desktop and mobile web browsers with zero installation.

---

## 🌟 Core Features

1. **SMS & Email Threat Analysis**: Detects urgency triggers, deadline coercion, credential harvesting, auto-renewal invoice scams, and fake parcel delivery pretexts.
2. **Safe URL Deconstruction**: Dissects URLs into protocol, apex domain, subdomains, and TLD without sending outbound HTTP requests to the target. Identifies raw IP hosts, Punycode homoglyphs, and high-risk disposable TLDs (`.tk`, `.top`, `.xyz`).
3. **Screenshot OCR Scanning**: Integrates zero-permission Android Photo Picker and on-device text recognition to inspect messages and receipts directly from screenshots.
4. **Transparent, Explainable Risk Scoring**: Calculates a normalized 0–100 risk score classified as:
   - 🟢 **LOW RISK (0–29)**
   - 🟡 **MEDIUM RISK (30–59)**
   - 🔴 **HIGH RISK (60–100)**
   Every score is accompanied by granular evidence, points breakdown, and actionable safety playbooks.
5. **Persistent Local History**: Stores analysis records locally in SQLite via Android Room Database with search, risk filters, detailed replay, and swipe/batch deletion.
6. **Air-Gapped Offline Protection**: Runs 100% locally on Android without requiring an internet connection or exposing private user communications to external cloud servers.
7. **Synthetic Hackathon Demo Scenarios**: 1-tap test cases covering bank smishing, USPS delivery scams, Geek Squad invoice fraud, and deceptive subdomain spoofing.
8. **Cybersecurity Design**: Modern cyber dark navy (`#090D1A`) with electric-blue neon accents, custom adaptive shield icon, and animated risk meter gauges.

---

## 🏗️ Architecture & Tech Stack

```
PhishLens Architecture
├── Android Mobile Client (Native Kotlin)
│   ├── UI: Jetpack Compose (Material 3 Cyber Theme)
│   ├── State: MVVM (ViewModel + MutableStateFlow)
│   ├── Storage: Room Database (SQLite on-device)
│   ├── Engine: PhishingRuleEngine & UrlSafetyAnalyzer (Kotlin)
│   ├── OCR: Android ML Kit Vision Text Recognition
│   └── Networking: Retrofit 2 + Moshi + OkHttp
└── Backend Service (Python FastAPI)
    ├── Endpoints: /health, /api/v1/analyze/text, /api/v1/analyze/url, /api/v1/history
    ├── Storage: SQLAlchemy + SQLite (phishlens_backend.db)
    └── Testing: Pytest + FastAPI TestClient
```

---

## 🚀 Beginner-Friendly Setup Instructions

### 1. Running the Android Application

#### In AI Studio / Emulator:
The application builds automatically. Run the compile step:
```bash
gradle assembleDebug
```
The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

#### In Android Studio:
1. Open the project folder in **Android Studio Ladybug or newer**.
2. Wait for Gradle sync to complete.
3. Select an Android Emulator (API 26+) or connect a physical Android device.
4. Click **Run** (`Shift + F10`).

---

### 2. Running the Python FastAPI Backend (Optional Hybrid Mode)

```bash
# 1. Navigate to backend directory
cd backend

# 2. Create and activate a virtual environment
python3 -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# 3. Install dependencies
pip install -r requirements.txt

# 4. Start the FastAPI development server
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

The API will now be live:
- Swagger Interactive Docs: [http://localhost:8000/docs](http://localhost:8000/docs)
- Health Check: [http://localhost:8000/health](http://localhost:8000/health)

#### Running Backend Tests:
```bash
pytest test_api.py -v
```

---

### 3. Running the Web Edition (Website)

You can run the PhishLens website in two simple ways:

#### Option A: Served via FastAPI (Recommended)
Starting the FastAPI server automatically serves the website at the root URL:
```bash
cd backend
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```
Open **[http://localhost:8000](http://localhost:8000)** in your browser!

#### Option B: Standalone Static Server (Node.js / Python / Vercel)
The web application is also completely self-contained in `/web` with client-side offline heuristics:
```bash
# Using Python built-in HTTP server:
cd web
python3 -m http.server 3000

# Or using Node.js / npx serve:
npx serve web -l 3000
```
Open **[http://localhost:3000](http://localhost:3000)** in any browser. It works 100% offline using the in-browser JavaScript detection engine!

---

## 🔒 Security Principles
- **No Automatic Link Opening**: Suspicious URLs are never opened in a browser. Only safe domain strings can be copied to the clipboard.
- **No False Absolute Safety**: PhishLens never claims a message is "100% safe," teaching users defense-in-depth and the Zero-Trust principle.
- **Privacy by Default**: Messages can be analyzed offline without transmitting any data over the internet.
