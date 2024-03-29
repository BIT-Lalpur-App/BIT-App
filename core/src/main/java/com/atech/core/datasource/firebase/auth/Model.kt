/*
 *  Created by aiyu
 *  Copyright (c) 2021 . All rights reserved.
 *  BIT App
 *
 */

package com.atech.core.datasource.firebase.auth

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import com.atech.core.datasource.room.attendance.AttendanceModel
import com.atech.core.datasource.room.attendance.Days
import kotlinx.parcelize.Parcelize

/**
 * Model class for User
 *
 */
@Keep
@Suppress("")
@Parcelize
data class UserModel(
    var uid: String? = null,
    var name: String? = null,
    var email: String? = null,
    var profilePic: String? = null,
    var syncTime: Long? = System.currentTimeMillis()
) : Parcelable

/**
 * Model class for Attendance that is uploaded to firebase
 */
@Keep
data class AttendanceUploadModel(
    @ColumnInfo(name = "subject_name")
    val subject: String,
    val total: Int,
    val present: Int,
    val teacher: String?,
    val fromSyllabus: Boolean? = false,
    val isArchive: Boolean? = false,
    val isFromOnlineSyllabus: Boolean? = false,
    val created: Long? = System.currentTimeMillis()
)

/**
 * Model class for Attendance that is downloaded from firebase
 * @see AttendanceUploadModel
 * @see AttendanceModel
 */
fun Array<AttendanceUploadModel>.toAttendanceModelList() =
    this.map {
        AttendanceModel(
            subject = it.subject,
            total = it.total,
            present = it.present,
            teacher = it.teacher,
            fromSyllabus = it.fromSyllabus,
            isArchive = it.isArchive,
            created = it.created,
            days = Days(
                presetDays = arrayListOf(),
                absentDays = arrayListOf(),
                totalDays = arrayListOf()
            ),
            fromOnlineSyllabus = it.isFromOnlineSyllabus ?: false
        )
    }

/**
 * Model class for User data
 */
@Keep
data class UserData(
    val courseSem: String? = null,
    val cgpa: String? = null,
    val attendance: String? = null
) {
    val course: String
        get() = courseSem?.split(" ")?.get(0) ?: "BCA"
    val sem: String
        get() = courseSem?.split(" ")?.get(1) ?: "1"
}