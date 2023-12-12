/*
 * BIT Lalpur App
 *
 * Created by Ayaan on 2/13/22, 10:50 PM
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 2/13/22, 8:05 PM
 */



package com.atech.core.data_source.room.attendance

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.io.Serializable
import java.util.ArrayDeque
import java.util.Deque

@Keep
@Entity(
    tableName = "attendance_table",
    indices = [Index(value = ["subject_name"], unique = true)]
)
@Parcelize
data class AttendanceModel(
    @ColumnInfo(name = "subject_name")
    val subject: String,
    val total: Int = 0,
    val present: Int = 0,
    val teacher: String? = null,
    val fromSyllabus: Boolean? = false,
    val isArchive: Boolean? = false,
    val fromOnlineSyllabus: Boolean? = false,
    @Embedded
    val days: Days = Days(),
    val stack: @RawValue Deque<AttendanceSave> = ArrayDeque(),
    val created: Long? = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
) : Parcelable, Serializable

@Keep
@Parcelize
data class Days(
    val presetDays: ArrayList<Long> = ArrayList(),
    val absentDays: ArrayList<Long> = ArrayList(),
    val totalDays: ArrayList<IsPresent> = ArrayList(),
) : Parcelable, Serializable

@Keep
@Parcelize
data class AttendanceSave(
    val total: Int,
    val present: Int,
    val days: Days
) : Parcelable, Serializable


@Keep
@Parcelize
data class IsPresent(val day: Long, var isPresent: Boolean, var totalClasses: Int? = null) :
    Parcelable, Serializable

object DaysTypeConvector {
    @TypeConverter
    @JvmStatic
    fun toString(value: String): ArrayList<Long> =
        Gson().fromJson(value, object : TypeToken<ArrayList<Long>>() {}.type)


    @TypeConverter
    @JvmStatic
    fun toArrayList(list: ArrayList<Long>): String = Gson().toJson(list)
}

object IsPresentTypeConvector {
    @TypeConverter
    @JvmStatic
    fun toString(value: String): ArrayList<IsPresent> =
        Gson().fromJson(value, object : TypeToken<ArrayList<IsPresent>>() {}.type)


    @TypeConverter
    @JvmStatic
    fun toArrayList(list: ArrayList<IsPresent>): String = Gson().toJson(list)
}

object StackTypeConvector {
    @TypeConverter
    @JvmStatic
    fun toString(value: String): Deque<AttendanceSave> =
        Gson().fromJson(value, object : TypeToken<Deque<AttendanceSave>>() {}.type)


    @TypeConverter
    @JvmStatic
    fun toDeque(stack: Deque<AttendanceSave>): String = Gson().toJson(stack)
}

