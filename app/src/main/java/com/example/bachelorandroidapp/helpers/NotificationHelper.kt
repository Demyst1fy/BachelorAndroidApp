package com.example.bachelorandroidapp.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.fragment.app.FragmentActivity
import com.example.bachelorandroidapp.data.LocationItem
import com.example.bachelorandroidapp.MainActivity
import com.example.bachelorandroidapp.utils.ImageUtil
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationHelper(private val activity: FragmentActivity, channelName : String, private val locationItem: LocationItem?) {
    companion object {
        const val CHANNEL_ID = "my_channel_01"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name : CharSequence = channelName
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            val notificationManager =
                activity.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    suspend fun createNotification() {
        val pushNotificationTrace: Trace =
            FirebasePerformance.getInstance().newTrace("create_notification")
        pushNotificationTrace.start()

        val itemId = locationItem!!.id

        val bitmapImage = withContext(Dispatchers.IO) {
            ImageUtil.bitmapFromUrl(
                "https://openweathermap.org/img/wn/" +
                        locationItem.icon +
                        ".png"
            )
        }

        val intent = Intent(activity, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            activity,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(activity, CHANNEL_ID)
            .setSmallIcon(IconCompat.createWithBitmap(bitmapImage!!))
            .setContentTitle(locationItem.name)
            .setContentText(locationItem.temp.toString() + ", " + locationItem.weatherDescription)
            .setLargeIcon(bitmapImage)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

        notificationManager.notify(itemId, notificationBuilder.build())

        pushNotificationTrace.stop()
    }
}