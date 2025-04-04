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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
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
import android.speech.tts.TextToSpeech
import androidx.camera.view.PreviewView
import android.provider.MediaStore
import androidx.lifecycle.LifecycleOwner

class ScanPreviewActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private var capturedImage by mutableStateOf<Bitmap?>(null)
    private lateinit var textToSpeech: TextToSpeech
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lastDetectedText by mutableStateOf("") // ✅ Store last detected text

    // ✅ Gallery Image Picker Launcher
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, it)
            capturedImage = bitmap
            processImage(bitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = java.util.Locale.US
            } else {
                Log.e("TTS", "Initialization failed")
            }
        }

        setContent {
            ScanPreviewScreen(
                onProfileClick = { startActivity(Intent(this, ProfileActivity::class.java)) },
                onGalleryClick = { galleryLauncher.launch("image/*") },
                onCaptureImage = { bitmap ->
                    capturedImage = bitmap
                    processImage(bitmap)
                },
                onDoubleTap = {
                    stopTTSAndReset()
                },
                onRepeat = { repeatText() } // ✅ Repeat button action
            )
        }
    }

    @Composable
    fun ScanPreviewScreen(
        onProfileClick: () -> Unit,
        onGalleryClick: () -> Unit,
        onCaptureImage: (Bitmap) -> Unit,
        onDoubleTap: () -> Unit,
        onRepeat: () -> Unit // ✅ Repeat function
    ) {
        val context = LocalContext.current
        var isCameraReady by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
            isCameraReady = true
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { onDoubleTap() } // Detect double-tap to go back
                    )
                }
        ) {
            if (capturedImage == null) {
                if (isCameraReady) {
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        onCaptureImage = onCaptureImage
                    )
                }
            } else {
                Image(
                    bitmap = capturedImage!!.asImageBitmap(),
                    contentDescription = "Captured Image",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
                Button(
                    onClick = { onRepeat() }, // ✅ Calls repeatText()
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    colors = ButtonDefaults.buttonColors(Color.Black)
                ) {
                    Text("Repeat", color = Color.White)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    IconButton(onClick = onGalleryClick, modifier = Modifier.size(64.dp)) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Gallery")
                    }
                    IconButton(onClick = onProfileClick, modifier = Modifier.size(64.dp)) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profile")
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
                cameraProviderFuture.addListener({
                    cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build()

                    cameraProvider?.unbindAll()
                    cameraProvider?.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview, imageCapture)
                }, ContextCompat.getMainExecutor(ctx))

                previewView.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        imageCapture?.let { capture ->
                            capture.takePicture(ContextCompat.getMainExecutor(ctx), object : ImageCapture.OnImageCapturedCallback() {
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
                    }
                    true
                }
                previewView
            }
        )
    }

    private fun processImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                lastDetectedText = visionText.text // ✅ Store detected text
                textToSpeech.speak(lastDetectedText, TextToSpeech.QUEUE_FLUSH, null, "")
            }
            .addOnFailureListener { e ->
                Log.e("MLKit", "Text recognition failed", e)
            }
    }

    private fun repeatText() {
        if (lastDetectedText.isNotEmpty()) {
            textToSpeech.speak(lastDetectedText, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    private fun stopTTSAndReset() {
        textToSpeech.stop()
        capturedImage = null
        lastDetectedText = "" // ✅ Clear text when resetting

        runOnUiThread {
            cameraProvider?.unbindAll()
            setContent {
                ScanPreviewScreen(
                    onProfileClick = { startActivity(Intent(this, ProfileActivity::class.java)) },
                    onGalleryClick = { galleryLauncher.launch("image/*") },
                    onCaptureImage = { bitmap ->
                        capturedImage = bitmap
                        processImage(bitmap)
                    },
                    onDoubleTap = {
                        stopTTSAndReset()
                    },
                    onRepeat = { repeatText() }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}
