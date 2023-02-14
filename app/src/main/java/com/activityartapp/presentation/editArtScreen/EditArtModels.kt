@file:OptIn(ExperimentalPagerApi::class)

package com.activityartapp.presentation.editArtScreen

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.room.Ignore
import com.activityartapp.R
import com.activityartapp.architecture.ViewEvent
import com.activityartapp.architecture.ViewState
import com.activityartapp.domain.models.ResolutionListFactory
import com.activityartapp.presentation.editArtScreen.subscreens.resize.ResolutionListFactoryImpl
import com.activityartapp.presentation.editArtScreen.subscreens.type.EditArtTypeSection
import com.activityartapp.presentation.editArtScreen.subscreens.type.EditArtTypeType
import com.activityartapp.util.enums.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import javax.inject.Inject
import kotlin.math.roundToInt

annotation class UnixMS

sealed interface EditArtViewEvent : ViewEvent {

    object ClickedInfoCheckeredBackground : EditArtViewEvent
    object ClickedInfoTransparentBackground : EditArtViewEvent
    object DialogDismissed : EditArtViewEvent
    object DialogNavigateUpConfirmed : EditArtViewEvent
    sealed interface FilterDistancePendingChange : EditArtViewEvent {
        val changedTo: String

        data class FilterDistancePendingChangeShortest(override val changedTo: String) :
            FilterDistancePendingChange

        data class FilterDistancePendingChangeLongest(override val changedTo: String) :
            FilterDistancePendingChange
    }

    object NavigateUpClicked : EditArtViewEvent
    data class PageHeaderClicked(val position: Int) : EditArtViewEvent
    object SaveClicked : EditArtViewEvent

    sealed interface ArtMutatingEvent : EditArtViewEvent {

        sealed interface FilterChanged : ArtMutatingEvent {
            val filterType: EditArtFilterType

            data class FilterDateSelectionChanged(val index: Int) : FilterChanged {
                override val filterType: EditArtFilterType
                    get() = EditArtFilterType.DATE
            }

            sealed interface FilterDateCustomChanged : FilterChanged {
                val changedTo: Long
                override val filterType: EditArtFilterType
                    get() = EditArtFilterType.DATE

                data class FilterAfterChanged(@UnixMS override val changedTo: Long) :
                    FilterDateCustomChanged

                data class FilterBeforeChanged(@UnixMS override val changedTo: Long) :
                    FilterDateCustomChanged
            }

            data class FilterDistanceChanged(val changedTo: ClosedFloatingPointRange<Double>) :
                FilterChanged {
                override val filterType: EditArtFilterType
                    get() = EditArtFilterType.DISTANCE
            }

            sealed interface FilterDistancePendingChangeConfirmed : FilterChanged {
                object StartConfirmed : FilterDistancePendingChangeConfirmed {
                    override val filterType: EditArtFilterType
                        get() = EditArtFilterType.DISTANCE
                }
                object EndConfirmed : FilterDistancePendingChangeConfirmed {
                    override val filterType: EditArtFilterType
                        get() = EditArtFilterType.DISTANCE
                }
            }

            data class FilterTypeToggled(val type: String) : FilterChanged {
                override val filterType: EditArtFilterType
                    get() = EditArtFilterType.TYPE
            }
        }

        data class SizeChanged(val changedIndex: Int) : ArtMutatingEvent
        sealed interface SizeCustomChanged : ArtMutatingEvent {
            val changedToPx: Int

            data class HeightChanged(override val changedToPx: Int) : SizeCustomChanged
            data class WidthChanged(override val changedToPx: Int) : SizeCustomChanged
        }

        data class SizeRotated(val rotatedIndex: Int) : ArtMutatingEvent
        data class SortDirectionChanged(val changedTo: EditArtSortDirectionType) : ArtMutatingEvent
        data class SortTypeChanged(val changedTo: EditArtSortType) : ArtMutatingEvent
        data class StyleBackgroundTypeChanged(val changedTo: BackgroundType) : ArtMutatingEvent
        data class StyleColorActivitiesChanged(
            val colorType: ColorType,
            val changedTo: Float
        ) : ArtMutatingEvent

