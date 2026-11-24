package com.empresa.caudal.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DonutSlice(
    val label: String,
    val amount: Double,
    val color: Color
)

@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    centerSubtitle: String,
    centerTitle: String,
    modifier: Modifier = Modifier,
    chartSize: Dp = 230.dp,
    strokeWidth: Dp = 24.dp
) {
    val total = slices.sumOf { it.amount }
    val emptyTrackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.size(chartSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(chartSize)) {
            val strokePx = strokeWidth.toPx()
            val diameter = size.minDimension - strokePx
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)
            val arcSize = Size(diameter, diameter)

            if (total <= 0.0 || slices.isEmpty()) {
                drawArc(
                    color = emptyTrackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            } else {
                val gapDegrees = if (slices.size > 1) 3f else 0f
                var startAngle = -90f

                slices.forEach { slice ->
                    val rawSweep = ((slice.amount / total) * 360.0).toFloat()
                    val sweep = (rawSweep - gapDegrees).coerceAtLeast(1f)

                    drawArc(
                        color = slice.color,
                        startAngle = startAngle + (gapDegrees / 2f),
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(
                            width = strokePx,
                            cap = if (slices.size == 1) StrokeCap.Round else StrokeCap.Butt
                        )
                    )
                    startAngle += rawSweep
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = centerTitle,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}