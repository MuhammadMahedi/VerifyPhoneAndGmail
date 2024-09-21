package com.mun.verifyphoneandgmail

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.mun.verifyphoneandgmail.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit
import java.util.Locale

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable Edge-to-Edge for the layout
        enableEdgeToEdge()

        // Set up view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets for proper padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance()

        // Set Firebase locale explicitly based on device language
        setFirebaseLocale()

        val phoneNumber = "+8801798959688"//"+880 1798-959688"
        //val phoneNumber = "+880 1989-531377"
                        //  "+8801798959688"
        // Set up button click listeners
        binding.btnSendOtp.setOnClickListener {
            sendVerificationCode(phoneNumber) // Replace with the actual phone number
        }

        binding.submitbtn.setOnClickListener {
            val otpCode = binding.etPhoneOtp.text.toString()
            if (otpCode.isNotEmpty()) {
                verifyCode(otpCode)
            } else {
                Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Set Firebase language based on device locale
    private fun setFirebaseLocale() {
        val locale = Locale.getDefault()
        auth.setLanguageCode(locale.language)
        Log.d("FirebaseAuth", "Locale set to: ${locale.language}")
    }

    // Send verification code via Firebase
    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()

        // Start phone number verification
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Firebase callback for phone authentication
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d("PhoneAuth", "Verification completed with credentials")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // Log and show the error message
            Log.e("PhoneAuth", "Verification failed", e)
            Toast.makeText(this@MainActivity, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            Log.d("PhoneAuth", "Code sent: $verificationId")
            Toast.makeText(this@MainActivity, "OTP sent to your phone", Toast.LENGTH_SHORT).show()
            this@MainActivity.verificationId = verificationId
        }

        override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
            super.onCodeAutoRetrievalTimeOut(verificationId)
            Toast.makeText(this@MainActivity, "Auto-retrieval timed out. Please enter the code manually.", Toast.LENGTH_LONG).show()
        }
    }

    // Sign in with phone auth credential
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
//                val user = task.result?.user
//                Log.d("PhoneAuth", "User: ${user?.phoneNumber}")
                if (task.isSuccessful) {
                    // Sign in success
                    val user = task.result?.user
                    Toast.makeText(this, "Authentication Success", Toast.LENGTH_SHORT).show()
                    Log.d("PhoneAuth", "User: ${user?.phoneNumber}")
                } else {
                    // Sign in failed
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // Invalid code
                        Toast.makeText(this, "Invalid verification code.", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("PhoneAuth", "Sign-in failed: ${task.exception?.message}")
                    }
                }
            }
    }

    // Manually verify the OTP code
    private fun verifyCode(code: String) {
        if (verificationId != null) {
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        } else {
            Toast.makeText(this, "Verification ID not found. Please request the OTP again.", Toast.LENGTH_SHORT).show()
        }
    }
}
