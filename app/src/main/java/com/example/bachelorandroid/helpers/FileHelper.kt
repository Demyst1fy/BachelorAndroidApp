package com.example.bachelorandroid.helpers

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.io.File

class FileHelper(private val context: Context) {

    fun setLatestImage(imageFile: File): Uri? {
        val setImageInStorageTrace: Trace = FirebasePerformance.getInstance().newTrace("set_latest_image_in_storage")
        setImageInStorageTrace.start()

        val latestImageUri = FileProvider.getUriForFile(
            context,
            "com.example.bachelorandroid.fileprovider",
            imageFile
        )
        val sharedPreferences = context.getSharedPreferences("MyPhoto", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("latest_image_uri", latestImageUri.toString())
        editor.apply()

        setImageInStorageTrace.stop()

        return latestImageUri
    }

    fun getLatestImage(photoFromCamera: ImageView) {
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
}