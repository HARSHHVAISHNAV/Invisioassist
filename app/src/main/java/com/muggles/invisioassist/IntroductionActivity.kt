package com.muggles.invisioassist
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class IntroductionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntroductionScreen {
                startActivity(Intent(this@IntroductionActivity, LoginActivity::class.java))
                finish()
            }
        }
    }
}

@Composable
fun IntroductionScreen(onAnimationEnd: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500) // Small delay before fade-in starts
        isVisible = true
        delay(3000) // Show animation for 3 seconds
        onAnimationEnd()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(visible = isVisible, enter = fadeIn()) {
                Text(
                    text = "Welcome to InvisioAssist",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewIntroductionScreen() {
    IntroductionScreen {}
}
