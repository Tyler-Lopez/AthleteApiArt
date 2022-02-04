package com.company.athleteapiart.presentation.filter_activities_screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.company.athleteapiart.Screen
import com.company.athleteapiart.presentation.composable.*
import com.company.athleteapiart.ui.spacing
import com.company.athleteapiart.ui.theme.*
import com.company.athleteapiart.util.AthleteActivities
import com.company.athleteapiart.util.monthFromIso8601
import com.company.athleteapiart.util.navigateAndReplaceStartRoute

@Composable
fun FilterActivitiesScreen(
    navController: NavHostController,
    viewModel: FilterActivitiesViewModel = hiltViewModel()
) {
    val isLoading by remember { viewModel.isLoading }
    val minDistanceSlider by remember { viewModel.minDistanceSlider }
    val maxDistanceSlider by remember { viewModel.maxDistanceSlider }

    val spacingMd = MaterialTheme.spacing.md

    // We cleared the stack prior to opening this screen
    // Ensure if you push back it goes back to selecting a year and does not close the app
    BackHandler {
        navController.backQueue.clear()
        navController.navigate(Screen.TimeSelect.route)
    }

    Scaffold(
        topBar = {
            ComposableTopBar(
                leftContent = {
                    ComposableReturnButton {
                        navController.backQueue.clear()
                        navController.navigate(Screen.TimeSelect.route)
                    }
                },
                rightContent = {
                    ComposableSubtext(
                        text = "Filters",
                        color = Color.White
                    )
                }
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            color = MaterialTheme.colors.primary
                        )
                        Row(
                            modifier = Modifier
                                .padding(vertical = 10.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            ComposableHeader(
                                text = "Processing",
                                isBold = true
                            )
                        }
                    }
                    else -> {
                        ComposableScreenWrapper(
                            // Create room for large button
                            modifier = Modifier.padding(bottom = 75.dp)
                        ) {
                            ComposableParagraph(
                                text = "Exclude certain activities prior to visualization if desired",
                                modifier = Modifier.padding(bottom = MaterialTheme.spacing.md)
                            )
                            ComposableShadowBox {
                                Column {
                                    ComposableItemContainer {
                                        ComposableHeader(
                                            text = "Months",
                                            color = StravaOrange,
                                            isBold = true,
                                            modifier = Modifier.padding(
                                                start = MaterialTheme.spacing.xxs,
                                                bottom = MaterialTheme.spacing.sm
                                            )
                                        )
                                        for (month in viewModel.months.reversed()) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = viewModel
                                                        .checkboxes
                                                        .getOrDefault(
                                                            month,
                                                            false
                                                        ),
                                                    onCheckedChange = {
                                                        viewModel.flipValue(month)
                                                    })
                                                ComposableParagraph(text = month)
                                            }
                                        }
                                    }
                                    ComposableItemContainer(
                                        modifier = Modifier.padding(vertical = MaterialTheme.spacing.sm)
                                    ) {
                                        ComposableHeader(
                                            text = "Activity Types",
                                            color = StravaOrange,
                                            isBold = true,

                                            modifier = Modifier.padding(
                                                start = MaterialTheme.spacing.xxs,
                                                bottom = MaterialTheme.spacing.sm
                                            )

                                        )
                                        for (activity in viewModel.activityTypes) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = viewModel
                                                        .checkboxes
                                                        .getOrDefault(
                                                            activity,
                                                            false
                                                        ),
                                                    onCheckedChange = {
                                                        viewModel.flipValue(activity)
                                                    })
                                                ComposableParagraph(text = activity)
                                            }
                                        }
                                    }
                                    ComposableItemContainer {
                                        ComposableHeader(
                                            text = "Distance",
                                            color = StravaOrange,
                                            isBold = true,
                                            modifier = Modifier.padding(
                                                start = MaterialTheme.spacing.xxs,
                                                bottom = MaterialTheme.spacing.sm
                                            )

                                        )
                                        val valueRange =
                                            viewModel.minimumDistance..viewModel.maximumDistance
                                        ComposableDistanceSlider(
                                            header = "Min",
                                            value = minDistanceSlider,
                                            valueRange = valueRange,
                                            onValueChange = {
                                                if (it < maxDistanceSlider)
                                                    viewModel.setMinDistanceSlider(it)
                                                else viewModel.setMinDistanceSlider(maxDistanceSlider)

                                            }
                                        )
                                        ComposableDistanceSlider(
                                            header = "Max",
                                            value = maxDistanceSlider,
                                            valueRange = valueRange,
                                            onValueChange = {
                                                if (it > minDistanceSlider)
                                                    viewModel.setMaxDistanceSlider(it)
                                                else viewModel.setMaxDistanceSlider(minDistanceSlider)
                                            }
                                        )
                                    }
                                    Spacer(
                                        modifier = Modifier.height(250.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            ComposableLargeButton(
                text = "Continue",
                onClick = {

                    AthleteActivities.filteredActivities.value.clear()

                    for (activity in AthleteActivities.activities.value) {
                        // Filter undesired months, activities, and distances
                        val activityMonth = activity.start_date_local.monthFromIso8601()
                        val activityType = activity.type
                        if (
                        // Month check
                            !(viewModel.checkboxes.getOrDefault(activityMonth, false)) ||
                            // Type check
                            !(viewModel.checkboxes.getOrDefault(activityType, false)) ||
                            // Distance check
                            activity.distance < viewModel.minDistanceSlider.value ||
                            activity.distance > viewModel.maxDistanceSlider.value
                        ) {
                            continue
                        }
                        AthleteActivities.filteredActivities.value.add(activity)
                    }
                    navController.navigate("${Screen.FormatActivitiesOne}")
                }
            )
        })
}