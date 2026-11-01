package com.example.trustsentry.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.trustsentry.ui.viewmodel.MainViewModel

@Composable
fun UploadScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isProcessing by remember { mutableStateOf(false) }

    // File picker launcher for all document/image types
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessing = true
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: ByteArray(0)
                inputStream?.close()
                val fileName = uri.lastPathSegment ?: "uploaded_document.pdf"
                viewModel.verifyDocument(fileName, bytes)
                Toast.makeText(context, "Document verified successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading file: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isProcessing = false
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface.copy(alpha = 0.95f))
                    .border(width = 1.dp, color = DarkSurfaceBorder)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp))
                        .testTag("upload_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "DOCUMENT INSPECTOR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "7-STAGE CRYPTOGRAPHIC PIPELINE",
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = GoldAccent,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(DarkSurfaceElevated, RoundedCornerShape(20.dp))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CLAMAV GATE", fontSize = 8.5.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = ObsidianBlack,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Upload Document or Credential",
                style = Typography.titleLarge.copy(fontSize = 18.sp),
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Upload official PDF credentials, diplomas, or certificates for 7-stage cryptographic integrity, ClamAV antivirus screening, and OCR layout inspection.",
                style = Typography.bodyMedium.copy(fontSize = 12.5.sp, lineHeight = 18.sp),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Dropzone & File Action Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .clickable { filePickerLauncher.launch("*/*") }
                    .testTag("file_upload_card"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(GoldAccent.copy(alpha = 0.12f), CircleShape)
                            .border(1.dp, GoldAccent.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = GoldAccent, modifier = Modifier.size(28.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.CloudUpload,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isProcessing) "Executing 7-Stage Pipeline..." else "Select Document File",
                        style = Typography.titleMedium.copy(fontSize = 15.sp),
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Supported: PDF, JPG, PNG (Max 15MB • ClamAV Protected)",
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Browse Device Files", color = Color(0xFF080808), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Test Verification Scenarios
            Text(
                text = "TEST VERIFICATION SCENARIOS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GoldAccent,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            DocumentPresetItem(
                title = "Valid Academic Diploma",
                subtitle = "RSA-PSS + X.509 Chain + 100% OCR Match",
                icon = Icons.Outlined.VerifiedUser,
                accentColor = StatusTrusted,
                testTag = "preset_valid_diploma",
                onClick = {
                    val fakeBytes = "%PDF-1.5 State Technological University Official Academic Credential: Elena Vance - TS-DIP-2026-9941".toByteArray()
                    viewModel.verifyDocument("State_Tech_University_Diploma_2026.pdf", fakeBytes, "validDiploma")
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            DocumentPresetItem(
                title = "Tampered Credential",
                subtitle = "Subject Claim Discrepancy & Hash Mismatch",
                icon = Icons.Outlined.GppBad,
                accentColor = StatusInvalid,
                testTag = "preset_tampered_doc",
                onClick = {
                    val fakeBytes = "%PDF-1.5 State Technological University Diploma Conferred Upon Altered Name - Modified Content".toByteArray()
                    viewModel.verifyDocument("Forged_Engineering_Degree.pdf", fakeBytes, "tamperedDoc")
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            DocumentPresetItem(
                title = "EICAR Malware Test File",
                subtitle = "Anti-Virus Gate Quarantine Test Pattern",
                icon = Icons.Outlined.Security,
                accentColor = StatusReview,
                testTag = "preset_eicar_test",
                onClick = {
                    val eicar = "X5O!P%@AP[4\\PZX54(P^)7CC)7}\$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!\$H+H*".toByteArray()
                    viewModel.verifyDocument("eicar_antivirus_test_file.pdf", eicar, "eicarTest")
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            DocumentPresetItem(
                title = "Revoked Key Clearance",
                subtitle = "CRL & OCSP Responder Key Compromise Flag",
                icon = Icons.Outlined.GppMaybe,
                accentColor = StatusSuspicious,
                testTag = "preset_revoked_clearance",
                onClick = {
                    val fakeBytes = "%PDF-1.4 AeroDefense Security Clearance Batch #9021".toByteArray()
                    viewModel.verifyDocument("AeroDefense_Clearance_Pass.pdf", fakeBytes, "revokedClearance")
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            DocumentPresetItem(
                title = "Unregistered Commercial PDF",
                subtitle = "Text Extracted • Unable to Verify Authenticity",
                icon = Icons.Outlined.Description,
                accentColor = CyanAccent,
                testTag = "preset_unregistered_invoice",
                onClick = {
                    val fakeBytes = "%PDF-1.4 Standard Unsigned Commercial Invoice #9042 Vendor: Apex Logix LLC Total: $14,200.00".toByteArray()
                    viewModel.verifyDocument("Commercial_Vendor_Invoice_9042.pdf", fakeBytes, "unregistered")
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Security Policy Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Zero-Trust Anti-Virus & PKI Gate",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Uploads are quarantined and scanned prior to execution. Cryptographic mathematical proof is required for Level A trust.",
                            fontSize = 11.5.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentPresetItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String = "preset_item",
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(13.dp)
        )
    }
}
