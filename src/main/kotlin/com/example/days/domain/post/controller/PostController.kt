package com.example.days.domain.post.controller

import com.example.days.domain.post.dto.request.PostRequest
import com.example.days.domain.post.dto.response.DeleteResponse
import com.example.days.domain.post.dto.response.PostResponse
import com.example.days.domain.post.dto.response.PostWithCommentResponse
import com.example.days.domain.post.model.PostType
import com.example.days.domain.post.service.PostService
import com.example.days.global.infra.security.UserPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RequestMapping("/api/posts")
@RestController
class PostController(
    private val postService: PostService
) {
    @GetMapping
    fun getAllPostList(): ResponseEntity<List<PostResponse>> {
        return ResponseEntity.status(HttpStatus.OK).body(postService.getAllPostList())
    }

    @GetMapping("/{postId}")
    fun getPost(@PathVariable postId: Long): ResponseEntity<PostWithCommentResponse> {
        return ResponseEntity.status(HttpStatus.OK).body(postService.getPostById(postId))
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    fun creatPost(
        @AuthenticationPrincipal userId: UserPrincipal,
        categoryId: Long,
        resolutionId: Long,
        type: PostType,
        @RequestBody request: PostRequest
    ): ResponseEntity<PostResponse> {
        return ResponseEntity.status(HttpStatus.OK).body(postService.createPost(userId, categoryId, resolutionId, type, request))
    }

    @PutMapping("/{postId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    fun updatePost(
        @AuthenticationPrincipal userId: UserPrincipal,
        type: PostType,
        @PathVariable postId: Long,
        @RequestBody request: PostRequest
    ): ResponseEntity<PostResponse> {
        return ResponseEntity.status(HttpStatus.OK).body(postService.updatePost(userId, type, postId, request))
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    fun deletePost(
        @AuthenticationPrincipal userId: UserPrincipal,
        @PathVariable postId: Long
    ): ResponseEntity<DeleteResponse> {
        postService.deletePost(userId, postId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}