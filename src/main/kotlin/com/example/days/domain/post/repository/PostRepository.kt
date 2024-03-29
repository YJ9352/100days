package com.example.days.domain.post.repository

import com.example.days.domain.post.dto.response.PostListResponse
import com.example.days.domain.post.model.Post
import com.example.days.domain.resolution.model.Resolution
import org.springframework.data.jpa.repository.JpaRepository

interface PostRepository : JpaRepository<Post, Long> {
    fun findByResolutionId(resolutionId: Resolution): List<PostListResponse>
}