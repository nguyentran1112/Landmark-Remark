package tcnguyen.app.landmarkremark.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tcnguyen.app.landmarkremark.util.toHslColor

@Composable
fun Avatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        val color = remember(name) {
            val name = listOf(name)
                .joinToString(separator = "")
                .uppercase()
            Color("$name / $name".toHslColor())
        }
        val initials = (name.take(1)).uppercase()
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(SolidColor(color))
        }
        Text(text = initials, style = textStyle, color = Color.White)
    }
}
