package com.example.bachelorandroid.customs

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.bachelorandroid.R
import com.example.bachelorandroid.data.models.LocationItem
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

            Glide.with(context)
                .load("https://openweathermap.org/img/wn/" +
                        clickedData?.icon + ".png")
                .apply(RequestOptions.overrideOf(70))
                .into(markerIcon)
        }
    }

    override fun onClose() {
        super.close()
    }
}
