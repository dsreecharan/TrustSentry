# TrustSentry Technical Documentation & System Specification

## Document Overview
- **System Name**: TrustSentry Android Platform
- **Release Version**: 1.0.0 (Phase 6 Production Release)
- **Target OS**: Android 16 (API Level 36), Minimum SDK: Android 7.0 (API Level 24)
- **Architecture Pattern**: MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF)
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Security Paradigm**: Zero-Trust Mathematical Proof Verification

---

## 1. System Objectives & Problem Statement

Modern mobile users and field auditors regularly encounter credentials in physical, paper, or digital formats:
- QR codes on university diplomas and degree certificates.
- Barcodes and Data Matrix codes on pharmaceutical packaging and clinical vials.
- Identity tokens from decentralized wallets (W3C VC / did:web).
- Government identity claims (Indian DigiLocker / MeriPehchaan).
- OpenID4VP presentation exchange challenges.

### Traditional Failure Modes
1. **Visual Superficiality**: Humans and traditional scanner apps merely check whether a code contains text or a URL, failing to inspect cryptographic signatures or issuer authority.
2. **Quishing & Redirection Attacks**: Ordinary camera scanners automatically open or prompt users to visit URLs, exposing devices to credential harvesting and phishing pages.
3. **Counterfeiting Commercial Barcodes**: 1D barcodes (EAN-13, UPC) simply encode numeric product IDs without asymmetric signatures, creating a false illusion of authenticity.
4. **Altered PDF Records**: Digital certificates can easily be edited using consumer software while retaining identical visual formatting.

### TrustSentry Solution
TrustSentry enforces mathematical verification via an immutable 7-stage engine. Every artifact is parsed, normalized via RFC 8785 canonicalization, checked against issuer public keys, queried against real-time Certificate Revocation Lists (CRL/OCSP), and evaluated by a multi-class risk model.

---

## 2. Technical Architecture & Component Interaction

```
[Camera Hardware] ──> [PreviewView]
       │
       ▼ (YUV_420_888 Frames)
[QrCodeAnalyzer] ──> [PlanarYUV Luminance] ──> [ZXing MultiFormatReader]
                                                        │
                                                        ▼ (Decoded String)
                                              [MainViewModel]
                                                        │
                        ┌───────────────────────────────┴───────────────────────────────┐
                        │                                                               │
                        ▼                                                               ▼
             [CryptoEngine.verifyPayload]                                [VerificationRepository]
                        │                                                               │
       ┌────────────────┴────────────────┐                                              ▼
       │ 1. Syntax Parsing               │                                       [AppDatabase]
       │ 2. DID / Issuer Resolution      │                                              │
       │ 3. RFC 8785 Canonical Hash      │                                              ▼
       │ 4. RSA-PSS / Ed25519 Signature  │                                    [verifications Table]
       │ 5. CRL Delta / OCSP Query       │
       │ 6. OCR Claim Comparison         │
       │ 7. Risk Score (0-100)           │
       └────────────────┬────────────────┘
                        │
                        ▼ (VerificationResult)
             [ResultDetailDialog (UI)]
```

---

## 3. Cryptographic Pipeline Specifications

### Stage 1: Payload Decode & Format Ingestion
- Ingests raw byte streams or camera frames.
- Identifies protocol headers:
  - `{"@context": ...}` -> W3C Verifiable Credential 2.0
  - `openid4vp://` -> OpenID for Verifiable Presentations
  - `https://id.gs1.org/...` -> GS1 Digital Link
  - `in.gov.meripehchaan...` -> DigiLocker / MeriPehchaan
  - `TS1.<payload>.<sig>` -> TrustSentry Tripartite Token
  - `http://` / `https://` -> External Web URL
  - Numeric 8-14 digits -> EAN / UPC commercial barcode

### Stage 2: Registered Issuer & DID Identity Resolution
- Resolves decentralized identifiers (`did:web`, `did:key`).
- Confirms whether the issuer's public key is registered in the trusted authority directory.

### Stage 3: RFC 8785 Canonical JSON & SHA-256 Digest
- Generates a deterministic, whitespace-independent byte serialization.
- Computes SHA-256 cryptographic digest to verify payload integrity.

### Stage 4: X.509 Certificate Chain & Asymmetric Signature
- Validates cryptographic digital signatures:
  - **RSA-PSS**: Probabilistic Signature Scheme with 2048 or 4096-bit keys and SHA-256 hashing.
  - **Ed25519**: Edwards-curve Digital Signature Algorithm for W3C credentials.
  - **XML DSig**: Enveloped digital signature for government documents.

### Stage 5: Real-Time CRL Delta & OCSP Revocation Query
- Queries BitstringStatusList 2021/2023 bit-index registries for Verifiable Credentials.
- Queries Certificate Revocation List (CRL) deltas and OCSP responder status for X.509 certificates to verify that signing keys have not been compromised or revoked.

