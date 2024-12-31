@file:Suppress("unused")

package us.huseli.retaintheme.annotatedtext.compose

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import us.huseli.retaintheme.annotatedtext.RetainAnnotatedStringState
import us.huseli.retaintheme.annotatedtext.RetainMutableAnnotatedString

internal fun onTextFieldValueChange(
    value: TextFieldValue,
    state: RetainAnnotatedStringState,
    callback: (RetainMutableAnnotatedString) -> Unit,
) {
    if (state.textFieldValue != value) {
        val isTextChanged = state.textFieldValue.text != value.text

        state.onTextFieldValueChange(value)
        if (isTextChanged) callback(state.mutableString)
    }
}

@Composable
fun AnnotatedTextField(
    state: RetainAnnotatedStringState,
    onValueChange: (RetainMutableAnnotatedString) -> Unit = {},
    modifier: Modifier = Modifier,
    cursorBrush: Brush = SolidColor(Color.Black),
    enabled: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false,
    singleLine: Boolean = false,
    textStyle: TextStyle = TextStyle.Default,
) {
    BasicTextField(
        value = state.textFieldValue,
        modifier = modifier,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        enabled = enabled,
        textStyle = textStyle,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        visualTransformation = { TransformedText(state.nativeAnnotatedString, OffsetMapping.Identity) },
        onValueChange = { onTextFieldValueChange(it, state, onValueChange) },
        cursorBrush = cursorBrush,
    )
}

@Composable
fun OutlinedAnnotatedTextField(
    state: RetainAnnotatedStringState,
    onValueChange: (RetainMutableAnnotatedString) -> Unit = {},
    modifier: Modifier = Modifier,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    ),
    enabled: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    placeholder: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    singleLine: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
) {
    OutlinedTextField(
        value = state.textFieldValue,
        modifier = modifier,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        enabled = enabled,
        textStyle = textStyle,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        visualTransformation = { TransformedText(state.nativeAnnotatedString, OffsetMapping.Identity) },
        onValueChange = { onTextFieldValueChange(it, state, onValueChange) },
        placeholder = placeholder,
        colors = colors,
    )
}
