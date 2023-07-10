package com.atech.bit.ui.fragments.events.detail

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.atech.bit.R
import com.atech.bit.databinding.FragmentNoticeEventDetailBinding
import com.atech.bit.utils.ImageGridAdapter
import com.atech.bit.utils.navigateToViewImage
import com.atech.core.firebase.firestore.Attach
import com.atech.core.firebase.firestore.EventModel
import com.atech.core.utils.DataState
import com.atech.core.utils.MAX_SPAWN
import com.atech.theme.Axis
import com.atech.theme.ToolbarData
import com.atech.theme.enterTransition
import com.atech.theme.getDate
import com.atech.theme.loadCircular
import com.atech.theme.openCustomChromeTab
import com.atech.theme.set
import com.atech.theme.toast
import dagger.hilt.android.AndroidEntryPoint

@UnstableApi
@AndroidEntryPoint
class EventDetailFragment : Fragment(R.layout.fragment_notice_event_detail) {

    private val binding: FragmentNoticeEventDetailBinding by viewBinding()

    private val viewModel: EventDetailViewModel by viewModels()

    private var player: ExoPlayer? = null

    private var link: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition(Axis.X)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            setToolbar()
        }
        observeData()
    }

    private fun observeData() {
        viewModel.getEvent().observe(viewLifecycleOwner) { dataState ->
            when (dataState) {
                DataState.Empty -> {
                    binding.isLoadComplete(false)
                }

                is DataState.Error -> {
                    binding.isLoadComplete(false)
                    dataState.exception.message?.let { toast(it) }
                }

                DataState.Loading -> {}
                is DataState.Success -> {
                    binding.isLoadComplete(true)
                    binding.setViews(dataState.data)
                }
            }
        }
        viewModel.getAttach().observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.setAttach(it)
            }
        }
    }

    private fun FragmentNoticeEventDetailBinding.isLoadComplete(isDone: Boolean) = this.apply {
        progressBarLinear.isVisible = false
        imageViewNoData.isVisible = !isDone
        cardViewEvent.isVisible = isDone
    }

    private fun FragmentNoticeEventDetailBinding.setToolbar() = this.includeToolbar.apply {
        set(
            ToolbarData(
                com.atech.theme.R.string.event,
                action = findNavController()::navigateUp,
            ),
        )
    }

    private fun FragmentNoticeEventDetailBinding.setAttach(
        list: List<Attach>
    ) {
        val imageGridAdapter = ImageGridAdapter {
            navigateToViewImage(
                it to ""
            )
        }
        attachmentRecyclerView.apply {
            binding.textImage.isVisible = true
            isVisible = true
            layoutManager = GridLayoutManager(
                requireContext(), MAX_SPAWN
            ).apply {
                spanSizeLookup = imageGridAdapter.variableSpanSizeLookup
            }
            adapter = imageGridAdapter
            imageGridAdapter.submitList(attachments = list)
        }
    }

    private fun FragmentNoticeEventDetailBinding.setViews(data: EventModel) = this.apply {
        subjectTextView.text = data.title
        senderTextView.text = data.society
        bodyTextView.apply {
            movementMethod = LinkMovementMethod.getInstance()
            text = data.content
        }
        textViewDate.text = data.created?.getDate()
        data.insta_link?.let { link ->
            linkIcon.isVisible = link.isNotEmpty()
            linkIcon.setOnClickListener {
                requireActivity().openCustomChromeTab(link)
            }
        }
        senderProfileImageView.loadCircular(
            data.logo_link
        )
        data.video_link?.let {
            if (it.isNotEmpty()) {
                exoPlayer.isVisible = true
                textVideo.isVisible = true
                link = it
                initializePlayer(it)
            }
        }
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            viewModel.playbackPosition = exoPlayer.currentPosition
            viewModel.currentItem = exoPlayer.currentMediaItemIndex
            viewModel.playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        player = null
    }

    private fun initializePlayer(link: String) {
        player = ExoPlayer.Builder(requireContext()).build().also { exoPlayer ->
            binding.exoPlayer.player = exoPlayer
            val mediaItem = MediaItem.fromUri(link)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.playWhenReady = viewModel.playWhenReady
            exoPlayer.seekTo(viewModel.currentItem, viewModel.playbackPosition)
            exoPlayer.prepare()
        }
    }

    override fun onResume() {
        super.onResume()
        if (player == null) initializePlayer(link)
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }
}
