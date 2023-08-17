package com.example.bachelorandroid.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.bachelorandroid.data.LocationItem
import com.example.bachelorandroid.MainActivity
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

object NotificationUtil {
    private const val CHANNEL_ID = "my_channel_01"

    fun registerNotificationChannel(context : Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name : CharSequence = "My Channel 01"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotification(context : Context, locationItem: LocationItem?) {
        val pushNotificationTrace: Trace = FirebasePerformance.getInstance().newTrace("create_push_notification")
        pushNotificationTrace.start()
        
        val itemId = locationItem!!.id

        val bitmapImage = ImageUtil.bitmapFromUrl(
            "https://openweathermap.org/img/wn/" +
                    locationItem.icon +
                    ".png"
        )

        val intent = Intent(context, MainActivity::class.java) // Replace YourMainActivity with your actual main activity class
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(IconCompat.createWithBitmap(bitmapImage!!))
            .setContentTitle(locationItem.name)
            .setContentText(locationItem.temp.toString() + ", " + locationItem.weatherDescription)
            .setLargeIcon(bitmapImage)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

        notificationManager.notify(itemId, notificationBuilder.build())

        pushNotificationTrace.stop()
    }
}