package com.flobsh.todo.workers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ColorSpace
import android.os.Build
import android.util.Log
import android.util.Log.DEBUG
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.graphics.get
import androidx.core.graphics.set
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.flobsh.todo.BuildConfig.DEBUG
import java.io.File
import java.io.InputStream
import java.lang.IllegalArgumentException


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
        //the imput file is now on original

        val filteredImage = sepiaFilter(original) // apply sepia filter

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
        Log.d("Sepia filter ", "filter succed ( in theorie ) " + filteredImage.getColor(1,1).toString())
        val outputData = Data.Builder()
            .putString("IMAGE_URI", tmpFile.toUri().toString())
            .build()
        return Result.success(outputData);


    }

    fun sepiaFilter(src: Bitmap): Bitmap {
        val temp = Bitmap.createBitmap(src, 0, 0, src.width, src.width)
        for (i in 0..src.width) {
            for (j in 0..src.height) {
                temp[i, j] = Color.BLACK;
                //temp.set(
                //    i,
                //    j,
                //    toSepia(src.getPixel(i, j))
                 // set the color of i,j pixel at sepia color of i,f original pixel
            }
        }
        return temp
    }

    private fun toSepia(color: Int): Int {
        // extract 8-bit color channels
        val i_r = color shr 16 and 0xff
        val i_g = color shr 8 and 0xff
        val i_b = color and 0xff

        // turn int rgb to float rgb
        val r = i_r.toFloat()
        val g = i_g.toFloat()
        val b = i_b.toFloat()

        // compile new rgb, basically a 3x3 matrix operation
        val newr = r * .393f + g * .769f + b * .189f
        val newg = r * .349f + g * .686f + b * .168f
        val newb = r * .272f + g * .534f + b * .131f

        // round floats to integers
        var o_r = (newr + 0.5f).toInt()
        var o_g = (newg + 0.5f).toInt()
        var o_b = (newb + 0.5f).toInt()

        // clamp result
        o_r = if (o_r > 255) 255 else o_r
        o_g = if (o_g > 255) 255 else o_g
        o_b = if (o_b > 255) 255 else o_b

        // turn back to 32-bit int format
        return 255 shl 24 or (o_r shl 16) or (o_g shl 8) or o_b
    }

}