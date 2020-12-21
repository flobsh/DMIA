package com.flobsh.todo.userinfo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.flobsh.todo.BuildConfig
import com.flobsh.todo.R
import com.flobsh.todo.network.Api
import com.flobsh.todo.workers.CoroutineCompressWorker
import com.flobsh.todo.workers.CoroutineUploadWorker
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class UserInfoActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        val takePictureButton = findViewById<Button>(R.id.take_picture_button)
        takePictureButton.setOnClickListener {
            askCameraPermissionAndOpenCamera()
        }

        val pickPhotoButton = findViewById<Button>(R.id.upload_image_button)
        pickPhotoButton.setOnClickListener {
            pickInGallery.launch("image/*")
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) openCamera()
            else showExplanationDialog()
        }

    private fun requestCameraPermission() =
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)

    @RequiresApi(Build.VERSION_CODES.M)
    private fun askCameraPermissionAndOpenCamera() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> openCamera()
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> showExplanationDialog()
            else -> requestCameraPermission()
        }
    }

    private fun showExplanationDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("On a besoin de la caméra sivouplé ! 🥺")
            setPositiveButton("Bon, ok") { _, _ ->
                requestCameraPermission()
            }
            setCancelable(true)
            show()
        }
    }

    // register
    @SuppressLint("RestrictedApi")
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            val tmpFile = File.createTempFile("avatar", "jpeg")
            tmpFile.outputStream().use {
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    100,
                    it
                ) //remove compresion will be done on a worker on handel images alow to work faster
            }
            handleImage(tmpFile.toUri())
        }

    // use
    private fun openCamera() = takePicture.launch()

    private fun handleImage(uri: Uri) {
        val compressWorker = OneTimeWorkRequestBuilder<CoroutineCompressWorker>()
            .setInputData(
                workDataOf(
                    "IMAGE_URI" to uri.toString()
                )
            ).build()
        val sepiaFilterWorker = OneTimeWorkRequestBuilder<CoroutineCompressWorker>().build()
        val uploadWorker = OneTimeWorkRequestBuilder<CoroutineUploadWorker>().build()
        WorkManager.getInstance(applicationContext)
            .beginWith(compressWorker)
            .then(sepiaFilterWorker)
            .then(uploadWorker)
            .enqueue()
    }

    // create a temp file and get a uri for it
    private val photoUri by lazy {
        FileProvider.getUriForFile(
            this,
            BuildConfig.APPLICATION_ID + ".fileprovider",
            File.createTempFile("avatar", ".jpeg", externalCacheDir)
        )
    }

    // register
    private val pickInGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            handleImage(uri)
        }
}