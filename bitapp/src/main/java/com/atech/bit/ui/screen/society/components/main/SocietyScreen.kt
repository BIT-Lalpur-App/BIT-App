package com.atech.bit.ui.screen.society.components.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.atech.bit.ui.screen.society.SocietyViewModel
import com.atech.components.BackToolbar
import com.atech.components.BottomPadding
import com.atech.components.singleElement
import com.atech.components.stateLoadingScreen
import com.atech.theme.BITAppTheme
import com.atech.view_model.OnErrorEvent
import kotlinx.coroutines.flow.collectLatest


@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun SocietyScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: SocietyViewModel = hiltViewModel()
) {
    val society = viewModel.fetchData.value.first
    val ngo = viewModel.fetchData.value.second
    var hasError by remember {
        mutableStateOf(false to "")
    }
    LaunchedEffect(key1 = true) {
        viewModel.oneTimeEvent.collectLatest { event ->
            when (event) {
                is OnErrorEvent.OnError -> hasError = true to event.message
            }
        }
    }
    Scaffold(modifier = modifier, topBar = {
        BackToolbar(title = stringResource(id = com.atech.theme.R.string.societies),
            onNavigationClick = {
                navController.navigateUp()
            })
    }) {
        LazyColumn(
            modifier = Modifier.consumeWindowInsets(it), contentPadding = it
        ) {
            if (society.isEmpty() && ngo.isEmpty()) {
                singleElement(key = "ProgressOrError") {
                    stateLoadingScreen(
                        isLoading = (society.isEmpty() && ngo.isEmpty()) && !hasError.first,
                        isHasError = hasError.first,
                        errorMessage = hasError.second
                    )
                }
                return@LazyColumn
            }
            val itemMap = mutableMapOf(
                "Society" to society, "NGO" to ngo
            )
            itemMap.forEach { (key, value) ->
                stickyHeader {
                    SocietyType(title = key)
                }
                items(value, key = { it1 -> it1.name + it1.sno }) { item ->
                    SocietyItem(model = item)
                }
            }
            singleElement(key = "padding") { BottomPadding() }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SocietyScreenPreview() {
    BITAppTheme {
        SocietyScreen()
    }
}