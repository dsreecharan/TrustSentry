package com.example.trustsentry.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trustsentry.model.VerificationStatus
import com.example.ui.theme.*

@Composable
fun TrustScoreGauge(
    score: Int,
    status: VerificationStatus,
    size: Dp = 150.dp,
    modifier: Modifier = Modifier
) {
    val ringColor = when (status) {
        VerificationStatus.TRUSTED -> StatusTrusted
        VerificationStatus.REVIEW -> StatusReview
        VerificationStatus.SUSPICIOUS -> StatusSuspicious
        VerificationStatus.INVALID -> StatusInvalid
        VerificationStatus.UNABLE_TO_VERIFY -> StatusOffline
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = this.size.width * 0.085f
            val arcSize = Size(this.size.width - strokeWidth, this.size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val startAngle = 135f
            val sweepAngle = 270f

            // Background track
            drawArc(
                color = DarkSurfaceBorder,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active progress track
            val progressSweep = (score / 100f).coerceIn(0f, 1f) * sweepAngle
            drawArc(
                color = ringColor,
                startAngle = startAngle,
                sweepAngle = progressSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                fontSize = (size.value * 0.28).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = ringColor
            )
            Text(
                text = "/ 100",
                fontSize = 12.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