        data class StyleColorsBackgroundChanged(
            val changedIndex: Int,
            val changedColorType: ColorType,
            val changedTo: Float
        ) : ArtMutatingEvent

        data class StyleColorFontChanged(
            val colorType: ColorType,
            val changedTo: Float
        ) : ArtMutatingEvent

        data class StyleColorFontUseCustomChanged(val useCustom: Boolean) : ArtMutatingEvent

        data class StylesStrokeWidthChanged(val changedTo: StrokeWidthType) : ArtMutatingEvent
        data class TypeCustomTextChanged(
            val section: EditArtTypeSection,
            val changedTo: String
        ) : ArtMutatingEvent

        data class TypeFontChanged(val changedTo: FontType) : ArtMutatingEvent
        data class TypeFontWeightChanged(val changedTo: FontWeightType) : ArtMutatingEvent
        data class TypeFontItalicChanged(val changedTo: Boolean) : ArtMutatingEvent
        data class TypeFontSizeChanged(val changedTo: FontSizeType) : ArtMutatingEvent
        data class TypeSelectionChanged(
            val section: EditArtTypeSection,
            val typeSelected: EditArtTypeType
        ) : ArtMutatingEvent
    }
}

sealed interface EditArtViewState : ViewState {

    companion object {
        private const val FADE_LENGTH_MS = 1000
    }

    val dialogActive: EditArtDialogType
    val pagerStateWrapper: PagerStateWrapper

    data class Loading(
        override val dialogActive: EditArtDialogType = EditArtDialogType.NONE,
        override val pagerStateWrapper: PagerStateWrapper = PagerStateWrapper(
            pagerHeaders = EditArtHeaderType.values().toList(),
            pagerState = PagerState(EditArtHeaderType.values().toList().size),
            fadeLengthMs = FADE_LENGTH_MS // todo
        )
    ) : EditArtViewState

