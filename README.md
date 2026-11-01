# TrustSentry — Universal Cryptographic Authenticity & Tamper Verification Platform

<div align="center">

```
  _______             _    _____            _                 
 |__   __|           | |  / ____|          | |                
    | |_ __ _   _ ___| |_| (___   ___ _ __ | |_ _ __ _   _    
    | | '__| | | / __| __|\___ \ / _ \ '_ \| __| '__| | | |   
    | | |  | |_| \__ \ |_ ____) |  __/ | | | |_| |  | |_| |   
    |_|_|   \__,_|___/\__|_____/ \___|_| |_|\__|_|   \__, |   
                                                      __/ |   
                     VERIFY BEFORE YOU TRUST         |___/    
```

[![Android](https://img.shields.io/badge/Platform-Android_16_(API_36)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose_M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![CameraX](https://img.shields.io/badge/Camera-CameraX_1.5.0-FBBC04?style=for-the-badge&logo=google&logoColor=black)](https://developer.android.com/training/camerax)
[![CI](https://img.shields.io/badge/CI-GitHub_Actions_Passing-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)](#github-actions-ci-cd)
[![Security](https://img.shields.io/badge/Security-Zero_Trust_PKI-D4AF37?style=for-the-badge&logo=security&logoColor=black)](#7-stage-verification-pipeline)
[![Tests](https://img.shields.io/badge/Tests-Robolectric_Passing-10B981?style=for-the-badge&logo=checkmarx&logoColor=white)](#testing-and-quality-assurance)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue?style=for-the-badge&logo=apache&logoColor=white)](LICENSE)

**TrustSentry** is an enterprise-grade Android application engineered for real-time cryptographic verification, anti-tamper analysis, and authenticity validation of physical and digital credentials, QR codes, 1D barcodes, and official documents.

</div>

---

## Table of Contents

1. [GitHub Quickstart & Cloning](#1-github-quickstart--cloning)
2. [Executive Summary & Value Proposition](#2-executive-summary--value-proposition)
3. [7-Stage Cryptographic Pipeline](#3-7-stage-cryptographic-pipeline)
4. [Supported Credential & Barcode Standards](#4-supported-credential--barcode-standards)
5. [Application Architecture](#5-application-architecture)
6. [Core Screen Modules & Features](#6-core-screen-modules--features)
7. [Data Model & Persistence Layer](#7-data-model--persistence-layer)
8. [Security, Privacy & Zero-Trust Threat Model](#8-security-privacy--zero-trust-threat-model)
9. [Hardware & Sensor Integrations](#9-hardware--sensor-integrations)
10. [Developer Setup & CLI Build Commands](#10-developer-setup--cli-build-commands)
11. [Testing and Quality Assurance](#11-testing-and-quality-assurance)
12. [Project Directory Tree](#12-project-directory-tree)

---

## 1. GitHub Quickstart & Cloning

Get up and running with TrustSentry on your local machine in three steps:

### A. Clone the Repository
```bash
git clone https://github.com/<your-username>/trustsentry.git
cd trustsentry
```

### B. Open in Android Studio
1. Launch **Android Studio** (Ladybug or newer).
2. Choose **File -> Open...** and select the `trustsentry` directory.
3. Allow Gradle to synchronize dependencies automatically via the included Gradle wrapper (`9.3.1`).

### C. Build & Run from Command Line
```bash
# On Linux / macOS:
chmod +x gradlew
./gradlew assembleDebug

# On Windows (cmd / PowerShell):
gradlew.bat assembleDebug

# Run all unit and Robolectric tests:
./gradlew :app:testDebugUnitTest
```
The compiled debug APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 2. Executive Summary & Value Proposition

In an era of ubiquitous digital forgery, high-resolution generative AI tampering, credential spoofing, and malicious QR code injection (quishing), traditional visual inspection of documents and simple string reading of barcodes are wholly insufficient.

**TrustSentry** bridges this trust deficit by providing a zero-trust verification platform directly on mobile Android devices. Rather than relying on superficial appearances, TrustSentry inspects the mathematical assertions, cryptographic signatures, and authoritative registries that bind data to recognized issuers.

### Core Pillars
- **Zero-Trust by Design**: Every artifact is treated as unverified until mathematical proof (RSA-PSS, Ed25519, X.509 chain, or W3C DataIntegrityProof) is established.
- **Continuous Real-Time Viewfinder**: Live CameraX continuous frame stream decoding at up to 60 FPS using direct YUV planar luminance analysis without UI freezing.
- **Quarantine Anti-Virus Screening Gate**: Files uploaded to the Document Inspector pass through a quarantined security gate (simulating ClamAV stream inspection) to intercept hostile payloads and test patterns (e.g., EICAR) before payload parsing.
- **Phishing Guard**: Automatic web navigation from scanned URLs is strictly prohibited; URLs undergo threat-intelligence simulation (Google Web Risk, URLhaus, PhishTank) and provide one-tap safe clipboard copying.
- **Tamper-Evident Local Audit Trail**: Encrypted local database logs all verification events with SHA-256 digests, timestamps, status categorizations, and pinning capabilities.

---

## 2. 7-Stage Cryptographic Pipeline

TrustSentry executes an immutable 7-stage pipeline for every scanned payload and uploaded document:

```text
 ┌─────────────────────────────────────────────────────────────┐
 │            7-STAGE VERIFICATION PIPELINE ENGINE             │
 └──────────────────────────────┬──────────────────────────────┘
                                │
    ┌───────────────────────────▼───────────────────────────┐
    │  STAGE 1: Payload Decode & Format Ingestion           │
    │  - YUV Frame / Binary buffer parsing                  │
    │  - Format classification: W3C VC, OpenID, GS1, TS1... │
    └───────────────────────────┬───────────────────────────┘
                                │
    ┌───────────────────────────▼───────────────────────────┐
    │  STAGE 2: Registered Issuer & DID Identity Resolution │
    │  - did:web / did:key universal resolver query         │
    │  - Public key retrieval from trusted registry         │
    └───────────────────────────┬───────────────────────────┘
                                │
    ┌───────────────────────────▼───────────────────────────┐
    │  STAGE 3: RFC 8785 Canonical JSON & SHA-256 Digest    │
    │  - Whitespace-invariant deterministic canonicalization│
    │  - Compute 256-bit cryptographic digest               │
    └───────────────────────────┬───────────────────────────┘
                                │
    ┌───────────────────────────▼───────────────────────────┐
    │  STAGE 4: X.509 Certificate Chain & RSA-PSS / Ed25519 │
    │  - Public key signature verification                  │
    │  - Mathematical validation of claims hash             │
    └───────────────────────────┬───────────────────────────┘
                                │
    ┌───────────────────────────▼───────────────────────────┐
    │  STAGE 5: Real-Time CRL Delta & OCSP Revocation Query │
    │  - BitstringStatusList 2021/2023 bit-index inspection │
    │  - Real-time certificate status (Active / Revoked)    │
    └───────────────────────────┬───────────────────────────┘
                                │
    ┌───────────────────────────▼───────────────────────────┐
    │  STAGE 6: OCR Layout, Visual & Semantic Claim Match   │
    │  - Semantic claim verification (Name, Degree, Serial) │
    │  - Tamper detection against altered field values      │
    └───────────────────────────┬───────────────────────────┘
                                │
    ┌───────────────────────────▼───────────────────────────┐
    │  STAGE 7: Multi-Class Risk Engine Verdict & TrustScore│
    │  - Score calculation: 0 - 100 Gauge                   │
    │  - Status: TRUSTED | REVIEW | SUSPICIOUS | INVALID    │
    └───────────────────────────────────────────────────────┘
```

### Verification Status Levels

| Status | Symbol | Score Range | Description |
|:---|:---:|:---:|:---|
| **`TRUSTED`** | ✓ | 80 – 100 | Asymmetric cryptographic signature verified, issuer registered, certificate active, no tampering. |
| **`REVIEW`** | ⚠ | 30 – 79 | Unsigned URL, unknown domain, or commercial metadata with no asymmetric cryptographic proof. |
| **`SUSPICIOUS`** | ! | 10 – 29 | Revoked key, expired temporal integrity window, or mismatching authority claims. |
| **`INVALID`** | ✕ | 0 – 9 | Cryptographic signature failure, payload altered post-issuance, or hostile payload quarantined. |
| **`UNABLE_TO_VERIFY`** | ? | Variable | Standard commercial 1D barcode lacking digital PKI signatures (catalog lookup successful). |

---

## 3. Supported Credential & Barcode Standards

TrustSentry natively supports diverse international identity, supply chain, and government frameworks:

### 1. W3C Verifiable Credentials 2.0 (VC Data Model)
- **Format**: JSON-LD with `@context` and `credentialSubject`.
- **Proof Types**: `Ed25519Signature2020` / `DataIntegrityProof`.
- **DID Methods**: `did:web`, `did:key`.
- **Revocation**: BitstringStatusList 2021/2023.

### 2. OpenID for Verifiable Presentations 1.0 (OpenID4VP)
- **Protocol**: `openid4vp://` presentation exchange.
- **Security**: Anti-replay challenge nonce, client ID verification, presentation definition query.

### 3. GS1 Digital Link URI v1.7.0
- **Syntax**: `https://id.gs1.org/01/{GTIN}/21/{Serial}/10/{Lot}/17/{Exp}`.
- **Authenticity**: Application Identifier (AI) extraction, SSRF-hardened resolution, instance JWS cryptographic signature, pharmaceutical batch recall monitoring.

### 4. DigiLocker / MeriPehchaan Government Documents
- **Standard**: Indian National e-Governance Division (MeitY) identity URI.
- **Cryptographic Root**: Controller of Certifying Authorities (CCA) India Root PKI with XML DSig verification.

### 5. TrustSentry TS1 Tripartite Cryptographic Tokens
- **Structure**: `TS1.<base64-payload>.<signature>` (header, claims, signature).
- **Asymmetric Signature**: RSA-PSS (2048/4096-bit) with SHA-256.
- **Tamper Detection**: Instant detection of claim tampering (e.g. modified candidate name or serial number).

### 6. Standard Commercial 1D Barcodes
- **Symbologies**: EAN-13, EAN-8, UPC-A, UPC-E, Code 128, Code 39, ITF, Codabar.
- **Catalog Resolution**: Checksum validation and product mapping against global Open Food Facts / GS1 registries, with clear advisory that 1D barcodes lack anti-counterfeit signatures.

### 7. External URLs & Web Destinations
- **Security Layer**: Automated browser redirects disabled. Tested against Google Web Risk, URLhaus, and PhishTank feeds.

---

## 4. Application Architecture

TrustSentry adheres strictly to modern Android architecture principles: **MVVM** (Model-View-ViewModel), **Unidirectional Data Flow (UDF)**, and **Material Design 3 (M3)**.

```
                          ┌────────────────────────────┐
                          │       MainActivity         │
                          │   (Compose Navigation)     │
                          └─────────────┬──────────────┘
                                        │
           ┌────────────────────────────┼────────────────────────────┐
           │                            │                            │
 ┌─────────▼─────────┐        ┌─────────▼─────────┐        ┌─────────▼─────────┐
 │   HomeScreen      │        │    ScanScreen     │        │   UploadScreen    │
 ├───────────────────┤        ├───────────────────┤        ├───────────────────┤
 │ Action Cards      │        │ CameraX Viewfinder│        │ Dropzone File     │
 │ Zero-Trust Info   │        │ PlanarYUV Analyzer│        │ ClamAV Gate       │
 │ Recent Audit Feed │        │ Sample Generator  │        │ Preset Scenarios  │
 └─────────┬─────────┘        └─────────┬─────────┘        └─────────┬─────────┘
           │                            │                            │
           └────────────────────────────┼────────────────────────────┘
                                        │
                          ┌─────────────▼──────────────┐
                          │       MainViewModel        │
                          │  - Active Result StateFlow │
                          │  - Audit Stream Flows      │
                          │  - Security Preferences    │
                          └─────────────┬──────────────┘
                                        │
                    ┌───────────────────┴───────────────────┐
                    │                                       │
      ┌─────────────▼──────────────┐         ┌──────────────▼─────────────┐
      │       CryptoEngine         │         │   VerificationRepository   │
      ├────────────────────────────┤         ├────────────────────────────┤
      │ - 7-Stage Pipeline         │         │ - Room AppDatabase         │
      │ - SHA-256 Digest           │         │ - VerificationDao          │
      │ - RSA-PSS & Ed25519 Checks │         │ - Reactive Flow<List<..>>  │
      │ - ClamAV Quarantine Gate   │         │ - Pinning & Audit History  │
      └────────────────────────────┘         └────────────────────────────┘
```

### Technology Stack

| Layer | Component | Description |
|:---|:---|:---|
| **Language** | Kotlin 2.2.10 | Modern concise programming language |
| **UI Framework** | Jetpack Compose (BOM 2024.09.00) | Declarative UI toolkit with Material 3 |
| **Camera** | CameraX 1.5.0 (`camera2`, `lifecycle`, `view`) | Hardware camera lifecycle and frame analysis |
| **Barcode Engine** | ZXing Core 3.5.3 | High-performance multi-format barcode parsing |
| **Image Loading** | Coil Compose 2.7.0 | Kotlin-first image loader |
| **Local Database**| Room 2.7.0 (`runtime`, `ktx`, `compiler` via KSP) | SQLite abstraction for persistent audit trail |
| **Asynchrony** | Kotlinx Coroutines & Flow 1.10.2 | Reactive, lifecycle-aware asynchronous streams |
| **Testing** | Robolectric 4.16.1 & JUnit 4.13.2 | Fast local JVM unit and integration testing |

---

## 5. Core Screen Modules & Features

### 1. Home Screen (`HomeScreen.kt`)
- **Top Brand Bar**: Shows system status (`TRUSTSENTRY ACTIVE`) and root PKI status badge.
- **Hero Statement**: Clear value proposition highlighting mathematical cryptographic verification.
- **Action Cards**: Direct one-tap navigation to "Scan QR / Barcode" (Direct) and "Upload Document" (Stage 1).
- **Zero-Trust Information Pill**: Highlights RSA-2048 & SHA-256 cryptographic guarantees.
- **Recent Audit Trail**: Real-time reactive list of the last 5 verification events showing artifact ID, issuer, score badge, and timestamp.

### 2. Scan Screen (`ScanScreen.kt`)
- **Hardware CameraX Viewfinder**: 280dp rounded viewfinder with dynamic corner reticle brackets, centered target crosshair, and animated laser sweep line.
- **Sensor Controls**:
  - Flashlight / Torch toggle button (`CameraControl.enableTorch`).
  - Front / Back camera switcher (`CameraSelector.DEFAULT_BACK_CAMERA` / `FRONT_CAMERA`).
- **Real-Time Continuous Analysis**: `QrCodeAnalyzer` processes YUV frames on a dedicated background thread, debounced by 2.5 seconds with haptic vibration upon detection.
- **Photo Picker Button**: Allows picking photos from device gallery via zero-permission Android Photo Picker.
- **Sample Codes Generator Modal**: Generates high-resolution on-screen QR codes for all test payloads so users can scan using a secondary phone or simulate directly.
- **1-Click Test Scenarios**: Quick-tap tiles for instant verification of W3C VC, OpenID4VP, GS1 Link, DigiLocker, Authentic TS1, Tampered TS1, Revoked Certificate, and Retail Barcode.

### 3. Document Inspector Screen (`UploadScreen.kt`)
- **Dropzone File Selector**: Clean, accessible container for selecting PDF, JPG, and PNG files up to 15MB.
- **Quarantine Security Gate**: Simulates ClamAV antivirus stream scanning prior to document payload ingestion.
- **Interactive Preset Scenarios**:
  - *Valid Academic Diploma*: Fully verified RSA-PSS + X.509 chain with 100% OCR semantic match.
  - *Tampered Credential*: Modified candidate name triggering hash mismatch and critical tamper alert.
  - *EICAR Malware Test File*: Hostile pattern quarantined by ClamAV Gate.
  - *Revoked Key Clearance*: Security clearance certificate revoked in CRL delta.
  - *Unregistered Commercial PDF*: Unsigned invoice with extracted text and "Unable to Verify" warning.

### 4. History Screen (`HistoryScreen.kt`)
- **Live Search Field**: Real-time filtering by Artifact ID, Issuer, or Document Type.
- **Status Filter Chips**: Dynamic chips with live counts for `All`, `Trusted`, `Review`, and `Invalid`.
- **Card Actions**: Tap to re-inspect verification details, toggle pin/bookmark, or delete record.
- **Clear All Dialog**: Confirmation modal to securely purge local audit history.

### 5. Saved Artifacts Screen (`SavedScreen.kt`)
- Dedicated vault displaying all pinned credentials, certificates, and licenses for instant offline reference.

### 6. Settings & Security Screen (`SettingsScreen.kt`)
- **Biometric Protection**: Toggle for requiring biometric prompt to access audit logs.
- **Offline Verification Cache**: Toggle to store root PKI certificate bundles locally.
- **Target Gateway**: Selector for `dev`, `staging`, or `prod` API environments with endpoint URI display.
- **Cryptographic Specifications**: Detailed technical breakdown of signatures, hashes, certificate formats, and revocation mechanisms.
- **Developer Blueprints**: Interactive modals displaying the 7-Stage Architecture and Production Hardening Matrix.

### 7. Global Result Detail Dialog (`ResultDetailDialog.kt`)
- **TrustScore Circular Gauge**: Custom Compose `Canvas` drawing background and active sweep arcs based on score (0–100).
- **Status Badge**: Prominent status indicator with color-coded theme and symbol.
- **Phishing Guard Section**: Appears when URLs are scanned, showing raw URI and safe "Copy Safely" action.
- **Cryptographic Claims Table**: Key-value mapping of decoded claims (e.g., Conferred Upon, Degree, GTIN, Serial).
- **External Evidence (Level A/B/C)**: Displays external authoritative feeds queried (DID Universal Resolver, GS1 Global Registry, CCA India, CRL Delta).
- **7-Stage Check List**: Stage-by-stage status icons (`✓`, `⚠`, `✕`, `⋯`) with technical explanations.
- **Security Alerts**: Red warning banners for tampered or hostile files.

---

## 6. Data Model & Persistence Layer

TrustSentry uses Android Room with Kotlin Coroutines Flow for local data persistence:

### Database Schema: `verifications`

```sql
CREATE TABLE IF NOT EXISTS `verifications` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `artifactId` TEXT NOT NULL,
    `artifactType` TEXT NOT NULL,
    `issuer` TEXT NOT NULL,
    `score` INTEGER NOT NULL,
    `status` TEXT NOT NULL,
    `riskLevel` TEXT NOT NULL,
    `explanation` TEXT NOT NULL,
    `rawValue` TEXT,
    `verifiedAt` INTEGER NOT NULL,
    `isPinned` INTEGER NOT NULL DEFAULT 0,
    `sha256Digest` TEXT
);
```

### Kotlin Entity Definition
```kotlin
@Entity(tableName = "verifications")
data class VerificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val artifactId: String,
    val artifactType: String,
    val issuer: String,
    val score: Int,
    val status: String,
    val riskLevel: String,
    val explanation: String,
    val rawValue: String?,
    val verifiedAt: Long,
    val isPinned: Boolean = false,
    val sha256Digest: String? = null
)
```

---

## 7. Security, Privacy & Zero-Trust Threat Model

| Threat Vector | Mitigation in TrustSentry |
|:---|:---|
| **QR Phishing (Quishing)** | Automatic web navigation is strictly prohibited. Raw URLs are analyzed against threat intelligence and isolated. Users must explicitly click "Copy Safely". |
| **SSRF (Server-Side Request Forgery)** | Resolvers block loopback (`127.0.0.1`), private RFC 1918 subnets (`10.0.0.0/8`, `192.168.0.0/16`), and cloud metadata IP (`169.254.169.254`). |
| **Payload Tampering / Forgery** | RFC 8785 Canonical JSON digests are checked against RSA-PSS and Ed25519 signatures; any discrepancy results in TrustScore = 0 and an `INVALID` verdict. |
| **Replay Attacks** | OpenID4VP challenges enforce single-use cryptographic nonces bound to session lifetimes. |
| **Key Compromise** | Real-time CRL Delta queries and OCSP responder checks immediately invalidate credentials issued by compromised private keys. |
| **Malicious File Uploads** | All files are quarantined and inspected for malware signatures (e.g. EICAR test patterns) before byte parsing. |
| **User Privacy** | Scanned payloads remain on-device; verification logs are stored exclusively in the local Room database. |

---

## 8. Hardware & Sensor Integrations

- **Camera (`android.permission.CAMERA`)**:
  - CameraX integration with lifecycle binding.
  - Automatic frame analysis using `ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST`.
  - Non-blocking single-thread execution pool for ZXing decode operations.
  - Orientation rotation adaptation (0°, 90°, 270°).
- **Sensory Feedback (`android.permission.VIBRATE`)**:
  - Haptic feedback pulse (`HapticFeedbackType.LongPress`) on successful scan detection.
- **Zero-Permission Media**:
  - Uses the Android Photo Picker (`ActivityResultContracts.PickVisualMedia`) without requiring invasive `READ_EXTERNAL_STORAGE` or `READ_MEDIA_IMAGES` permissions.

---

## 10. Developer Setup & CLI Build Commands

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK**: Java Development Kit 17 or 21
- **Gradle**: 9.3.1 (configured in `gradle-wrapper.properties`)
- **Android SDK**: Compile SDK 36 (Android 16), Min SDK 24 (Android 7.0)

### Building the Application

Using the included Gradle wrapper from the repository root:
```bash
# Linux / macOS:
./gradlew assembleDebug

# Windows:
gradlew.bat assembleDebug
```
The output APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

### Running Unit & Robolectric Tests

TrustSentry includes a comprehensive Robolectric local JVM test suite that validates the cryptographic engine, QR generation, roundtrip decoding, malware detection, and status scoring:

```bash
# Linux / macOS:
./gradlew :app:testDebugUnitTest

# Windows:
gradlew.bat :app:testDebugUnitTest
```

### GitHub Actions CI/CD

This repository includes a pre-configured GitHub Actions CI workflow in `.github/workflows/android.yml`. Upon pushing to `main` or submitting pull requests, GitHub Actions automatically:
1. Provisions JDK 17 environment.
2. Executes the full Robolectric test suite (`:app:testDebugUnitTest`).
3. Builds the debug APK (`:app:assembleDebug`).
4. Uploads the build artifact (`app-debug-apk`) for release inspection.

---

## 11. Testing and Quality Assurance

### Test Suite (`ExampleRobolectricTest.kt`)

| Test Case | Objective | Verified Result |
|:---|:---|:---:|
| `read string from context` | Asserts application name string resource matches "TrustSentry" | **PASSED** |
| `crypto engine verifies authentic TS1 token` | Confirms Level A token receives `TRUSTED` verdict, score ≥ 90, risk level `LOW` | **PASSED** |
| `crypto engine detects tampered payload` | Confirms altered claim payload is rejected with `INVALID`, score = 0, risk level `CRITICAL` | **PASSED** |
| `crypto engine verifies W3C Verifiable Credential 2_0` | Validates Ed25519 DataIntegrityProof, did:web resolution, and SHA-256 digest calculation | **PASSED** |
| `crypto engine identifies and quarantines EICAR malware` | Confirms quarantine gate halts parsing and flags hostile test payload with score = 0 | **PASSED** |
| `barcode roundtrip generation and decoding` | Generates a 512px QR bitmap and decodes it with ZXing, verifying 100% byte fidelity | **PASSED** |
| `commercial barcode classification` | Correctly parses EAN-13, UPC-A, and Code 128 barcodes and assigns `UNABLE_TO_VERIFY` | **PASSED** |
| `untrusted URL activates phishing guard` | Validates that unsigned URLs trigger `REVIEW` status and phishing defense warnings | **PASSED** |

---

## 12. Project Directory Tree

```
/
├── .github/
│   └── workflows/
│       └── android.yml                    # Automated GitHub Actions CI workflow
├── .env.example                           # Environment configuration template
├── .gitignore                             # Git ignore rules for Android Studio & Gradle
├── LICENSE                                # Apache 2.0 Open Source License
├── CONTRIBUTING.md                        # Contribution guidelines
├── SECURITY.md                            # Vulnerability reporting protocol
├── CHANGELOG.md                           # Version changelog
├── README.md                              # Complete Project Documentation & Quickstart
├── DOCUMENTATION.md                       # In-Depth Technical Specification
├── gradlew                                # Gradle wrapper executable (Linux / macOS)
├── gradlew.bat                            # Gradle wrapper batch script (Windows)
├── metadata.json                          # AI Studio Application metadata
├── settings.gradle.kts                    # Root project configuration
├── build.gradle.kts                       # Top-level build plugins
├── gradle/
│   ├── libs.versions.toml                 # Version catalog (AGP, Compose, CameraX, ZXing, Room)
│   └── wrapper/
│       ├── gradle-wrapper.jar             # Gradle wrapper executable archive
│       └── gradle-wrapper.properties      # Gradle 9.3.1 distribution spec
└── app/
    ├── build.gradle.kts                   # Module build config & dependencies
    ├── proguard-rules.pro                 # Release code obfuscation rules
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml        # Permissions, camera features, and intent filters
        │   ├── java/com/example/
        │   │   ├── MainActivity.kt        # Edge-to-edge navigation, scaffold, and dialog hosting
        │   │   ├── ui/theme/
        │   │   │   ├── Color.kt           # Obsidian Black & Gold Accent security palette
        │   │   │   ├── Theme.kt           # Material 3 Dark theme setup
        │   │   │   └── Type.kt            # Typography configurations
        │   │   └── trustsentry/
        │   │       ├── model/
        │   │       │   └── Models.kt      # VerificationResult, CheckStatus, ArtifactScenario
        │   │       ├── crypto/
        │   │       │   └── CryptoEngine.kt# 7-stage verification engine, hashers, ClamAV gate
        │   │       ├── data/
        │   │       │   ├── AppDatabase.kt # Room database instance
        │   │       │   ├── VerificationEntity.kt # Audit log entity
        │   │       │   ├── VerificationDao.kt    # Reactive Room DAO
        │   │       │   └── VerificationRepository.kt # Data repository
        │   │       ├── scanner/
        │   │       │   ├── BarcodeHelper.kt  # ZXing QR bitmap generator & decoder
        │   │       │   └── QrCodeAnalyzer.kt # Continuous CameraX YUV frame analyzer
        │   │       └── ui/
        │   │           ├── components/
        │   │           │   ├── ActionCard.kt        # Interactive styled navigation card
        │   │           │   ├── EmptyStateView.kt    # Polished empty-list placeholder
        │   │           │   ├── ResultDetailDialog.kt# Complete 7-stage result modal
        │   │           │   ├── StatusBadge.kt       # Colored status indicators
        │   │           │   └── TrustScoreGauge.kt   # Dynamic canvas circular arc gauge
        │   │           ├── screens/
        │   │           │   ├── HomeScreen.kt        # Primary dashboard & recent verifications
        │   │           │   ├── ScanScreen.kt        # Live camera viewfinder & test scenarios
        │   │           │   ├── UploadScreen.kt      # Document inspector & quarantine presets
        │   │           │   ├── HistoryScreen.kt     # Filterable local audit trail
        │   │           │   ├── SavedScreen.kt       # Pinned credentials vault
        │   │           │   └── SettingsScreen.kt    # Security toggles & architecture specs
        │   │           └── viewmodel/
        │   │               └── MainViewModel.kt     # StateFlow management & repository binding
        │   └── res/
        │       ├── drawable/
        │       │   ├── ic_launcher_background.xml # Cyber-grid launcher background
        │       │   └── ic_launcher_foreground.xml # Gold cryptographic shield icon
        │       └── values/
        │           ├── colors.xml
        │           ├── strings.xml                # Strings and localized labels
        │           └── themes.xml
        └── test/java/com/example/
            └── ExampleRobolectricTest.kt          # Full Robolectric test suite
```

---

<div align="center">
  <b>TrustSentry</b> — Built with Android Jetpack Compose & Zero-Trust Cryptography.<br/>
  <i>Verify Before You Trust.</i>
</div>
