package com.example.bachelorandroidapp.helpers

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.example.bachelorandroidapp.utils.TypeConverterUtil
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace


class FileHelper(private val context: Context) {

    private fun createImage(inputBitmap: Bitmap): String {
        val createImageTrace: Trace = FirebasePerformance.getInstance().newTrace("create_image")
        createImageTrace.start()

        val outputBitmap = Bitmap.createScaledBitmap(inputBitmap, 240, 180, false)
        val bitmapAsBase64 = TypeConverterUtil.bitmapToBase64(outputBitmap)

        createImageTrace.stop()

        return bitmapAsBase64
    }

    fun loadImage(photoFromCamera: ImageView) {
        val latestImageAsBase64 = getImageFromStorage();

        if (latestImageAsBase64 != null) {
            val loadImageTrace: Trace = FirebasePerformance.getInstance().newTrace("load_image");
            loadImageTrace.start();

            val bitmap = TypeConverterUtil.base64ToBitmap(latestImageAsBase64)
            photoFromCamera.setImageBitmap(bitmap)

            loadImageTrace.stop();
        }
    }

    fun setImageInStorage(photoBitmap: Bitmap) {
        val createdImageAsBase64 = createImage(photoBitmap)

        val setImageInStorageTrace: Trace = FirebasePerformance.getInstance().newTrace("set_latest_image_in_storage")
        setImageInStorageTrace.start()

        val sharedPreferences = context.getSharedPreferences("MyPhoto", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("latest_image", createdImageAsBase64)
        editor.apply()

        setImageInStorageTrace.stop()
    }

    private fun getImageFromStorage(): String? {
        val getImageFromStorageTrace: Trace = FirebasePerformance.getInstance().newTrace("get_latest_image_from_storage")
        getImageFromStorageTrace.start()

        val sharedPreferences = context.getSharedPreferences("MyPhoto", Context.MODE_PRIVATE)
        val bitmapAsBase64 = sharedPreferences.getString("latest_image", null)

        getImageFromStorageTrace.stop()

        return bitmapAsBase64
    }
}