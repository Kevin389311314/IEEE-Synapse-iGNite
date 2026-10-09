# LinkGuard MVP

LinkGuard is an MVP security tool designed to protect users at the moment of navigation. It consists of a Chromium Manifest V3 browser extension and a Node.js Fastify backend API that analyzes links upon click and returns a deterministic safety recommendation before allowing navigation.

---

## Architecture Overview

```
[ Web Page (User Clicks Link) ]
             |
             v
 [ Content Script (content.ts) ]
   - Intercepts primary click
   - Extracts destination URL & anchor text
   - Pauses navigation & displays "Checking link..." modal
             |
             v (chrome.runtime.sendMessage)
[ Background Worker (background.ts) ]
             |
             v (HTTP POST /api/check-link)
[ Fastify Backend API (server.ts) ]
   ├── URL Feature Extractor (WHATWG URL + psl)
   │     - Parses hostname, domain, subdomain, path
   │     - Redacts token/credential parameters
   ├── Reputation Provider Adapter (Google Safe Browsing v4)
   │     - Queries known threat feeds
   ├── LLM Provider Adapter (OpenAI gpt-4o-mini)
   │     - Analyzes text-destination mismatch & deception signals
   │     - Returns structured JSON evidence
   └── Deterministic Policy Engine (policy.ts)
         - proceed: Checks completed, no known threat or warning signs
         - warn: Suspicious indicators or deceptive mismatch found
         - block: Known threat match or critical deception detected
         - hold: Checks failed, timed out, or providers unconfigured
             |
             v (JSON response)
[ Content Script Modal Overlay ]
   - 'proceed' -> Automatically completes navigation
   - 'warn' / 'block' / 'hold' -> Halts navigation, renders dialog with findings
```

---

## Directory Structure

```
Synapse/
├── package.json                 # Monorepo workspaces config
├── packages/
│   ├── shared/                  # Shared Zod schemas and TypeScript types
│   │   ├── package.json
│   │   ├── tsconfig.json
│   │   └── src/
│   │       └── index.ts         # Request/Response schemas & types
│   │
│   ├── api/                     # Node.js Fastify Backend Service
│   │   ├── package.json
│   │   ├── tsconfig.json
│   │   ├── .env.example         # Template for environment variables
│   │   ├── test-policy.ts       # Self-test suite for policy & extraction
│   │   └── src/
│   │       ├── server.ts        # Fastify server & route handlers
│   │       ├── features.ts      # WHATWG URL parser + PSL + Redaction
│   │       ├── policy.ts        # Deterministic decision policy engine
│   │       └── adapters/
│   │           ├── reputation.ts # Google Safe Browsing v4 adapter
│   │           └── llm.ts        # OpenAI structured evidence adapter
│   │
│   └── extension/               # Chromium Manifest V3 TypeScript Extension
│       ├── package.json
│       ├── tsconfig.json
│       ├── build.mjs            # esbuild bundler script
│       ├── test-page.html       # Sample test page with diverse links
│       ├── public/
│       │   └── manifest.json    # Chrome Extension Manifest V3
│       └── src/
│           ├── background.ts    # Background service worker (API bridge)
│           ├── content.ts       # Click interception & navigation flow
│           └── modal.ts         # Shadow DOM modal overlay UI
└── README.md
```

---

## Decision Policy

The backend applies a strict, deterministic rule set:

1. **`proceed`**: All configured checks completed with **no known threat match** or warning indicators.
   - *Crucial note*: A proceed decision indicates that no known threat was identified; it is **never** a guarantee of safety.
2. **`warn`**: Suspicious signals detected (e.g., mismatch between anchor text and target domain, raw IP address, non-standard port). Navigation is paused and user review is required.
3. **`block`**: Active malicious threat match from the reputation provider or critical phishing deception pattern. Automated navigation is prohibited.
4. **`hold`**: Service unconfigured, offline, timed out, or token redaction prevented a meaningful check. Navigation does not proceed automatically. LinkGuard **never** simulates a successful check when providers are missing.

