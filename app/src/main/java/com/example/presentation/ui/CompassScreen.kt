package com.example.presentation.ui

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.CompassViewModel
import com.example.presentation.ui.components.BearingLockCard
import com.example.presentation.ui.components.CalibrationDialog
import com.example.presentation.ui.components.CompassDial
import com.example.presentation.ui.components.HeadingTelemetryDisplay
import com.example.presentation.ui.components.SensorUnavailableBanner
import com.example.ui.theme.CardinalCyan
import com.example.ui.theme.CompassNeedleRed
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassScreen(
    viewModel: CompassViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Setup Haptic feedback on cardinal point crossing
    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    LaunchedEffect(viewModel, vibrator) {
        viewModel.onCardinalCrossed = {
            vibrator?.let { vib ->
                if (vib.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vib.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vib.vibrate(18)
                    }
                }
            }
        }
    }

    // Permission launcher for Location (used to calculate True North magnetic declination)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.refreshLocation()
        }
    }

    // Strictly manage sensor registration with Lifecycle (ON_RESUME / ON_PAUSE)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.startSensors()
                Lifecycle.Event.ON_PAUSE -> viewModel.stopSensors()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopSensors()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("compass_screen_scaffold"),
        containerColor = SlateDark,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = CompassNeedleRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PRECISION COMPASS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                        )
                    }
                },
                actions = {
                    // Location / Declination trigger
                    IconButton(
                        onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        modifier = Modifier.testTag("request_location_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Sync True North Location",
                            tint = if (uiState.locationInfo.hasLocation) CardinalCyan else TextSecondary
                        )
                    }

                    // Calibration trigger
                    IconButton(
                        onClick = { viewModel.showCalibrationDialog() },
                        modifier = Modifier.testTag("open_calibration_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompassCalibration,
                            contentDescription = "Calibrate Compass",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = SlateDark,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val screenMaxWidth = maxWidth
            val isWideScreen = screenMaxWidth > 600.dp
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .widthIn(max = 600.dp)
                    .align(Alignment.TopCenter)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Sensor Unavailable or Demo Simulation Notice
                SensorUnavailableBanner(
                    isHardwareAvailable = uiState.isHardwareAvailable,
                    isSimulated = uiState.isSimulated,
                    currentHeading = uiState.displayHeading,
                    onManualHeadingChange = { viewModel.setManualHeading(it) },
                    onToggleSimulation = { viewModel.toggleSimulation() }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // The Compass Rose / Dial
                val dialSize = if (isWideScreen) 380.dp else (screenMaxWidth * 0.88f)
                Box(
                    modifier = Modifier
                        .size(dialSize)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CompassDial(
                        uiState = uiState,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Real-time numeric telemetry & degree readout
                HeadingTelemetryDisplay(
                    uiState = uiState,
                    onToggleTrueNorth = { viewModel.toggleTrueNorth() },
                    onShowCalibration = { viewModel.showCalibrationDialog() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Course Bearing Locking Card
                BearingLockCard(
                    uiState = uiState,
                    onToggleLock = { viewModel.toggleBearingLock() },
                    onClearLock = { viewModel.clearLockedBearing() }
                )

                // Location coordinates if available
                if (uiState.locationInfo.hasLocation) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = SlateSurface.copy(alpha = 0.5f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = String.format(
                                "%.4f° %s, %.4f° %s • Elev %.0fm",
                                Math.abs(uiState.locationInfo.latitude),
                                if (uiState.locationInfo.latitude >= 0) "N" else "S",
                                Math.abs(uiState.locationInfo.longitude),
                                if (uiState.locationInfo.longitude >= 0) "E" else "W",
                                uiState.locationInfo.altitudeMeters
                            ),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal figure-8 calibration guide
    if (uiState.showCalibrationDialog) {
        CalibrationDialog(
            accuracy = uiState.accuracy,
            onDismiss = { viewModel.dismissCalibrationDialog() }
        )
    }
}
