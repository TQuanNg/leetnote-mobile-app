package com.example.leetnote.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.leetnote.ui.screens.home.HomeScreen
import com.example.leetnote.ui.screens.learning.LearningResourcesScreen
import com.example.leetnote.ui.screens.problem.ProblemScreen
import com.example.leetnote.ui.screens.solving.SolvingScreen
import com.example.leetnote.ui.screens.home.HomeViewModel
import com.example.leetnote.ui.screens.solving.EvaluationScreen
import com.example.leetnote.ui.screens.learning.LearningItemScreen
import com.example.leetnote.ui.screens.setting.SettingScreen
import com.example.leetnote.ui.screens.problem.SolutionScreen
import com.example.leetnote.ui.screens.learning.LearningResViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.leetnote.ui.screens.login.AuthViewModel
import com.example.leetnote.ui.screens.login.LoginScreen
import com.example.leetnote.ui.screens.login.SignupScreen
import com.example.leetnote.ui.screens.onboarding.OnboardingScreen
import com.example.leetnote.ui.screens.onboarding.OnboardingViewModel
import com.example.leetnote.ui.screens.problem.ProblemDetailViewModel
import com.example.leetnote.ui.screens.profile.EvaluationDetailScreen
import com.example.leetnote.ui.screens.profile.ProfileScreen
import com.example.leetnote.ui.screens.profile.ProfileViewModel
import com.example.leetnote.ui.screens.setting.SettingViewModel
import com.example.leetnote.ui.screens.solving.SolvingPageViewModel
import com.example.leetnote.ui.screens.splash.SplashScreen

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        composable(Screen.Login.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                navController,
                viewModel = viewModel
            )

            val currentUser by viewModel.currentUser.collectAsState()
            LaunchedEffect(currentUser) {
                if (currentUser != null) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true } // clear backstack
                    }
                }
            }
        }
        composable(Screen.Signup.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            SignupScreen(navController, viewModel)
        }
        composable(Screen.Onboarding.route) {
            val viewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(navController, viewModel)
        }
        composable(Screen.Home.route) {backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val viewModel: HomeViewModel = hiltViewModel(parentEntry)
            HomeScreen(navController, viewModel)
        }
        composable(Screen.Profile.route) {
            val viewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(viewModel, navController)
        }
        composable(Screen.Learning.route) {
            val viewModel: LearningResViewModel = hiltViewModel()
            LearningResourcesScreen(navController, viewModel = viewModel)
        }
        composable(Screen.Settings.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            val settingViewModel: SettingViewModel = hiltViewModel()
            SettingScreen(navController,viewModel, settingViewModel)
        }
        composable(Screen.Problem.route) { backStackEntry ->
            val problemId = backStackEntry.arguments?.getString("problemId")?.toLongOrNull() ?: -1
            val viewModel: ProblemDetailViewModel = hiltViewModel()
            ProblemScreen(problemId = problemId, navController = navController, viewModel = viewModel)
        }
        composable(Screen.Solving.route) { backStackEntry ->
            val problemId = backStackEntry.arguments?.getString("problemId")?.toLongOrNull() ?: -1
            val viewModel: SolvingPageViewModel = hiltViewModel()
            SolvingScreen(problemId = problemId, navController = navController, viewModel = viewModel)
        }
        composable(Screen.Solution.route) { backStackEntry ->
            val problemId = backStackEntry.arguments?.getString("problemId")?.toLongOrNull() ?: -1
            SolutionScreen(problemId = problemId)
        }
        composable(Screen.Evaluation.route) { backStackEntry ->
            val problemId = backStackEntry.arguments?.getString("problemId")?.toLongOrNull() ?: -1
            val viewModel: SolvingPageViewModel = hiltViewModel()
            EvaluationScreen(problemId = problemId, viewModel)
        }
        composable(Screen.EvaluationDetail.route) { backStackEntry ->
            val problemId = backStackEntry.arguments?.getString("problemId")?.toLongOrNull() ?: -1
            val evaluationId = backStackEntry.arguments?.getString("evaluationId")?.toLongOrNull() ?: -1
            val viewModel: ProfileViewModel = hiltViewModel()
            EvaluationDetailScreen(
                problemId = problemId, 
                evaluationId = evaluationId,
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
        composable(Screen.LearningItem.route) { backStackEntry ->
            val patternId = backStackEntry.arguments?.getString("patternId")?.toIntOrNull() ?: -1
            val viewModel: LearningResViewModel = hiltViewModel()
            val patterns by viewModel.patterns.collectAsState()
            val pattern = patterns.find { it.id == patternId }
            pattern?.let { LearningItemScreen(it) }
        }
    }
}