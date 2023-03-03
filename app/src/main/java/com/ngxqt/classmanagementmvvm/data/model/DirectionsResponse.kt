package com.ngxqt.classmanagementmvvm.data.model

import com.google.gson.JsonArray

data class DirectionsResponse(
    var geocoded_waypoints: JsonArray,
    var routes: JsonArray,
    var status: String
)
