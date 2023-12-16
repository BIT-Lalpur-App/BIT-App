package com.atech.bit.ui.graph

import androidx.annotation.Keep
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.atech.bit.ui.screen.home.compose.HomeScreen
import com.atech.utils.fadeThroughComposable
import com.atech.utils.getSimpleName
import com.atech.view_model.SharedViewModel


@Keep
sealed class HomeScreenRoutes(val route: String) {
    data object HomeScreen : HomeScreenRoutes("home_screen")
}

fun NavGraphBuilder.homeNavigation(
    navController: NavController,
    communicatorViewModel: SharedViewModel
) {
    navigation(
        startDestination = HomeScreenRoutes.HomeScreen.route,
        route = getSimpleName(HomeScreenRoutes::class.java)
    ) {
        fadeThroughComposable(
            route = HomeScreenRoutes.HomeScreen.route
        ) {
            HomeScreen(
                communicatorViewModel = communicatorViewModel
            )
        }
    }

}





