package com.flobsh.todo.userinfo

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.work.*
import com.flobsh.todo.BuildConfig
import com.flobsh.todo.R
import com.flobsh.todo.workers.CoroutineCompressWorker
import com.flobsh.todo.workers.CoroutineSepiaFilterWorker
import com.flobsh.todo.workers.CoroutineUploadWorker
import java.io.File

class UserInfoActivity : AppCompatActivity() {
    companion object {
        private const val WORKER_TAG = "WORKER_TAG"
        private const val SEPIA_FILTER_WORKER = "SEPIA_FILTER_WORKER"
        private const val COMPRESS_WORKER = "COMPRESS_WORKER"
        private const val UPLOAD_WORKER = "UPLOAD_WORKER"
    }

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

    fun workInfoToString(workInfo: WorkInfo): String {
        Log.e("Worker info ", "aaa" + workInfo.tags)
        return when (workInfo.tags.first()) {
            COMPRESS_WORKER -> "Compress"
            UPLOAD_WORKER -> "Upload"
            SEPIA_FILTER_WORKER -> "Sepia filter"
            else -> "not handeled "
        }
    }

    /*
     /**
      * Worker observer lunch toast in function of the state of the different worker
      */
     private val workerObserver = Observer<List<WorkInfo>> { state ->
         state?.let {
             if (!it.isNullOrEmpty()) {
                 val workInfo = it[0];
                 when (workInfo.state) {
                     WorkInfo.State.ENQUEUED -> {
                         Toast.makeText(
                             applicationContext,
                             "Worker2" + workInfoToString(workInfo) + " finished",
                             Toast.LENGTH_LONG
                         ).show()
                     }
                     WorkInfo.State.RUNNING -> {
                         Toast.makeText(
                             applicationContext,
                             "worker " + workInfoToString(workInfo) + " running ",
                             Toast.LENGTH_LONG
                         ).show()
                     }
                     WorkInfo.State.SUCCEEDED -> {
                         Toast.makeText(
                             applicationContext,
                             "worker:+ " + workInfoToString(workInfo) + " succeed",
                             Toast.LENGTH_LONG
                         ).show()
                     }
                     else -> Toast.makeText(
                         applicationContext,
                         "worker:+ " + workInfoToString(workInfo) + " response not handled yet ",
                         Toast.LENGTH_LONG
                     ).show()
                 }
             }
         }
     }
 */
    fun HandelWorkInfoState(workInfostate: WorkInfo.State, workerType: String): String {
        return workerType +
                when (workInfostate) {
                    WorkInfo.State.SUCCEEDED -> " succed "
                    WorkInfo.State.FAILED -> " Failed"
                    WorkInfo.State.RUNNING -> " Running"
                    WorkInfo.State.BLOCKED -> " Blocked"
                    WorkInfo.State.CANCELLED -> " Cancelled"
                    WorkInfo.State.ENQUEUED -> " Enqueued"
                }
    }

    private fun observeWorkers() {
        WorkManager.getInstance(applicationContext)
            .getWorkInfosByTagLiveData(SEPIA_FILTER_WORKER)
            .observe(this, { workInfoList ->
                if (workInfoList.isNullOrEmpty())
                    Toast.makeText(applicationContext, "no worker active", Toast.LENGTH_LONG).show()
                else {
                    val workInfo = workInfoList[0]

                    Toast.makeText(
                        applicationContext,
                        HandelWorkInfoState(workInfo.state, "Sepia filter "),
                        Toast.LENGTH_LONG
                    ).show()

                }
            }
            )
        WorkManager.getInstance(applicationContext)
            .getWorkInfosByTagLiveData(UPLOAD_WORKER)
            .observe(this,
                { workInfoList ->
                    if (workInfoList.isNullOrEmpty())
                        Toast.makeText(applicationContext, "no worker active", Toast.LENGTH_LONG)
                            .show()
                    else {
                        val workInfo = workInfoList[0]
                            Toast.makeText(
                                applicationContext,
                                HandelWorkInfoState(workInfo.state, "Upload "),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            )
        WorkManager.getInstance(applicationContext)
            .getWorkInfosByTagLiveData(COMPRESS_WORKER)
            .observe(this,
                { workInfoList ->
                    if (workInfoList.isNullOrEmpty())
                        Toast.makeText(applicationContext, "no worker active", Toast.LENGTH_LONG)
                            .show()
                    else {
                        val workInfo = workInfoList[0]
                        Toast.makeText(
                            applicationContext,
                            HandelWorkInfoState(workInfo.state, "Compress "),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
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
            )
            .addTag(COMPRESS_WORKER)
            .build()
        val sepiaFilterWorker = OneTimeWorkRequestBuilder<CoroutineSepiaFilterWorker>()
            .addTag(SEPIA_FILTER_WORKER)
            .build()
        val uploadWorker = OneTimeWorkRequestBuilder<CoroutineUploadWorker>()
            .addTag(UPLOAD_WORKER)
            .build()
        WorkManager.getInstance(applicationContext)
            .beginWith(compressWorker)
            .then(sepiaFilterWorker)
            .then(uploadWorker)
            .enqueue()

        observeWorkers()
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
