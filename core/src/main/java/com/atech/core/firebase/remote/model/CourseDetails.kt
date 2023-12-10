package com.atech.core.firebase.remote.model

import androidx.annotation.Keep


@Keep
data class CourseDetails(
    val course: List<CourseDetailModel>
)

@Keep
data class CourseDetailModel(
    val name: String,
    val sem: Int
)