    /**
     * @param styleActivities The color of the activities on the art.
     * @param styleBackground The color of the background of the art.
     * @param styleFont The color of text on the art. When set to null, [styleActivities] determines
     * the color of the text.
     */
    @Parcelize
    data class Standby(
        @IgnoredOnParcel val bitmap: Bitmap? = null,
        @IgnoredOnParcel override val dialogActive: EditArtDialogType = EditArtDialogType.NONE,
        val filterActivitiesCountDate: Int = 0,
        val filterActivitiesCountDistance: Int = 0,
        val filterActivitiesCountType: Int = 0,
        val filterDateSelections: List<DateSelection>? = null,
        val filterDateSelectionIndex: Int = INIT_SELECTION_INDEX,
        val filterDistanceSelectedStart: Double? = null,
        val filterDistanceSelectedEnd: Double? = null,
        val filterDistanceTotalStart: Double? = null,
        val filterDistanceTotalEnd: Double? = null,
        @IgnoredOnParcel val filterDistancePendingChangeStart: String? = null,
        @IgnoredOnParcel val filterDistancePendingChangeEnd: String? = null,
        val filterTypes: Map<String, Boolean>? = null,
        @IgnoredOnParcel override val pagerStateWrapper: PagerStateWrapper = PagerStateWrapper(
            pagerHeaders = EditArtHeaderType.values().toList(),
            pagerState = PagerState(EditArtHeaderType.values().toList().size),
            fadeLengthMs = FADE_LENGTH_MS
        ),
        @IgnoredOnParcel val scrollStateFilter: ScrollState = ScrollState(INITIAL_SCROLL_STATE),
        @IgnoredOnParcel val scrollStateStyle: ScrollState = ScrollState(INITIAL_SCROLL_STATE),
        @IgnoredOnParcel val scrollStateType: ScrollState = ScrollState(INITIAL_SCROLL_STATE),
        @IgnoredOnParcel val scrollStateResize: ScrollState = ScrollState(INITIAL_SCROLL_STATE),
        @IgnoredOnParcel val scrollStateSort: ScrollState = ScrollState(INITIAL_SCROLL_STATE),
        val sizeResolutionList: List<Resolution> = ResolutionListFactoryImpl().create(),
        val sizeResolutionListSelectedIndex: Int = INITIAL_SELECTED_RES_INDEX,
        @IgnoredOnParcel val sizeCustomMaxPx: Int = CUSTOM_SIZE_MAXIMUM_PX,
        @IgnoredOnParcel val sizeCustomMinPx: Int = CUSTOM_SIZE_MINIMUM_PX,
        @IgnoredOnParcel val sizeCustomOutOfBoundsWidth: Int? = null,
        @IgnoredOnParcel val sizeCustomOutOfBoundsHeight: Int? = null,
        val sortTypeSelected: EditArtSortType = EditArtSortType.DATE,
        val sortDirectionTypeSelected: EditArtSortDirectionType = EditArtSortDirectionType.ASCENDING,
        val styleActivities: ColorWrapper = ColorWrapper(
            alpha = INIT_ACTIVITIES_ALPHA,
            blue = INIT_ACTIVITIES_BLUE,
            green = INIT_ACTIVITIES_GREEN,
            red = INIT_ACTIVITIES_RED
        ),
        val styleBackgroundType: BackgroundType = BackgroundType.SOLID,
        val styleBackgroundColors: List<ColorWrapper> = listOf(
            ColorWrapper(
                alpha = INIT_BACKGROUND_ALPHA,
                blue = INIT_BACKGROUND_BLUE,
                green = INIT_BACKGROUND_GREEN,
                red = INIT_BACKGROUND_RED
            )
        ),
        val styleFont: ColorWrapper? = null,
        val styleStrokeWidthType: StrokeWidthType = INIT_STROKE_WIDTH,
        val typeActivitiesDistanceMetersSummed: Int = -1,
        val typeFontSelected: FontType = INIT_TYPE_FONT_SELECTION,
        val typeFontWeightSelected: FontWeightType = INIT_TYPE_FONT_WEIGHT_SELECTION,
        val typeFontItalicized: Boolean = INIT_TYPE_IS_ITALICIZED,
        val typeFontSizeSelected: FontSizeType = INIT_TYPE_FONT_SIZE_SELECTION,
        @IgnoredOnParcel val typeMaximumCustomTextLength: Int = CUSTOM_TEXT_MAXIMUM_LENGTH,
        val typeLeftSelected: EditArtTypeType = INIT_TYPE_TYPE,
        val typeLeftCustomText: String = INIT_TYPE_CUSTOM_TEXT,
        val typeCenterSelected: EditArtTypeType = INIT_TYPE_TYPE,
        val typeCenterCustomText: String = INIT_TYPE_CUSTOM_TEXT,
        val typeRightSelected: EditArtTypeType = INIT_TYPE_TYPE,
        val typeRightCustomText: String = INIT_TYPE_CUSTOM_TEXT,
    ) : EditArtViewState, Parcelable {

        companion object {
            private const val CUSTOM_SIZE_MINIMUM_PX = 100
            private const val CUSTOM_SIZE_MAXIMUM_PX = 12000

            private const val INITIAL_SCROLL_STATE = 0
            private const val INITIAL_SELECTED_RES_INDEX = 0

            /** Initial Style settings **/
            private const val INIT_ACTIVITIES_ALPHA = 1f
            private const val INIT_ACTIVITIES_BLUE = 1f
            private const val INIT_ACTIVITIES_GREEN = 1f
            private const val INIT_ACTIVITIES_RED = 1f
            private const val INIT_BACKGROUND_ALPHA = 1f
            private const val INIT_BACKGROUND_BLUE = 0f
            private const val INIT_BACKGROUND_GREEN = 0f
            private const val INIT_BACKGROUND_RED = 0f
            private val INIT_STROKE_WIDTH = StrokeWidthType.MEDIUM

            /** Initial Type settings **/
            private const val CUSTOM_TEXT_MAXIMUM_LENGTH = 30
            private const val INIT_TYPE_CUSTOM_TEXT = ""
            private val INIT_TYPE_FONT_SELECTION = FontType.JOSEFIN_SANS
            private val INIT_TYPE_FONT_WEIGHT_SELECTION = FontWeightType.REGULAR
            private val INIT_TYPE_FONT_SIZE_SELECTION = FontSizeType.MEDIUM
            private const val INIT_TYPE_IS_ITALICIZED = false
            private val INIT_TYPE_TYPE = EditArtTypeType.NONE

            private const val NO_ACTIVITIES_COUNT = 0

            private const val INIT_SELECTION_INDEX = 0
        }

        @Inject
        @IgnoredOnParcel
        lateinit var resolutionListFactory: ResolutionListFactory

        @IgnoredOnParcel
        val atLeastOneActivitySelected
            get() = minOf(
                filterActivitiesCountDate,
                filterActivitiesCountDistance,
                filterActivitiesCountType
            ) != NO_ACTIVITIES_COUNT

        @IgnoredOnParcel
        val filterDateSelectionUnset = filterDateSelectionIndex == INIT_SELECTION_INDEX

        @IgnoredOnParcel
        val filteredTypes: List<String>
            get() = filterTypes?.entries?.filter { it.value }?.map { it.key } ?: emptyList()

        val filteredDistanceRangeMeters: IntRange
            get() = (filterDistanceSelectedStart
                ?.roundToInt()
                ?: Int.MIN_VALUE)
                .rangeTo(
                    filterDistanceSelectedEnd
                        ?.roundToInt()
                        ?: Int.MAX_VALUE
                )

    }
}

