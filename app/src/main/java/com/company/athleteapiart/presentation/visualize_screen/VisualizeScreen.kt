package com.company.athleteapiart.presentation.visualize_screen

import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.company.athleteapiart.presentation.visualize_screen.VisualizeScreenState.*

@Composable
fun VisualizeScreen(
    athleteId: Long,
    yearMonths: Array<Pair<Int, Int>>,
    navController: NavHostController,
    activityTypes: Array<String>? = null, // If null then do not filter by activityTypes
    gears: Array<String?>? = null, // If null do not filter, if string is null then that means null gearId is included
    distances: ClosedFloatingPointRange<Float>? = null,
    viewModel: VisualizeScreenViewModel = hiltViewModel()
) {

    val screenState by remember { viewModel.screenState }
    val context = LocalContext.current
    val bitmap by remember { viewModel.bitmap }
    when (screenState) {
        LAUNCH -> {
            println("here in launch")
            SideEffect {
                viewModel.loadActivities(
                    context = context,
                    athleteId = athleteId,
                    yearMonths = yearMonths,
                    activityTypes = activityTypes,
                    gears = gears,
                    distances = distances
                )
            }
        }
        LOADING -> {

        }
        PERMISSION_ACCEPTED -> {

        }
        STANDBY -> {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                val maxWidth = this.maxWidth
                val backgroundPaint = Paint().also {
                    it.color = android.graphics.Color.WHITE
                }
                val activityPaint = Paint().also {
                    it.color = android.graphics.Color.parseColor("#fc4c02")
                }
                Card(elevation = 4.dp) {
                    VisualizeImage(
                        bitmap = visualizeBitmap(
                            deviceWidth = LocalDensity.current.run { maxWidth.roundToPx() },
                            height = 1080f,
                            width = 1920f,
                            activities = viewModel.activities,
                            backgroundPaint = backgroundPaint,
                            activityPaint = activityPaint
                        )
                    )
                }
            }
        }
    }
}