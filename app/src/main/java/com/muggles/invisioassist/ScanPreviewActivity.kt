package com.muggles.invisioassist

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.lifecycle.LifecycleOwner
import java.util.Locale
import android.speech.tts.TextToSpeech
import androidx.camera.view.PreviewView



class ScanPreviewActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private var capturedImage by mutableStateOf<Bitmap?>(null)
    private lateinit var textToSpeech: TextToSpeech


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.US
            } else {
                Log.e("TTS", "Initialization failed")
            }
        }

        setContent {
            ScanPreviewScreen(
                onProfileClick = { startActivity(Intent(this, ProfileActivity::class.java)) },
                onGalleryClick = { /* TODO: Implement gallery picker */ },
                onCaptureImage = { bitmap ->
                    capturedImage = bitmap
                    processImage(bitmap)
                }
            )
        }
    }

    @Composable
    fun ScanPreviewScreen(
        onProfileClick: () -> Unit,
        onGalleryClick: () -> Unit,
        onCaptureImage: (Bitmap) -> Unit
    ) {
        val context = LocalContext.current
        var isCameraReady by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
            isCameraReady = true
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (isCameraReady) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onCaptureImage = onCaptureImage
                )
            }

            // Overlay UI elements
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
                Button(
                    onClick = { onCaptureImage },
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    colors = ButtonDefaults.buttonColors(Color.Black)
                ) {
                    Text("Repeat", color = Color.White)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    IconButton(onClick = onGalleryClick, modifier = Modifier.size(64.dp)) {
                        Text("📷")
                    }
                    IconButton(onClick = onProfileClick, modifier = Modifier.size(64.dp)) {
                        Text("👤")
                    }
                }
            }
        }
    }

    @Composable
    fun CameraPreview(modifier: Modifier, onCaptureImage: (Bitmap) -> Unit) {
        val context = LocalContext.current
        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                }
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()
                cameraProvider.bindToLifecycle(context as LifecycleOwner, cameraSelector, preview, imageCapture)
                previewView.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        imageCapture.takePicture(ContextCompat.getMainExecutor(ctx), object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val bitmap = image.toBitmap()
                                onCaptureImage(bitmap)
                                image.close()
                            }

                            override fun onError(exc: ImageCaptureException) {
                                Log.e("Camera", "Image capture failed", exc)
                            }
                        })
                    }
                    true
                }
                previewView
            }
        )
    }
    private fun processImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)  // Convert Bitmap to ML Kit InputImage
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val detectedText = visionText.text
                textToSpeech.speak(detectedText, TextToSpeech.QUEUE_FLUSH, null, "")
            }
            .addOnFailureListener { e ->
                Log.e("MLKit", "Text recognition failed", e)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

}
