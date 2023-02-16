package com.activityartapp.presentation.editArtScreen.subscreens.style.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.activityartapp.R
import com.activityartapp.presentation.editArtScreen.ColorType
import com.activityartapp.presentation.editArtScreen.ColorWrapper
import com.activityartapp.presentation.editArtScreen.composables.TextFieldSliderSpecification
import com.activityartapp.presentation.editArtScreen.composables.TextFieldSliders

@Composable
fun ColorSlidersRGB(
    color: ColorWrapper,
    onColorChanged: (ColorType, Float) -> Unit,
    onColorPendingChanged: (ColorType, String) -> Unit,
    onColorPendingChangeConfirmed: (ColorType) -> Unit
) {
    TextFieldSliders(
        specifications = listOf(
            TextFieldSliderSpecification(
                keyboardType = KeyboardType.Number,
                textFieldLabel = stringResource(R.string.edit_art_style_color_red),
                pendingChangesMessage = color.pendingRed?.let {
                    stringResource(R.string.edit_art_style_color_pending_change_prompt)
                },
                sliderValue = color.red,
                textFieldValue = color.pendingRed ?: color.redAsEightBit.toString(),
                sliderRange = ColorWrapper.RATIO_RANGE,
                onSliderChanged = { onColorChanged(ColorType.RED, it) },
                onTextFieldChanged = { onColorPendingChanged(ColorType.RED, it) },
                onTextFieldDone = { onColorPendingChangeConfirmed(ColorType.RED) }
            ),
            TextFieldSliderSpecification(
                keyboardType = KeyboardType.Number,
                textFieldLabel = stringResource(R.string.edit_art_style_color_green),
                sliderValue = color.green,
                pendingChangesMessage = color.pendingGreen?.let {
                    stringResource(R.string.edit_art_style_color_pending_change_prompt)
                },
                textFieldValue = color.pendingGreen ?: color.greenAsEightBit.toString(),
                sliderRange = ColorWrapper.RATIO_RANGE,
                onSliderChanged = { onColorChanged(ColorType.GREEN, it) },
                onTextFieldChanged = { onColorPendingChanged(ColorType.GREEN, it) },
                onTextFieldDone = { onColorPendingChangeConfirmed(ColorType.GREEN) }
            ),
            TextFieldSliderSpecification(
                keyboardType = KeyboardType.Number,
                textFieldLabel = stringResource(R.string.edit_art_style_color_blue),
                sliderValue = color.blue,
                pendingChangesMessage = color.pendingBlue?.let {
                    stringResource(R.string.edit_art_style_color_pending_change_prompt)
                },
                textFieldValue = color.pendingBlue ?: color.blueAsEightBit.toString(),
                sliderRange = ColorWrapper.RATIO_RANGE,
                onSliderChanged = { onColorChanged(ColorType.BLUE, it) },
                onTextFieldChanged = { onColorPendingChanged(ColorType.BLUE, it) },
                onTextFieldDone = { onColorPendingChangeConfirmed(ColorType.BLUE) }
            )
        )
    )
}