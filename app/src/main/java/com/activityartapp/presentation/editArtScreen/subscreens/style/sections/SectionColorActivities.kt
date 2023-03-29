package com.activityartapp.presentation.editArtScreen.subscreens.style.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.activityartapp.R
import com.activityartapp.presentation.common.button.Button
import com.activityartapp.presentation.common.button.ButtonEmphasis
import com.activityartapp.presentation.common.button.ButtonSize
import com.activityartapp.presentation.common.layout.ColumnMediumSpacing
import com.activityartapp.presentation.editArtScreen.ColorWrapper
import com.activityartapp.presentation.editArtScreen.EditArtViewEvent
import com.activityartapp.presentation.editArtScreen.EditArtViewEvent.ArtMutatingEvent.StyleColorChanged
import com.activityartapp.presentation.editArtScreen.EditArtViewEvent.ArtMutatingEvent.StyleColorPendingChangeConfirmed
import com.activityartapp.presentation.editArtScreen.EditArtViewEvent.StyleColorPendingChanged
import com.activityartapp.presentation.editArtScreen.EditArtViewState
import com.activityartapp.presentation.editArtScreen.StyleIdentifier
import com.activityartapp.presentation.editArtScreen.subscreens.style.composables.ColorPreview
import com.activityartapp.presentation.editArtScreen.subscreens.style.composables.ColorSlidersRGB
import com.activityartapp.presentation.ui.theme.spacing
import com.activityartapp.util.classes.ActivityColorRule

@Composable
fun SectionColorActivities(
    colorRules: List<ActivityColorRule>,
    onColorChanged: (StyleColorChanged) -> Unit,
    onColorPendingChangeConfirmed: (StyleColorPendingChangeConfirmed) -> Unit,
    onColorPendingChanged: (StyleColorPendingChanged) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = spacing.medium, end = spacing.medium, bottom = spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.medium)
    ) {
        Text(
            text = stringResource(R.string.edit_art_style_activities_description),
            style = MaterialTheme.typography.body1
        )
        Text(
            text = stringResource(R.string.edit_art_style_activities_description_additional_1),
            style = MaterialTheme.typography.body1
        )
        Text(
            text = stringResource(R.string.edit_art_style_activities_description_additional_2),
            style = MaterialTheme.typography.body1
        )
    }

    val lazyListState = rememberLazyListState()
    val colorCount = colorRules.size
    LazyRow(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)
            .padding(vertical = spacing.small),
        horizontalArrangement = Arrangement.spacedBy(spacing.xSmall)
    ) {
        item { Spacer(modifier = Modifier.width(spacing.xSmall)) }
        items(colorCount) {
            ListItem(
                index = it,
                color = colorRules[it],
                colorsCount = colorCount,
                onColorChanged = onColorChanged,
                onColorRemoved = {},
                onColorPendingChangeConfirmed = onColorPendingChangeConfirmed,
                onColorPendingChanged = onColorPendingChanged
            )
        }
        item { Spacer(modifier = Modifier.width(spacing.xSmall)) }
    }
    /*
    ColorPreview(colorWrapper = color)
    ColorSlidersRGB(
        color = color,
        onColorChanged = { colorType, changedTo ->
            onColorChanged(
                StyleColorChanged(
                    style = StyleIdentifier.Activities,
                    colorType = colorType,
                    changedTo = changedTo
                )
            )
        },
        onColorPendingChanged = { colorType, changedTo ->
            onColorPendingChanged(
                StyleColorPendingChanged(
                    style = StyleIdentifier.Activities,
                    colorType = colorType,
                    changedTo = changedTo
                )
            )
        },
        onColorPendingChangeConfirmed = {
            onColorPendingChangeConfirmed(
                StyleColorPendingChangeConfirmed(
                    style = StyleIdentifier.Activities
                )
            )
        }
    )
    
     */
}


