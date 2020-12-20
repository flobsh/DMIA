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

        val readStream = applicationContext.contentResolver.openInputStream(imagePath.toUri())
        val original = BitmapFactory.decodeStream(readStream)
        val cropped = Bitmap.createBitmap(original, 0, 0, original.width, original.width)
        val resized = Bitmap.createScaledBitmap(cropped, 128, 128, false)

        val tmpFile = File.createTempFile("resized", "png")
        tmpFile.outputStream().use {
            resized.compress(Bitmap.CompressFormat.PNG, 50, it)
        }

        val outputData = Data.Builder()
                .putString("IMAGE_URI", tmpFile.toUri().toString())
                .build()

        return Result.success(outputData)
    }
}