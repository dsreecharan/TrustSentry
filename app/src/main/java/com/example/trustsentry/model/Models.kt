package com.example.trustsentry.model

enum class VerificationStatus(val label: String, val symbol: String) {
    TRUSTED("TRUSTED", "✓"),
    REVIEW("REVIEW", "⚠"),
    SUSPICIOUS("SUSPICIOUS", "!"),
    INVALID("INVALID", "✕"),
    UNABLE_TO_VERIFY("UNABLE TO VERIFY", "?")
}

enum class CheckStatus(val symbol: String) {
    PASSED("✓"),
    WARNING("⚠"),
    FAILED("✕"),
    PENDING("⋯")
}

data class VerificationCheck(
    val stage: Int,
    val title: String,
    val description: String,
    val status: CheckStatus,
    val technicalDetails: String? = null
)

data class ExternalEvidence(
    val provider: String,
    val type: String,
    val status: String,
    val evidenceClass: String,
    val authoritative: Boolean,
    val details: String? = null
)

data class VerificationResult(
    val artifactId: String,
    val artifactType: String,
    val issuer: String,
    val score: Int,
    val status: VerificationStatus,
    val riskLevel: String, // LOW, MEDIUM, HIGH, CRITICAL, UNKNOWN
    val checks: List<VerificationCheck>,
    val externalEvidence: List<ExternalEvidence> = emptyList(),
    val warnings: List<String> = emptyList(),
    val explanation: String,
    val verifiedAt: Long = System.currentTimeMillis(),
    val isOffline: Boolean = false,
    val rawValue: String? = null,
    val extractedClaims: Map<String, String> = emptyMap(),
    val sha256Digest: String? = null
)

