package com.example.bachelorandroidapp.helpers

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

class CameraHelper(activity: FragmentActivity, private var fileHelper: FileHelper, private val photoFromCamera: ImageView) {

    fun launchCamera() {
        val cameraActivationTrace: Trace = FirebasePerformance.getInstance().newTrace("activate_camera")
        cameraActivationTrace.start()

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(takePictureIntent)

        cameraActivationTrace.stop()
    }

    private val cameraLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val photoBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.extras?.getParcelable("data", Bitmap::class.java)
                } else {
                    result.data?.extras?.getParcelable("data") as Bitmap?
                }

                if (photoBitmap != null) {
                    // Process and display the captured image
                    fileHelper.setImageInStorage(photoBitmap)
                    fileHelper.loadImage(photoFromCamera)
                }
            }
        }
}
