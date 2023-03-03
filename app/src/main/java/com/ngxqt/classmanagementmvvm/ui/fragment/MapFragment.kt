package com.ngxqt.classmanagementmvvm.ui.fragment

import android.Manifest
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.JsonArray
import com.ngxqt.classmanagementmvvm.R
import com.ngxqt.classmanagementmvvm.databinding.FragmentMapBinding
import com.ngxqt.classmanagementmvvm.databinding.ToolbarBinding
import com.ngxqt.classmanagementmvvm.utils.Resource
import com.tbruyelle.rxpermissions3.RxPermissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@AndroidEntryPoint
class MapFragment : Fragment(),
    OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener {

    private val viewModel: MapViewModel by viewModels()
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var toolbarBinding: ToolbarBinding
    private lateinit var className: String
    private lateinit var subjectName: String
    private lateinit var position: String
    private var cid: Long? = null

    private lateinit var mMap: GoogleMap
    private var markerPoints = arrayListOf<LatLng>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val classLocation = LatLng(21.004097, 105.844646)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val view = binding.root

        // Hiển thị map
        var mapFragment : SupportMapFragment
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        className = arguments?.getString("className").toString()
        subjectName = arguments?.getString("subjectName").toString()
        position = arguments?.getInt("position", -1).toString()
        cid = arguments?.getLong("cid",-1)!!
        setToolbar()
    }

    /**Hiển thị Toolbar*/
    private fun setToolbar() {
        binding.toolbarMap.apply {
            titleToolbar.setText("Map")
            subtitleToolbar.setText("$className - $subjectName")
            back.setOnClickListener { requireActivity().onBackPressed() }
            save.isInvisible = true
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        markerPoints.add(classLocation)
        mMap.addMarker(MarkerOptions()
            .position(classLocation)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            .title("Classroom D7-505\nLập trình ứng dụng di động\n137364 - ET4710")
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(classLocation, 13F))

        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMyLocationClickListener(this)

        if (mMap.isMyLocationEnabled){
            mMap.setMyLocationEnabled(true)
        } else {
            checkPermissions()
        }

    }

    override fun onMyLocationButtonClick(): Boolean {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if (location != null) {
                    Toast.makeText(requireContext(), "Get Current Location", Toast.LENGTH_SHORT).show()
                    val userLocation = LatLng(location.latitude, location.longitude)
                    if (markerPoints.size > 1) {
                        markerPoints.clear()
                        mMap.clear()
                    }

                    // Adding new item to the ArrayList
                    markerPoints.add(userLocation)

                    // Add new marker to the Google Map Android API V2
                    mMap.addMarker(MarkerOptions()
                        .position(userLocation)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .title("Your Locaion")
                    )
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13F))

                    markerPoints.add(classLocation)

                    // Checks, whether start and end locations are captured
                    if (markerPoints.size >= 2) {
                        //val origin = markerPoints.get(0) as LatLng
                        //val dest = markerPoints.get(1) as LatLng
                        //getDirections("${origin.latitude},${origin.longitude}","${dest.latitude},${dest.longitude}")
                        getDirections("${userLocation.latitude},${userLocation.longitude}","${classLocation.latitude},${classLocation.longitude}")
                    }
                }
            }
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(requireContext(), "${location?.latitude} + ${location?.longitude}", Toast.LENGTH_SHORT).show()
    }

    private fun checkPermissions() {
        val rxPermissions = RxPermissions(this)

        rxPermissions
            .request(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .subscribe { granted ->
                if (granted) {
                    if (mMap != null) {
                        mMap.setMyLocationEnabled(true)
                    }
                } else {
                    Toast.makeText(requireContext(), "Please Allow App To Access Location Permission", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun getDirections(origin: String, dest: String){
        lifecycleScope.launch {
            viewModel.getDirections(origin,dest)
        }
        viewModel.directionsResponseLiveData.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when(it){
                    is Resource.Success -> {
                        Log.d("GETDIRECTIONS_OBSERVER_SUCCESS", it.data?.routes.toString())
                        if (it.data?.status=="OK"){
                            drawRoute(it.data.routes)
                        } else {
                            Toast.makeText(requireContext(), "Get Directions Failure", Toast.LENGTH_LONG).show()
                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), "Get Directions Failure", Toast.LENGTH_LONG).show()
                        Log.d("GETDIRECTIONS_OBSERVER_ERROR", it.data.toString())
                    }
                    is Resource.Loading -> {}
                }
            }
        })
    }

    private fun drawRoute(jRoute: JsonArray){
        val points = arrayListOf<LatLng>()
        val lineOptions = PolylineOptions()

        /** Traversing all routes */
        for (i in 0 until jRoute.size()) {
            val jLegs = JSONArray(JSONObject(jRoute[i].toString()).getString("legs"))
            /** Traversing all legs */
            for (j in 0 until jLegs.length()){
                val jSteps = JSONArray(JSONObject(jLegs[j].toString()).getString("steps"))
                /** Traversing all steps */
                for (k in 0 until jSteps.length()){
                    val polyline = JSONObject(jSteps[k].toString()).getJSONObject("polyline").getString("points")
                    val listPolyline =  decodePoly(polyline)
                    for(poly in listPolyline){
                        points.add(poly)
                    }
                }
                lineOptions.addAll(points)
                lineOptions.width(12f)
                lineOptions.color(Color.RED)
                lineOptions.geodesic(true)
                mMap.addPolyline(lineOptions)
            }
        }


    }

    private fun decodePoly(encoded: String): ArrayList<LatLng> {
        val poly = arrayListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val p = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(p)
        }
        return poly
    }
}