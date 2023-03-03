package com.ngxqt.classmanagementmvvm.repository

import com.ngxqt.classmanagementmvvm.data.model.DirectionsResponse
import com.ngxqt.classmanagementmvvm.data.remote.ApiInterface
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapsRepository @Inject constructor(
    private val mapsApi: ApiInterface
){
    suspend fun mapsDirections(origin: String,destination: String): Response<DirectionsResponse>{
        return mapsApi.mapsDirections(origin,destination)
    }
}