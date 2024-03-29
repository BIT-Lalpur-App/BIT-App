/*
 *  Created by aiyu
 *  Copyright (c) 2021 . All rights reserved.
 *  BIT App
 *
 */

package com.atech.bit.ui.navigation

import android.content.Intent
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.atech.bit.ui.screens.event.EventViewModel
import com.atech.bit.ui.screens.event.component.detail.EventDetailScreen
import com.atech.bit.ui.screens.event.component.event.EventScreen
import com.atech.bit.utils.animatedComposable
import com.atech.bit.utils.sharedViewModel


sealed class EventRoute(val route: String) {
    data object EventScreen : EventRoute("event_screen")
    data object DetailScreen : EventRoute("detail_screen")
}

fun NavGraphBuilder.eventGraph(
    navHostController: NavHostController,
) = this.apply {
    navigation(
        startDestination = EventRoute.EventScreen.route,
        route = RouteName.EVENT.value
    ) {
        animatedComposable(
            route = EventRoute.EventScreen.route
        ) {
            val viewModel = it.sharedViewModel<EventViewModel>(navController = navHostController)
            EventScreen(
                navController = navHostController,
                viewModel = viewModel
            )
        }
        animatedComposable(
            route = EventRoute.DetailScreen.route + "?eventId={eventId}",
            deepLinks = listOf(
                navDeepLink{
                    uriPattern = DeepLinkRoutes.EventDetailScreen().route
                    action = Intent.ACTION_VIEW
                }
            ),
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.StringType
                    defaultValue = ""
                })
        ) {
            val viewModel = it.sharedViewModel<EventViewModel>(navController = navHostController)
            EventDetailScreen(
                navController = navHostController,
                viewModel = viewModel
            )
        }
    }
}