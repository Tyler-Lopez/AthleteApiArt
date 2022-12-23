package com.company.activityart.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.company.activityart.BuildConfig
import com.company.activityart.domain.FileRepository
import com.company.activityart.util.Response
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    private val context: Context
) : FileRepository {

    override suspend fun saveBitmapToGallery(bitmap: Bitmap): Response<Unit> {
        return try {
            val fileName = System.currentTimeMillis().toString() + ".png"
            val resolver = context.contentResolver

            val uri: Uri? = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                val fileDir = context.getExternalFilesDir(DIRECTORY_PICTURES)!!.absolutePath

                /**
                 * https://stackoverflow.com/questions/8560501/android-save-image-into-gallery
                 * https://stackoverflow.com/questions/57726896/mediastore-images-media-insertimage-deprecated
                 */
                // blah do it here
                val values = initializeContentValues(fileName).apply {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, DIRECTORY_PICTURES)
                }
                resolver.insert(
                    MediaStore.Files.getContentUri("external"),
                    initializeContentValues(fileName).apply {
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            "$fileDir/ActivityArt"
                        )
                    }
                )
            } else {
                //      val cacheDir = File(context.cacheDir, "images")
                val fileDir = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES)
                val file = File(fileDir, fileName)
                /** MediaStore.Images.Media.DATA is deprecated in API 29 **/
                resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    initializeContentValues(fileName).apply {
                        put(MediaStore.Images.Media.DATA, file.absolutePath)
                    }
                )
            }

            uri!!.let {
                val outputStream = context.contentResolver.openOutputStream(it)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream?.flush()
                outputStream?.close()
                File(it.path!!)
            }

            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(exception = e)
        }
    }

    override suspend fun saveBitmapToCache(bitmap: Bitmap): Response<Uri> {
        return try {
            val imageFolder = File(context.cacheDir, "images")
            val file = File(imageFolder, "shared_image.png")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            Response.Success(FileProvider.getUriForFile(context, "com.company.activityart", file))
        } catch (e: Exception) {
            Response.Error(exception = e)
        }
    }

    private fun initializeContentValues(fileName: String): ContentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
    }
}