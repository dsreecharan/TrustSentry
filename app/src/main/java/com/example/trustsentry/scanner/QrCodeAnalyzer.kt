package com.example.trustsentry.scanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.util.EnumMap

class QrCodeAnalyzer(
    private val onCodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
            put(DecodeHintType.TRY_HARDER, true)
            put(
                DecodeHintType.POSSIBLE_FORMATS,
                listOf(
                    BarcodeFormat.QR_CODE,
                    BarcodeFormat.DATA_MATRIX,
                    BarcodeFormat.AZTEC,
                    BarcodeFormat.PDF_417,
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.CODE_39,
                    BarcodeFormat.EAN_13,
                    BarcodeFormat.EAN_8,
                    BarcodeFormat.UPC_A,
                    BarcodeFormat.UPC_E,
                    BarcodeFormat.ITF,
                    BarcodeFormat.CODABAR
                )
            )
            put(DecodeHintType.CHARACTER_SET, "UTF-8")
        }
        setHints(hints)
    }

    private var lastScannedCode: String? = null
    private var lastScannedTime: Long = 0L
    private val debounceCooldownMs: Long = 2500L

    override fun analyze(imageProxy: ImageProxy) {
        try {
            val plane = imageProxy.planes[0]
            val buffer = plane.buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            val width = imageProxy.width
            val height = imageProxy.height

            // Standard PlanarYUV Luminance Source
            var source: LuminanceSource = PlanarYUVLuminanceSource(
                data,
                width,
                height,
                0, 0,
                width,
                height,
                false
            )

            // Adjust for camera orientation rotation if necessary
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            if (rotationDegrees == 90 || rotationDegrees == 270) {
                // If rotated by 90 or 270 degrees, rotate luminance source
                source = when (rotationDegrees) {
                    90 -> rotateYuv90(data, width, height)
                    270 -> rotateYuv270(data, width, height)
                    else -> source
                }
            }

            var binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            var result: Result? = null

            try {
                result = reader.decodeWithState(binaryBitmap)
            } catch (_: NotFoundException) {
                // Try inverted colors (e.g. white code on black screen / dark mode)
                try {
                    val invertedSource = source.invert()
                    binaryBitmap = BinaryBitmap(HybridBinarizer(invertedSource))
                    result = reader.decodeWithState(binaryBitmap)
                } catch (_: NotFoundException) {
                    // Normal frame miss
                }
            } finally {
                reader.reset()
            }

            if (result != null && !result.text.isNullOrBlank()) {
                val detectedText = result.text.trim()
                val now = System.currentTimeMillis()
                if (detectedText != lastScannedCode || (now - lastScannedTime > debounceCooldownMs)) {
                    lastScannedCode = detectedText
                    lastScannedTime = now
                    onCodeDetected(detectedText)
                }
            }
        } catch (_: Exception) {
            // Ignore frame analysis errors
        } finally {
            imageProxy.close()
        }
    }

    private fun rotateYuv90(data: ByteArray, width: Int, height: Int): LuminanceSource {
        val rotatedData = ByteArray(data.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                rotatedData[x * height + height - y - 1] = data[x + y * width]
            }
        }
        return PlanarYUVLuminanceSource(rotatedData, height, width, 0, 0, height, width, false)
    }

    private fun rotateYuv270(data: ByteArray, width: Int, height: Int): LuminanceSource {
        val rotatedData = ByteArray(data.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                rotatedData[(width - x - 1) * height + y] = data[x + y * width]
            }
        }
        return PlanarYUVLuminanceSource(rotatedData, height, width, 0, 0, height, width, false)
    }
}
