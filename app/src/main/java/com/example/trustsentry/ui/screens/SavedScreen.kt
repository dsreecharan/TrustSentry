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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
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
import com.example.trustsentry.data.VerificationEntity
import com.example.trustsentry.model.VerificationStatus
import com.example.trustsentry.ui.components.EmptyStateView
import com.example.trustsentry.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.trustsentry.ui.viewmodel.MainViewModel

@Composable
fun SavedScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val pinnedVerifications by viewModel.pinnedVerifications.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        // Top Bar
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
                    text = "SAVED ARTIFACTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "PINNED CREDENTIALS & LICENSES",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = GoldAccent,
                    letterSpacing = 1.sp
                )
            }
        }

        if (pinnedVerifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Outlined.BookmarkBorder,
                    title = "No saved artifacts",
                    message = "Pin frequently verified credentials, tickets, and certificates here for quick status checks and offline availability."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(pinnedVerifications, key = { it.id }) { entity ->
                    SavedItemCard(
                        entity = entity,
                        onClick = {
                            viewModel.verifyScannedPayload(entity.rawValue ?: entity.artifactId)
                        },
                        onUnpin = { viewModel.togglePin(entity) }
                    )
                }
            }
        }
    }
}

@Composable
fun SavedItemCard(
    entity: VerificationEntity,
    onClick: () -> Unit,
    onUnpin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = try {
        VerificationStatus.valueOf(entity.status)
    } catch (_: Exception) {
        VerificationStatus.UNABLE_TO_VERIFY
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, GoldAccent.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("saved_item_${entity.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(GoldAccent.copy(alpha = 0.12f), CircleShape)
                    .border(1.dp, GoldAccent.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${entity.score}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = GoldAccent
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entity.artifactId,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = entity.issuer,
                    fontSize = 11.5.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusBadge(status = status, compact = true)
            }

            IconButton(onClick = onUnpin) {
                Icon(
                    imageVector = Icons.Filled.Bookmark,
                    contentDescription = "Unpin",
                    tint = GoldAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
