package com.example.days.domain.messages.dto.response

import com.example.days.domain.messages.model.MessagesEntity
import java.time.LocalDateTime

data class MessagesReceiveResponse(
    val id: Long,
    val title: String,
    val content: String,
    val senderAccountId: String,
    val receiverAccountId: String,
    val sentAt: LocalDateTime,
    val readStatus: Boolean,
    val senderNickname: String,
    val receiverNickname: String
){
    companion object {
        fun from(messages: MessagesEntity): MessagesReceiveResponse {
            return MessagesReceiveResponse(
                id = messages.id!!,
                title = messages.title,
                content = messages.content,
                receiverAccountId = messages.receiver.accountId,
                senderAccountId = messages.sender.accountId,
                sentAt = messages.sentAt,
                readStatus = messages.readStatus,
                senderNickname = messages.receiver.nickname,
                receiverNickname = messages.sender.nickname
            )
        }
    }
}
