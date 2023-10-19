package com.example.opsc_birdapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.opsc_birdapp.databinding.ActivitySettingsBinding
import com.example.opsc_birdapp.databinding.ActivityViewAllBirdsBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ViewAllBirds : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener  {

    lateinit var toggle: ActionBarDrawerToggle
    lateinit var binding: ActivityViewAllBirdsBinding
    private var radius: String =""
    private var pref: String =""
    private var languagePref: String = ""
    private val database = Firebase.database
    private lateinit var auth : FirebaseAuth


    private lateinit var listView: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewAllBirdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        listView = findViewById(R.id.tableLayoutView)

        myRef1.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                // Add rows dynamically
                for (birdDataSnapshot in snapshot.children) {
                    val birdData: BirdsData? =
                        birdDataSnapshot.getValue(BirdsData::class.java)
                    birdData?.let {
                        val rowData = arrayOf(
                            it.location.toString(),
                            it.name.toString(),
                            it.species.toString(),
                            it.user_id.toString()
                        )

                        val row = TableRow(this@ViewAllBirds)
                        val layoutParams = TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT
                        )
                        row.layoutParams = layoutParams

                        for (i in rowData.indices) {
                            val cell = TextView(this@ViewAllBirds)
                            cell.text = rowData[i]
                            cell.setPadding(16, 16, 16, 16)
                            row.addView(cell)
                        }

                        listView.addView(row)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
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
            R.id.nav_logout -> signout()

        }
        return true;
    }
}