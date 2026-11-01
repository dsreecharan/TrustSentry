package com.example.trustsentry.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trustsentry.model.VerificationStatus
import com.example.ui.theme.*

@Composable
fun StatusBadge(
    status: VerificationStatus,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val fgColor = when (status) {
        VerificationStatus.TRUSTED -> StatusTrusted
        VerificationStatus.REVIEW -> StatusReview
        VerificationStatus.SUSPICIOUS -> StatusSuspicious
        VerificationStatus.INVALID -> StatusInvalid
        VerificationStatus.UNABLE_TO_VERIFY -> StatusOffline
    }

    val bgColor = when (status) {
        VerificationStatus.TRUSTED -> StatusTrustedBg.copy(alpha = 0.45f)
        VerificationStatus.REVIEW -> StatusReviewBg.copy(alpha = 0.45f)
        VerificationStatus.SUSPICIOUS -> StatusSuspiciousBg.copy(alpha = 0.45f)
        VerificationStatus.INVALID -> StatusInvalidBg.copy(alpha = 0.45f)
        VerificationStatus.UNABLE_TO_VERIFY -> DarkSurfaceBorder.copy(alpha = 0.5f)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, fgColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(
                horizontal = if (compact) 8.dp else 12.dp,
                vertical = if (compact) 4.dp else 6.dp
            )
    ) {
        Text(
            text = status.symbol,
            color = fgColor,
            fontWeight = FontWeight.Black,
            fontSize = if (compact) 11.sp else 13.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = status.label,
            color = fgColor,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 10.sp else 12.sp,
            letterSpacing = 0.8.sp
        )
    }
}
