package com.activityartapp.presentation.editArtScreen.subscreens.style.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.activityartapp.R
import com.activityartapp.presentation.editArtScreen.ColorType
import com.activityartapp.presentation.editArtScreen.ColorWrapper
import com.activityartapp.presentation.editArtScreen.composables.TextFieldSliderSpecification
import com.activityartapp.presentation.editArtScreen.composables.TextFieldSliders
import kotlin.math.roundToInt

@Composable
fun ColorSlidersRGB(
    color: ColorWrapper,
    enabled: Boolean,
    onColorChanged: (ColorType, Float) -> Unit
) {
    TextFieldSliders(specifications = listOf(
        TextFieldSliderSpecification(
            enabled = enabled,
            errorMessage = color.outOfBoundsRed?.let {
                stringResource(R.string.edit_art_style_color_too_large_error, 255)
            },
            keyboardType = KeyboardType.Number,
            textFieldLabel = stringResource(R.string.edit_art_style_color_red),
            sliderValue = color.red,
            textFieldValue = color.outOfBoundsRed?.times(255f)?.roundToInt()?.toString()
                ?: color.redAsEightBit.toString(),
            sliderRange = ColorWrapper.VALUE_RANGE,
            onSliderChanged = { onColorChanged(ColorType.RED, it) },
            onTextFieldChanged = { str ->
                str.toFloatOrNull()?.div(255f)?.let {
                    onColorChanged(ColorType.RED, it)
                }
            }
        )
    ))
}