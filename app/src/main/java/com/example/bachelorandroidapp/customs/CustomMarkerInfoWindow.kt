package com.example.bachelorandroidapp.customs

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import android.widget.TextView
import com.example.bachelorandroidapp.R
import com.example.bachelorandroidapp.data.LocationItem
import com.example.bachelorandroidapp.utils.ImageUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

class CustomMarkerInfoWindow(layoutResId: Int, mapView: MapView, private val clickedData: LocationItem?, private val context: Context) : InfoWindow(layoutResId, mapView) {
    override fun onOpen(item: Any) {
        if (mView != null && item is Marker) {

            val markerTitle = mView.findViewById<TextView>(R.id.markerTitle)
            val markerTemp = mView.findViewById<TextView>(R.id.markerTemp)
            val markerHumidity = mView.findViewById<TextView>(R.id.markerHumidity)
            val markerWindSpeed = mView.findViewById<TextView>(R.id.markerWindSpeed)
            val markerPressure = mView.findViewById<TextView>(R.id.markerPressure)
            val markerSubDescription = mView.findViewById<TextView>(R.id.markerSubDescription)
            val markerIcon = mView.findViewById<ImageView>(R.id.markerIcon)

            markerTitle?.text = clickedData?.name
            markerTemp?.text = context.getString(R.string.temp, clickedData?.temp)
            markerHumidity?.text = context.getString(R.string.humidity, clickedData?.humidity)
            markerWindSpeed?.text = context.getString(R.string.windSpeed, clickedData?.windSpeed)
            markerPressure?.text = context.getString(R.string.pressure, clickedData?.pressure)
            markerSubDescription?.text = clickedData?.weatherDescription

            // Use a CoroutineScope to perform the image download
            CoroutineScope(Dispatchers.IO).launch {
                val image = ImageUtil.bitmapFromUrl("https://openweathermap.org/img/wn/" +
                        clickedData?.icon + ".png")

                withContext(Dispatchers.Main) {
                    // Update the UI on the main thread with the downloaded image
                    val resizedBitmap = image?.let { Bitmap.createScaledBitmap(it, 70, 70, true) }
                    markerIcon.setImageBitmap(resizedBitmap)
                }
            }
        }
    }

    override fun onClose() {
        super.close()
    }
}
