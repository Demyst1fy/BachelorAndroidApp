package com.example.bachelorandroid.helpers

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.ImageView
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.io.File

class FileHelper(private val context: Context) {
    fun createImageFile(): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("image", ".jpg", storageDir)
    }

    fun setCapturedImage(imageFile: File): Uri? {
        val setImageInStorageTrace: Trace = FirebasePerformance.getInstance().newTrace("set_latest_image_in_storage")
        setImageInStorageTrace.start()

        val latestImageUri = FileProvider.getUriForFile(
            context,
            "com.example.bachelorandroid.fileprovider",
            imageFile
        )
        updateLatestImageUri(latestImageUri)

        setImageInStorageTrace.stop()

        return latestImageUri
    }

    fun getLatestImageUri(photoFromCamera: ImageView) {
        val getImageFromStorageTrace: Trace = FirebasePerformance.getInstance().newTrace("get_latest_image_from_storage")
        getImageFromStorageTrace.start()

        val sharedPreferences = context.getSharedPreferences("MyPhoto", Context.MODE_PRIVATE)
        val latestImageUriString = sharedPreferences.getString("latest_image_uri", null)
        val latestImageUri = latestImageUriString?.let { Uri.parse(it) }

        if (latestImageUri != null) {
            Glide.with(context)
                .load(latestImageUri)
                .apply(RequestOptions.overrideOf(160, 120))
                .into(photoFromCamera)
        }

        getImageFromStorageTrace.stop()
    }

    private fun updateLatestImageUri(uri: Uri) {
        val sharedPreferences = context.getSharedPreferences("MyPhoto", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("latest_image_uri", uri.toString())
        editor.apply()
    }
}