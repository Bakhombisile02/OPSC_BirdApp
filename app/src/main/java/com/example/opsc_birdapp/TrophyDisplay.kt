package com.example.opsc_birdapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.opsc_birdapp.databinding.ActivityTrophyDisplayBinding
import com.example.opsc_birdapp.databinding.ActivityViewAllBirdsBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class TrophyDisplay : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener  {

    private lateinit var binding: ActivityTrophyDisplayBinding
    private  lateinit var toggle: ActionBarDrawerToggle
    private var radius: String =""
    private var pref: String =""
    private var languagePref: String = ""
    private lateinit var auth : FirebaseAuth
    private val database = Firebase.database


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrophyDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val id = auth.currentUser?.uid ?: ""
        val myRef1 = database.getReference("users").child(id).child("Sightings")

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.navView)

        val level1ProgressBar: ProgressBar = findViewById(R.id.level1bar)
        val level2ProgressBar: ProgressBar = findViewById(R.id.level2bar)
        val level3ProgressBar: ProgressBar = findViewById(R.id.level3bar)

        val getTrophyButton1: Button = findViewById(R.id.gettrophy1)
        val getTrophyButton2: Button = findViewById(R.id.gettrophy2)
        val getTrophyButton3: Button = findViewById(R.id.gettrophy3)

        val ImageV1: ImageView = findViewById(R.id.Trophy1)
        val ImageV2: ImageView = findViewById(R.id.Trophy2)
        val ImageV3: ImageView = findViewById(R.id.Trophy3)


        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        navView.bringToFront()
        navView.setNavigationItemSelectedListener(this)
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


        myRef1.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {


                val itemCount = snapshot.childrenCount.toInt()
                val cappedItemCount1 = minOf(itemCount, level1ProgressBar.max)
                val cappedItemCount2 = minOf(itemCount, level2ProgressBar.max)
                val cappedItemCount3 = minOf(itemCount, level3ProgressBar.max)

                level1ProgressBar.progress = cappedItemCount1
                level2ProgressBar.progress = cappedItemCount2
                level3ProgressBar.progress = cappedItemCount3

                val sharedPreferences = getSharedPreferences("your_preference_name", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putInt("itemCount", itemCount)
                editor.apply()
            }

            override fun onCancelled(error: DatabaseError) {


            }
        })

        getTrophyButton1.setOnClickListener{
            if (level1ProgressBar.progress < level1ProgressBar.max) {

                Toast.makeText(this, "You have not achived this level yet", Toast.LENGTH_SHORT).show()

            } else {
                //Trophy animation
                ImageV1.animate().apply {
                    alpha(1f)
                    scaleX(1.5f)
                    scaleY(1.5f)
                    rotation(360f)
                    duration = 1000
                }.withEndAction {
                    ImageV1.animate().apply {
                        scaleX(1f)
                        scaleY(1f)
                        duration = 500
                    }.start()
                }.start()
                Toast.makeText(this, "Yes sir you have achived level 1 !!", Toast.LENGTH_SHORT).show()

            }
        }

        getTrophyButton2.setOnClickListener{
            if (level2ProgressBar.progress < level2ProgressBar.max) {

                Toast.makeText(this, "You have not achived this level yet", Toast.LENGTH_SHORT).show()

            } else {
                //Trophy animation
                ImageV2.animate().apply {
                    alpha(1f)
                    scaleX(1.5f)
                    scaleY(1.5f)
                    rotation(360f)
                    duration = 1000
                }.withEndAction {
                    ImageV2.animate().apply {
                        scaleX(1f)
                        scaleY(1f)
                        duration = 500
                    }.start()
                }.start()
                Toast.makeText(this, "Yes sir you have achived level 2 !!", Toast.LENGTH_SHORT).show()

            }
        }

        getTrophyButton3.setOnClickListener{
            if (level3ProgressBar.progress < level3ProgressBar.max) {

                Toast.makeText(this, "You have not achived this level yet", Toast.LENGTH_SHORT).show()

            } else {
                //Trophy animation
                ImageV3.animate().apply {
                    alpha(1f)
                    scaleX(1.5f)
                    scaleY(1.5f)
                    rotation(360f)
                    duration = 1000
                }.withEndAction {

                    ImageV3.animate().apply {
                        scaleX(1f)
                        scaleY(1f)
                        duration = 500
                    }.start()
                }.start()
                Toast.makeText(this, "Yes sir fully maxed out keep going!!", Toast.LENGTH_SHORT).show()

            }
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.nav_save_bird -> openIntent(applicationContext, "", SaveBird::class.java,radius,pref,languagePref)
            R.id.nav_currentlocation -> openIntent(applicationContext, "", UserLocation::class.java,radius,pref,languagePref)
            R.id.nav_settings -> openIntent(applicationContext, "", Settings::class.java,radius,pref,languagePref)
            R.id.nav_view_all_birds -> openIntent(applicationContext, "", ViewAllBirds::class.java, radius, pref, languagePref)
            R.id.navTrophyDisplay -> openIntent(applicationContext, "", TrophyDisplay::class.java, radius, pref, languagePref)

            R.id.nav_logout -> signout()

        }
        return true;
    }
    override fun onOptionsItemSelected(item: MenuItem):Boolean
    {
        if(toggle.onOptionsItemSelected(item)){
            true
        }
        return super.onOptionsItemSelected(item)

    }
    private fun signout(){
        auth.signOut()
        openIntent(applicationContext, "", MainActivity::class.java,radius,pref,languagePref)
    }
}