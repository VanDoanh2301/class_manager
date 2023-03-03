package com.ngxqt.classmanagementmvvm.ui.fragment

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngxqt.classmanagementmvvm.data.model.DirectionsResponse
import com.ngxqt.classmanagementmvvm.repository.MapsRepository
import com.ngxqt.classmanagementmvvm.utils.Event
import com.ngxqt.classmanagementmvvm.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapsRepository: MapsRepository,
    @ApplicationContext private val context: Context
) : ViewModel(){
    val _directionsResponseLiveData: MutableLiveData<Event<Resource<DirectionsResponse>>> = MutableLiveData()
    val directionsResponseLiveData: LiveData<Event<Resource<DirectionsResponse>>>
        get() = _directionsResponseLiveData

    var directionsResponse: DirectionsResponse? = null

    fun getDirections(origin: String,destination: String) = viewModelScope.launch(Dispatchers.IO) {
        safeGetDirections(origin,destination)
    }

    private suspend fun safeGetDirections(origin: String,destination: String) {
        val response = mapsRepository.mapsDirections(origin, destination)
        _directionsResponseLiveData.postValue(Event(handleLoginResponse(response)))
    }

    private fun handleLoginResponse(response: Response<DirectionsResponse>): Resource<DirectionsResponse> {
        if (response.isSuccessful) {
            Log.d("DIRECTIONS_RETROFIT_SUCCESS", response.body()?.status.toString())
            response.body()?.let { resultResponse ->
                //Fix it
                /*if (loginResponse == null){
                    loginResponse = resultResponse
                }*/
                return Resource.Success(directionsResponse ?: resultResponse)
            }
        } else {
            Log.e("LOGIN_RETROFIT_ERROR", response.toString())
        }
        return Resource.Error((directionsResponse ?: response.message()).toString())
    }
}