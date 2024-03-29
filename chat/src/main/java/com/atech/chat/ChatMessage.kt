/*
 *  Created by aiyu
 *  Copyright (c) 2021 . All rights reserved.
 *  BIT App
 *
 */

package com.atech.chat

import androidx.annotation.Keep
import com.atech.core.datasource.room.chat.ChatModel
import com.atech.core.utils.EntityMapper
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import java.util.UUID
import javax.inject.Inject

@Keep
enum class Participant {
    USER, MODEL, ERROR
}

@Keep
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    var text: String = "",
    val participant: Participant = Participant.USER,
    val linkedId: String = ""
)

class ChatMessageToModelMapper @Inject constructor() : EntityMapper<ChatMessage, ChatModel> {
    override fun mapFormEntity(entity: ChatMessage): ChatModel = ChatModel(
        id = entity.id, text = entity.text,
        participant = entity.participant.name,
        linkId = entity.linkedId
    )

    override fun mapToEntity(domainModel: ChatModel): ChatMessage = ChatMessage(
        id = domainModel.id,
        text = domainModel.text,
        participant = Participant.valueOf(domainModel.participant),
        linkedId = domainModel.linkId
    )

    fun mapFromEntityList(entities: List<ChatModel>): List<ChatMessage> =
        entities.map { mapToEntity(it) }

    fun mapToEntityList(domainModels: List<ChatMessage>): List<ChatModel> =
        domainModels.map { mapFormEntity(it) }
}

fun List<ChatMessage>.toContent(): List<Content> = this.map {
    content(role = it.participant.name.lowercase()) {
        text(it.text)
    }
}