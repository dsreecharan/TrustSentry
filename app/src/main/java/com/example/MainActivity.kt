package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trustsentry.firebase.configureAppCheck
import com.example.trustsentry.ui.components.ResultDetailDialog
import com.example.trustsentry.ui.screens.*
import com.example.ui.theme.*
import com.example.trustsentry.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MANDATORY: Configure App Check before UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TrustSentryMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TrustSentryMainApp(viewModel: MainViewModel) {
    val currentRoute by viewModel.currentRoute.collectAsState()
    val activeResult by viewModel.activeResult.collectAsState()

    // Handle Android system Back button
    BackHandler(enabled = currentRoute != "home") {
        viewModel.navigateTo("home")
    }

    val showBottomNav = currentRoute !in listOf("scan", "upload", "auth")

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = DarkSurface.copy(alpha = 0.95f),
                    contentColor = TextSecondary,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 1.dp, color = DarkSurfaceBorder)
                        .navigationBarsPadding()
                        .testTag("main_bottom_nav")
                ) {
                    val items = listOf(
                        Triple("home", "Home", Icons.Outlined.Home to Icons.Filled.Home),
                        Triple("saved", "Vault", Icons.Outlined.BookmarkBorder to Icons.Filled.Bookmark),
                        Triple("history", "History", Icons.Outlined.History to Icons.Filled.History),
                        Triple("settings", "Settings", Icons.Outlined.Settings to Icons.Filled.Settings)
                    )
                    items.forEach { (route, label, icons) ->
                        val selected = currentRoute == route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { viewModel.navigateTo(route) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) icons.second else icons.first,
                                    contentDescription = label,
                                    tint = if (selected) GoldAccent else TextMuted
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) GoldAccent else TextMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = GoldAccentMuted,
                                selectedIconColor = GoldAccent,
                                unselectedIconColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_item_$route")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            when (currentRoute) {
                "home" -> HomeScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )
                "scan" -> ScanScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo("home") }
                )
                "upload" -> UploadScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo("home") }
                )
                "auth" -> AuthScreen(
                    onAuthSuccess = { viewModel.navigateTo("home") }
                )
                "history" -> HistoryScreen(
                    viewModel = viewModel
                )
                "saved" -> SavedScreen(
                    viewModel = viewModel
                )
                "settings" -> SettingsScreen(
                    viewModel = viewModel
                )
                else -> HomeScreen(
                    viewModel = viewModel,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }

            // Global Result Dialog (shown whenever activeResult is set)
            if (activeResult != null) {
                ResultDetailDialog(
                    result = activeResult!!,
                    onDismiss = { viewModel.clearActiveResult() }
                )
            }
        }
    }
}
