package com.example.bachelorandroidapp.utils

import android.content.Context
import android.util.Log
import com.example.bachelorandroidapp.data.LocationItem
import com.example.bachelorandroidapp.MainActivity
import com.example.bachelorandroidapp.R
import com.google.firebase.perf.FirebasePerformance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

object DownloadUtil {
    private fun getResponseFromHttpURL(urlString : String) : String? {
        val con : HttpURLConnection? = null


        try {
            val url = URL(urlString)
            return url.readText()

        } catch (ex : MalformedURLException) {
            Log.e(MainActivity.LOG_TAG, "URL is malformed", ex)
            return null

        } catch (ex : IOException) {
            Log.e(MainActivity.LOG_TAG, "I/O exception.", ex)
            return null

        } catch (ex : Exception) {
            Log.e(MainActivity.LOG_TAG, "An error occurred. Please try again later.", ex)
            return null

        } finally {
            con?.disconnect()
        }
    }

    suspend fun getWeatherDataFromLocationLatLon(context: Context, latitude : Double, longitude : Double) : LocationItem? {
        val apiCallLocationLatLonTrace = FirebasePerformance.getInstance().newTrace("call_openweather_api_via_location_latlon");
        apiCallLocationLatLonTrace.start();

        val response = withContext(Dispatchers.IO) {
            getResponseFromHttpURL(
                "https://api.openweathermap.org/data/2.5/weather?lat=${latitude}" +
                        "&lon=${longitude}&lang=de&units=metric&" +
                        "appid=${context.getString(R.string.apikey)}"
            )
        }

        apiCallLocationLatLonTrace.stop();

        return if (response == null || JSONObject(response).optInt("cod") != 200) {
            null
        } else {
            val jsonObject = JSONObject(response)

            val id = jsonObject.optInt("id")
            val lon = jsonObject.optJSONObject("coord")?.optDouble("lon")
            val lat = jsonObject.optJSONObject("coord")?.optDouble("lat")
            val name = jsonObject.optString("name")
            val temp = jsonObject.optJSONObject("main")?.optDouble("temp")
            val humidity = jsonObject.optJSONObject("main")?.optDouble("humidity")
            val windSpeed = jsonObject.optJSONObject("wind")?.optDouble("speed")
            val pressure = jsonObject.optJSONObject("main")?.optDouble("pressure")
            val weatherDescription = jsonObject.optJSONArray("weather")?.getJSONObject(0)?.optString("description")
            val icon = jsonObject.optJSONArray("weather")?.getJSONObject(0)?.optString("icon")

            LocationItem(
                id = id,
                lon = lon,
                lat = lat,
                name = name,
                temp = temp,
                humidity = humidity,
                windSpeed = windSpeed,
                pressure = pressure,
                weatherDescription = weatherDescription,
                icon = icon
            )
        }
    }

    suspend fun getWeatherDataFromLocationName(context: Context, locationFromMic : String) : LocationItem? {
        val apiCallLocationNameTrace = FirebasePerformance.getInstance().newTrace("call_openweather_api_via_location_name");
        apiCallLocationNameTrace.start();


        val response = withContext(Dispatchers.IO) {
            getResponseFromHttpURL(
                "https://api.openweathermap.org/data/2.5/weather?q=${locationFromMic}" +
                        "&lang=de&units=metric&appid=${context.getString(R.string.apikey)}"
            )
        }

        apiCallLocationNameTrace.stop();

        return if (response == null || JSONObject(response).optInt("cod") != 200) {
            null
        } else {
            val jsonObject = JSONObject(response)

            val id = jsonObject.optInt("id")
            val lon = jsonObject.optJSONObject("coord")?.optDouble("lon")
            val lat = jsonObject.optJSONObject("coord")?.optDouble("lat")
            val name = jsonObject.optString("name")
            val temp = jsonObject.optJSONObject("main")?.optDouble("temp")
            val humidity = jsonObject.optJSONObject("main")?.optDouble("humidity")
            val windSpeed = jsonObject.optJSONObject("wind")?.optDouble("speed")
            val pressure = jsonObject.optJSONObject("main")?.optDouble("pressure")
            val weatherDescription = jsonObject.optJSONArray("weather")?.getJSONObject(0)?.optString("description")
            val icon = jsonObject.optJSONArray("weather")?.getJSONObject(0)?.optString("icon")

            LocationItem(
                id = id,
                lon = lon,
                lat = lat,
                name = name,
                temp = temp,
                humidity = humidity,
                windSpeed = windSpeed,
                pressure = pressure,
                weatherDescription = weatherDescription,
                icon = icon
            )
        }
    }
}