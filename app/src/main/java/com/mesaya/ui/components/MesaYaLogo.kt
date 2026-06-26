package com.mesaya.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MesaYaLogo(
    modifier: Modifier = Modifier,
    size: Dp = 78.dp,
    backgroundColor: Color = Color.White,
    markColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.72f)) {
            val stroke = this.size.minDimension * 0.08f
            drawCircle(
                color = markColor.copy(alpha = 0.14f),
                radius = this.size.minDimension * 0.5f
            )
            drawArc(
                color = markColor,
                startAngle = 210f,
                sweepAngle = 300f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                size = Size(this.size.width, this.size.height)
            )
            drawLine(
                color = markColor,
                start = Offset(this.size.width * 0.22f, this.size.height * 0.7f),
                end = Offset(this.size.width * 0.78f, this.size.height * 0.7f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = markColor,
                start = Offset(this.size.width * 0.32f, this.size.height * 0.18f),
                end = Offset(this.size.width * 0.32f, this.size.height * 0.56f),
                strokeWidth = stroke * 0.7f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = markColor,
                start = Offset(this.size.width * 0.68f, this.size.height * 0.18f),
                end = Offset(this.size.width * 0.68f, this.size.height * 0.56f),
                strokeWidth = stroke * 0.7f,
                cap = StrokeCap.Round
            )
        }
        Text(
            "M",
            color = markColor,
            fontWeight = FontWeight.Black,
            fontSize = (size.value * 0.28f).sp
        )
    }
}
