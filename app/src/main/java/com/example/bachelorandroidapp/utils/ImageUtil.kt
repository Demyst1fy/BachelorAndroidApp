package com.example.bachelorandroidapp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

object ImageUtil {
    private val LOG_TAG = ImageUtil::class.java.simpleName

    suspend fun bitmapFromUrl(urlString: String?): Bitmap? {
        return withContext(Dispatchers.IO) {
            val url = URL(urlString)
            var urlConnection: HttpURLConnection? = null
            try {
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.connectTimeout = 5000
                val statusCode: Int = urlConnection.responseCode
                if (statusCode != 200) {
                    Log.e(LOG_TAG, "Error downloading image from $url. Response code: $statusCode")
                    return@withContext null
                }
                val inputStream = urlConnection.inputStream
                if (inputStream == null) {
                    Log.e(LOG_TAG, "Error downloading image from $url")
                    return@withContext null
                }
                return@withContext BitmapFactory.decodeStream(inputStream)
            } catch (ex: MalformedURLException) {
                Log.e(LOG_TAG, "Malformed url", ex)
                return@withContext null
            } catch (ex: IOException) {
                Log.e(LOG_TAG, "Error downloading image from $url", ex)
                return@withContext null
            } finally {
                urlConnection?.disconnect()
            }
        }
    }





}