@Parcelize
data class PagerStateWrapper(
    val pagerHeaders: List<EditArtHeaderType>,
    val pagerState: @RawValue PagerState, // Todo, evaluate whether RawValue works
    val fadeLengthMs: Int,
) : Parcelable

@Parcelize
data class FilterStateWrapper(
    val excludedActivityTypes: Set<String> = setOf(),
    val unixSecondSelectedStart: Long,
    val unixSecondSelectedEnd: Long
) : Parcelable

@Parcelize
data class ColorWrapper(
    val alpha: Float,
    val blue: Float,
    val green: Float,
    val red: Float,
    @IgnoredOnParcel val outOfBoundsAlpha: Float? = null,
    @IgnoredOnParcel val outOfBoundsBlue: Float? = null,
    @IgnoredOnParcel val outOfBoundsGreen: Float? = null,
    @IgnoredOnParcel val outOfBoundsRed: Float? = null
) : Parcelable {

    companion object {
        private const val RATIO_LIMIT_LOWER = 0f
        private const val RATIO_LIMIT_UPPER = 1f
        private const val EIGHT_BIT_LIMIT_LOWER = 0
        private const val EIGHT_BIT_LIMIT_UPPER = 255

        fun ratioToEightBit(ratio: Float): Int {
            return (ratio * EIGHT_BIT_LIMIT_UPPER).toInt()
        }

        fun eightBitToRatio(eightBit: Int): Float {
            return (eightBit / EIGHT_BIT_LIMIT_UPPER.toFloat())
        }

        val RATIO_RANGE = RATIO_LIMIT_LOWER..RATIO_LIMIT_UPPER
        val EIGHT_BIT_RANGE = EIGHT_BIT_LIMIT_LOWER..EIGHT_BIT_LIMIT_UPPER
    }

    val color get() = Color(red, green, blue, alpha)
    val redAsEightBit get() = ratioToEightBit(red)
    val greenAsEightBit get() = ratioToEightBit(green)
    val blueAsEightBit get() = ratioToEightBit(blue)
}

sealed interface DateSelection : Parcelable {

    @Parcelize
    object All : DateSelection

    @Parcelize
    data class Year(val year: Int) : DateSelection

