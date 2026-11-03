package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.trustsentry.crypto.CryptoEngine
import com.example.trustsentry.model.ArtifactScenario
import com.example.trustsentry.model.VerificationStatus
import com.example.trustsentry.scanner.BarcodeHelper
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TrustSentry", appName)
    }

    @Test
    fun `crypto engine verifies authentic TS1 token`() {
        val result = CryptoEngine.verifyPayload(ArtifactScenario.VALID_TS1.samplePayload)
        assertEquals(VerificationStatus.TRUSTED, result.status)
        assertTrue(result.score >= 90)
        assertEquals("LOW", result.riskLevel)
    }

    @Test
    fun `crypto engine detects tampered payload and fails with INVALID`() {
        val result = CryptoEngine.verifyPayload(ArtifactScenario.TAMPERED_TS1.samplePayload)
        assertEquals(VerificationStatus.INVALID, result.status)
        assertEquals(0, result.score)
        assertEquals("CRITICAL", result.riskLevel)
    }

    @Test
    fun `crypto engine verifies W3C Verifiable Credential 2_0`() {
        val result = CryptoEngine.verifyPayload(ArtifactScenario.VALID_W3C_VC.samplePayload)
        assertEquals(VerificationStatus.TRUSTED, result.status)
        assertEquals(98, result.score)
        assertNotNull(result.sha256Digest)
    }

    @Test
    fun `crypto engine identifies and quarantines EICAR malware string`() {
        val eicarBytes = "X5O!P%@AP[4\\PZX54(P^)7CC)7}\$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!\$H+H*".toByteArray()
        val result = CryptoEngine.verifyDocumentBytes("eicar_test.pdf", eicarBytes)
        assertEquals(VerificationStatus.INVALID, result.status)
        assertEquals(0, result.score)
        assertEquals("CRITICAL", result.riskLevel)
    }

    @Test
    fun `barcode roundtrip generation and decoding produces exact content`() {
        val samplePayload = ArtifactScenario.VALID_TS1.samplePayload
        val bitmap = BarcodeHelper.generateQrBitmap(samplePayload, size = 400)
        assertNotNull("Generated QR bitmap should not be null", bitmap)
        val decoded = BarcodeHelper.decodeBitmap(bitmap!!)
        assertEquals("Decoded text should match original payload", samplePayload, decoded)
    }

    @Test
    fun `commercial barcode classification accurately identifies EAN13 and UPC`() {
        val eanResult = CryptoEngine.verifyPayload("5449000000996")
        assertEquals(VerificationStatus.UNABLE_TO_VERIFY, eanResult.status)
        assertEquals("EAN-13", eanResult.artifactType)
        assertTrue(eanResult.issuer.contains("Coca-Cola"))

        val code128Result = CryptoEngine.verifyPayload("SER-TEST-9901")
        assertEquals(VerificationStatus.UNABLE_TO_VERIFY, code128Result.status)
        assertEquals("Code 128", code128Result.artifactType)
    }

    @Test
    fun `untrusted URL activates phishing guard and review status`() {
        val urlResult = CryptoEngine.verifyPayload("https://suspicious-login.example.com/auth")
        assertEquals(VerificationStatus.REVIEW, urlResult.status)
        assertEquals("MEDIUM", urlResult.riskLevel)
        assertTrue(urlResult.artifactType.contains("URL"))
    }

    @Test
    fun `crypto engine calculates deterministic sha256 hex digest`() {
        val digest = CryptoEngine.computeSha256("abc")
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", digest)
    }

    @Test
    fun `verification entity instantiates with proper attributes`() {
        val entity = com.example.trustsentry.data.VerificationEntity(
            artifactId = "DIP-9081",
            artifactType = "W3C VC 2.0",
            issuer = "did:web:mit.edu",
            score = 98,
            status = "TRUSTED",
            riskLevel = "LOW",
            explanation = "Mathematical proof confirmed",
            rawValue = "sample-raw",
            verifiedAt = System.currentTimeMillis(),
            isPinned = true,
            sha256Digest = "abcdef1234567890"
        )
        assertEquals("DIP-9081", entity.artifactId)
        assertEquals("TRUSTED", entity.status)
        assertTrue(entity.isPinned)
        assertEquals(98, entity.score)
    }
}
