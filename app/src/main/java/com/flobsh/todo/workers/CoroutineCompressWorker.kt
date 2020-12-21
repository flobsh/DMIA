package com.flobsh.todo.workers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import java.io.File
import java.io.InputStream
import java.lang.IllegalArgumentException


class CoroutineCompressWorker(
        context: Context,
        params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val imagePath = inputData.getString("IMAGE_URI")
        if (imagePath.isNullOrEmpty()) {
            // Toast.makeText(applicationContext, "Failed to load image", Toast.LENGTH_LONG).show()
            return Result.failure()
        }
        var readStream: InputStream?
        try {
            readStream = applicationContext.contentResolver.openInputStream(imagePath.toUri())
        } catch (e: java.io.FileNotFoundException) {
            Log.e("Failed to Load image", "cant load :  $imagePath", e )
            Toast.makeText(applicationContext, "Failed to load $imagePath : File no found ", Toast.LENGTH_LONG).show()
            return Result.failure();
        }
        val original = BitmapFactory.decodeStream(readStream)
        val cropped = Bitmap.createBitmap(original, 0, 0, original.width, original.width)
        val resized = Bitmap.createScaledBitmap(cropped, 128, 128, false)
        lateinit var tmpFile: File
        try {
            tmpFile = File.createTempFile("resized", "png") ?: return Result.failure()
        } catch (e: java.io.IOException) {
            Log.e("Failed to create file:", "file name : " + "resized.png", e)
            Toast.makeText(applicationContext, "Failed to create compressed image file ", Toast.LENGTH_LONG).show()
            return Result.failure()
        } catch (e: IllegalArgumentException) {
            Log.e("Failed to create file:", "IllegalArgument prefix should have more than 3 char ", e)
            Toast.makeText(applicationContext, "Failed to create compressed image file ", Toast.LENGTH_LONG).show()
            return Result.failure()
        } catch (e: SecurityException) {
            Log.e("Failed to create file:", "Security Exception : right not allowed on this folder", e)
        }

        tmpFile.outputStream().use {
            resized.compress(Bitmap.CompressFormat.PNG, 50, it)
        }

        val outputData = Data.Builder()
                .putString("IMAGE_URI", tmpFile.toUri().toString())
                .build()

        Toast.makeText(applicationContext, "Compression succeed ", Toast.LENGTH_LONG).show()
        return Result.success(outputData)
    }
}