package com.example.bachelorandroid.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bachelorandroid.utils.DownloadUtil
import com.example.bachelorandroid.utils.NotificationUtil

class PeriodicNotificationWorker(private val context : Context, params : WorkerParameters) :
    CoroutineWorker(context, params) {
    companion object {
        val LOG_TAG: String = PeriodicNotificationWorker::class.java.simpleName
    }

    override suspend fun doWork(): Result {
        Log.d(LOG_TAG, "NotificationWork started.")

        val longitude = inputData.getDouble("longitude", 0.0)
        val latitude = inputData.getDouble("latitude", 0.0)

        val current = DownloadUtil.getWeatherDataFromLocationLatLon(context, latitude, longitude)

        NotificationUtil.createNotification(context, current)

        Log.d(LOG_TAG, "NotificationWork finished.")
        return Result.success()
    }


}
