@file:Suppress("unused")

package us.huseli.retaintheme.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SmallOutlinedButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    height: Dp = 25.dp,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        modifier = modifier.heightIn(min = height),
        onClick = onClick,
        shape = ShapeDefaults.ExtraSmall,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
        enabled = enabled,
        content = content,
        colors = colors,
    )
}

@Composable
fun SmallOutlinedButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    leadingIcon: ImageVector? = null,
    text: String,
    height: Dp = 25.dp,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
) {
    SmallOutlinedButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        height = height,
        colors = colors,
        content = {
            if (leadingIcon != null) Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(height).padding(end = 5.dp),
            )
            Text(text = text, style = textStyle)
        },
    )
}
