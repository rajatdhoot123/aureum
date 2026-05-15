package kwiktwik.ratewatch.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kwiktwik.ratewatch.app.ui.theme.ChartGreen

@Composable
fun LineChart(data: List<Float>, modifier: Modifier = Modifier, color: Color = ChartGreen) {
    Canvas(modifier = modifier) {
        val path = Path()
        val spacing = size.width / (data.size - 1)

        data.forEachIndexed { index, value ->
            val x = index * spacing
            val y = size.height - (size.height * value)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        val fillPath = Path().apply {
            addPath(path)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.2f), Color.Transparent)
            )
        )
    }
}
