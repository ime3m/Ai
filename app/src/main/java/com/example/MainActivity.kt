package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.VoiceViewModel
import com.example.ui.screens.DictionaryScreen
import com.example.ui.screens.ExploreDialectsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.VoiceConversationScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppDestination(val route: String, val label: String, val icon: ImageVector, val tag: String) {
    HOME("home", "Home", Icons.Default.Home, "nav_home"),
    VOICE("voice", "Voice", Icons.Default.Mic, "nav_voice"),
    EXPLORE("explore", "Explore", Icons.Default.Public, "nav_explore"),
    DICTIONARY("dictionary", "Dictionary", Icons.Default.Book, "nav_dictionary"),
    PROFILE("profile", "Profile", Icons.Default.Person, "nav_profile")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }
    val userNotice by viewModel.userFeedbackNotice.collectAsState()

    // Microphone runtime permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                AppDestination.entries.forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = { Text(destination.label) },
                        modifier = Modifier.testTag(destination.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentDestination) {
            AppDestination.HOME -> {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToVoice = { currentDestination = AppDestination.VOICE },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppDestination.VOICE -> {
                VoiceConversationScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppDestination.EXPLORE -> {
                ExploreDialectsScreen(
                    viewModel = viewModel,
                    onStartVoiceChat = { currentDestination = AppDestination.VOICE },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppDestination.DICTIONARY -> {
                DictionaryScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppDestination.PROFILE -> {
                ProfileScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
