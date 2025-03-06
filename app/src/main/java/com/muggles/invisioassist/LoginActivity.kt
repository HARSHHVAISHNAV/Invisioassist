package com.muggles.invisioassist

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var signInClient: SignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        signInClient = Identity.getSignInClient(this)

        if (auth.currentUser != null) {
            Log.d("GoogleSignIn", "User already signed in, skipping login")
            startActivity(Intent(this, ScanPreviewActivity::class.java))
            finish()
            return
        }

        setContent {
            LoginScreen()
        }

    }

    @Composable
    fun LoginScreen() {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Welcome to InvisioAssist", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = { signInWithGoogle() }) {
                    Text("Sign in with Google")
                }
            }
        }
    }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val credential = signInClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken

                if (idToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                Log.d("GoogleSignIn", "Sign-in successful")
                                startActivity(Intent(this, ScanPreviewActivity::class.java))
                                finish()
                            } else {
                                Log.e("GoogleSignIn", "Sign-in failed", task.exception)
                            }
                        }
                } else {
                    Log.e("GoogleSignIn", "ID Token is null. Could not sign in.")
                }
            } else {
                Log.e("GoogleSignIn", "Sign-in cancelled.")
            }
        }

    private fun signInWithGoogle() {
        Log.d("GoogleSignIn", "signInWithGoogle() called")

        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId("148376877219-htk0ds8deiii4c44df62498nrliv7gt4.apps.googleusercontent.com") // Ensure this is your Web Client ID
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true) // Optional: Tries to auto-login users
            .build()

        signInClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                Log.d("GoogleSignIn", "beginSignIn success")
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent).build()
                    googleSignInLauncher.launch(intentSenderRequest)
                } catch (e: Exception) {
                    Log.e("GoogleSignIn", "Error launching intent sender", e)
                }
            }
            .addOnFailureListener { e ->
                Log.e("GoogleSignIn", "Sign-in failed: ${e.localizedMessage}", e)
            }
    }

}
