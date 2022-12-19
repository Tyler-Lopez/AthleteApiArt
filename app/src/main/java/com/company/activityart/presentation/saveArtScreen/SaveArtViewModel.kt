package com.company.activityart.presentation.saveArtScreen

import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import android.util.Size
import androidx.core.graphics.scale
import com.company.activityart.BuildConfig
import com.company.activityart.architecture.BaseRoutingViewModel
import com.company.activityart.domain.FileRepository
import com.company.activityart.domain.use_case.activities.GetActivitiesFromCacheUseCase
import com.company.activityart.presentation.MainDestination
import com.company.activityart.presentation.MainDestination.*
import com.company.activityart.presentation.editArtScreen.StrokeWidthType
import com.company.activityart.presentation.saveArtScreen.SaveArtViewState.*
import com.company.activityart.presentation.saveArtScreen.SaveArtViewEvent.*
import com.company.activityart.util.ImageSizeUtils
import com.company.activityart.util.NavArgSpecification.*
import com.company.activityart.util.VisualizationUtils
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class SaveArtViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    activitiesFromCacheUseCase: GetActivitiesFromCacheUseCase,
    gson: Gson,
    imageSizeUtils: ImageSizeUtils,
    ssh: SavedStateHandle,
    visualizationUtils: VisualizationUtils
) : BaseRoutingViewModel<SaveArtViewState, SaveArtViewEvent, MainDestination>() {

    private val activityTypes = gson.fromJson<List<String>>(
        ActivityTypes.rawArg(ssh),
        List::class.java
    )
    private val activities = activitiesFromCacheUseCase()
    private val colorActivitiesArgb = ColorActivitiesArgb.rawArg(ssh).toInt()
    private val colorBackgroundArgb = ColorBackgroundArgb.rawArg(ssh).toInt()
    private val filterDateAfterMs = FilterDateAfterMs.rawArg(ssh).toLong()
    private val filterDateBeforeMs = FilterDateBeforeMs.rawArg(ssh).toLong()
    private val sizeHeightPx = SizeHeightPx.rawArg(ssh).toInt()
    private val sizeWidthPx = SizeWidthPx.rawArg(ssh).toInt()
    private val strokeWidthType = StrokeWidthType.valueOf(StrokeWidth.rawArg(ssh))

    init {
        Loading.push()
        viewModelScope.launch(Dispatchers.Default) {
            println("here, sizes are $sizeHeightPx, $sizeWidthPx")
            val bitmap = visualizationUtils.createBitmap(
                activities = activities,
                colorActivitiesArgb = colorActivitiesArgb,
                colorBackgroundArgb = colorBackgroundArgb,
                bitmapSize = Size(sizeWidthPx, sizeHeightPx),
                strokeWidthType = strokeWidthType
            )
            val newSize = imageSizeUtils.sizeToMaximumSize(
                Size(sizeWidthPx, sizeHeightPx),
                Size(1000, 1000),
            )
            val scaledBitmap = bitmap.scale(newSize.width, newSize.height)
            Standby(scaledBitmap).push()
        }
    }


    override fun onEvent(event: SaveArtViewEvent) {
        when (event) {
            is ClickedDownload -> onClickedDownload()
            is ClickedNavigateUp -> onClickedNavigateUp()
            is ClickedShare -> onClickedShare()
        }
    }

    private fun onClickedDownload() {
        (lastPushedState as? Standby)?.copyDownloadStart()?.push()
        viewModelScope.launch(Dispatchers.IO) {

            (lastPushedState as? Standby)?.run {
                fileRepository.saveBitmap(bitmap)
            }
            delay(3000)
            (lastPushedState as? Standby)?.copyDownloadTerminate()?.push()
        }
    }



    private fun onClickedNavigateUp() {
        viewModelScope.launch {
            routeTo(NavigateUp)
        }
    }

    private fun onClickedShare() {

    }
}