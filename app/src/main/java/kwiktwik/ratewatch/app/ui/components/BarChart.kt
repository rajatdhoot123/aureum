package kwiktwik.ratewatch.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import kwiktwik.ratewatch.app.ui.theme.ChartGreen

@Composable
fun BarChart(data: List<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val spacing = size.width / (data.size * 1.5f)
        val barWidth = spacing * 0.8f

        data.forEachIndexed { index, value ->
            val barHeight = size.height * value
            drawRoundRect(
                color = ChartGreen,
                topLeft = Offset(index * spacing * 1.5f, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
    }
}
