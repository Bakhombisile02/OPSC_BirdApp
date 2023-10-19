package com.example.opsc_birdapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.opsc_birdapp.databinding.ActivitySettingsBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.MalformedURLException
import java.net.URL
import kotlin.concurrent.thread

class Settings : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var toggle: ActionBarDrawerToggle
    lateinit var binding: ActivitySettingsBinding
    private var radius: String =""
    private var pref: String =""
    private var languagePref: String = ""

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent.getStringExtra("radius") != null )
        {
            radius = intent.getStringExtra("radius").toString()
        }
        if(intent.getStringExtra("prefer") != null )
        {
            pref =intent.getStringExtra("prefer").toString()
        }
        if(intent.getStringExtra("lanpref") != null)
        {
            languagePref =intent.getStringExtra("lanpref").toString()

        }
        if(intent.getStringExtra("lanpref") == "")
        {
            languagePref ="Common"
        }
        if(intent.getStringExtra("prefer") == "" )
        {
            pref = "false"

        }

        when (pref) {
            "true" -> binding.radioButtonKilometers.isChecked = true
            "false" -> binding.radioButtonMiles.isChecked = true
        }

        // Set the checked state for the language preference
        when (languagePref) {
            "Scientific" -> binding.ScientificName.isChecked = true
            "Common" -> binding.CommonName.isChecked = true
        }
        auth = FirebaseAuth.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.email
        binding.emailLabel.text = userId.toString()

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.navView)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        navView.bringToFront()
        navView.setNavigationItemSelectedListener(this)

        binding.editPassword.setOnClickListener{
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }
        binding.birdsNames.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.ScientificName -> {

                    languagePref = "Scientific"
                }
                R.id.CommonName -> {
                    languagePref = "Common"
                }

            }
        }
        binding.txtRadius.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                radius = s.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                radius = s.toString()
            }
        })
        binding.radioButtons.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonMiles -> {
                    pref = "false"
                    binding.prefLabel.text = "Radius from location (miles)"
                }
                R.id.radioButtonKilometers -> {
                    pref = "true"
                    binding.prefLabel.text = "Radius from location (kilometers)"
                }

            }
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.nav_login -> openIntent(applicationContext, "", LogIn::class.java,radius,pref,languagePref)
            R.id.nav_register -> openIntent(applicationContext, "", Register::class.java,radius,pref,languagePref)
            R.id.nav_save_bird -> openIntent(applicationContext, "", SaveBird::class.java,radius,pref,languagePref)
            R.id.nav_currentlocation -> openIntent(applicationContext, "", UserLocation::class.java,radius,pref,languagePref)
            R.id.nav_settings -> openIntent(applicationContext, "", Settings::class.java,radius,pref,languagePref)
            R.id.nav_view_all_birds -> openIntent(applicationContext, "", ViewAllBirds::class.java, radius, pref, languagePref)
            R.id.nav_logout -> signout()
        }
        return true;
    }

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
}