---

## Privacy & Redaction

- **Minimal Data**: LinkGuard sends only the clicked link URL and its displayed anchor text. It **never** inspects browser history, cookies, or full webpage DOM.
- **Token Redaction**: Sensitive query parameters (such as `token`, `auth`, `session`, `key`, `password`, `jwt`) and fragment hashes are stripped before logging or querying external adapters.
- **Server Privacy**: Fastify request logs do not write full URLs; only parsed domain names and protocols are logged.

---

## Prerequisites

- Node.js LTS (v20+ or v24+)
- npm v10+
- Google Chrome or any Chromium-based browser (Brave, Edge, etc.)

---

## Setup & Running Locally

### 1. Install Dependencies & Build Packages

From the repository root:
```bash
npm install
npm run build
```

### 2. Configure Backend Environment

Copy `.env.example` in `packages/api` to `.env`:
```bash
cp packages/api/.env.example packages/api/.env
```

Edit `packages/api/.env`:
```env
PORT=3000
HOST=127.0.0.1
LOG_LEVEL=info

# Optional: Google Safe Browsing API Key
GOOGLE_SAFE_BROWSING_API_KEY=your_key_here

# Optional: OpenAI API Key
OPENAI_API_KEY=your_key_here
OPENAI_MODEL=gpt-4o-mini
```

> **Note on Provider Configuration**:
> If either API key is left blank, LinkGuard honestly marks that provider as unconfigured and returns a `hold` decision. It **never** pretends an unconfigured provider evaluated the link as safe.

### 3. Start the Backend API

```bash
cd packages/api
npm run dev
# Or from root:
# npm run dev --workspace=@linkguard/api
```
The server will start at `http://127.0.0.1:3000`. You can verify health at `http://127.0.0.1:3000/health`.

### 4. Build and Load the Chromium Extension

Build the extension:
```bash
npm run build --workspace=@linkguard/extension
```
This generates the packaged extension in `packages/extension/dist/`.

To load in Chrome/Chromium:
1. Open Chrome and navigate to `chrome://extensions/`.
2. Toggle on **Developer mode** in the top right.
3. Click **Load unpacked**.
4. Select the folder: `packages/extension/dist`.
5. LinkGuard is now active.

---

## Testing LinkGuard

You can test LinkGuard using the included test page:
1. Open `packages/extension/test-page.html` in your Chromium browser.
2. Click any link on the page:
   - **Benign Link**: Intercepted -> Checked -> Proceeds if clean.
   - **Deceptive Mismatch Link**: Intercepted -> Warn/Block modal displayed with findings.
   - **Token Link**: Token parameters redacted in logs and external lookups.
   - **Unconfigured/Offline Backend**: Clear `HOLD` dialog explaining incomplete verification.

### Running the Automated Test Suite

To run all automated tests across workspaces with local mocks (without requiring API keys or contacting external services):
```bash
npm test
```
Or run tests per workspace:
```bash
# Test API endpoints, URL features, and policy logic:
npm test --workspace=@linkguard/api

# Test Extension click interception and decision handling:
npm test --workspace=@linkguard/extension
```

You can also run the backend self-test script directly:
```bash
npx tsx packages/api/test-policy.ts
```

---

## Current Coverage & Limitations

- **Intercepted Scope**: This MVP intercepts **primary left-clicks on standard HTML anchor links (`<a>`)** on web pages.
- **Navigation Not Covered**:
  - Direct address bar inputs (omnibox)
  - Browser bookmarks
  - Clicks with modifier keys (`Ctrl+Click`, `Cmd+Click`, Middle-click) that open tabs directly in the background
  - JavaScript-driven navigations that don't originate from link clicks (e.g. `window.location = ...` triggered by timer or non-anchor click)
  - HTTP redirects (301/302) executed server-side after initial navigation

