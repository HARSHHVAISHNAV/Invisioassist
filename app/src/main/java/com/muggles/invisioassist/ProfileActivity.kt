package com.muggles.invisioassist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileScreen()
        }
    }
}

@Composable
fun ProfileScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.clickable { /* Handle Back */ }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("User Profile & Settings", fontSize = 20.sp, style = MaterialTheme.typography.headlineMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Info Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_user_placeholder),
                    contentDescription = "User Profile",
                    modifier = Modifier.size(64.dp).background(Color.Gray, CircleShape)
                )
                Text("John Doe", fontSize = 18.sp)
                Text("Regular User", fontSize = 14.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Drop-Down Menu
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileOption(Icons.Rounded.Language, "Language Settings") {}
                ProfileOption(Icons.Rounded.History, "History & Past Orders") {}
                ProfileOption(Icons.Rounded.HelpOutline, "Help & Support") {}
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back Button
            Button(
                onClick = { /* Handle Back */ },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = ButtonDefaults.buttonColors(Color.Black)
            ) {
                Text("Back", color = Color.White)
            }
        }
    }
}

@Composable
fun ProfileOption(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 16.sp)
    }
}
