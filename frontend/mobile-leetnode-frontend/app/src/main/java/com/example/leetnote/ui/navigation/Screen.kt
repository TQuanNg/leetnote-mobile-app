package com.example.leetnote.ui.navigation

import com.example.leetnote.R

sealed class Screen(
    val route: String,
    val title: String,
    val iconRes: Int? = null,
    val topIconRes: Int? = null,
) {
    object Splash : Screen("splash", "Splash", R.drawable.login_icon)

    object Login : Screen("login", "Login", R.drawable.login_icon)
    object Signup : Screen("signup", "Sign Up", R.drawable.login_icon)

    object Onboarding : Screen("onboarding", "Onboarding", R.drawable.login_icon)

    object Home : Screen("home",
        "LeetNote",
        R.drawable.home_icon,
        R.drawable.settings_icon,
        )
    object Profile : Screen("profile", "Profile", R.drawable.account_icon)
    object Learning : Screen("learning", "Learning", R.drawable.learning_icon)
    object Settings : Screen("settings", "Settings", R.drawable.home_icon)

    object Problem : Screen("problem/{problemId}", "Problem") {
        fun createRoute(problemId: Long) = "problem/$problemId"
    }

    object Solving : Screen(
        "solving/{problemId}",
        "Solving",
        topIconRes = R.drawable.help_icon,
    ) {
        fun createRoute(problemId: Long) = "solving/$problemId"
    }

    object Solution : Screen(
        "solution/{problemId}",
        "Solution",
        topIconRes = R.drawable.help_icon,
        ) {
        fun createRoute(problemId: Long) = "solution/$problemId"
    }

    object Evaluation : Screen(
        "evaluation/{problemId}",
        "Evaluation",
        topIconRes = R.drawable.help_icon,
    ) {
        fun createRoute(problemId: Long) = "evaluation/$problemId"
    }

    object LearningItem : Screen("learningItem/{patternId}", "Learning Item") {
        fun createRoute(patternId: Int) = "learningItem/$patternId"
    }

    companion object {
        val bottomNavScreens = listOf(Home, Profile, Learning)
        val allScreens = listOf(Home, Profile, Learning, Settings, Problem, Solution, Solving, Evaluation, LearningItem)
    }
}