### Stage 6: OCR Layout, Visual & Semantic Claim Match
- Compares declared payload claims with registered visual layout values.
- Flags discrepancies between holder identities (e.g. registered name "Elena Vance" vs. forged name "Marcus Wright").

### Stage 7: Multi-Class Risk Engine Verdict & TrustScore
- Scores the artifact on a normalized 0 to 100 scale:
  - `score >= 80`: **`TRUSTED`** (Low Risk)
  - `30 <= score < 80`: **`REVIEW`** (Medium Risk)
  - `10 <= score < 30`: **`SUSPICIOUS`** (High Risk)
  - `score < 10`: **`INVALID`** (Critical Risk)

---

## 4. UI / UX Design System

### Color Specification (`Color.kt`)
- `ObsidianBlack` (`#080808`): Deep ambient background.
- `DarkSurface` (`#0E1117`): Base surface color.
- `DarkSurfaceElevated` (`#161A22`): Elevated cards and dialog backgrounds.
- `DarkSurfaceBorder` (`#262C38`): Card boundary outlines.
- `DarkDivider` (`#1E222D`): Subtle divider lines.
- `GoldAccent` (`#D4AF37`): Primary brand luxury gold.
- `GoldAccentLight` (`#E5C158`): Secondary golden highlight.
- `CyanAccent` (`#00F0FF`): Secondary technical indicator.
- `StatusTrusted` (`#10B981`): Emerald green status.
- `StatusReview` (`#F59E0B`): Amber warning status.
- `StatusSuspicious` (`#F97316`): Orange alert status.
- `StatusInvalid` (`#EF4444`): Crimson error/tamper status.

### Custom UI Components
1. **`TrustScoreGauge`**: Custom `Canvas` arc visualizer with rounded caps, dynamic sweep angle, and centered score typography.
2. **`StatusBadge`**: Pill container with color-coded border, semi-transparent background, and bold status label.
3. **`ActionCard`**: Large 48dp+ touch target card with icon box, title, description, and status indicator tag.
4. **`ResultDetailDialog`**: Full-screen modal containing the TrustScore gauge, status badge, metadata table, cryptographic claims list, external evidence table, 7-stage checklist, and warnings.
5. **`EmptyStateView`**: Clean placeholder view with circular icon frame, title, and helpful explanatory text.

---

## 5. Security Protocols & Threat Mitigations

### 1. Phishing Guard & Safe URL Handling
- **Mitigation**: Scanned web URLs never trigger `Intent.ACTION_VIEW` or automatic browser launching.
- **Threat Feeds**: URL reputation is queried against simulated Google Web Risk, URLhaus, and PhishTank feeds.
- **Copy Protection**: Users can safely copy the sanitized URL string via a dedicated "Copy Safely" button without triggering navigation.

### 2. SSRF Protection
- **Mitigation**: Any network resolution within the GS1 or DID pipeline rejects private IP ranges (10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16), localhost (127.0.0.1, ::1), and cloud provider metadata services (169.254.169.254).

### 3. File Quarantine & Antivirus Gate
- **Mitigation**: Uploaded PDF and image files are screened in a memory-isolated buffer.
- **Hostile Pattern Check**: Detects EICAR test patterns and known malicious sequences, halting pipeline execution immediately and returning TrustScore = 0 (`INVALID`).

---

## 6. Testing & Quality Verification

### Robolectric Test Suite Coverage
All critical paths are verified through local JVM tests (`gradle :app:testDebugUnitTest`):
- `read string from context`: Validates Android resource configuration.
- `crypto engine verifies authentic TS1 token`: Validates authentic token parsing and high score assignment.
- `crypto engine detects tampered payload and fails with INVALID`: Validates tamper detection on altered claims.
- `crypto engine verifies W3C Verifiable Credential 2_0`: Validates JSON-LD VC claims and Ed25519 proof logic.
- `crypto engine identifies and quarantines EICAR malware string`: Validates anti-virus quarantine gate halt.
- `barcode roundtrip generation and decoding produces exact content`: Validates ZXing QR bitmap creation and decode integrity.
- `commercial barcode classification accurately identifies EAN13 and UPC`: Validates 1D barcode checksum and catalog assignment.
- `untrusted URL activates phishing guard and review status`: Validates automated redirect prevention and threat intel checks.

---

## 7. Versioning & Maintenance Guide

### Updating Root Certificates
To register new Root CA certificates or trusted issuer DIDs:
1. Navigate to `CryptoEngine.kt`.
2. Add the issuer identifier to the trusted registry map.
3. Bind the corresponding X.509 public key or Ed25519 verification method.

### Adding New Barcode Formats
To add additional barcode symbologies:
1. Update `QrCodeAnalyzer.kt` in the `POSSIBLE_FORMATS` list.
2. Update `BarcodeHelper.decodeBitmap()` with the new `BarcodeFormat`.
3. Add symbology classification logic in `CryptoEngine.verifyCommercialBarcode()`.
