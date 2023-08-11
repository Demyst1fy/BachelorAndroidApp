package com.example.bachelorandroid.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.io.File

class CameraHelper(private val activity: FragmentActivity, private var fileHelper: FileHelper, private val photoFromCamera: ImageView) {
    private lateinit var photoFile: File

    fun launchCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = fileHelper.createImageFile()

        val photoURI = FileProvider.getUriForFile(
            activity,
            "com.example.bachelorandroid.fileprovider",
            photoFile
        )

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

        val cameraActivationTrace: Trace = FirebasePerformance.getInstance().newTrace("camera_activation")
        cameraActivationTrace.start()

        cameraLauncher.launch(takePictureIntent)

        cameraActivationTrace.stop()
    }

    private val cameraLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val saveImageInStorageTrace: Trace = FirebasePerformance.getInstance().newTrace("save_image_in_storage")
                saveImageInStorageTrace.start()

                val latestImageUri = fileHelper.saveCapturedImage(photoFile)
                if (latestImageUri != null) {
                    Glide.with(activity)
                        .load(latestImageUri)
                        .apply(RequestOptions.overrideOf(160, 120))
                        .into(photoFromCamera)
                }

                saveImageInStorageTrace.stop()
            }
        }
}
