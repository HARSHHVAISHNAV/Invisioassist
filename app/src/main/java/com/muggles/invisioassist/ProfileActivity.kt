package com.muggles.invisioassist

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext

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
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) } // For Language Dropdown
    var selectedLanguage by remember { mutableStateOf("English") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // 🔙 Back Button
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { context.startActivity(Intent(context, ScanPreviewActivity::class.java)) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(text = "User Profile & Settings", fontSize = 20.sp, modifier = Modifier.padding(start = 8.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 👤 User Info
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Person, contentDescription = "Profile Icon", modifier = Modifier.size(80.dp))
            Text(text = "John Doe", fontSize = 22.sp)
            Text(text = "Regular User", fontSize = 14.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 🌍 Language Settings (Dropdown)
        Row(
            modifier = Modifier.fillMaxWidth().clickable { expanded = true }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Language, contentDescription = "Language")
            Spacer(modifier = Modifier.width(12.dp))
            Text("Language Settings", fontSize = 16.sp)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf("Hindi", "English", "Marathi").forEach { language ->
                DropdownMenuItem(
                    text = { Text(language) },
                    onClick = {
                        selectedLanguage = language
                        expanded = false
                    }
                )
            }
        }

        // 📜 History & Past Orders
        Row(
            modifier = Modifier.fillMaxWidth().clickable { /* Future feature */ }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.History, contentDescription = "History")
            Spacer(modifier = Modifier.width(12.dp))
            Text("History & Past Orders", fontSize = 16.sp)
        }

        // ❓ Help & Support (Navigates to HelpSupportActivity)
        Row(
            modifier = Modifier.fillMaxWidth().clickable {
                context.startActivity(Intent(context, HelpSupportActivity::class.java))
            }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.HelpOutline, contentDescription = "Help")
            Spacer(modifier = Modifier.width(12.dp))
            Text("Help & Support", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 🔙 Back Button
        Button(
            onClick = { context.startActivity(Intent(context, ScanPreviewActivity::class.java)) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(Color.Black)
        ) {
            Text("Back", color = Color.White)
        }
    }
}