@Composable
private fun ListItem(
    index: Int,
    color: ActivityColorRule,
    colorsCount: Int,
    onColorChanged: (StyleColorChanged) -> Unit,
    onColorRemoved: (EditArtViewEvent.ClickedRemoveGradientColor) -> Unit,
    onColorPendingChangeConfirmed: (StyleColorPendingChangeConfirmed) -> Unit,
    onColorPendingChanged: (StyleColorPendingChanged) -> Unit
) {
    Card {
        ColumnMediumSpacing(
            modifier = Modifier
                .width(360.dp)
                .padding(spacing.small)
        ) {
            if (color is ActivityColorRule.Any) {
                Text(
                    text = stringResource(R.string.edit_art_style_activities_default),
                    style = MaterialTheme.typography.subtitle2
                )
                Text(
                    text = stringResource(R.string.edit_art_style_activities_default_description),
                    style = MaterialTheme.typography.subtitle1
                )
            } else {
                // todo
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ColorPreview(
                    colorWrapper = ColorWrapper.White, // todo
                    modifier = Modifier.weight(1f, true)
                )
                // For an unknown reason, if this event is not defined
                // before than a runtime crash occurs
                val event = EditArtViewEvent.ClickedRemoveGradientColor(index)
                if (colorsCount > EditArtViewState.MIN_GRADIENT_BG_COLORS) {
                    IconButton(onClick = {
                        //    onColorRemoved(event)
                    }) {
                        Icon(imageVector = Icons.Outlined.Delete, null)
                    }
                }
            }
            ColorSlidersRGB(
                color = ColorWrapper.White, // todo
                onColorChanged = { colorType, changedTo ->
                    /*
                    onColorChanged(
                        StyleColorChanged(
                            style = StyleIdentifier.Background(index = index),
                            colorType = colorType,
                            changedTo = changedTo
                        )
                    )

                     */
                },
                onColorPendingChanged = { colorType, changedTo ->
                    /*
                    onColorPendingChanged(
                        StyleColorPendingChanged(
                            style = StyleIdentifier.Background(index = index),
                            colorType = colorType,
                            changedTo = changedTo
                        )
                    )

                     */
                },
                onColorPendingChangeConfirmed = {
                    /*
                    onColorPendingChangeConfirmed(
                        StyleColorPendingChangeConfirmed(
                            style = StyleIdentifier.Background(index = index),
                        )
                    )

                     */
                }
            )
        }
    }
}

/**
@Composable
fun SectionColorBackgroundGradient(
colorList: List<ColorWrapper>,
colorsCount: State<Int>,
onColorChanged: (StyleColorChanged) -> Unit,
onColorAdded: (EditArtViewEvent.ArtMutatingEvent.StyleBackgroundColorAdded) -> Unit,
onColorRemoved: (EditArtViewEvent.ClickedRemoveGradientColor) -> Unit,
onColorPendingChangeConfirmed: (StyleColorPendingChangeConfirmed) -> Unit,
onColorPendingChanged: (StyleColorPendingChanged) -> Unit
) {
val lazyListState = rememberLazyListState()
LazyRow(
state = lazyListState,
modifier = Modifier
.padding(
bottom = if ((colorsCount.value >= EditArtViewState.MAX_GRADIENT_BG_COLORS)) {
spacing.medium
} else {
0.dp
}
)
.background(MaterialTheme.colors.background)
.padding(vertical = spacing.small),
horizontalArrangement = Arrangement.spacedBy(spacing.xSmall)
) {
item { Spacer(modifier = Modifier.width(spacing.xSmall)) }
items(colorsCount.value) {
val color = colorList[it]
ListItem(
index = it,
color = color,
colorsCount = colorsCount.value,
onColorChanged = onColorChanged,
onColorRemoved = onColorRemoved,
onColorPendingChangeConfirmed = onColorPendingChangeConfirmed,
onColorPendingChanged = onColorPendingChanged
)
}
item { Spacer(modifier = Modifier.width(spacing.xSmall)) }
}

if (colorsCount.value < EditArtViewState.MAX_GRADIENT_BG_COLORS) {
Column(
modifier = Modifier
.fillMaxWidth()
.padding(bottom = spacing.medium, end = spacing.medium),
horizontalAlignment = Alignment.End,
) {
Button(
emphasis = ButtonEmphasis.HIGH,
size = ButtonSize.MEDIUM,
text = stringResource(R.string.edit_art_style_background_gradient_add_color_button),
leadingIcon = Icons.Outlined.Add,
leadingIconContentDescription = stringResource(R.string.edit_art_style_background_gradient_add_color_button_cd)
) {
onColorAdded(EditArtViewEvent.ArtMutatingEvent.StyleBackgroundColorAdded)
}
}
}
}

 **/