package com.example.bachelorandroid.helpers

import android.content.Intent
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.bachelorandroid.R
import com.example.bachelorandroid.customs.CustomMarkerInfoWindow
import com.example.bachelorandroid.utils.DownloadUtil
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.util.Locale

class MicHelper(private val activity: FragmentActivity, private val mapView: MapView, private var clickedMarker: Marker?) {
    fun launchMicrophone() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMAN)

        val microphoneActivationTrace: Trace = FirebasePerformance.getInstance().newTrace("microphone_activation")
        microphoneActivationTrace.start()

        speechLauncher.launch(intent)

        microphoneActivationTrace.stop()
    }

    private val speechLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
                // In case of success, extract the data from the result intent.
                val microphoneRecognitionTrace: Trace = FirebasePerformance.getInstance().newTrace("microphone_speechrecognition")
                microphoneRecognitionTrace.start()

                val data = result.data
                val res: ArrayList<String>? =
                    data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                microphoneRecognitionTrace.stop()

                // Remove previous marker
                clickedMarker?.let {
                    it.infoWindow.close()
                    mapView.overlays.remove(it)
                }

                // Check if the result list is not null and not empty before accessing the first item.
                if (!res.isNullOrEmpty()) {
                    activity.lifecycleScope.launch {
                        // Add a new marker at the clicked position

                        val apiCallTranscriptedLocationNameTrace = FirebasePerformance.getInstance().newTrace("call_openweather_api_via_transcripted_location_name");
                        apiCallTranscriptedLocationNameTrace.start();

                        val current = DownloadUtil.getCurrentDataFromMic(activity, res[0])

                        apiCallTranscriptedLocationNameTrace.stop();

                        if (current != null) {
                            val newMarker = Marker(mapView)

                            newMarker.position = GeoPoint(current.lat!!, current.lon!!)
                            newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            newMarker.infoWindow = CustomMarkerInfoWindow(
                                R.layout.custom_marker_info_window,
                                mapView,
                                current,
                                activity
                            )

                            newMarker.showInfoWindow()

                            mapView.overlays.add(newMarker)
                            clickedMarker = newMarker

                            mapView.controller.setZoom(18.0)
                            mapView.controller.setCenter(newMarker.position)
                        }
                    }
                }
            }
        }
}