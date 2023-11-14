package com.example.opsc_birdapp

import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.opsc_birdapp.databinding.ActivitySaveBirdBinding
import com.example.opsc_birdapp.databinding.ActivitySettingsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.*

class SaveBird : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var toggle: ActionBarDrawerToggle
    private var radius: String =""
    private var pref: String = ""
    lateinit var binding: ActivitySaveBirdBinding
    private var languagePref: String = ""
    private lateinit var auth : FirebaseAuth
    private val database = Firebase.database
    private var PhotoExist: Boolean = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var storageRef: StorageReference
    private var fileID: String =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaveBirdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storageRef = FirebaseStorage.getInstance().reference.child("images")

        val getResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val intentData: Intent? = result.data
                if (intentData != null) {
                    val bitmap = intentData.extras?.get("data") as? Bitmap
                    if (bitmap != null) {
                        binding.uploadImage.setImageBitmap(bitmap)
                        PhotoExist = true
                    }
                }
            }
        }
        auth = FirebaseAuth.getInstance()
        val id = auth.currentUser?.uid ?: ""
        val myRef = database.getReference("users").child(id).child("Sightings")

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

        binding.uploadImage.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            getResult.launch(intent)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.btnSubmit.setOnClickListener {
            val name = binding.txtbirdname.text.toString()
            val species = binding.txtSpecies.text.toString()
            val bitmap = (binding.uploadImage.drawable).toBitmap()


                // Inform the user that both name and species are required (you can customize this part)
            if (binding.txtbirdname.text?.isEmpty()!!) {
                Toast.makeText(this, "Please enter the name", Toast.LENGTH_SHORT).show();
            } else if (binding.txtSpecies.text?.isEmpty()!!) {
                Toast.makeText(this, "Please enter the species", Toast.LENGTH_SHORT).show();
            } else if (PhotoExist == false) {
                Toast.makeText(this, "Please add an image", Toast.LENGTH_SHORT).show();
            }
            else {

                if (checkLocationPermission()) {
                    // If permission granted, get the user's location

                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            // Got last known location
                            location?.let {
                                // Create a map to hold the data (name, species, user ID, and location)
                                val data = hashMapOf(
                                    "name" to name,
                                    "species" to species,
                                    "user_id" to auth.currentUser?.email,
                                    "location" to "${it.latitude},${it.longitude}",
                                    "photoName" to fileID
                                )

                                // Push the data to the database under a new child node
                                val newSightingRef = myRef.push()
                                newSightingRef.setValue(data)
                                    .addOnSuccessListener {
                                        GlobalScope.launch(Dispatchers.IO) {
                                            uploadImageToStorage(bitmap)
                                        }
                                        Toast.makeText(this, "Sighting Added", Toast.LENGTH_SHORT)
                                            .show()

                                    }.addOnFailureListener {
                                        Toast.makeText(
                                            this,
                                            "Failed to add sighting: ${it.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                } else {
                    // Request location permission
                    requestLocationPermission()
                }
            }




        }

        val drawerLayout : DrawerLayout = findViewById(R.id.drawer_layout)
        val navView : NavigationView = findViewById(R.id.navView)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toggle = ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        navView.bringToFront()
        navView.setNavigationItemSelectedListener(this)

    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            R.id.nav_login -> openIntent(applicationContext, "", LogIn::class.java,radius,pref,languagePref)
            R.id.nav_register -> openIntent(applicationContext, "", Register::class.java,radius,pref,languagePref)
            R.id.nav_save_bird -> openIntent(applicationContext, "", SaveBird::class.java,radius,pref,languagePref)
            R.id.nav_currentlocation -> openIntent(applicationContext, "", UserLocation::class.java,radius,pref,languagePref)
            R.id.nav_settings -> openIntent(applicationContext, "", Settings::class.java,radius,pref,languagePref)
            R.id.nav_view_all_birds -> openIntent(applicationContext, "", ViewAllBirds::class.java, radius, pref, languagePref)
            R.id.navTrophyDisplay -> openIntent(applicationContext, "", TrophyDisplay::class.java, radius, pref, languagePref)

        }

        return true;
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
//-------------------------------------------------------------------------------------------------------------------------------//
private fun checkLocationPermission(): Boolean {
    return (ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED)
}
    //-------------------------------------------------------------------------------------------------------------------------------//
    // Function to request location permission
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }
    //-------------------------------------------------------------------------------------------------------------------------------//
    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now call the location-related functions
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
//-------------------------------------------------------------------------------------------------------------------------------//
    private suspend fun uploadImageToStorage(bitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            // Convert bitmap to a byte array
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            // Create a unique file name for the image
            val fileName = "${UUID.randomUUID()}.jpg"

            fileID = fileName.toString()

            val imageRef = storageRef.child(auth.currentUser?.uid.toString()).child(binding.txtSpecies.text.toString())

            // Upload image to Firebase Storage
            imageRef.putBytes(data)
                .addOnSuccessListener {
                    // Image uploaded successfully

                    Toast.makeText(this@SaveBird, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    binding.uploadImage.setImageResource(R.drawable.baseline_upload)
                    PhotoExist = false
                    binding.txtbirdname.text?.clear()
                    binding.txtSpecies.text?.clear()

                }
                .addOnFailureListener {
                    // Handle errors during image upload

                    Toast.makeText(this@SaveBird, "Failed to upload image: ${it.message}", Toast.LENGTH_SHORT).show()

                }
        }
    }
    //-------------------------------------------------------------------------------------------------------------------------------//
}