enum class ArtifactScenario(
    val title: String,
    val tag: String,
    val category: String,
    val description: String,
    val samplePayload: String
) {
    VALID_W3C_VC(
        title = "W3C Verifiable Credential 2.0",
        tag = "Ed25519 / did:web",
        category = "Decentralized Identity",
        description = "W3C standard JSON-LD VC with Ed25519 cryptographic DataIntegrityProof bound to Elena Vance.",
        samplePayload = """{"@context":["https://www.w3.org/ns/credentials/v2"],"id":"urn:uuid:degree-8819","type":["VerifiableCredential","UniversityDegreeCredential"],"issuer":"did:web:anurag.edu.in","validFrom":"2024-06-01T00:00:00Z","credentialSubject":{"id":"did:key:z6MkpTHR8VNsBxYAAWHut2Geadd9jSwuBV8xRoAnwWsdvktH","name":"Elena Vance","degree":"B.Tech Computer Science & Engineering","honors":"Summa Cum Laude"},"proof":{"type":"Ed25519Signature2020","verificationMethod":"did:web:anurag.edu.in#key-1","jws":"eyJhbGciOiJFZERTQSI...MEQCIA1jK891238471"}}"""
    ),
    OPENID_VP(
        title = "OpenID4VP 1.0 Presentation Request",
        tag = "Nonce Challenge",
        category = "Protocols",
        description = "OpenID for Verifiable Presentations challenge requiring holder wallet signature with anti-replay nonce.",
        samplePayload = "openid4vp://?client_id=https://trustsentry.internal/verifier&response_uri=https://trustsentry.internal/api/v1/credentials/presentations/sess_912a&nonce=k79vL_x99a&presentation_definition=pd_academic_degree"
    ),
    GS1_SIGNED_LINK(
        title = "GS1 Digital Link URI v1.7.0",
        tag = "GTIN + JWS Crypto",
        category = "Supply Chain",
        description = "GS1 serialized healthcare batch URI with Application Identifiers (GTIN, Serial, Lot, Expiry) and cryptographic JWS signature.",
        samplePayload = "https://id.gs1.org/01/00012345678905/21/SER-TEST-9901/10/LOT-2026/17/261231?sig=MEQCIA1jK891238471982734918237491823749182374918237491823749182734918273491823"
    ),
    DIGILOCKER_DOC(
        title = "DigiLocker / MeriPehchaan Ref",
        tag = "Govt PKI XML DSig",
        category = "Government PKI",
        description = "Indian National e-Governance Division (MeitY) identity URI authenticated via Controller of Certifying Authorities (CCA) PKI.",
        samplePayload = "in.gov.meripehchaan.doc.aadhaar.982104921"
    ),
    VALID_TS1(
        title = "TrustSentry TS1 Authenticated Token",
        tag = "RSA-PSS + X.509",
        category = "TrustSentry Token",
        description = "Level A Cryptographic Token signed by US State Tech University Root PKI with active X.509 certificate chain.",
        samplePayload = "TS1.eyJhaWQiOiJUUy1ESVAtMjAyNi05OTQxIiwiaXNzIjoiVVMtU1RBVEUtVEVDSC1VTklWIiwic3ViIjoiRWxlbmEgVmFuY2UiLCJ0eXAiOiJESVBMT01BIiwiY2VydF9yZWYiOiJkOWU4ZjdhYzc2MWE5ODc2IiwiaWF0IjoiMjAyNi0wMS0xNVQwMDowMDowMFoiLCJleHAiOiIyMDMxLTAxLTE1VDAwOjAwOjAwWiIsImhhc2giOiJhN2M5ZjgyNjM4NDAxODI2In0.MEQCIA1jK891238471982734918237491823749182374918237491823749182734918273491823"
    ),
    TAMPERED_TS1(
        title = "Tampered TS1 Token (Modified Claim)",
        tag = "Forged Signature",
        category = "Tamper Detection",
        description = "Cryptographic payload altered from 'Elena Vance' to 'Modified Name'. RSA-PSS signature check fails with Critical Tamper alert.",
        samplePayload = "TS1.eyJhaWQiOiJUUy1ESVAtMjAyNi05OTQxIiwiaXNzIjoiVVMtU1RBVEUtVEVDSC1VTklWIiwic3ViIjoiTW9kaWZpZWQgTmFtZSIsInR5cCI6IkRJUExPTUEifQ.MEQCIA1jK891238471982734918237491823749182374918237491823749182734918273491823"
    ),
    REVOKED_CERT(
        title = "Revoked Certificate Artifact",
        tag = "CRL / OCSP Revoked",
        category = "Certificate Authority",
        description = "Credential issued with a signing certificate subsequently revoked in TrustSentry CRL Delta due to key compromise.",
        samplePayload = "TS1.eyJhaWQiOiJUUy1QQVJULTIwMjYtNDQwMSIsImlzcyI6IkFFUk9ERUZFTlNFLU1GRyIsInN1YiI6IlR1cmJpbmUgQmxhZGUgQmF0Y2ggIzkwMjEiLCJ0eXAiOiJQQVJUX0NFUlRJRklDQVRFIn0.SIG..."
    ),
    EXPIRED_TS1(
        title = "Expired Credential Artifact",
        tag = "Temporal Lapsed",
        category = "Expiration",
        description = "Artifact validity expired on 2025-12-31. Fails temporal integrity window check.",
        samplePayload = "TS1.eyJhaWQiOiJUUy1ET0MtMjAyNS0wMDAxIiwiaXNzIjoiVVMtU1RBVEUtVEVDSC1VTklWIiwic3ViIjoiQ291cnNlIFBhc3MiLCJleHAiOiIyMDI1LTEyLTMxVDIzOjU5OjU5WiJ9.SIG..."
    ),
    URL_QR(
        title = "External Web URL (Phishing Guard)",
        tag = "Google Web Risk",
        category = "Threat Intel",
        description = "Unsigned arbitrary web destination. Automated redirects blocked; tested against Web Risk, URLhaus, and PhishTank.",
        samplePayload = "https://security-advisory.trustsentry.app/advisory/TS-99"
    ),
    RETAIL_BARCODE(
        title = "Commercial Retail 1D Barcode",
        tag = "EAN-13 / UPC",
        category = "Commercial 1D",
        description = "Standard 1D commercial retail barcode. Decoded accurately but highlighted as lacking cryptographic authenticity proof.",
        samplePayload = "5449000000996"
    )
}
