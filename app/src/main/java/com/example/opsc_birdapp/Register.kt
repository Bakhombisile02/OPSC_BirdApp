package com.example.opsc_birdapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.opsc_birdapp.databinding.ActivityRegisterBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class Register : AppCompatActivity() {

    private lateinit var birddb: FirebaseAuth
    private val database = Firebase.database
    private val myRef1 = database.getReference("users")
    private var radius: String =""
    private var pref: String =""
    private lateinit var binding: ActivityRegisterBinding
    private var fullname: String = ""
    private var email: String = ""
    private var pass: String = ""
    private var conpass: String = ""
    private var terms: Boolean = false
    private var languagePref: String = ""

    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)


        birddb = FirebaseAuth.getInstance()
        val drawerLayout : DrawerLayout = findViewById(R.id.drawer_layout)
        val navView : NavigationView = findViewById(R.id.navView)

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


        toggle = ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {

            when(it.itemId){

                R.id.nav_login -> openIntent(applicationContext,"",LogIn::class.java,radius,pref,languagePref)
                R.id.nav_register -> openIntent(applicationContext,"",Register::class.java,radius,pref,languagePref)
                R.id.nav_save_bird -> openIntent(applicationContext,"",SaveBird::class.java,radius,pref,languagePref)
            }
            true
        }

        binding.btnSubmit.setOnClickListener{
            fullname = binding.txtName.text.toString()
            email = binding.txtEmail.text.toString()
            pass = binding.txtPassword.text.toString()
            conpass = binding.txtConfirmPassword.text.toString()

            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            {
                Toast.makeText(this,"Not an email address", Toast.LENGTH_SHORT).show()
            }

            if(!binding.checkBoxTerms.isChecked){
                Toast.makeText(this,"Review Terms", Toast.LENGTH_SHORT).show()
            }
            if(pass != conpass )
            {
                Toast.makeText(this,"Passwords do not match",Toast.LENGTH_SHORT).show()
            }

            if(!fullname.isEmpty() && !email.isEmpty() && !pass.isEmpty() && !conpass.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()  && pass == conpass && binding.checkBoxTerms.isChecked)
            {
                birddb.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {

                            val user = birddb.currentUser

                            if (user != null) {
                                UserData(user.uid.toString(),fullname,email)
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT,).show()
                        }
                    }
            }
        }

    }
    override fun onOptionsItemSelected(item: MenuItem):Boolean
    {
        if(toggle.onOptionsItemSelected(item)){
            true
        }
        return super.onOptionsItemSelected(item)

    }

    fun UserData(userID: String ,name:String,ema:String)
    {
        val data = AddData(name,ema)
        val childRef = myRef1.child(userID)

        childRef.setValue(data).addOnSuccessListener {
            Toast.makeText(this, "Registered", Toast.LENGTH_SHORT).show()
            openIntent(applicationContext,"",LogIn::class.java,radius,pref,languagePref)
        }
    }

}