package com.example.opsc_birdapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.opsc_birdapp.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnforgotPassword.setOnClickListener {
            val userEmail = binding.txtUsername.text.toString()

            if (binding.txtUsername.text.toString().isEmpty() || binding.txtPassword.text.toString()
                    .isEmpty() || binding.txtconPassword.text.toString().isEmpty()
            ) {

                Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show()

            } else {
                // Check if the email is valid (optional, but recommended)
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                    // Check if the email exists in Firebase Authentication
                    auth.fetchSignInMethodsForEmail(userEmail)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val signInMethods = task.result?.signInMethods
                                if (signInMethods.isNullOrEmpty()) {
                                    // Email does not exist
                                    Toast.makeText(this, "Email does not exist", Toast.LENGTH_SHORT)
                                        .show()
                                } else {
                                    // Email exists, now update the password
                                    val newPassword = binding.txtPassword.text.toString()
                                    val userId = auth.currentUser

                                    if (binding.txtPassword.text.toString() == binding.txtconPassword.text.toString()) {
                                        userId!!.updatePassword(newPassword)
                                            .addOnCompleteListener { updateTask ->
                                                if (updateTask.isSuccessful) {
                                                    // Password updated successfully
                                                    Toast.makeText(
                                                        this,
                                                        "Password updated successfully",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    auth.signOut()
                                                    val intent = Intent(this, LogIn::class.java)
                                                    startActivity(intent)
                                                } else {
                                                    // Handle password update failure (e.g., show an error message)
                                                    Toast.makeText(
                                                        this,
                                                        "Password update failed",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Passwords don't match",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                }
                            } else {
                                // Handle fetchSignInMethodsForEmail failure (e.g., show an error message)
                                Toast.makeText(
                                    this,
                                    "Failed to fetch sign-in methods",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    // Invalid email address, show an error message
                    Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
