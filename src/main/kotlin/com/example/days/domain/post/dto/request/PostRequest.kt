package com.example.days.domain.post.dto.request

import com.example.days.domain.post.model.Post
import com.example.days.domain.post.model.PostType
import com.example.days.domain.resolution.model.Resolution
import com.example.days.domain.user.model.User

data class PostRequest(
    val title: String,
    val content: String,
    val imageUrl: String
){
    companion object{
        fun of(request: PostRequest, resolution: Resolution, selectedType: PostType, user: User): Post{
            if (selectedType == PostType.APPEND){
                return Post(
                    title = request.title,
                    content = request.content,
                    imageUrl = request.imageUrl,
                    type = selectedType,
                    resolutionId = resolution,
                    userId = user
                )
            }
            else {
                return Post(
                    title = request.title,
                    content = "",
                    imageUrl = "",
                    type = selectedType,
                    resolutionId = resolution,
                    userId = user
                )
            }

        }
    }
}