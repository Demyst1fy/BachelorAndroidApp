package com.example.bachelorandroidapp.helpers

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.io.File

class CameraHelper(private val activity: FragmentActivity, private var fileHelper: FileHelper, private val photoFromCamera: ImageView) {
    private lateinit var photoFile: File

    fun launchCamera() {
        photoFile = fileHelper.createImage()

        val photoURI = FileProvider.getUriForFile(
            activity,
            "com.example.bachelorandroidapp.fileprovider",
            photoFile
        )

        val cameraActivationTrace: Trace = FirebasePerformance.getInstance().newTrace("activate_camera")
        cameraActivationTrace.start()

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

        cameraLauncher.launch(takePictureIntent)

        cameraActivationTrace.stop()
    }

    private val cameraLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val photoURI = FileProvider.getUriForFile(
                    activity,
                    "com.example.bachelorandroidapp.fileprovider",
                    photoFile
                )

                val latestImageUri = fileHelper.setImageInStorage(photoURI)

                fileHelper.loadImage(latestImageUri, photoFromCamera)
            }
        }
}
