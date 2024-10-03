package com.example.parakeet_application.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import com.example.parakeet_application.R
import com.example.parakeet_application.data.constants.AppConstant
import com.example.parakeet_application.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentHomeBinding
    private var mGoogleMap: GoogleMap? = null
    private lateinit var appPermission: RuntimePermission
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var permissionRequest = mutableListOf<String>()
    private var isLocationPermissionOk: Boolean = false
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var fusedLocationProviderClient: FusedLocationProviderClient?= null
    private lateinit var currentLocation: Location
    private  var currentMarkerOptions: Marker? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private var isTrafficEnable: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appPermission = AppPermissions()
        loadingDialog = LoadingDialog(requireActivity())
        firebaseAuth = Firebase.auth
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
                permissions -> isLocationPermissionOk= permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            if (isLocationPermissionOk){
                setUpGoogleMap()
            }else{
                Snackbar.make(binding.root, "Location permission was denied", Snackbar.LENGTH_LONG).show()
            }
        }
        val mapFragment = (childFragmentManager.findFragmentById(R.id.homeMap) as SupportMapFragment?)
        mapFragment?.getMapAsync(this)

        for(placeModel in AppConstant.placesName){
            val chip = Chip(requireContext())
            chip.text = placeModel.name
            chip.id = placeModel.id
            chip.setPadding(0, 0, 0, 0)
            chip.setTextColor(resources.getColor(R.color.white, null))
            chip.chipBackgroundColor = resources.getColorStateList(R.color.black, null)
            chip.chipIcon= ResourcesCompat.getDrawable(resources, placeModel.drawableId, null)
            chip.isClickable = true
            chip.isCheckedIconVisible = false
            binding.placesGroup.addView(chip)
        }
        binding.enableTraffic.setOnClickListener(){
             if(isTrafficEnable){
                 mGoogleMap?.apply {
                     isTrafficEnabled = true
                     isTrafficEnable = true
                 }
             } else {
                 mGoogleMap?.apply {
                     isTrafficEnabled = true
                     isTrafficEnable = true
                 }
             }
        }
        binding.currentLocation.setOnClickListener(){
            getCurrentLocation()
        }
        binding.btnMapType.setOnClickListener(){
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.apply {
                menuInflater.inflate(R.menu.map_type_menu, menu)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.btnNormal -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                        R.id.btnSatellite -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
                        R.id.btnTerrain -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN

                    }
                    true
                    }
                show()
                }
            }
        }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        when {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                isLocationPermissionOk = true
                setUpGoogleMap()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Location Permission")
                    .setMessage("Parakeet is requesting access to the location permission")
                    .setPositiveButton("Ok"){
                        _, _ -> requestLocation()
                    }.create().show()
            }
            else -> {
                requestLocation()
            }
        }
    }

    private fun requestLocation() {
        permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissionRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        permissionLauncher.launch(permissionRequest.toTypedArray())
    }

    private fun setUpGoogleMap() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mGoogleMap?.isMyLocationEnabled = true
        mGoogleMap?.uiSettings?.isTiltGesturesEnabled = true
        setUpLocationUpdate()
    }

    private fun setUpLocationUpdate() {

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()
        locationCallback = object :LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations){
                    Log.d("TAG", "onLocationResult: ${location.longitude} ${location.latitude}")
                }
            }

            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
            }
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionOk = true
           return
        }
        fusedLocationProviderClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )?.addOnCompleteListener{ task ->
            if (task.isSuccessful){
                Toast.makeText(requireContext(), "Location update start", Toast.LENGTH_SHORT).show()
            }
        }
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionOk = false
             return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            currentLocation = it
            moveCameraToLocation(currentLocation)
        }
    }

    private fun moveCameraToLocation(location: Location) {
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(
            location.latitude,
            location.longitude
        ), 17f)
        val markerOptions = MarkerOptions()
            .position(LatLng(location.latitude, location.longitude))
            .title("Current Location")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            .snippet(firebaseAuth.currentUser?.displayName)
        currentMarkerOptions?.remove()
        currentMarkerOptions= mGoogleMap?.addMarker(markerOptions)
        currentMarkerOptions?.tag = 703
        mGoogleMap?.animateCamera(cameraUpdate)
    }
    private fun stopLocationUpdates(){
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        Log.d("TAG", "stopLocationUpdates: Location Update Stop")
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        if (fusedLocationProviderClient != null){
            startLocationUpdates()
            currentMarkerOptions?.remove()
        }
    }
}