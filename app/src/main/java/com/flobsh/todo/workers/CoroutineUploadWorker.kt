package com.flobsh.todo.workers

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.flobsh.todo.network.Api
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class CoroutineUploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val imageUriInput = inputData.getString("IMAGE_URI")
        if (imageUriInput.isNullOrEmpty()) {
            // Toast does'nt work in workers
            // Toast.makeText(applicationContext, "Failed to load image", Toast.LENGTH_LONG).show()
            return  Result.failure()
        }

        Log.e("UPLOAD", imageUriInput)

        val image = MultipartBody.Part.createFormData(
            name = "avatar",
            filename = "temp.jpeg",
            body = applicationContext.contentResolver.openInputStream(imageUriInput.toUri())!!.readBytes().toRequestBody()
        )

        val response = Api.INSTANCE.userService.updateAvatar(image)
        return if (response.isSuccessful) {
            // Toast.makeText(applicationContext, "Avatar updated", Toast.LENGTH_LONG).show()
            Result.success()
        } else {
            // Toast.makeText(applicationContext, "Failed to update avatar", Toast.LENGTH_LONG).show()
            Result.failure()
        }
    }
}