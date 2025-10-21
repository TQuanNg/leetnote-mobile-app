package com.example.leetnote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.leetnote.ui.components.GuidancePopup
import com.example.leetnote.ui.navigation.NavigationGraph
import com.example.leetnote.ui.navigation.Screen

@Composable
fun MainScreen(
    modifier: Modifier
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentScreen = Screen.allScreens.find { it.route == currentRoute } ?: Screen.Home
    val canNavigateBack = navController.previousBackStackEntry != null &&
            currentRoute !in Screen.bottomNavScreens.map { it.route }
    val showTopBar = currentRoute !in listOf(
        Screen.Login.route,
        Screen.Signup.route,
        Screen.Onboarding.route,
        Screen.Splash.route,
        Screen.EvaluationDetail.route
    )

    Scaffold(
        topBar = {
            if (showTopBar) {
                AppBar(
                    currentScreen = currentScreen,
                    canNavigateBack = canNavigateBack,
                    navigateUp = { navController.navigateUp() },
                    navController = navController,
                    modifier = modifier,
                )
            }
        },
        bottomBar = {
            if (currentRoute in Screen.bottomNavScreens.map { it.route}) {
                BottomNavBar(navController = navController)
            }

        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavigationGraph(navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    currentScreen: Screen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    var showGuidancePopup by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(
            text = currentScreen.title,
            fontWeight = FontWeight.Bold
        ) },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back button",
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF6B83DA), // background color
            titleContentColor = Color.White,     // title text color
            navigationIconContentColor = Color.White // nav icon color
        ),
        actions = {
            currentScreen.topIconRes?.let { iconRes ->
                IconButton(onClick = {
                    when (currentScreen) {
                        is Screen.Home -> {
                            navController.navigate(Screen.Settings.route)
                        }
                        is Screen.Solving -> {
                            showGuidancePopup = true
                        }
                        else -> {}
                    }
                }) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = "Top icon",
                    )
                }
            }
        }
    )

    // Show guidance popup for solving screen
    if (currentScreen is Screen.Solving) {
        GuidancePopup(
            isVisible = showGuidancePopup,
            onDismiss = { showGuidancePopup = false }
        )
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(Screen.Home, Screen.Profile, Screen.Learning)
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    Column {
        // Top border line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp) // thickness of the border
                .background(Color.Black) // border color (neo-brutalism style)
        )

        NavigationBar {
            items.forEach { screen ->
                NavigationBarItem(
                    selected = currentRoute == screen.route,
                    onClick = {
                        if (currentRoute != screen.route) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        screen.iconRes?.let {
                            Icon(
                                painter = painterResource(id = it),
                                contentDescription = screen.title
                            )
                        }
                    },
                    label = { Text(screen.title) }
                )
            }
        }
    }


}