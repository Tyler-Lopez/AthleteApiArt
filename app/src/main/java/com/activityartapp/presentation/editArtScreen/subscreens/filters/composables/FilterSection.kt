package com.activityartapp.presentation.editArtScreen.subscreens.filters.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.activityartapp.R
import com.activityartapp.presentation.editArtScreen.composables.Section

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColumnScope.FilterSection(
    count: Int,
    description: String,
    header: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Section(
        header = header,
        description = description,
    ) {
        content()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = count.takeIf { it > 0 }?.let {
                    pluralStringResource(
                        id = R.plurals.edit_art_filters_activities_filtered,
                        count = count,
                        count
                    )
                } ?: stringResource(id = R.string.edit_art_filters_activities_filtered_zero),
                style = MaterialTheme.typography.caption,
                color = if (count > 0) {
                    MaterialTheme.colors.primary
                } else {
                    MaterialTheme.colors.error
                }
            )
        }
    }
}