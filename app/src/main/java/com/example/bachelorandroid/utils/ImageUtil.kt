package com.example.bachelorandroid.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

object ImageUtil {
    private val LOG_TAG = ImageUtil::class.java.simpleName

    fun bitmapFromUrl(urlString: String?): Bitmap? {
        val url = URL(urlString)
        var urlConnection: HttpURLConnection? = null
        return try {
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connectTimeout = 5000
            val statusCode: Int = urlConnection.responseCode
            if (statusCode != 200) {
                Log.e(LOG_TAG, "Error downloading image from $url. Response code: $statusCode")
                return null
            }
            val inputStream = urlConnection.inputStream
            if (inputStream == null) {
                Log.e(LOG_TAG, "Error downloading image from $url")
                return null
            }
            return BitmapFactory.decodeStream(inputStream)
        }catch (ex: MalformedURLException) {
            Log.e(LOG_TAG, "Malformed url", ex)
            return null
        } catch (ex: IOException) {
            Log.e(LOG_TAG, "Error downloading image from $url", ex)
            null
        } finally {
            urlConnection?.disconnect()
        }
    }
}