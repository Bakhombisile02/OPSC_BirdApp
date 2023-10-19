package com.example.opsc_birdapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.opsc_birdapp.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {


    lateinit var toggle: ActionBarDrawerToggle
    lateinit var binding: ActivityMainBinding
    private var radius: String =""
    private var pref: String =""
    private var languagePref: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toggle = ActionBarDrawerToggle(this,binding.drawerLayout,R.string.open,R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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

        binding.navView.setNavigationItemSelectedListener {

            when(it.itemId){

                R.id.nav_login -> openIntent(applicationContext,"",LogIn::class.java,radius,pref,languagePref)
                R.id.nav_register -> openIntent(applicationContext,"",Register::class.java,radius,pref,languagePref)

            }
            true
        }

        binding.btnLog.setOnClickListener{
             openIntent(applicationContext,"",LogIn::class.java,radius,pref,languagePref)
        }
        binding.btnReg.setOnClickListener{
            openIntent(applicationContext,"",Register::class.java,radius,pref,languagePref)
        }


    }
    override fun onOptionsItemSelected(item: MenuItem):Boolean
    {
        if(toggle.onOptionsItemSelected(item)){
            true
        }
            return super.onOptionsItemSelected(item)

    }
}