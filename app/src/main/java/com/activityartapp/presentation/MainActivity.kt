package com.activityartapp.presentation

import android.content.Intent
import android.content.Intent.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.activityartapp.architecture.Router
import com.activityartapp.data.cache.ActivitiesCache
import com.activityartapp.presentation.MainDestination.*
import com.activityartapp.presentation.MainViewEvent.LoadAuthentication
import com.activityartapp.presentation.MainViewState.Authenticated
import com.activityartapp.presentation.ui.theme.AthleteApiArtTheme
import com.activityartapp.util.NavArgSpecification.*
import com.activityartapp.util.Screen.*
import com.activityartapp.util.constants.TokenConstants.authUri
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@AndroidEntryPoint
class MainActivity : ComponentActivity(), Router<MainDestination> {

    private lateinit var navController: NavHostController

    @Inject
    lateinit var activitiesCache: ActivitiesCache

    @Inject
    lateinit var gson: Gson

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        // Push event to ViewModel to determine authentication
        val intentUri = intent.data.also { intent.data = null }
        super.onCreate(savedInstanceState)

        // Install Splash Screen before content is setContent
        val splashScreen = installSplashScreen()
        setContent {
            viewModel = hiltViewModel()

            viewModel.apply {
                /** Set splash screen to on while loading authentication **/
                splashScreen.setKeepOnScreenCondition {
                    viewState.value == null
                }
                /** Push event to [MainViewModel] to determine authentication **/
                LaunchedEffect(key1 = intentUri) {
                    println("Intent uri is $intentUri")
                    onEvent(LoadAuthentication(intentUri))
                }
                /** Set global nav controller for [routeTo] **/
                navController = rememberAnimatedNavController()


                viewState.collectAsState().value?.let {
                    val startScreen = if (it is Authenticated) {
                        Welcome.route
                    } else {
                        Login.route
                    }
                    AthleteApiArtTheme {
                        MainNavHost(
                            navController = navController,
                            startRoute = startScreen,
                            router = this@MainActivity,
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun routeTo(destination: MainDestination) {
        when (destination) {
            is ConnectWithStrava -> connectWithStrava()
            is NavigateAbout -> navigateAbout()
            is NavigateLoadActivities -> navigateLoadActivities(destination)
            is NavigateLogin -> navigateLogin()
            is NavigateError -> onNavigateError(destination)
            is NavigateEditArt -> navigateMakeArt(destination)
            is NavigateSaveArt -> navigateSaveArt(destination)
            is NavigateUp -> navigateUp()
            is ShareFile -> shareFile(destination)
        }
    }

    private fun connectWithStrava() {
        finish()
        println("Connecting with Strava, authUri is $authUri")
        startActivity(Intent(ACTION_VIEW, authUri))
    }

    private fun navigateAbout() {
        navController.navigate(About.route)
    }

    private fun navigateLoadActivities(destination: NavigateLoadActivities) {
        destination.apply {
            navController.navigate(
                route = LoadActivities.route
            )
        }
    }

    private fun navigateLogin() {
        navController.navigate(route = Login.route) {
            popUpTo(
                route = Welcome.route
                // todo identify what this was doing
                //+
                //     "?${athleteIdNavSpec.route}&${accessTokenNavSpec.route}"
            ) {
                inclusive = true
            }
        }
    }

    private fun onNavigateError(destination: NavigateError) {
        navController.navigate(
            route = Error.withArgs(
                args = arrayOf(
                    ErrorScreen to destination.errorScreenType.toString()
                )
            )
        ) {
            if (destination.clearNavigationHistory) {
                popUpTo(route = Welcome.route) {
                    inclusive = true
                }
            }
        }
    }

    private fun navigateMakeArt(destination: NavigateEditArt) {
        viewModel.onEvent(MainViewEvent.LoadedActivities)
        navController.navigate(EditArt.route) {
            destination.apply {
                if (fromLoad) {
                    popUpTo(route = LoadActivities.route) { inclusive = true }
                }
            }
        }
    }

    private fun navigateSaveArt(destination: NavigateSaveArt) {
        destination.apply {
            navController.navigate(
                route = SaveArt.withArgs(
                    args = arrayOf(
                        ActivityTypes to gson.toJson(activityTypes),
                        BackgroundType to backgroundType.toString(),
                        ColorActivitiesArgb to colorActivitiesArgb.toString(),
                        ColorsBackgroundArgb to gson.toJson(backgroundColorsArgb),
                        ColorFontArgb to colorFontArgb.toString(),
                        FilterDateAfterMs to filterAfterMs.toString(),
                        FilterDateBeforeMs to filterBeforeMs.toString(),
                        FilterDistanceLessThanMeters to filterDistanceLessThanMeters.toString(),
                        FilterDistanceMoreThanMeters to filterDistanceMoreThanMeters.toString(),
                        SizeHeightPx to sizeHeightPx.toString(),
                        SizeWidthPx to sizeWidthPx.toString(),
                        SortType to sortType.toString(),
                        SortDirectionType to sortDirectionType.toString(),
                        StrokeWidth to strokeWidthType.toString(),
                        TextLeft to (textLeft ?: ""),
                        TextCenter to (textCenter ?: ""),
                        TextRight to (textRight ?: ""),
                        TextFontAssetPath to textFontAssetPath,
                        TextFontSize to textFontSize.toString()
                    )
                )
            )
        }
    }

    private fun navigateUp() {
        navController.navigateUp()
    }

    private fun shareFile(shareFile: ShareFile) {
        val intent = Intent(ACTION_SEND)
        intent.putExtra(EXTRA_STREAM, shareFile.uri)
        intent.putExtra(EXTRA_TEXT, "I made this art from my activities with Activity Art!")
        intent.putExtra(EXTRA_SUBJECT, "Activity Art")
        intent.type = "image/png"
        val chooserIntent = createChooser(intent, "Share Via")
        chooserIntent.addFlags(FLAG_ACTIVITY_NEW_TASK) // Allows running this on Android 11 outside of Activity
        applicationContext.startActivity(chooserIntent)
    }
}