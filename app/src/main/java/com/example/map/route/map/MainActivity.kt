package com.example.map.route.map

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.map.R
import com.example.map.route.base.BaseActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task

class MainActivity : BaseActivity(),OnMapReadyCallback{
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var googleMap:GoogleMap?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        if(isGPSPermissionAllowed()){
            getUserLocation()
        }else{
            requestPermission()

        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
         this.googleMap = googleMap
        drawUserMarkerOnMap()

    }
    var userLocation :Location?=null

    fun drawUserMarkerOnMap(){
        if(userLocation == null) return
        if (googleMap==null)return
        val latlng =LatLng(userLocation?.latitude?:0.0,userLocation?.longitude?:0.0)
        val markerOptions = MarkerOptions()
        markerOptions.position (latlng)// get this from user location
        markerOptions.title ("current location")
        googleMap?.addMarker(markerOptions)
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,12.0f))

    }

    val requestGPSPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getUserLocation()

                // Permission is granted. Continue the action or workflow in your
                // app.
            } else {

                   showDialog("we Can't get nearst workspace,"+"to use this feature allow location permission")

            }
        }
//4
    fun requestPermission(){
       if( if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
               shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)
           } else {
               TODO("VERSION.SDK_INT < M")
           }
       ) {
           // show explanation to the user
          // show dialog
           showDialog(message = "Please enable location permission to find nearest workspace"
               , posActionName = "yes"
               ,posAction = { dialogInterface, i ->
                   dialogInterface.dismiss()
                   requestGPSPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)

           },
               negActionName = "No"
               ,negAction = { dialogInterface, i ->

                   dialogInterface.dismiss()
               }

               )
       }
       // low awl mara atlop meno al permission
       else{
           requestGPSPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)

       }
    }

    //1 lazm at2akd al awl al app masmohlo ysta5dm al permision wala la

    fun isGPSPermissionAllowed():Boolean{
        //2 check permission f hanady 3ala al fun de
        // low raga3t permission grande ba hya samht an ana asta5dm al permission
        return ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED

    }
    val locationCallBack:LocationCallback = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            result?:return
            for (location in result.locations){
                Log.e("location update",""+location.latitude+""+location.longitude)
                userLocation =location
                drawUserMarkerOnMap()
            }
        }

    }
    val locationRequest = LocationRequest.create().apply {
        // interval ben kol update 10sawanii
        interval = 10000
        fastestInterval = 5000
        // priority hig accuracy 3lshan ygeb al location mn al gps

        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    val  REQUEST_LOCATION_CODE =120
    @SuppressLint("MissingPermission")
    fun getUserLocation(){
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            fusedLocationClient.requestLocationUpdates(locationRequest,locationCallBack,Looper.getMainLooper())
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@MainActivity,
                        REQUEST_LOCATION_CODE)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }


 // lw 3ayza agep al location bta3ii mara wahda
        //fusedLocationClient.getCurrentLocation().addOnSuccessListener {location: Location?->
           //if(location == null){
              // Log.e("location","null")
               //return@addOnSuccessListener

           //}

            //Log.e("lat",""+location.latitude)
            //Log.e("long",""+location.longitude)

        //}

      // Toast.makeText(this,"we can access user location", Toast.LENGTH_LONG)
           // .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
      super.onActivityResult(requestCode, resultCode, data)
      if(requestCode == REQUEST_LOCATION_CODE){
          if(requestCode == RESULT_OK){
              getUserLocation()
          }
      }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallBack)
    }
}