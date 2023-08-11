package com.example.bachelorandroid.helpers

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.bachelorandroid.MainActivity
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.io.File

class FileHelper(private val context: Context) {
    fun createImageFile(): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("image", ".jpg", storageDir)
    }

    fun saveCapturedImage(imageFile: File): Uri? {
        val latestImageUri = FileProvider.getUriForFile(
            context,
            "com.example.bachelorandroid.fileprovider",
            imageFile
        )
        updateLatestImageUri(latestImageUri)
        return latestImageUri
    }

    fun getLatestImageUri(): Uri? {
        val sharedPreferences = context.getSharedPreferences("MyPhoto", Context.MODE_PRIVATE)
        val latestImageUriString = sharedPreferences.getString("latest_image_uri", null)

        return latestImageUriString?.let { Uri.parse(it) }
    }

    private fun updateLatestImageUri(uri: Uri) {
        val sharedPreferences = context.getSharedPreferences("MyPhoto", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("latest_image_uri", uri.toString())
        editor.apply()
    }
}