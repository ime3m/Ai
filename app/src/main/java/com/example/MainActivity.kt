package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.VoiceViewModel
import com.example.ui.screens.ExploreDialectsScreen
import com.example.ui.screens.FuturisticMainScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppDestination(val route: String, val label: String, val tag: String) {
    MAIN("main", "Home", "nav_chat"),
    HISTORY("history", "History", "nav_history"),
    EXPLORE("explore", "Explore", "nav_explore"),
    PROFILE("profile", "Profile", "nav_profile")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable True Edge-to-Edge display extending content behind status and navigation bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            MyApplicationTheme {
                RegionalVoiceApp()
            }
        }
    }
}

@Composable
fun RegionalVoiceApp(viewModel: VoiceViewModel = viewModel()) {
    val context = LocalContext.current
    var currentDestination by remember { mutableStateOf(AppDestination.MAIN) }
    val snackbarHostState = remember { SnackbarHostState() }
    val userNotice by viewModel.userFeedbackNotice.collectAsState()

    // Microphone runtime permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Handled
    }

    LaunchedEffect(Unit) {
        val hasMicPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasMicPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Show feedback notice in snackbar
    LaunchedEffect(userNotice) {
        userNotice?.let { notice ->
            snackbarHostState.showSnackbar(notice)
            viewModel.dismissNotice()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { _ ->
        when (currentDestination) {
            AppDestination.MAIN -> {
                FuturisticMainScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
            AppDestination.HISTORY -> {
                BackHandler { currentDestination = AppDestination.MAIN }
                HistoryScreen(
                    viewModel = viewModel,
                    onNavigateToChat = { currentDestination = AppDestination.MAIN },
                    modifier = Modifier.fillMaxSize()
                )
            }
            AppDestination.EXPLORE -> {
                BackHandler { currentDestination = AppDestination.MAIN }
                ExploreDialectsScreen(
                    viewModel = viewModel,
                    onStartVoiceChat = { currentDestination = AppDestination.MAIN },
                    modifier = Modifier.fillMaxSize()
                )
            }
            AppDestination.PROFILE -> {
                BackHandler { currentDestination = AppDestination.MAIN }
                ProfileScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
