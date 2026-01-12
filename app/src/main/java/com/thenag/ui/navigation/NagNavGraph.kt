package com.thenag.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.thenag.ui.screens.create.CreateNagScreen
import com.thenag.ui.screens.edit.EditNagScreen
import com.thenag.ui.screens.home.HomeScreen

/**
 * Main navigation graph for The Nag app.
 * Defines all navigation routes and destinations.
 */
@Composable
fun NagNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Home screen - list of all nags
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToCreate = {
                    navController.navigate(Screen.Create.route)
                },
                onNavigateToEdit = { nagId ->
                    navController.navigate(Screen.Edit.createRoute(nagId))
                },
                onNavigateToStats = {
                    navController.navigate(Screen.Stats.route)
                }
            )
        }

        // Create nag screen
        composable(route = Screen.Create.route) {
            CreateNagScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Edit nag screen
        composable(
            route = Screen.Edit.route,
            arguments = listOf(
                navArgument("nagId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val nagId = backStackEntry.arguments?.getInt("nagId") ?: return@composable
            EditNagScreen(
                nagId = nagId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Statistics screen
        composable(route = Screen.Stats.route) {
            // StatsScreen(
            //     onNavigateBack = {
            //         navController.popBackStack()
            //     }
            // )
        }
    }
}

/**
 * Sealed class representing all navigation destinations in the app.
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Create : Screen("create")
    object Edit : Screen("edit/{nagId}") {
        fun createRoute(nagId: Int) = "edit/$nagId"
    }
    object Stats : Screen("stats")
}
