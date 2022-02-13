package com.company.athleteapiart.presentation.activity_visualize_screen

import android.content.Context
import android.graphics.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import com.company.athleteapiart.data.DistanceRule
import com.company.athleteapiart.data.remote.responses.Activity
import com.company.athleteapiart.util.AthleteActivities
import com.company.athleteapiart.util.meterToMiles
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sqrt

fun activitiesVisualizeCanvas(
    maxWidth: Int,
    context: Context,
    activities: List<Activity>
): Bitmap {

    val format = AthleteActivities.formatting
    val conditions = format.value.conditions
    val background = format.value.backgroundColor
    val actColor = format.value.activityColor

    // Create a bitmap which is a scaled representation of a 3420x4320 image
    val desiredWidth = 3420f
    val desiredHeight = 4320f
    val ratioMultiplier = desiredHeight / desiredWidth
    // Determine height of image given width
    val maxHeight = (maxWidth * ratioMultiplier).toInt()

    // Create a bitmap which will be drawn on by canvas
    val bitmap = Bitmap.createBitmap(
        maxWidth,
        maxHeight,
        Bitmap.Config.ARGB_8888
    )
    // Create canvas to draw on

    val canvas = Canvas(bitmap)
    val center = Offset(
        x = canvas.width / 2f,
        y = canvas.height / 2f
    )
    // Draw canvas background
    val backgroundColor = Paint()
    backgroundColor.color = Color
        .rgb(background.red, background.green, background.blue)
    canvas.drawRect(
        Rect(
            0,
            0,
            maxWidth,
            maxHeight
        ),
        backgroundColor
    )


    val x = maxWidth.times(0.9f)
    val y = maxHeight.times(if (true) 0.85f else 0.9f)
    val marginX = (maxWidth.times(0.1f)).div(2f)
    val marginY = (maxHeight.times(0.1f)).div(2f)

    val testPaint = Paint()
    testPaint.color = Color.CYAN
    canvas.drawRect(
        Rect(
            marginX.toInt(),
            marginY.toInt(),
            x.toInt() + marginX.toInt(),
            y.toInt() + marginY.toInt()
        ),
        testPaint
    )
    // https://math.stackexchange.com/questions/466198/algorithm-to-get-the-maximum-size-of-n-squares-that-fit-into-a-rectangle-with-a
//    val activityWidth  = desiredWidth / sqrt((area / activities.size).toDouble()).toFloat()

    val ratio = x / y
    val n = activities.size
    var colCount: Float = sqrt(n * ratio).toFloat()
    var rowCount: Float = n / colCount

    // Find option to fill whole height
    var numRowsFromHeight = ceil(rowCount)
    var numColsFromHeight = ceil(n / numRowsFromHeight)
    while (numRowsFromHeight * ratio < numColsFromHeight) {
        numRowsFromHeight++
        numColsFromHeight = ceil(n / numRowsFromHeight)
    }
    val sizeFromHeight = y / numRowsFromHeight

    // Find option to fill whole width
    var numColsFromWidth = ceil(colCount)
    var numRowsFromWidth = ceil(n / numColsFromWidth)
    while (numColsFromWidth < ratio * numRowsFromWidth) {
        numColsFromWidth++
        numRowsFromWidth = ceil(n / numColsFromWidth)
    }
    val sizeFromWidth = x / numColsFromWidth

    var activityWidth: Float
    if (sizeFromHeight < sizeFromWidth) {
        rowCount = numRowsFromWidth
        colCount = numColsFromWidth
        activityWidth = sizeFromWidth.toFloat()
    } else {
        rowCount = numRowsFromHeight
        colCount = numColsFromHeight
        activityWidth = sizeFromHeight.toFloat()
    }

    // Iterate through each activity, determining X and Y position
    val initialX = (x - (activityWidth * colCount)) / 2
    val initialY = (y - (activityWidth * rowCount)) / 2

    var xOffset = initialX + marginX
    var yOffset = initialY + marginY
    var column = colCount.toInt()

    var activityCount = 1

    for (activity in activities) {
        val summaryPolyline = activity.map.summary_polyline
        //if (activity.type != "Run") continue
      //  if (summaryPolyline == "null" || summaryPolyline == null) continue


        if (column == colCount.toInt()) {
            xOffset = initialX + marginX
            if (activityCount != 1) yOffset += activityWidth
            column = 0
        } else  {
            xOffset += activityWidth
        }

        val blankSpaces = ((rowCount * colCount) - activities.size).toInt()

        if (activityCount - 1 == (activities.size - (colCount.toInt() - blankSpaces))) {
                xOffset += ((blankSpaces * activityWidth / 2))
        }

        activityCount++
        column++

        // Decode Polyline into a List<LatLng>
        val latLngList = PolyUtil.decode(summaryPolyline)

        var top = Double.MAX_VALUE.times(-1.0)
        var bottom = Double.MAX_VALUE
        var left = Double.MAX_VALUE
        var right = Double.MAX_VALUE.times(-1.0)

        val normalizedLatLngList = mutableListOf<LatLng>()
        for (latLng in latLngList) {
            val lat = latLng.latitude
            val lng = latLng.longitude
            // Determine bounds
            if (lat > top) top = lat
            if (lat < bottom) bottom = lat
            if (lng < left) left = lng
            if (lng > right) right = lng
        }

        for (latLng in latLngList) {
            val normalX = latLng.longitude.minus((left.plus(right)).div(2.0))
            val normalY = latLng.latitude.minus((top.plus(bottom)).div(2.0)).times(-1.0)
            normalizedLatLngList.add(LatLng(normalY, normalX))
        }

        val heightNorm = top.minus(bottom)
        val widthNorm = right.minus(left)
        val largestSide = if (heightNorm < widthNorm) widthNorm else heightNorm


        val multiplier = (activityWidth.times(0.8f)).div(largestSide)

        val points = mutableListOf<Float>()

        for (normalLatLng in normalizedLatLngList) {
            // x
            points.add((normalLatLng.longitude.times(multiplier)).toFloat() + xOffset + (activityWidth / 2f))
            //   y
            points.add((normalLatLng.latitude.times(multiplier)).toFloat() + yOffset + (activityWidth / 2f))
        }

        val pointsPaint = Paint()

        val distance = activity.distance.meterToMiles()

        pointsPaint.color = Color.rgb(actColor.red, actColor.green, actColor.blue)


        // Are they any condition rules
        for (condition in conditions) {
            if (condition is DistanceRule) {
                if (condition.conditionMatched(distance as Comparable<Any>)) {
                    pointsPaint.color = Color.rgb(condition.color.red, condition.color.green, condition.color.blue)
                    break
                }
            }
        }

        pointsPaint.isAntiAlias = true
        pointsPaint.strokeCap = Paint.Cap.ROUND
        pointsPaint.style = Paint.Style.STROKE
        pointsPaint.strokeWidth = sqrt(maxWidth.toDouble() * maxHeight.toDouble()).toFloat() * 0.0035f

        val path = Path()
        path.setLastPoint(points[0], points[1])
        for (i in 2 until points.lastIndex step 2) {
            path.lineTo(points[i], points[i + 1])
        }
        canvas.drawPath(path, pointsPaint)
    }

    val textSize = y * 0.05f
    val textPaint = Paint()
    textPaint.textSize = textSize
    textPaint.color = Color.WHITE
    textPaint.typeface = Typeface.createFromAsset(context.assets, "maisonneue_demi.otf")
    canvas.drawText("REBECCA YURGENS", marginX, maxHeight.times(0.95f) - marginY + textSize, textPaint)
    textPaint.typeface = Typeface.createFromAsset(context.assets, "maisonneue_demi.otf")
    textPaint.color = Color.argb(100, 255, 255, 255)
    canvas.drawText("2022", maxWidth.times(0.9f) - (textSize * 4), maxHeight.times(0.95f) - marginY + textSize, textPaint)

    return bitmap
}