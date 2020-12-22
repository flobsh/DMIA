package com.flobsh.todo.workers

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import java.io.File
import java.io.InputStream


class CoroutineSepiaFilterWorker(
        context: Context,
        params: WorkerParameters
) : CoroutineWorker(context, params) {
    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun doWork(): Result {
        //get the imput file
        val imagePath = inputData.getString("IMAGE_URI")
        if (imagePath.isNullOrEmpty()) {
            // Toast.makeText(applicationContext, "Failed to load image", Toast.LENGTH_LONG).show()
            return Result.failure()
        }
        var readStream: InputStream?
        try {
            readStream = applicationContext.contentResolver.openInputStream(imagePath.toUri())
        } catch (e: java.io.FileNotFoundException) {
            Log.e("Failed to Load image", "cant load :  $imagePath", e)
            Toast.makeText(
                    applicationContext,
                    "Failed to load $imagePath : File no found ",
                    Toast.LENGTH_LONG
            ).show()
            return Result.failure();
        }
        val original = BitmapFactory.decodeStream(readStream)

        val filteredImage = toSepia(original) // apply sepia filter

        // create a file to save filtered image
        lateinit var tmpFile: java.io.File
        try {
            tmpFile = File.createTempFile("filtered", "png") ?: return Result.failure()
        } catch (e: java.io.IOException) {
            Log.e("Failed to create file:", "file name : " + "resized.png", e)
            Toast.makeText(
                    applicationContext,
                    "Failed to create compressed image file ",
                    Toast.LENGTH_LONG
            ).show()
            return Result.failure()
        } catch (e: IllegalArgumentException) {
            Log.e(
                    "Failed to create file:",
                    "IllegalArgument prefix should have more than 3 char ",
                    e
            )
            Toast.makeText(
                    applicationContext,
                    "Failed to create compressed image file ",
                    Toast.LENGTH_LONG
            ).show()
            return Result.failure()
        } catch (e: SecurityException) {
            Log.e(
                    "Failed to create file:",
                    "Security Exception : right not allowed on this folder",
                    e
            )
        }

        tmpFile.outputStream().use {
            filteredImage.compress(Bitmap.CompressFormat.PNG, 100, it)
        } //save
        Log.d("Sepia filter ", "filter succed ( in theorie )")
        val outputData = Data.Builder()
                .putString("IMAGE_URI", tmpFile.toUri().toString())
                .build()
        return Result.success(outputData);
    }

    private fun toSepia(bmpOriginal: Bitmap): Bitmap {
        val height = bmpOriginal.height
        val width = bmpOriginal.width
        val depth = 20;
        val bmpSephia = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas: Canvas = Canvas(bmpSephia);
        val paint = Paint();
        val cm = ColorMatrix();
        cm.setScale(.3f, .3f, .3f, 1.0f); //filtre de c
        val f = ColorMatrixColorFilter(cm);
        paint.colorFilter = f;
        canvas.drawBitmap(bmpOriginal, 0.0f, 0.0f, paint);
        for (x in 0 until width) {
            for (y in 0 until height) {
                val c = bmpOriginal.getPixel(x, y);
                var r = Color.red(c);
                var g = Color.green(c);
                var b = Color.blue(c);

                val gry = (r + g + b) / 3;
                r = gry
                g = gry
                b = gry

                r += (depth * 2);
                g += depth;

                if (r > 255) {
                    r = 255;
                }
                if (g > 255) {
                    g = 255;
                }
                bmpSephia.setPixel(x, y, Color.rgb(r, g, b));
            }
        }
        return bmpSephia
    }

}