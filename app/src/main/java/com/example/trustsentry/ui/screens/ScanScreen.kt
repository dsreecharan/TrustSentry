package com.example.trustsentry.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.trustsentry.model.ArtifactScenario
import com.example.trustsentry.scanner.BarcodeHelper
import com.example.trustsentry.scanner.QrCodeAnalyzer
import com.example.ui.theme.*
import com.example.trustsentry.ui.viewmodel.MainViewModel
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var isTorchOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var isBackCamera by remember { mutableStateOf(true) }

    // Test QR Modal State
    var showSampleModal by remember { mutableStateOf(false) }
    var modalScenario by remember { mutableStateOf(ArtifactScenario.VALID_W3C_VC) }
    var generatedQrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Photo Picker for decoding QR from gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                val decodedText = BarcodeHelper.decodeBitmap(bitmap)
                if (decodedText != null) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.verifyScannedPayload(decodedText)
                } else {
                    Toast.makeText(context, "No QR code or barcode detected in image. Try Document Inspector.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Generate QR when modal scenario changes
    LaunchedEffect(modalScenario, showSampleModal) {
        if (showSampleModal) {
            generatedQrBitmap = BarcodeHelper.generateQrBitmap(modalScenario.samplePayload, size = 512)
        }
    }

    // Laser Animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserProgress"
    )

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface.copy(alpha = 0.95f))
                    .border(width = 1.dp, color = DarkSurfaceBorder)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp))
                        .testTag("scan_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TRUSTSENTRY SCANNER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 1.5.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (hasCameraPermission) StatusTrusted else StatusReview, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasCameraPermission) "HARDWARE SENSOR ACTIVE" else "STANDBY / SIMULATOR",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = GoldAccent,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Torch Button
                    IconButton(
                        onClick = {
                            isTorchOn = !isTorchOn
                            cameraControl?.enableTorch(isTorchOn)
                        },
                        enabled = hasCameraPermission,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isTorchOn) GoldAccent.copy(alpha = 0.25f) else DarkSurfaceElevated,
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (isTorchOn) GoldAccent else DarkSurfaceBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .testTag("scan_torch_button")
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Torch",
                            tint = if (isTorchOn) GoldAccent else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Camera Switch (Front/Back)
                    IconButton(
                        onClick = { isBackCamera = !isBackCamera },
                        enabled = hasCameraPermission,
                        modifier = Modifier
                            .size(36.dp)
                            .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp))
                            .testTag("scan_flip_camera_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = "Flip Camera",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        containerColor = ObsidianBlack,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Main Viewfinder Viewport
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.Black)
                    .border(1.5.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    key(isBackCamera) {
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx)
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.surfaceProvider = previewView.surfaceProvider
                                    }
                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                        .also { analysis ->
                                            analysis.setAnalyzer(
                                                cameraExecutor,
                                                QrCodeAnalyzer(
                                                    onCodeDetected = { rawCode ->
                                                        ContextCompat.getMainExecutor(ctx).execute {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            viewModel.verifyScannedPayload(rawCode)
                                                        }
                                                    }
                                                )
                                            )
                                        }

                                    val cameraSelector = if (isBackCamera) {
                                        CameraSelector.DEFAULT_BACK_CAMERA
                                    } else {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    }

                                    try {
                                        cameraProvider.unbindAll()
                                        val cam = cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            cameraSelector,
                                            preview,
                                            imageAnalysis
                                        )
                                        cameraControl = cam.cameraControl
                                    } catch (_: Exception) {
                                        // Camera unavailable
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    // Standby UI when camera permission is not yet granted
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Camera Access Required",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Grant camera permission for live hardware scanning or simulate scenarios below",
                            fontSize = 11.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Enable Camera", color = Color(0xFF080808), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Laser Scan Sweep Line
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(2.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = (280 * laserProgress).dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    GoldAccent,
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Corner Reticle Brackets
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    // Top-Left
                    Box(modifier = Modifier.align(Alignment.TopStart).size(20.dp, 20.dp)) {
                        Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(GoldAccent))
                        Box(modifier = Modifier.fillMaxHeight().width(3.dp).background(GoldAccent))
                    }
                    // Top-Right
                    Box(modifier = Modifier.align(Alignment.TopEnd).size(20.dp, 20.dp)) {
                        Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(GoldAccent))
                        Box(modifier = Modifier.align(Alignment.TopEnd).fillMaxHeight().width(3.dp).background(GoldAccent))
                    }
                    // Bottom-Left
                    Box(modifier = Modifier.align(Alignment.BottomStart).size(20.dp, 20.dp)) {
                        Box(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().height(3.dp).background(GoldAccent))
                        Box(modifier = Modifier.fillMaxHeight().width(3.dp).background(GoldAccent))
                    }
                    // Bottom-Right
                    Box(modifier = Modifier.align(Alignment.BottomEnd).size(20.dp, 20.dp)) {
                        Box(modifier = Modifier.align(Alignment.BottomEnd).fillMaxWidth().height(3.dp).background(GoldAccent))
                        Box(modifier = Modifier.align(Alignment.TopEnd).fillMaxHeight().width(3.dp).background(GoldAccent))
                    }
                }

                // Center Guide Target
                Icon(
                    imageVector = Icons.Default.CenterFocusWeak,
                    contentDescription = null,
                    tint = GoldAccent.copy(alpha = 0.25f),
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Point camera at any QR Code or Barcode",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "Supports TS1 Signed QR • W3C VC • GS1 Link • Barcode 1D",
                fontSize = 11.sp,
                color = TextMuted,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Pick Image & Show Test QRs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Upload Image Button
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("upload_qr_image_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(DarkSurfaceBorder, DarkSurfaceBorder)))
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload QR", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                // Show Test QRs Button
                Button(
                    onClick = { showSampleModal = true },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp)
                        .testTag("show_test_qrs_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Icon(Icons.Outlined.QrCode, contentDescription = null, tint = Color(0xFF080808), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Show Test QRs", fontSize = 12.sp, color = Color(0xFF080808), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Instant Test Scenarios Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .background(DarkSurface, RoundedCornerShape(18.dp))
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INSTANT TEST SCENARIOS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "1-CLICK VERIFY",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scenario grid items
                val scenarios = listOf(
                    ArtifactScenario.VALID_W3C_VC,
                    ArtifactScenario.OPENID_VP,
                    ArtifactScenario.GS1_SIGNED_LINK,
                    ArtifactScenario.DIGILOCKER_DOC,
                    ArtifactScenario.VALID_TS1,
                    ArtifactScenario.TAMPERED_TS1,
                    ArtifactScenario.REVOKED_CERT,
                    ArtifactScenario.RETAIL_BARCODE
                )

                scenarios.chunked(2).forEach { rowScenarios ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowScenarios.forEach { sc ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.loadScenario(sc)
                                    },
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = sc.title,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = sc.tag,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = GoldAccentLight
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Modal: Scannable QR Codes on Screen
    if (showSampleModal) {
        AlertDialog(
            onDismissRequest = { showSampleModal = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.5.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SCANNABLE TEST CODES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            letterSpacing = 1.sp
                        )
                        IconButton(onClick = { showSampleModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Point another phone's camera at this screen, or test with 1-click verify!",
                        fontSize = 11.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scenario Selector Dropdown or buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = modalScenario.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = modalScenario.tag,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = GoldAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Rendered QR Code
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (generatedQrBitmap != null) {
                            Image(
                                bitmap = generatedQrBitmap!!.asImageBitmap(),
                                contentDescription = "Scannable QR",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            CircularProgressIndicator(color = GoldAccent)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scenario Selector Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Payload", modalScenario.samplePayload))
                                Toast.makeText(context, "Payload copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Payload", fontSize = 11.5.sp, color = TextPrimary)
                        }

                        Button(
                            onClick = {
                                showSampleModal = false
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.loadScenario(modalScenario)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("Simulate Scan", fontSize = 11.5.sp, color = Color(0xFF080808), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
