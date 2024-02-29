package tcnguyen.app.landmarkremark.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun AreaTextField(
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    hintText: String = "",
    maxLines: Int = 1
) {

    BasicTextField(
        value = value,
        modifier = modifier,
        onValueChange = onValueChanged,
        maxLines = maxLines,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = Color.Gray
        ),
        decorationBox = {
            innerTextField ->
            Box(
                modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = hintText,
                        color = LocalContentColor.current.copy(alpha = 0.5F)
                    )
                }
                innerTextField()
            }
        }
    )
}
