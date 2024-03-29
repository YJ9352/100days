package com.example.days.domain.post.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class PostListResponse (
    val id: Long,
    val title: String,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime
){
    companion object{
        fun from(response: PostListResponse) = PostListResponse(
            id = response.id,
            title = response.title,
            createdAt = response.createdAt
        )
    }
}