    @Parcelize
    data class Custom(
        val dateSelectedStartUnixMs: Long,
        val dateSelectedEndUnixMs: Long,
        val dateTotalStartUnixMs: Long,
        val dateTotalEndUnixMs: Long
    ) : DateSelection {
        @IgnoredOnParcel
        val dateSelected: LongProgression
            get() = dateSelectedStartUnixMs..dateSelectedEndUnixMs

        @IgnoredOnParcel
        val dateTotal: LongProgression
            get() = dateTotalStartUnixMs..dateTotalEndUnixMs
    }
}

enum class ColorType(@StringRes val strRes: Int) {
    RED(R.string.edit_art_style_color_red),
    GREEN(R.string.edit_art_style_color_green),
    BLUE(R.string.edit_art_style_color_blue),
    ALPHA(R.string.edit_art_style_color_alpha)
}

enum class StrokeWidthType(val headerId: Int) {
    THIN(R.string.edit_art_style_stroke_thin),
    MEDIUM(R.string.edit_art_style_stroke_medium),
    THICK(R.string.edit_art_style_stroke_thick);
}


sealed interface Resolution : Parcelable {

    val widthPx: Int
    val heightPx: Int
    val stringResourceId: Int

    @Composable
    fun displayTextResolution(): String

    companion object {
        private const val DEFAULT_CUSTOM_WIDTH_PX = 1000
        private const val DEFAULT_CUSTOM_HEIGHT_PX = 1000
    }

    interface RotatingResolution : Resolution {
        val isRotated: Boolean
        val swappingChangesSize: Boolean
            get() = widthPx != heightPx
        val origWidthPx: Int
        val origHeightPx: Int

        override val heightPx: Int
            get() = if (isRotated) origWidthPx else origHeightPx

        override val widthPx: Int
            get() = if (isRotated) origHeightPx else origWidthPx

        fun copyWithRotation(): RotatingResolution

        @Composable
        fun displayTextPixels(): String {
            return stringResource(
                R.string.edit_art_resize_pixels_placeholder,
                if (isRotated) origHeightPx else origWidthPx,
                if (isRotated) origWidthPx else origHeightPx,
            )
        }
    }

    @Parcelize
    data class ComputerResolution(
        override val stringResourceId: Int,
        override val origWidthPx: Int,
        override val origHeightPx: Int,
        override val isRotated: Boolean = false
    ) : RotatingResolution {
        @Composable
        override fun displayTextResolution(): String {
            return stringResource(stringResourceId)
        }

        override fun copyWithRotation(): RotatingResolution {
            return copy(isRotated = !isRotated)
        }
    }

    @Parcelize
    data class PrintResolution(
        override val origWidthPx: Int,
        override val origHeightPx: Int,
        val widthMeasurementValue: Int,
        val heightMeasurementValue: Int,
        override val isRotated: Boolean = false
    ) : RotatingResolution {

        companion object {
            private const val MEASUREMENT_UNIT: String = "\""
        }

        @Composable
        override fun displayTextResolution(): String {
            return stringResource(
                stringResourceId,
                if (isRotated) heightMeasurementValue else widthMeasurementValue,
                MEASUREMENT_UNIT,
                if (isRotated) widthMeasurementValue else heightMeasurementValue,
                MEASUREMENT_UNIT
            )
        }

        override fun copyWithRotation(): RotatingResolution {
            return copy(isRotated = !isRotated)
        }

        override val stringResourceId: Int
            get() = R.string.edit_art_resize_option_print
    }

    @Parcelize
    data class CustomResolution(
        override val widthPx: Int = DEFAULT_CUSTOM_WIDTH_PX,
        override val heightPx: Int = DEFAULT_CUSTOM_HEIGHT_PX,
    ) : Resolution {

        override val stringResourceId: Int
            get() = R.string.edit_art_resize_option_custom

        @Composable
        override fun displayTextResolution(): String {
            return stringResource(stringResourceId)
        }
    }
}
