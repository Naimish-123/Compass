package com.example.presentation.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.example.domain.haptics.CompassHapticManager
import com.example.presentation.CompassMode
import com.example.presentation.CompassViewModel
import com.example.presentation.ui.components.CalibrationDialog
import com.example.presentation.ui.components.DirectionView
import com.example.presentation.ui.components.HyperOSModeSwitcher
import com.example.presentation.ui.components.HyperOSSettingsSheet
import com.example.presentation.ui.components.LevelView
import com.example.ui.theme.HyperOSBlack
import com.example.ui.theme.HyperOSTextPrimary
import com.example.ui.theme.HyperOSTextSecondary

/**
 * Xiaomi HyperOS Compass Screen (Version 17.1.4.1 aesthetic).
 * Strictly separates the Direction function and Level function.
 * When on Direction: only shows Direction telemetry and dial.
 * When on Level: only shows Spirit Level and tilt measurements.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassScreen(
    viewModel: CompassViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Subtle tactile haptic feedback manager
    val hapticManager = remember(context) { CompassHapticManager(context) }

    LaunchedEffect(viewModel, hapticManager) {
        viewModel.onCardinalCrossed = { direction ->
            hapticManager.performCardinalHaptic(direction)
        }
        viewModel.onLevelAligned = {
            hapticManager.performLevelAlignedHaptic()
        }
    }

    // Permission launcher for Location (used to calculate GPS coordinates and True North declination)
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
            .testTag("hyperos_compass_scaffold"),
        containerColor = HyperOSBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Compass",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HyperOSTextPrimary
                        )
                    )
                },
                actions = {
                    // Sync GPS Location button if location is not yet resolved
                    if (!uiState.locationInfo.hasLocation) {
                        IconButton(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            modifier = Modifier.testTag("hyperos_location_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Enable Location",
                                tint = HyperOSTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Xiaomi HyperOS Enlarged Three-Dot Menu (version 17.1.x feature)
                    IconButton(
                        onClick = { viewModel.openSettingsSheet() },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("hyperos_more_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Settings",
                            tint = HyperOSTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = HyperOSBlack,
                    titleContentColor = HyperOSTextPrimary
                )
            )
        },
        bottomBar = {
            // Xiaomi HyperOS Floating Pill Switcher ("Direction" | "Level")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HyperOSBlack)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                HyperOSModeSwitcher(
                    currentMode = uiState.currentMode,
                    onModeSelected = { mode ->
                        viewModel.setMode(mode)
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HyperOSBlack)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main View Area: Exclusively Direction OR Level
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
            ) {
                AnimatedContent(
                    targetState = uiState.currentMode,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith
                                fadeOut(animationSpec = tween(220))
                    },
                    label = "modeTransition"
                ) { mode ->
                    when (mode) {
                        CompassMode.DIRECTION -> {
                            // Strictly Direction Function
                            DirectionView(
                                uiState = uiState,
                                onDialClick = { viewModel.toggleBearingLock() },
                                onClearBearing = { viewModel.clearLockedBearing() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        CompassMode.LEVEL -> {
                            // Strictly Spirit Level Function
                            LevelView(
                                uiState = uiState,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }

    // Xiaomi HyperOS 17.1.4.1 Settings Bottom Sheet
    if (uiState.showSettingsSheet) {
        HyperOSSettingsSheet(
            uiState = uiState,
            onToggleTrueNorth = { viewModel.toggleTrueNorth() },
            onToggleHaptics = { viewModel.toggleHaptics() },
            onOpenCalibration = { viewModel.showCalibrationDialog() },
            onToggleSimulation = { viewModel.toggleSimulation() },
            onDismiss = { viewModel.dismissSettingsSheet() }
        )
    }

    // Modal Figure-8 Calibration Guide
    if (uiState.showCalibrationDialog) {
        CalibrationDialog(
            accuracy = uiState.accuracy,
            onDismiss = { viewModel.dismissCalibrationDialog() }
        )
    }
}
