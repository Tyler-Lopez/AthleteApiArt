package com.company.activityart.presentation.editArtScreen.subscreens.type

import android.graphics.Typeface
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.company.activityart.R
import com.company.activityart.presentation.editArtScreen.EditArtViewEvent.ArtMutatingEvent.*
import com.company.activityart.architecture.EventReceiver
import com.company.activityart.presentation.common.type.Subhead
import com.company.activityart.presentation.common.type.SubheadHeavy
import com.company.activityart.presentation.editArtScreen.EditArtViewEvent
import com.company.activityart.presentation.editArtScreen.subscreens.filters.Section
import com.company.activityart.presentation.ui.theme.spacing
import com.company.activityart.util.FontSizeType
import com.company.activityart.util.enums.FontType
import com.company.activityart.util.enums.FontWeightType
import kotlin.math.roundToInt

@Composable
fun EditArtTypeScreen(
    activitiesDistanceMetersSummed: Int,
    athleteName: String,
    customTextCenter: String,
    customTextLeft: String,
    customTextRight: String,
    fontSelected: FontType,
    fontWeightSelected: FontWeightType,
    fontItalicized: Boolean,
    fontSizeSelected: FontSizeType,
    maximumCustomTextLength: Int,
    selectedEditArtTypeTypeCenter: EditArtTypeType,
    selectedEditArtTypeTypeLeft: EditArtTypeType,
    selectedEditArtTypeTypeRight: EditArtTypeType,
    scrollState: ScrollState,
    eventReceiver: EventReceiver<EditArtViewEvent>
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(spacing.medium)
    ) {
        EditArtTypeSection.values().forEach { section ->
            Section(
                header = stringResource(section.header),
                description = stringResource(section.description)
            ) {
                EditArtTypeType.values().forEach { type ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = type == when (section) {
                                EditArtTypeSection.LEFT -> selectedEditArtTypeTypeLeft
                                EditArtTypeSection.CENTER -> selectedEditArtTypeTypeCenter
                                EditArtTypeSection.RIGHT -> selectedEditArtTypeTypeRight
                            },
                            onClick = {
                                eventReceiver.onEvent(
                                    TypeSelectionChanged(
                                        section = section,
                                        typeSelected = type
                                    )
                                )
                            })
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                            Subhead(text = stringResource(type.header))
                            when (type) {
                                EditArtTypeType.NONE -> {}
                                EditArtTypeType.NAME -> SubheadHeavy(text = athleteName)
                                EditArtTypeType.DISTANCE_MILES -> SubheadHeavy(
                                    text = activitiesDistanceMetersSummed.meterToMilesStr()
                                )
                                EditArtTypeType.DISTANCE_KILOMETERS -> SubheadHeavy(
                                    text = activitiesDistanceMetersSummed.meterToKilometerStr()
                                )
                                EditArtTypeType.CUSTOM -> {
                                    OutlinedTextField(
                                        value = when (section) {
                                            EditArtTypeSection.LEFT -> customTextLeft
                                            EditArtTypeSection.CENTER -> customTextCenter
                                            EditArtTypeSection.RIGHT -> customTextRight
                                        },
                                        onValueChange = {
                                            eventReceiver.onEvent(
                                                TypeCustomTextChanged(
                                                    section = section,
                                                    changedTo = it
                                                )
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions.Default.copy(
                                            autoCorrect = false,
                                            keyboardType = KeyboardType.Text,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = { focusManager.clearFocus() }
                                        ),
                                        singleLine = true,
                                        maxLines = 1,
                                        enabled = EditArtTypeType.CUSTOM == when (section) {
                                            EditArtTypeSection.LEFT -> selectedEditArtTypeTypeLeft
                                            EditArtTypeSection.CENTER -> selectedEditArtTypeTypeCenter
                                            EditArtTypeSection.RIGHT -> selectedEditArtTypeTypeRight
                                        },
                                        modifier = Modifier.sizeIn(maxWidth = 254.dp)
                                    )
                                    SubheadHeavy(
                                        text = "${
                                            when (section) {
                                                EditArtTypeSection.LEFT -> customTextLeft.length
                                                EditArtTypeSection.CENTER -> customTextCenter.length
                                                EditArtTypeSection.RIGHT -> customTextRight.length
                                            }
                                        } / $maximumCustomTextLength"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Section(
            header = stringResource(R.string.edit_art_type_font_header),
            description = stringResource(R.string.edit_art_type_font_description)
        ) {
            FontType.values().forEach {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = it == fontSelected,
                        onClick = { eventReceiver.onEvent(TypeFontChanged(changedTo = it)) }
                    )
                    Text(
                        text = stringResource(it.strRes),
                        fontFamily = FontFamily(Typeface.createFromAsset(
                            context.assets,
                            it.getAssetPath(
                                /** Provides a loud failure if missing regular font **/
                                it.fontWeightTypes.firstOrNull {
                                    it == FontWeightType.REGULAR
                                } ?: error("Missing REGULAR font for font $it.")
                            )
                        ))
                    )
                }
            }
        }
        if (fontSelected.fontWeightTypes.containsMultipleTypes) {
            Section(
                header = stringResource(R.string.edit_art_type_font_weight_header),
                description = stringResource(R.string.edit_art_type_font_weight_description)
            ) {
                fontSelected.fontWeightTypes.forEach {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = fontWeightSelected == it,
                            onClick = { eventReceiver.onEvent(TypeFontWeightChanged(changedTo = it)) }
                        )
                        Text(
                            text = stringResource(it.stringRes),
                            fontFamily = FontFamily(
                                Typeface.createFromAsset(
                                    context.assets,
                                    fontSelected.getAssetPath(it)
                                )
                            )
                        )
                    }
                }
            }
        }
        if (fontSelected.isItalic) {
            Section(
                header = stringResource(R.string.edit_art_type_font_italic_header),
                description = stringResource(R.string.edit_art_type_font_italic_description)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(checked = fontItalicized, onCheckedChange = {
                        eventReceiver.onEvent(TypeFontItalicChanged(changedTo = it))
                    })
                    Text(
                        text = stringResource(
                            if (fontItalicized) {
                                R.string.edit_art_type_font_italic_enabled
                            } else {
                                R.string.edit_art_type_font_italic_disabled
                            }
                        ),
                        fontFamily = FontFamily(
                            Typeface.createFromAsset(
                                context.assets,
                                fontSelected.getAssetPath(fontWeightSelected, fontItalicized)
                            )
                        )
                    )
                }
            }
        }
        Section(
            header = stringResource(R.string.edit_art_type_size_header),
            description = stringResource(R.string.edit_art_type_size_description)
        ) {
            FontSizeType.values().forEach {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = fontSizeSelected == it,
                        onClick = {
                            eventReceiver.onEvent(
                                EditArtViewEvent.ArtMutatingEvent.TypeFontSizeChanged(
                                    it
                                )
                            )
                        }
                    )
                    Subhead(text = stringResource(it.strRes))
                }
            }
        }
    }
}

private fun Int.meterToMilesStr(): String = "${(this * 0.000621371192).roundToInt()} mi"

private fun Int.meterToKilometerStr(): String = "${(this / 1000f).roundToInt()} km"

private const val SINGLE_ITEM_SIZE = 1
private val List<FontWeightType>.containsMultipleTypes get() = size > SINGLE_ITEM_SIZE