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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
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
import com.example.trustsentry.crypto.CryptoEngine
import com.example.trustsentry.data.VerificationEntity
import com.example.trustsentry.model.VerificationStatus
import com.example.trustsentry.ui.components.EmptyStateView
import com.example.trustsentry.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.trustsentry.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val filter by viewModel.historyFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val verifications by viewModel.filteredVerifications.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    val filterOptions = listOf("All", "Trusted", "Review", "Invalid")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface.copy(alpha = 0.95f))
                .border(width = 1.dp, color = DarkSurfaceBorder)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "VERIFICATION HISTORY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "SECURE LOCAL AUDIT TRAIL",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = GoldAccent,
                    letterSpacing = 1.sp
                )
            }

            if (verifications.isNotEmpty()) {
                IconButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp))
                        .testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Clear History",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search by ID, issuer, or type...", fontSize = 12.sp, color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = GoldAccent.copy(alpha = 0.5f),
                unfocusedBorderColor = DarkSurfaceBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .height(52.dp)
                .testTag("history_search_field")
        )

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterOptions.forEach { opt ->
                val isSelected = filter == opt
                val count = when (opt) {
                    "All" -> viewModel.allVerifications.collectAsState().value.size
                    "Trusted" -> viewModel.allVerifications.collectAsState().value.count { it.status == "TRUSTED" }
                    "Review" -> viewModel.allVerifications.collectAsState().value.count { it.status == "REVIEW" }
                    "Invalid" -> viewModel.allVerifications.collectAsState().value.count { it.status == "INVALID" || it.status == "SUSPICIOUS" }
                    else -> 0
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) GoldAccent else DarkSurfaceElevated)
                        .border(1.dp, if (isSelected) GoldAccent else DarkSurfaceBorder, RoundedCornerShape(20.dp))
                        .clickable { viewModel.setHistoryFilter(opt) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$opt ($count)",
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF080808) else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // History Records List or Empty State
        if (verifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Outlined.History,
                    title = "No audit records found",
                    message = if (searchQuery.isNotEmpty()) "No results matching '$searchQuery'." else "Verified artifacts will be saved securely to your device with cryptographic timestamps and audit trail."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(verifications, key = { it.id }) { entity ->
                    HistoryItemCard(
                        entity = entity,
                        onClick = {
                            viewModel.verifyScannedPayload(entity.rawValue ?: entity.artifactId)
                        },
                        onTogglePin = { viewModel.togglePin(entity) },
                        onDelete = { viewModel.deleteVerification(entity) }
                    )
                }
            }
        }
    }

    // Confirmation Dialog for Clearing History
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Audit History?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove all stored local verification events and audit logs from your device.", color = TextSecondary, fontSize = 13.sp) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear All", color = StatusInvalid, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}

@Composable
fun HistoryItemCard(
    entity: VerificationEntity,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = try {
        VerificationStatus.valueOf(entity.status)
    } catch (_: Exception) {
        VerificationStatus.UNABLE_TO_VERIFY
    }
    val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm:ss", Locale.getDefault())
    val formattedTime = sdf.format(Date(entity.verifiedAt))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("history_item_${entity.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                when (status) {
                                    VerificationStatus.TRUSTED -> StatusTrusted.copy(alpha = 0.15f)
                                    VerificationStatus.REVIEW -> StatusReview.copy(alpha = 0.15f)
                                    VerificationStatus.SUSPICIOUS -> StatusSuspicious.copy(alpha = 0.15f)
                                    VerificationStatus.INVALID -> StatusInvalid.copy(alpha = 0.15f)
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
                            fontSize = 13.sp,
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

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = entity.artifactId,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                        Text(
                            text = entity.artifactType,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = status, compact = true)
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = if (entity.isPinned) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Pin",
                            tint = if (entity.isPinned) GoldAccent else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DarkDivider, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Issuer: ${entity.issuer}",
                    fontSize = 11.5.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
            }
        }
    }
}
