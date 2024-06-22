/*
 *  Created by aiyu
 *  Copyright (c) 2021 . All rights reserved.
 *  BIT App
 *
 */

package com.atech.bit.ui.screens.notice

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atech.core.datasource.firebase.firestore.NoticeModel
import com.atech.core.datasource.retrofit.model.CollegeNotice
import com.atech.core.usecase.CollegeNoticeUseCases
import com.atech.core.usecase.FirebaseCase
import com.atech.core.usecase.GetAttach
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val case: FirebaseCase,
    private val collegeNotice: CollegeNoticeUseCases,
    val getAttach: GetAttach,
    state: SavedStateHandle
) : ViewModel() {
    val noticeId = state.get<String>("noticeId") ?: ""

    private val _fetchNotice = mutableStateOf<List<NoticeModel>>(emptyList())
    val fetchNotice: State<List<NoticeModel>> get() = _fetchNotice

    private val _fetchCollegeNotice = mutableStateOf<List<CollegeNotice>>(emptyList())
    val fetchCollegeNotice: State<List<CollegeNotice>> get() = _fetchCollegeNotice

    private var job: Job? = null

    private val _currentClickNotice = mutableStateOf(NoticeModel())
    val currentClickNotice: State<NoticeModel?> get() = _currentClickNotice


    fun onEvent(event: NoticeScreenEvent) {
        when (event) {
            is NoticeScreenEvent.OnEventClick ->
                _currentClickNotice.value = event.model
        }
    }

    init {
        fetchNotice()
        fetchAllCollegeNotice()
    }

    private fun fetchAllCollegeNotice() = viewModelScope.launch {
        _fetchCollegeNotice.value = collegeNotice.allNotice.invoke()
    }

    private fun fetchNotice() {
        job?.cancel()
        job = case.getNotice()
            .onEach {
                _fetchNotice.value = it
                if (noticeId != "") {
                    _currentClickNotice.value =
                        it.find { it1 -> it1.path == noticeId } ?: NoticeModel(
                            title = "Something went wrong",
                            body = "Try some time later !! 🥲🥹"
                        )
                }
            }.launchIn(viewModelScope)
    }
}