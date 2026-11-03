package com.example.trustsentry.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.trustsentry.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val biometrics by viewModel.biometricsEnabled.collectAsState()
    val offlineCache by viewModel.offlineCacheEnabled.collectAsState()
    val environment by viewModel.selectedEnvironment.collectAsState()

    var showArchDialog by remember { mutableStateOf(false) }
    var showChecklistDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface.copy(alpha = 0.95f))
                .border(width = 1.dp, color = DarkSurfaceBorder)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SETTINGS & SECURITY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "CRYPTOGRAPHIC AUDIT & PREFERENCES",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = GoldAccent,
                    letterSpacing = 1.sp
                )
            }
        }

        val currentUser by viewModel.currentUser.collectAsState()
        val context = androidx.compose.ui.platform.LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val credentialManager = remember { androidx.credentials.CredentialManager.create(context) }

        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
            // Section 0: Firebase Account & Google Auth
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "FIREBASE ACCOUNT & GOOGLE AUTH",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    letterSpacing = 1.sp
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (currentUser != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentUser?.displayName ?: "Authenticated Verifier",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = currentUser?.email ?: "No email",
                                        fontSize = 11.5.sp,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "UID: " + (currentUser?.uid?.take(16) ?: "") + "...",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = GoldAccent
                                    )
                                }
                                Button(
                                    onClick = {
                                        signOutUser(context, credentialManager, coroutineScope) {}
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp).testTag("btn_settings_sign_out")
                                ) {
                                    Text("Sign Out", fontSize = 11.sp, color = StatusInvalid, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Not signed in", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("Sign in with Google to sync audit logs to Firestore", fontSize = 11.sp, color = TextMuted)
                                }
                                Button(
                                    onClick = { viewModel.navigateTo("auth") },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp).testTag("btn_settings_sign_in")
                                ) {
                                    Text("Sign In", fontSize = 11.sp, color = Color(0xFF080808), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Section 1: Security & Biometrics
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SECURITY & BIOMETRICS",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    letterSpacing = 1.sp
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(GoldAccent.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                        .border(1.dp, GoldAccent.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Biometric Protection", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("Require Biometric prompt to open audit log", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                            Switch(
                                checked = biometrics,
                                onCheckedChange = { viewModel.biometricsEnabled.value = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF080808),
                                    checkedTrackColor = GoldAccent,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = DarkSurfaceElevated
                                ),
                                modifier = Modifier.testTag("biometrics_switch")
                            )
                        }

                        HorizontalDivider(color = DarkDivider, modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(CyanAccent.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                        .border(1.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Storage, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Offline Verification Cache", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("Cache root PKI certs for offline mode", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                            Switch(
                                checked = offlineCache,
                                onCheckedChange = { viewModel.offlineCacheEnabled.value = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF080808),
                                    checkedTrackColor = GoldAccent,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = DarkSurfaceElevated
                                ),
                                modifier = Modifier.testTag("offline_cache_switch")
                            )
                        }
                    }
                }
            }

            // Section 2: Target Environment
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "TARGET GATEWAY & ENVIRONMENT",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    letterSpacing = 1.sp
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Active Gateway", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(
                                text = "HTTPS TLS 1.3",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                modifier = Modifier
                                    .background(GoldAccent.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                    .border(1.dp, GoldAccent.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("dev", "staging", "prod").forEach { env ->
                                val isSelected = environment == env
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) GoldAccent else DarkSurfaceElevated)
                                        .border(1.dp, if (isSelected) GoldAccent else DarkSurfaceBorder, RoundedCornerShape(10.dp))
                                        .clickable { viewModel.selectedEnvironment.value = env }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = env.uppercase(),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF080808) else TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Endpoint: https://api.$environment.trustsentry.app/api/v1",
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                    }
                }
            }

            // Section 3: Cryptographic Engine Specifications
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "CRYPTOGRAPHIC ENGINE SPECIFICATIONS",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    letterSpacing = 1.sp
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SpecRow(title = "Digital Signature", value = "RSA-PSS (2048/4096-bit)")
                        HorizontalDivider(color = DarkDivider, modifier = Modifier.padding(vertical = 8.dp))
                        SpecRow(title = "Hashing Standard", value = "SHA-256 Digest")
                        HorizontalDivider(color = DarkDivider, modifier = Modifier.padding(vertical = 8.dp))
                        SpecRow(title = "PKI Certificate", value = "X.509 v3 Chain")
                        HorizontalDivider(color = DarkDivider, modifier = Modifier.padding(vertical = 8.dp))
                        SpecRow(title = "Revocation Check", value = "CRL Delta + OCSP Stapling")
                    }
                }
            }

            // Section 4: Architecture & Blueprint Docs
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "DEVELOPER & BLUEPRINT SPECS",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    letterSpacing = 1.sp
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(18.dp))
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showArchDialog = true }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Layers, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Architecture Diagrams", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                        }

                        HorizontalDivider(color = DarkDivider)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showChecklistDialog = true }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Verified, contentDescription = null, tint = StatusTrusted, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Implementation Checklist (12/12 Verified)", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                        }
                    }
                }
            }

            // Section 5: App Version
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TrustSentry Android v1.0.0 (Phase 6 Production Release)",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
            }
        }
    }

    // Architecture Dialog
    if (showArchDialog) {
        AlertDialog(
            onDismissRequest = { showArchDialog = false },
            title = { Text("7-Stage Verification Pipeline", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "1. Payload Decode & Format Ingestion\n" +
                               "2. Registered Issuer & DID Identity Resolution\n" +
                               "3. RFC 8785 Canonical JSON & SHA-256 Digest\n" +
                               "4. X.509 Certificate Chain & RSA-PSS Signature\n" +
                               "5. Real-Time CRL Delta & OCSP Revocation Query\n" +
                               "6. OCR Layout, Visual & Semantic Claim Match\n" +
                               "7. Multi-Class Risk Engine Verdict (0-100 Score)",
                        fontSize = 12.5.sp,
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showArchDialog = false }) {
                    Text("Close", color = GoldAccent)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    // Checklist Dialog
    if (showChecklistDialog) {
        AlertDialog(
            onDismissRequest = { showChecklistDialog = false },
            title = { Text("Production Hardening Matrix", color = StatusTrusted, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "✓ W3C Verifiable Credentials 2.0 Engine\n" +
                               "✓ OpenID for Verifiable Presentations (OpenID4VP)\n" +
                               "✓ GS1 Digital Link v1.7.0 & JWS Verification\n" +
                               "✓ Indian DigiLocker / MeriPehchaan XML DSig\n" +
                               "✓ TrustSentry TS1 Tripartite Token Engine\n" +
                               "✓ ClamAV Antivirus Quarantine Gate\n" +
                               "✓ Tamper-Evident SHA-256 Audit Log Chaining\n" +
                               "✓ Zero-PII Privacy-by-Design Architecture\n" +
                               "✓ Hardware CameraX Sensor with Flashlight & Zoom\n" +
                               "✓ Zero-Permission Android Photo Picker",
                        fontSize = 12.5.sp,
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showChecklistDialog = false }) {
                    Text("Done", color = StatusTrusted)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}

@Composable
private fun SpecRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 12.sp, color = TextMuted)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace, color = TextPrimary)
    }
}
