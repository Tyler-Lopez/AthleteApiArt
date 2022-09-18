package com.company.activityart.presentation.edit_art_screen.subscreens.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.company.activityart.architecture.EventReceiver
import com.company.activityart.presentation.edit_art_screen.subscreens.filters.composables.FilterSectionActivityType
import com.company.activityart.presentation.edit_art_screen.subscreens.filters.composables.FilterSectionDistances
import com.company.activityart.util.classes.YearMonthDay

@Composable
fun EditArtFiltersStandby(
    dateMaxDateUnixMilliseconds: Long,
    dateMinDateUnixMilliSeconds: Long,
    dateYearMonthDayAfter: YearMonthDay,
    dateYearMonthDayBefore: YearMonthDay,
    typesWithSelectedFlag: Map<String, Boolean>,
    eventReceiver: EventReceiver<EditArtFiltersViewEvent>
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterSectionDate(
            dateMaxDateUnixMilliseconds = dateMaxDateUnixMilliseconds,
            dateMinDateUnixMilliSeconds = dateMinDateUnixMilliSeconds,
            dateYearMonthDayAfter = dateYearMonthDayAfter,
            dateYearMonthDayBefore = dateYearMonthDayBefore,
            eventReceiver = eventReceiver
        )
        FilterSectionActivityType(
            typesWithSelectedFlag = typesWithSelectedFlag,
            eventReceiver = eventReceiver
        )
        FilterSectionDistances()

    }
}