package com.ngxqt.classmanagementmvvm.data.remote

import com.ngxqt.classmanagementmvvm.data.model.DirectionsResponse
import com.ngxqt.classmanagementmvvm.utils.KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("/maps/api/directions/json")
    suspend fun mapsDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") key: String = KEY
    ): Response<DirectionsResponse>
}