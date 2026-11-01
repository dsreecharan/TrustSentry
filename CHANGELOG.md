# Changelog

All notable changes to **TrustSentry** are documented in this file.

## [1.0.0] - 2026-11-01

### Added
- **Continuous CameraX Viewfinder**: Real-time YUV frame analysis (`ImageAnalysis` with `PlanarYUVLuminanceSource`) and ZXing multi-format decoding.
- **7-Stage Cryptographic Pipeline**:
  - RFC 8785 canonical JSON formatting and SHA-256 digest computation.
  - Ed25519 & RSA-PSS asymmetric signature checks with X.509 certificate chains.
  - W3C Verifiable Credentials 2.0 with `did:web` and `did:key` resolution.
  - OpenID for Verifiable Presentations 1.0 (OpenID4VP) challenge & anti-replay nonce validation.
  - GS1 Digital Link v1.7.0 serialized attributes (GTIN, lot, serial, expiration) with SSRF-hardened resolution.
  - Indian DigiLocker / MeriPehchaan CCA Root PKI XML DSig validation.
  - TrustSentry TS1 tripartite token engine with instant tamper detection.
- **Document Inspector**: Multi-stage file screening with simulated ClamAV antivirus quarantine gate for PDF and image credentials.
- **Phishing Guard**: Automatic browser navigation prevention for scanned URLs with safe clipboard copy and threat reputation checks.
- **Local Persistent Audit Trail**: Room Database storage for verification logs, live filtering, and credential pinning.
- **UI & Accessibility**: Modern Material 3 dark luxury palette (Obsidian & Gold), TrustScore gauge, edge-to-edge support, custom adaptive launcher icon.
- **Robolectric Test Suite**: 8 automated test cases verifying cryptographic integrity, QR generation and decoding, malware defense, and barcode classification.
- **GitHub Ready**: Gradle wrapper 9.3.1, GitHub Actions CI workflow, Apache 2.0 license, contributing guidelines, and security policy.
