package com.atech.bit.ui.screens.event.component.detail

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.atech.bit.R
import com.atech.bit.ui.comman.BackToolbar
import com.atech.bit.ui.comman.ImageLoader
import com.atech.bit.ui.screens.event.EventViewModel
import com.atech.bit.ui.theme.BITAppTheme
import com.atech.bit.ui.theme.bottomPaddingSize
import com.atech.bit.ui.theme.bottom_nav_height
import com.atech.bit.ui.theme.captionColor
import com.atech.bit.ui.theme.grid_1
import com.atech.bit.ui.theme.grid_2
import com.atech.core.utils.getDate
import io.sanghun.compose.video.VideoPlayer
import io.sanghun.compose.video.controller.VideoPlayerControllerConfig
import io.sanghun.compose.video.uri.VideoPlayerMediaItem
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: EventViewModel = hiltViewModel(),
    navController: NavController = rememberNavController()
) {
    val event = viewModel.currentClickEvent.value
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scrollState = rememberScrollState()
    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BackToolbar(
                title = "",
                onNavigationClick = {
                    navController.navigateUp()
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        if (event == null) {
            Toast.makeText(context, "Something went wrong !!", Toast.LENGTH_SHORT).show()
            navController.navigateUp()
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(it)
                .padding(horizontal = grid_1)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = event.title ?: "",
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(grid_1))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.society ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(grid_1))
                    Icon(
                        modifier = Modifier.size(
                            MaterialTheme.typography.bodyMedium.fontSize.value.dp
                        ),
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(grid_1))
                    Text(
                        text = event.created?.getDate() ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                ImageLoader(
                    modifier = Modifier.size(
                        30.dp
                    ), imageUrl = event.logo_link, isRounderCorner = true
                )
            }
            Spacer(modifier = Modifier.height(grid_1))
            Text(
                text = event.content ?: "",
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(grid_1))
            if (!event.attach.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(grid_2))
                Text(
                    text = stringResource(R.string.attached_images),
                    color = MaterialTheme.colorScheme.captionColor,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(grid_1))
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(3),
                    verticalItemSpacing = 4.dp,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    content = {
                        items(event.attach?.size ?: 0) { attach ->
                            ImageLoader(
                                imageUrl = event.attach?.get(attach)?.link,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            calculateSize(event.attach?.size ?: 0)
                        )
                )
            }
            if (!event.video_link.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(grid_2))
                Text(
                    text = stringResource(R.string.attached_video),
                    color = MaterialTheme.colorScheme.captionColor,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(grid_1))
                VideoPlayer(
                    mediaItems = listOf(
                        VideoPlayerMediaItem.NetworkMediaItem(
                            url = event.video_link ?: "",
                        )
                    ),
                    handleLifecycle = true,
                    autoPlay = false,
                    usePlayerController = true,
                    enablePip = false,
                    handleAudioFocus = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp),
                    controllerConfig = VideoPlayerControllerConfig(
                        showSpeedAndPitchOverlay = false,
                        showSubtitleButton = false,
                        showCurrentTimeAndTotalTime = true,
                        showBufferingProgress = true,
                        showForwardIncrementButton = false,
                        showBackwardIncrementButton = false,
                        showBackTrackButton = false,
                        showNextTrackButton = false,
                        showRepeatModeButton = false,
                        controllerShowTimeMilliSeconds = 5_000,
                        controllerAutoShow = true,
                        showFullScreenButton = true
                    )
                )

            }
            Spacer(modifier = Modifier.height(bottomPaddingSize))
        }
    }
}

private fun calculateSize(size: Int) = (ceil(size.toFloat() / 3.0) * 200).dp


@Preview(showBackground = true)
@Composable
private fun EventDetailScreenPreview() {
    BITAppTheme {
        EventDetailScreen()
    }
}