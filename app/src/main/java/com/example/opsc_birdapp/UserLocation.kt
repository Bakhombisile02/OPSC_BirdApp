package com.example.opsc_birdapp

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.opsc_birdapp.databinding.ActivityUserLocationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.SphericalUtil
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.*


class UserLocation : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    OnMapReadyCallback {

    lateinit var binding: ActivityUserLocationBinding
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mGoogleMap: GoogleMap? = null
    private var fiveDaylList = mutableListOf<EbirdData>()
    private val dataReadyLatch = CountDownLatch(1)
    private val selectedBirdName: String = ""
    private var radius: String = "0"
    private var pref: String = ""
    private var languagePref: String = ""
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val database = Firebase.database
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()


        if (intent.getStringExtra("radius") != null) {
            radius = intent.getStringExtra("radius").toString()
        }
        if (intent.getStringExtra("prefer") != null) {
            pref = intent.getStringExtra("prefer").toString()
        }
        if (intent.getStringExtra("lanpref") != null) {
            languagePref = intent.getStringExtra("lanpref").toString()
        }
        if (intent.getStringExtra("lanpref") == "") {
            languagePref = "Common"
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.navView)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        navView.bringToFront()
        navView.setNavigationItemSelectedListener(this)




        // Initialize fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Check and request location permission at runtime if needed
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
        } else {
            // Permission granted, proceed with getting location
            getAndDisplayLocation()

        }

        executeSpecificCode()

    }

    //------------------------------------------------------------------------------------------------------------------------------//
    private fun executeSpecificCode() {
        GlobalScope.launch(Dispatchers.IO) {
            val weather = try {
                buildURLForBird()?.readText()
            } catch (e: Exception) {
                return@launch
            }

            launch(Dispatchers.Main) {
                val distance = radius.toDoubleOrNull()

                if (distance == null) {
                    consumeJson(weather, 20.0)
                }
                else {
                    consumeJson(weather, distance)
                }
            }
        }
    }




    //--------------------------------------------------------------------------------------------------------------------------//
    private fun requestLocationPermission() {

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    //------------------------------------------------------------------------------------------------------------------------------//
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with getting location
                getAndDisplayLocation()
            } else {
                // Permission denied, show a dialog to the user
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    AlertDialog.Builder(this)
                        .setMessage("This app requires location permission to work properly.")
                        .setPositiveButton("Ask again") { _, _ ->
                            requestLocationPermission()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    //--------------------------------------------------------------------------------------------------------------------------------------------------------------------//
    private suspend fun consumeJson(birdJSON: String?, distanceKm: Double) {
        try {
            val id = auth.currentUser?.uid ?: ""
            val myRef = database.getReference("users").child(id).child("Sightings")

            val LOGGING_TAG = "birdDATA"

            birdJSON?.let { json ->
                val rootWeatherData = JSONArray(json)
                val userThreads = mutableListOf<Thread>()

                for (i in 0 until rootWeatherData.length()) {
                    val birdData = rootWeatherData.getJSONObject(i)
                    val forecastObject = EbirdData()

                    val bname = when (languagePref.toString()) {
                        "Common" -> birdData.getString("comName")
                        "Scientific" -> birdData.getString("sciName")
                        else -> ""
                    }

                    forecastObject.b_name = bname

                    forecastObject.b_latitude = birdData.getString("lat")
                    forecastObject.b_longitude = birdData.getString("lng")

                    val userThread = thread {
                        try {
                            buildURLForLocation(forecastObject.b_latitude, forecastObject.b_longitude)?.readText()?.let { weather ->
                                val rootWeatherData = JSONObject(weather)
                                val LocData = rootWeatherData.getJSONArray("features")
                                val d = LocData.getJSONObject(0).getJSONObject("properties").getJSONObject("geocoding").getString("label")
                                forecastObject.b_location = d
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    userThreads.add(userThread)

                    fiveDaylList.add(forecastObject)
                }

                myRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

                        dataSnapshot.children.forEach { snapshot ->
                            val birdDataMap = snapshot.value as? Map<*, *>
                            birdDataMap?.let {
                                val name = it["name"] as? String
                                val species = it["species"] as? String
                                val location = it["location"] as? String
                                val userId = it["user_id"] as? String

                                if (name != null && species != null && location != null && userId != null) {
                                    val locationParts = location.split(",")
                                    val latitude = locationParts[0]
                                    val longitude = locationParts[1]

                                    if (currentUserEmail == userId) {
                                        val ebirdData = EbirdData()
                                        ebirdData.b_name = name
                                        ebirdData.b_latitude = latitude
                                        ebirdData.b_longitude = longitude
                                        fiveDaylList.add(ebirdData)
                                    }
                                }
                            }
                        }
                        showMarkersForLocations(distanceKm)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w("FirebaseDB", "Failed to read value.", databaseError.toException())
                    }
                })

                userThreads.forEach(Thread::interrupt)
            }

            Log.d("LocationData", "Size of fiveDaylList: ${fiveDaylList.size}")
            fiveDaylList.forEach { location ->
                Log.d(
                    "LocationData",
                    "Name: ${location.b_name}, Lat: ${location.b_latitude}, Lng: ${location.b_longitude}"
                )
            }

            dataReadyLatch.countDown()
            showMarkersForLocations(distanceKm)

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }



    //------------------------------------------------------------------------------------------------------------------------------------------------------------------//


    private fun isWithinDistance(
        birdLat: Double,
        birdLng: Double,
        myLat: Double,
        myLng: Double,
        distanceKm: Double
    ): Boolean {
        val R = 6371 // Radius of the earth in km
        val dLat = Math.toRadians(myLat - birdLat)
        val dLon = Math.toRadians(myLng - birdLng)
        val a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(birdLat)) * Math.cos(
                Math.toRadians(myLat)
            ) * Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = R * c // Distance in km
        return distance <= distanceKm
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------//
    private fun getAndDisplayLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mGoogleMap?.isMyLocationEnabled = true

                val locationRequest = LocationRequest.create().apply {
                    interval = 10000
                    fastestInterval = 5000
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        for (location in locationResult.locations) {
                            val currentLatLng = LatLng(location.latitude, location.longitude)

                            mGoogleMap?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    currentLatLng,
                                    10f
                                )
                            )
                        }

                        // Remove location updates after the first location is received
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }

                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }
        } catch (se: SecurityException) {
            se.printStackTrace()
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------//
    private fun showMarkersForLocations(distanceKm: Double) {
        try {
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Loading...")
            progressDialog.setCancelable(false)

            progressDialog.show()

            mGoogleMap?.let {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Get current location
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val currentLocation = LatLng(location.latitude, location.longitude)

                            // Filter and display markers within the specified distance
                            CoroutineScope(Dispatchers.Default).launch {
                                val jobs = mutableListOf<Job>()
                                for (location in fiveDaylList) {
                                    val birdLat = location.b_latitude.toDouble()
                                    val birdLng = location.b_longitude.toDouble()
                                    if (isWithinDistance(
                                            birdLat,
                                            birdLng,
                                            currentLocation.latitude,
                                            currentLocation.longitude,
                                            distanceKm
                                        )
                                    ) {
                                        jobs.add(
                                            launch(Dispatchers.Main) {
                                                val latLng = LatLng(birdLat, birdLng)
                                                it.addMarker(
                                                    MarkerOptions().position(latLng)
                                                        .title(location.b_name.toString())
                                                        .snippet(location.b_location)
                                                )
                                            }
                                        )
                                    }
                                }
                                jobs.joinAll() // Wait for all markers to be added

                                // Draw circle around current location
                                drawCircle(distanceKm)
                            }
                        }
                    }
                } else {
                    requestLocationPermission()
                }
            } ?: run {
                Log.e("MapError", "mGoogleMap is null")
            }
            progressDialog.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    //---------------------------------------------------------------------------------------------------------------------------//
    private fun drawCircle(distanceKm: Double) {
        try {
            mGoogleMap?.let {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Get current location
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val currentLocation = LatLng(location.latitude, location.longitude)

                            // Draw a circle with radius
                            val circleOptions = CircleOptions()
                                .center(currentLocation)
                                .radius(distanceKm * 1000) // Convert km to meters
                                .strokeWidth(1f)
                                .strokeColor(Color.BLUE)
                                .fillColor(Color.parseColor("#500084d3")) // Semi-transparent blue color

                            it.addCircle(circleOptions)
                        }
                    }
                } else {
                    requestLocationPermission()
                }
            } ?: run {
                Log.e("MapError", "mGoogleMap is null")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    //------------------------------------------------------------------------------------------------------------------------------//
    override fun onMapReady(googleMap: GoogleMap) {
        try {
            mGoogleMap = googleMap

            // Check if location permission is granted
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mGoogleMap?.isMyLocationEnabled = true
            }

            mGoogleMap?.uiSettings?.apply {
                isZoomControlsEnabled = true
                isCompassEnabled = true
                isRotateGesturesEnabled = true
                isScrollGesturesEnabled = true
                isTiltGesturesEnabled = true
                isZoomGesturesEnabled = true
                isMapToolbarEnabled = true
                isIndoorLevelPickerEnabled = true
                isScrollGesturesEnabledDuringRotateOrZoom = true

            }


            // Set up a map long press listener
            mGoogleMap?.setOnMapLongClickListener { point ->
                // When the map is long pressed, get the coordinates and show a marker

                mGoogleMap?.addMarker(MarkerOptions().position(point))

                // Get the current location
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val currentLocation = LatLng(location.latitude, location.longitude)

                        // Get directions from current location to the selected point
                        getDirections(currentLocation, point)
                    }
                }
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    //------------------------------------------------------------------------------------------------------------------------------//
    private fun getDirections(origin: LatLng, destination: LatLng) {

        try {
            val geoApiContext = GeoApiContext.Builder()
                .apiKey("AIzaSyAmCbmg5Xx4LLf--kPMEr_fqPuHQWQXU74")
                .build()

            try {
                val directionsResult: DirectionsResult = DirectionsApi.getDirections(
                    geoApiContext,
                    "${origin.latitude},${origin.longitude}",
                    "${destination.latitude},${destination.longitude}"
                ).await()
                Toast.makeText(this, "Directions retrieved", Toast.LENGTH_SHORT).show()

                val route = directionsResult.routes[0] // Assuming you want the first route

                // Clear any existing polylines
                mGoogleMap?.clear()

                // Extract and draw the polyline
                val decodedPath = PolylineEncoding.decode(route.overviewPolyline.encodedPath)
                val points = ArrayList<LatLng>()

                var totalDistance = 0.0 // Initialize total distance

                for (i in 0 until decodedPath.size - 1) {
                    val point1 = decodedPath[i]
                    val point2 = decodedPath[i + 1]

                    val latLng1 = LatLng(point1.lat, point1.lng)
                    val latLng2 = LatLng(point2.lat, point2.lng)

                    val distance = SphericalUtil.computeDistanceBetween(latLng1, latLng2)
                    totalDistance += distance
                }

                val totalDistanceKilometers = totalDistance / 1000.0 // Convert meters to kilometers
                val totalDistanceMiles =
                    totalDistanceKilometers * 0.621371 // Convert kilometers to miles

                if (pref == "true") {
                    // Display distance in kilometers
                    binding.distanceLabel.text =
                        "Total distance: $totalDistanceKilometers Kilometers"
                } else {
                    // Display distance in miles
                    binding.distanceLabel.text = "Total distance: $totalDistanceMiles Miles"
                }

                for (point in decodedPath) {
                    points.add(LatLng(point.lat, point.lng))
                }

                mGoogleMap?.addPolyline(
                    PolylineOptions()
                        .addAll(points)
                        .color(Color.BLUE)
                        .width(10f)
                )
                mGoogleMap?.addMarker(
                    MarkerOptions()
                        .position(LatLng(destination.latitude, destination.longitude))
                        .title("Destination")
                )

            } catch (e: Exception) {
                println("An error occurred: ${e.message}")
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------//
    private fun showDirectionsInMaps(origin: LatLng, destination: LatLng) {
        try {
            val uri =
                "http://maps.google.com/maps?saddr=${origin.latitude},${origin.longitude}&daddr=${destination.latitude},${destination.longitude}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//------------------------------------------------------------------------------------------------------------------------------//


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_login -> openIntent(applicationContext, "", LogIn::class.java, radius, pref, languagePref)
            R.id.nav_register -> openIntent(applicationContext, "", Register::class.java, radius, pref, languagePref)
            R.id.nav_save_bird -> openIntent(applicationContext, "", SaveBird::class.java, radius, pref, languagePref)
            R.id.nav_currentlocation -> openIntent(applicationContext, "", UserLocation::class.java, radius,pref, languagePref)
            R.id.nav_settings -> openIntent(applicationContext, "", Settings::class.java, radius, pref, languagePref)
            R.id.nav_logout -> openIntent(applicationContext, "", MainActivity::class.java, radius, pref, languagePref)
            R.id.nav_view_all_birds -> openIntent(applicationContext, "", ViewAllBirds::class.java, radius, pref, languagePref)
            R.id.nav_logout -> signout()
        }
        return true
    }

    //------------------------------------------------------------------------------------------------------------------------------//
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun signout(){
        auth.signOut()
        openIntent(applicationContext, "", MainActivity::class.java,radius,pref,languagePref)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}

