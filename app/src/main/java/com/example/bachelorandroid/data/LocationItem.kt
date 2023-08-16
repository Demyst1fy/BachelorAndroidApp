package com.example.bachelorandroid.data

data class LocationItem(
    var id : Int,
    var lat : Double?,
    var lon : Double?,
    var name : String,
    var temp : Double?,
    var humidity : Double?,
    var windSpeed : Double?,
    var pressure : Double?,
    var weatherDescription : String?,
    var icon : String?,
)

