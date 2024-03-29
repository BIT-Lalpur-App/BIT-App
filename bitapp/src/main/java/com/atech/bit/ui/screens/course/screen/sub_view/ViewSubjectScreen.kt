/*
 *  Created by aiyu
 *  Copyright (c) 2021 . All rights reserved.
 *  BIT App
 *
 */

package com.atech.bit.ui.screens.course.screen.sub_view

import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.atech.bit.R
import com.atech.bit.ui.comman.BackToolbar
import com.atech.bit.ui.comman.ImageIconButton
import com.atech.bit.ui.comman.NetworkScreenEmptyScreen
import com.atech.bit.ui.theme.BITAppTheme
import com.atech.bit.utils.hexToRgb
import com.atech.bit.utils.shareOnlineSyllabus
import com.atech.syllabus.getFragment
import com.mukesh.MarkdownView
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewSubjectScreen(
    modifier: Modifier = Modifier,
    viewModel: ViewSubjectViewModel = hiltViewModel(),
    navController: NavController = rememberNavController()
) {

    val toolbarScroll = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val data = viewModel.onlineMdContent.value
    val isOnline = viewModel.isOnline
    val hasError = viewModel.hasError.value
    var isComposeViewVisible by rememberSaveable {
        mutableStateOf(false)
    }
    val courseSem = viewModel.courseSem
    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        delay(500)
        isComposeViewVisible = true
    }
    BackHandler {
        isComposeViewVisible = false
        navController.navigateUp()
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(toolbarScroll.nestedScrollConnection),
        topBar = {
            BackToolbar(
                title = "",
                onNavigationClick = {
                    isComposeViewVisible = false
                    navController.navigateUp()
                },
                scrollBehavior = toolbarScroll,
                actions = {
                    if (isOnline || !hasError.first)
                        ImageIconButton(
                            icon = Icons.Outlined.Share,
                            onClick = {
                                context.shareOnlineSyllabus(
                                    viewModel.subject to viewModel.courseSem
                                )
                            }
                        )
                }
            )
        }) {
        Column(
            modifier = modifier
                /*.verticalScroll(scrollState)*/
                .padding(it)
                .background(
                    MaterialTheme.colorScheme.surface
                ),
            verticalArrangement = Arrangement.Center,
        ) {
            if (hasError.first) {
                NetworkScreenEmptyScreen(
                    modifier = Modifier.fillMaxSize(),
                    text = hasError.second
                )
                return@Scaffold
            }
            if (isOnline) {
                if (isComposeViewVisible) LoadMarkDown(
                    data = data
                )
            } else LoadSyllabusFromXml(res = courseSem, viewModel = viewModel)
        }
    }

}

@Composable
fun LoadSyllabusFromXml(res: String, viewModel: ViewSubjectViewModel) {
    AndroidView(
        factory = {
            try {
                View.inflate(
                    it, getFragment(res), null
                )
            } catch (e: Exception) {
                viewModel.onEvent(
                    ViewSubjectViewModel.ViewSubjectEvents.OnError(
                        "Error on loading online syllabus"
                    )
                )
                View.inflate(
                    it, com.atech.syllabus.R.layout.fragment_no_sylabus_found, null
                )
            }
        }, modifier = Modifier.fillMaxSize()
    )
}


@Composable
fun LoadMarkDown(
    modifier: Modifier = Modifier, data: String = ""
) {
    val d = data + "<br> <br><style> body{background-color: ${
        MaterialTheme.colorScheme.surface.hexToRgb()
    } ; color:${MaterialTheme.colorScheme.onSurface.hexToRgb()};}</style>"
    AndroidView(factory = { context ->
        View.inflate(
            context,
            R.layout.layout_markdown,
            null
        )
    },
        modifier = modifier.fillMaxSize(),
        update = {
            it.findViewById<MarkdownView>(R.id.markdown).setMarkDownText(d)
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ViewSubjectScreenPreview() {
    BITAppTheme {
        LoadMarkDown()
    }
}

