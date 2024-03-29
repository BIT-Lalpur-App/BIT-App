/*
 *  Created by aiyu
 *  Copyright (c) 2021 . All rights reserved.
 *  BIT App
 *
 */

package com.atech.core.datasource.room.chat

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "chat_table")
data class ChatModel(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val text: String,
    val participant: String,
    val created: Long = System.currentTimeMillis(),
    val linkId: String = ""
)

