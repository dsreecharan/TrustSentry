package com.example.trustsentry.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trustsentry.crypto.CryptoEngine
import com.example.trustsentry.data.VerificationEntity
import com.example.trustsentry.model.VerificationStatus
import com.example.trustsentry.ui.components.ActionCard
import com.example.trustsentry.ui.components.EmptyStateView
import com.example.trustsentry.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.trustsentry.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val recentVerifications by viewModel.allVerifications.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Sticky Header / Top App Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface.copy(alpha = 0.95f))
                    .border(width = 1.dp, color = DarkSurfaceBorder)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
                            .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "TrustSentry Shield",
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TRUSTSENTRY",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ACTIVE",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                modifier = Modifier
                                    .background(GoldAccent.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                    .border(1.dp, GoldAccent.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = "CRYPTOGRAPHIC VAULT",
                            fontSize = 8.5.sp,
                            color = TextMuted,
                            letterSpacing = 1.5.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(DarkSurfaceElevated, RoundedCornerShape(20.dp))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(GoldAccent, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PKI ACTIVE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Hero Section
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                Text(
                    text = "VERIFY BEFORE YOU TRUST",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Cryptographic Artifact &\nTamper Verification",
                    style = Typography.displayMedium.copy(fontSize = 22.sp, lineHeight = 28.sp),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Scan physical or digital QR codes, barcodes, academic credentials, and official documents to verify mathematical signatures against the PKI trust network.",
                    style = Typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
                    color = TextSecondary
                )
            }
        }

        // Main Action Cards
        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ActionCard(
                    title = "Scan QR / Barcode",
                    description = "Direct camera scan with instant cryptographic payload parsing",
                    badgeText = "DIRECT",
                    icon = Icons.Default.QrCodeScanner,
                    accentColor = GoldAccent,
                    isPrimary = true,
                    testTag = "btn_scan_qr_barcode",
                    onClick = { onNavigate("scan") }
                )

                ActionCard(
                    title = "Upload Document",
                    description = "Select PDF or image (JPG, PNG) for multi-stage tamper inspection",
                    badgeText = "STAGE 1",
                    icon = Icons.Outlined.Description,
                    accentColor = CyanAccent,
                    isPrimary = false,
                    testTag = "btn_upload_document",
                    onClick = { onNavigate("upload") }
                )
            }
        }

        // Zero Trust Architecture Pill
        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(14.dp))
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Zero Trust Architecture: Artifacts are verified against RSA-2048 & SHA-256 PKI roots.",
                    fontSize = 11.5.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        // Recent Verifications Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT VERIFICATIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "HISTORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .clickable { onNavigate("history") }
                        .testTag("btn_view_history")
                )
            }
        }

        // Recent Verifications List or Empty State
        if (recentVerifications.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    EmptyStateView(
                        icon = Icons.Outlined.History,
                        title = "No verifications yet",
                        message = "Your cryptographic verification logs will appear here once you scan or upload an artifact."
                    )
                }
            }
        } else {
            items(recentVerifications.take(5), key = { it.id }) { entity ->
                RecentVerificationItem(
                    entity = entity,
                    onClick = {
                        viewModel.verifyScannedPayload(entity.rawValue ?: entity.artifactId)
                    },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
fun RecentVerificationItem(
    entity: VerificationEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = try {
        VerificationStatus.valueOf(entity.status)
    } catch (_: Exception) {
        VerificationStatus.UNABLE_TO_VERIFY
    }
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val formattedTime = sdf.format(Date(entity.verifiedAt))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Score Badge
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    when (status) {
                        VerificationStatus.TRUSTED -> StatusTrusted.copy(alpha = 0.12f)
                        VerificationStatus.REVIEW -> StatusReview.copy(alpha = 0.12f)
                        VerificationStatus.SUSPICIOUS -> StatusSuspicious.copy(alpha = 0.12f)
                        VerificationStatus.INVALID -> StatusInvalid.copy(alpha = 0.12f)
                        else -> DarkSurfaceBorder
                    },
                    CircleShape
                )
                .border(
                    1.dp,
                    when (status) {
                        VerificationStatus.TRUSTED -> StatusTrusted.copy(alpha = 0.4f)
                        VerificationStatus.REVIEW -> StatusReview.copy(alpha = 0.4f)
                        VerificationStatus.SUSPICIOUS -> StatusSuspicious.copy(alpha = 0.4f)
                        VerificationStatus.INVALID -> StatusInvalid.copy(alpha = 0.4f)
                        else -> DarkSurfaceBorder
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${entity.score}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = when (status) {
                    VerificationStatus.TRUSTED -> StatusTrusted
                    VerificationStatus.REVIEW -> StatusReview
                    VerificationStatus.SUSPICIOUS -> StatusSuspicious
                    VerificationStatus.INVALID -> StatusInvalid
                    else -> TextMuted
                }
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entity.artifactId,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )
                StatusBadge(status = status, compact = true)
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = entity.issuer,
                    fontSize = 11.5.sp,
                    color = TextMuted,
                    maxLines = 1
                )
                Text(
                    text = formattedTime,
                    fontSize = 10.5.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
