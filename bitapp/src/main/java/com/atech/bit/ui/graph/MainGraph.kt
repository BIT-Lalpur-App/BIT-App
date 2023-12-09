package com.atech.bit.ui.graph

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.atech.attendance.AttendanceScreenRoutes
import com.atech.attendance.attendanceNavigation
import com.atech.course.CourseScreenRoute
import com.atech.course.courseNavigation
import com.atech.utils.getSimpleName

sealed class MainScreenRoutes(val route: String) {
    data object Attendance :
        MainScreenRoutes(route = getSimpleName(AttendanceScreenRoutes::class.java))

    data object Course : MainScreenRoutes(route = getSimpleName(CourseScreenRoute::class.java))
    data object Home : MainScreenRoutes(route = getSimpleName(HomeScreenRoutes::class.java))

}


val listOfFragmentsWithBottomAppBar = listOf(
    HomeScreenRoutes.HomeScreen.route,
    AttendanceScreenRoutes.AttendanceScreen.route,
    CourseScreenRoute.CourseScreen.route
)

@Composable
fun HomeNavigation(
    navHostController: NavHostController
) {
    NavHost(
        navController = navHostController,
        startDestination = MainScreenRoutes.Home.route
    ) {
        homeNavigation(navHostController)
        attendanceNavigation(navHostController)
        courseNavigation(navHostController)

    }
}
