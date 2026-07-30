package com.pdp.gotronome.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdp.gotronome.MockMetronomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private val VALUE_FIELD_HORIZONTAL_PADDING = 12.dp
private val VALUE_FIELD_DIGIT_WIDTH = 16.dp
private val VALUE_FIELD_MIN_HEIGHT = 48.dp

@Composable
fun NumSelector(
    label: String,
    numProperty: StateFlow<Int>,
    propertySetter: (Int) -> Unit,
    propertyStorer: () -> Unit,
    minVal: Int,
    maxVal: Int,
    modifier: Modifier = Modifier,
) {
    val value by numProperty.collectAsStateWithLifecycle()
    val leftInteractionSource = remember { MutableInteractionSource() }
    val rightInteractionSource = remember { MutableInteractionSource() }
    val leftPressed by leftInteractionSource.collectIsPressedAsState()
    val rightPressed by rightInteractionSource.collectIsPressedAsState()
    val viewConfiguration = LocalViewConfiguration.current
    var leftLongPressed by remember { mutableStateOf(false) }
    var rightLongPressed by remember { mutableStateOf(false) }
    var valueChanged by remember { mutableStateOf(false) }

    // Long-press the down button to keep decreasing; persist once on release.
    LaunchedEffect(leftPressed) {
        if (leftPressed) {
            valueChanged = true
            leftLongPressed = false
            delay(viewConfiguration.longPressTimeoutMillis)
            leftLongPressed = true
            while (leftPressed) {
                propertySetter(max(minVal, value - 4))
                delay(50)
            }
        } else {
            leftLongPressed = false
            if (valueChanged) {
                propertyStorer()
                valueChanged = false
            }
        }
    }

    // Long-press the up button to keep increasing; persist once on release.
    LaunchedEffect(rightPressed) {
        if (rightPressed) {
            valueChanged = true
            rightLongPressed = false
            delay(viewConfiguration.longPressTimeoutMillis)
            rightLongPressed = true
            while (rightPressed) {
                propertySetter(min(maxVal, value + 4))
                delay(50)
            }
        } else {
            rightLongPressed = false
            if (valueChanged) {
                propertyStorer()
                valueChanged = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.width(8.dp))
            EditableValue(
                label = label,
                value = value,
                minVal = minVal,
                maxVal = maxVal,
                onCommit = { propertySetter(it); propertyStorer() },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            IconButton(
                interactionSource = leftInteractionSource,
                onClick = { if (!leftLongPressed) propertySetter(max(minVal, value - 1)) },
                enabled = value > minVal,
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    tint = MaterialTheme.colorScheme.secondary,
                    contentDescription = "Decrease $label",
                )
            }
            Slider(
                value = value.toFloat(),
                onValueChange = { propertySetter(it.roundToInt().coerceIn(minVal, maxVal)) },
                onValueChangeFinished = { propertyStorer() },
                valueRange = minVal.toFloat()..maxVal.toFloat(),
                modifier = Modifier.weight(1f),
            )
            IconButton(
                interactionSource = rightInteractionSource,
                onClick = { if (!rightLongPressed) propertySetter(min(maxVal, value + 1)) },
                enabled = value < maxVal,
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    tint = MaterialTheme.colorScheme.secondary,
                    contentDescription = "Increase $label",
                )
            }
        }
    }
}

/**
 * Shows [value] inside an outlined box that reads as a text field at rest. Tapping it starts
 * in-place editing with the numeric keypad; the value is committed on Done or on focus loss,
 * clamped to [minVal]..[maxVal], and left untouched if what was typed is not a number.
 */
@Composable
private fun EditableValue(
    label: String,
    value: Int,
    minVal: Int,
    maxVal: Int,
    onCommit: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember { mutableStateOf<TextFieldValue?>(null) }
    // onFocusChanged reports "unfocused" once at composition, before requestFocus lands; without
    // this the field would commit and close itself on the frame it appeared.
    var focusGained by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val maxDigits = maxVal.toString().length
    val editing = draft != null

    val commit: (String) -> Unit = { typed ->
        // Disarm the focus-loss handler first, so clearFocus below doesn't re-enter this lambda.
        focusGained = false
        typed.toIntOrNull()?.let { onCommit(it.coerceIn(minVal, maxVal)) }
        // Drop focus explicitly rather than letting it fall through to a neighbouring button,
        // which would then treat a hardware Enter as a press on itself.
        focusManager.clearFocus()
        keyboard?.hide()
        draft = null
    }

    LaunchedEffect(editing) { if (editing) focusRequester.requestFocus() }

    val valueStyle = MaterialTheme.typography.headlineSmall.copy(
        color = MaterialTheme.colorScheme.secondary,
        textAlign = TextAlign.Center,
    )

    Box(
        modifier = modifier
            .width(VALUE_FIELD_HORIZONTAL_PADDING * 2 + VALUE_FIELD_DIGIT_WIDTH * maxDigits)
            .heightIn(min = VALUE_FIELD_MIN_HEIGHT)
            .border(
                width = if (editing) 2.dp else 1.dp,
                color = if (editing) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                shape = MaterialTheme.shapes.small,
            )
            .then(
                if (editing) {
                    Modifier
                } else {
                    Modifier.clickable(onClickLabel = "Type $label") {
                        draft = TextFieldValue(
                            text = value.toString(),
                            selection = TextRange(0, value.toString().length),
                        )
                    }
                }
            )
            .padding(horizontal = VALUE_FIELD_HORIZONTAL_PADDING),
        contentAlignment = Alignment.Center,
    ) {
        val current = draft
        if (current == null) {
            Text(text = value.toString(), style = valueStyle)
        } else {
            BasicTextField(
                value = current,
                onValueChange = { new ->
                    val digits = new.text.filter(Char::isDigit).take(maxDigits)
                    draft = if (digits == new.text) {
                        new
                    } else {
                        TextFieldValue(digits, TextRange(digits.length))
                    }
                },
                textStyle = valueStyle,
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { commit(current.text) }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        if (state.isFocused) {
                            focusGained = true
                        } else if (focusGained) {
                            draft?.let { commit(it.text) }
                        }
                    },
            )
        }
    }
}

@Preview
@Composable
fun NumSelectorPreview() {
    val viewModel = viewModel<MockMetronomeViewModel>()
    NumSelector(
        label = "Loop bars",
        viewModel.numBars,
        { viewModel.setNumBars(it) },
        { viewModel.storeNumBars() },
        2,
        32,
    )
}
