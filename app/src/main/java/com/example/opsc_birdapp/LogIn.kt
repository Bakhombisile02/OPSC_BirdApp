package com.example.opsc_birdapp



import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.opsc_birdapp.databinding.ActivityLogInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LogIn : AppCompatActivity() {

    private lateinit var birddb: FirebaseAuth
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var binding: ActivityLogInBinding
    private lateinit var auth: FirebaseAuth
    private var radius: String =""
    private var pref: String =""
    private var languagePref: String = ""




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_log_in)
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


        birddb = FirebaseAuth.getInstance()
        auth= FirebaseAuth.getInstance()


        toggle = ActionBarDrawerToggle(this,binding.drawerLayout,R.string.open,R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.navView.setNavigationItemSelectedListener {

            when(it.itemId){
                R.id.nav_login -> openIntent(applicationContext,"",LogIn::class.java,radius,pref,languagePref)
                R.id.nav_register -> openIntent(applicationContext,"",Register::class.java,radius,pref,languagePref)
            }
            true
        }


        binding.btnLog.setOnClickListener{

            val username = binding.txtUsername.text
            val pass = binding.txtPassword.text

            if (!username.toString().isEmpty() && !pass.toString().isEmpty())
            {
                if (TextUtils.isEmpty(username))
                {
                    Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show()

                }

                if (TextUtils.isEmpty(pass))
                {
                    Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show()

                }

                birddb.signInWithEmailAndPassword(username.toString(), pass.toString()).addOnCompleteListener(this)
                { task ->
                    if (task.isSuccessful)
                    {
                        val user = birddb.currentUser

                        openIntent(applicationContext,"",UserLocation::class.java,radius,pref,languagePref)

                    }
                    else
                    {
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
}
