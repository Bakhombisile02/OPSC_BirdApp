package com.example.opsc_birdapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc_birdapp.databinding.ActivitySettingsBinding
import com.example.opsc_birdapp.databinding.ActivityViewAllBirdsBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ViewAllBirds : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener  {

    lateinit var toggle: ActionBarDrawerToggle
    lateinit var binding: ActivityViewAllBirdsBinding
    private var radius: String =""
    private var pref: String =""
    private var languagePref: String = ""
    private val database = Firebase.database
    private lateinit var auth : FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var birdList: MutableList<BirdsData>
    private lateinit var loadingProgressBar: ProgressBar



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewAllBirdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingProgressBar = findViewById(R.id.loadingProgressBar)

        auth = FirebaseAuth.getInstance()
        val id = auth.currentUser?.uid ?: ""
        val myRef1 = database.getReference("users").child(id).child("Sightings")

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.navView)

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

        recyclerView = findViewById(R.id.rvBirdListView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        myRef1.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                loadingProgressBar.visibility = View.VISIBLE

                GlobalScope.launch(Dispatchers.IO) {
                    val birdDataList = mutableListOf<BirdsData>()

                    if (snapshot.exists()) {
                        for (birdSnapshot in snapshot.children) {
                            val birdinfo = birdSnapshot.getValue(BirdsData::class.java)
                            birdinfo?.let { birdDataList.add(it) }
                        }
                    }

                    launch(Dispatchers.Main) {
                        loadingProgressBar.visibility = View.GONE
                        updateUI(birdDataList)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                loadingProgressBar.visibility = View.GONE
            }
        })


    }

    override fun onOptionsItemSelected(item: MenuItem):Boolean
    {
        if(toggle.onOptionsItemSelected(item)){
            true
        }
        return super.onOptionsItemSelected(item)

    }
    private fun updateUI(dataList: MutableList<BirdsData>) {
        birdList = dataList
        recyclerView.adapter = DisplayAdapter(dataList as ArrayList<BirdsData>)
    }

    private fun signout(){
        auth.signOut()
        openIntent(applicationContext, "", MainActivity::class.java,radius,pref,languagePref)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_login -> openIntent(applicationContext, "", LogIn::class.java,radius,pref,languagePref)
            R.id.nav_register -> openIntent(applicationContext, "", Register::class.java,radius,pref,languagePref)
            R.id.nav_save_bird -> openIntent(applicationContext, "", SaveBird::class.java,radius,pref,languagePref)
            R.id.nav_currentlocation -> openIntent(applicationContext, "", UserLocation::class.java,radius,pref,languagePref)
            R.id.nav_settings -> openIntent(applicationContext, "", Settings::class.java,radius,pref,languagePref)
            R.id.nav_view_all_birds -> openIntent(applicationContext, "", ViewAllBirds::class.java, radius, pref, languagePref)
            R.id.navTrophyDisplay -> openIntent(applicationContext, "", TrophyDisplay::class.java, radius, pref, languagePref)
            R.id.nav_logout -> signout()

        }
        return true;
    }
}