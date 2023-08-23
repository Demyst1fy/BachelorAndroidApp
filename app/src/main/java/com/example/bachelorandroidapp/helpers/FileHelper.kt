package com.example.bachelorandroidapp.helpers

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.io.File

class FileHelper(private val context: Context) {

    fun createImage(): File {
        val createImageTrace: Trace = FirebasePerformance.getInstance().newTrace("create_image")
        createImageTrace.start()

        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val tempFile = File.createTempFile("image", ".jpg", storageDir)

        createImageTrace.start()

        return tempFile
    }

    fun loadImage(latestImageUri: Uri?, photoFromCamera: ImageView) {
        if (latestImageUri != null) {
            val loadImageTrace: Trace = FirebasePerformance.getInstance().newTrace("load_image");
            loadImageTrace.start();

            Glide.with(context)
                .load(latestImageUri)
                .apply(RequestOptions.overrideOf(160, 120))
                .into(photoFromCamera)

            loadImageTrace.stop();
        }
    }

    fun setImageInStorage(photoURI: Uri): Uri {
        val setImageInStorageTrace: Trace = FirebasePerformance.getInstance().newTrace("set_latest_image_in_storage")
        setImageInStorageTrace.start()

        val sharedPreferences = context.getSharedPreferences("MyPhoto", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("latest_image", photoURI.toString())
        editor.apply()

        setImageInStorageTrace.stop()

        return photoURI
    }

    fun getImageFromStorage() : Uri? {
        val getImageFromStorageTrace: Trace =
            FirebasePerformance.getInstance().newTrace("get_latest_image_from_storage")
        getImageFromStorageTrace.start()

        val sharedPreferences = context.getSharedPreferences("MyPhoto", Context.MODE_PRIVATE)
        val latestImageUriString = sharedPreferences.getString("latest_image", null)
        val latestImageUri = latestImageUriString?.let { Uri.parse(it) }

        getImageFromStorageTrace.stop()

        return latestImageUri
    }
}