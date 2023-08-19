package com.example.bachelorandroid

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.bachelorandroid.customs.CustomMarkerInfoWindow
import com.example.bachelorandroid.helpers.CameraHelper
import com.example.bachelorandroid.helpers.FileHelper
import com.example.bachelorandroid.helpers.MicHelper
import com.example.bachelorandroid.helpers.NotificationHelper
import com.example.bachelorandroid.utils.DownloadUtil
import com.google.firebase.FirebaseApp
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

class MainActivity : AppCompatActivity(), LocationListener {
    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager

    private lateinit var locationTitle: TextView
    private lateinit var locationTemp: TextView
    private lateinit var locationHumidity: TextView
    private lateinit var locationWindSpeed: TextView
    private lateinit var locationPressure: TextView
    private lateinit var locationSubDescription: TextView
    private lateinit var locationIcon: ImageView
    private lateinit var photoFromCamera: ImageView

    private lateinit var fileHelper: FileHelper
    private lateinit var cameraHandler: CameraHelper
    private lateinit var micHelper: MicHelper
    private lateinit var notificationHelper: NotificationHelper

    private var currentMarker: Marker? = null
    private var clickedMarker: Marker? = null

    private var isMarkerClickInProgress: Boolean = false

    companion object {
        val LOG_TAG = MainActivity::class.simpleName
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        const val MY_PERMISSIONS_REQUEST_CAMERA = 100
        const val MY_PERMISSIONS_REQUEST_MICROPHONE = 101
        const val MY_PERMISSIONS_REQUEST_NOTIFICATIONS = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        // Initialize osmdroid
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, getPreferences(Context.MODE_PRIVATE))

        // Content without Action-Bar
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        // Initialize map view
        mapView = findViewById(R.id.mapview)
        mapView.setMultiTouchControls(true)
        mapView.minZoomLevel = 3.0

        locationTitle = findViewById(R.id.locationTitle)
        locationTemp = findViewById(R.id.locationTemp)
        locationHumidity = findViewById(R.id.locationHumidity)
        locationWindSpeed = findViewById(R.id.locationWindSpeed)
        locationPressure = findViewById(R.id.locationPressure)
        locationSubDescription = findViewById(R.id.locationSubDescription)
        locationIcon = findViewById(R.id.locationIcon)
        photoFromCamera = findViewById(R.id.photoFromCamera)

        // Initialize location manager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        fileHelper = FileHelper(this)
        cameraHandler = CameraHelper(this, fileHelper, photoFromCamera)
        micHelper = MicHelper(this, mapView, clickedMarker)

        // Load the latest image on app startup
        fileHelper.getLatestImage(photoFromCamera)

        // Check location permission
        checkAndRequestLocationPermission()

        // Set a click listener for the button to send push-notification.
        val notifButton = findViewById<Button>(R.id.notifButton)
        notifButton.setOnClickListener {
            lifecycleScope.launch {
                notificationHelper.createNotification()
            }
        }

        // Set a click listener for the button to start speech recognition.
        val micButton = findViewById<Button>(R.id.micButton)
        micButton.setOnClickListener {
            checkAndRequestMicrophonePermission()
        }

        // Set a click listener for the button to start camera capture.
        val cameraButton = findViewById<Button>(R.id.cameraButton)
        cameraButton.setOnClickListener {
            checkAndRequestCameraPermission()
        }
    }

    private fun checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        } else {
            onLocationScan()
        }
    }

    private fun onLocationScan() {
        // Permission already granted, get current location

        currentMarker?.let {
            mapView.overlays.remove(it)
        }

        val geoPoint = getCurrentLocation()
        val newMarker = Marker(mapView)
        newMarker.position = geoPoint
        newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        lifecycleScope.launch {
            val current = DownloadUtil.getWeatherDataFromLocationLatLon(this@MainActivity, geoPoint.latitude, geoPoint.longitude)

            newMarker.infoWindow = CustomMarkerInfoWindow(R.layout.custom_marker_info_window, mapView, current, this@MainActivity)

            locationTitle.text = current?.name
            locationTemp.text = getString(R.string.temp, current?.temp)
            locationHumidity.text = getString(R.string.humidity, current?.humidity)
            locationWindSpeed.text = getString(R.string.windSpeed, current?.windSpeed)
            locationPressure.text = getString(R.string.pressure, current?.pressure)
            locationSubDescription.text = current?.weatherDescription

            Glide.with(this@MainActivity)
                .load("https://openweathermap.org/img/wn/" +
                        current?.icon + ".png")
                .apply(RequestOptions.overrideOf(70))
                .into(locationIcon)

            mapView.overlays.add(newMarker)
            currentMarker = newMarker

            mapView.controller.setZoom(18.0)
            mapView.controller.setCenter(geoPoint)
            mapView.overlays.add(mapClickOverlay)
            mapView.invalidate()

            notificationHelper = NotificationHelper(this@MainActivity, "My Channel 01", current)
        }
    }

    private fun checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                MY_PERMISSIONS_REQUEST_CAMERA
            )
        } else {
            cameraHandler.launchCamera()
        }
    }

    private fun checkAndRequestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                MY_PERMISSIONS_REQUEST_MICROPHONE
            )
        } else {
            micHelper.launchMicrophone()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show()
                }
                onLocationScan()
            }
            MY_PERMISSIONS_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraHandler.launchCamera()
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show()
                }
            }
            MY_PERMISSIONS_REQUEST_MICROPHONE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    micHelper.launchMicrophone()
                } else {
                    Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getCurrentLocation() : GeoPoint {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val geoLocationTrace: Trace = FirebasePerformance.getInstance().newTrace("get_geolocation")
            geoLocationTrace.start()

            // Request location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, this)

            // Get last known location
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            geoLocationTrace.stop()
            return GeoPoint(location?.latitude ?: 0.0, location?.longitude ?: 0.0)
        }
        else {
            return GeoPoint(0.0, 0.0)
        }
    }

    override fun onLocationChanged(location: Location) { }

    // Create a custom overlay for handling map clicks
    private val mapClickOverlay = object : Overlay() {
        override fun onSingleTapUp(event: MotionEvent, mapView: MapView): Boolean {
            if (event.action == MotionEvent.ACTION_UP && !isMarkerClickInProgress) {
                isMarkerClickInProgress = true

                // Remove previous clicked marker
                clickedMarker?.let {
                    it.infoWindow.close()
                    mapView.overlays.remove(it)
                }

                currentMarker?.infoWindow?.close()

                // Get the clicked position
                val clickedPosition = mapView.projection.fromPixels(
                    event.x.toInt(), event.y.toInt()
                ) as GeoPoint

                val newClickedMarker = Marker(mapView)
                newClickedMarker.position = clickedPosition
                newClickedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                lifecycleScope.launch {
                    // Add a new marker at the clicked position

                    val current = DownloadUtil.getWeatherDataFromLocationLatLon(
                        this@MainActivity,
                        clickedPosition.latitude,
                        clickedPosition.longitude
                    )

                    newClickedMarker.infoWindow = CustomMarkerInfoWindow(R.layout.custom_marker_info_window, mapView, current, this@MainActivity)
                    newClickedMarker.showInfoWindow();

                    mapView.overlays.add(newClickedMarker)
                }

                clickedMarker = newClickedMarker
            }

            isMarkerClickInProgress = false
            return super.onTouchEvent(event, mapView)
        }
    }
}
