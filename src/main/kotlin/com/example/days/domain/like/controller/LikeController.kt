package com.example.days.domain.like.controller

import com.example.days.domain.like.dto.response.LikeStatusResponse
import com.example.days.domain.like.service.LikeService
import com.example.days.global.infra.security.UserPrincipal
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/likes")
class LikeController(
    private val likeService: LikeService
) {
    @Operation(summary = "좋아요 증가")
    @PostMapping
    fun insertLike(
        @RequestParam resolutionId: Long,
        @AuthenticationPrincipal userId: UserPrincipal,
    ): ResponseEntity<Unit>{
        return ResponseEntity.ok(likeService.insertLike(resolutionId, userId))
    }

    @Operation(summary = "좋아요 취소")
    @DeleteMapping
    fun deleteLike(
        @RequestParam resolutionId: Long,
        @AuthenticationPrincipal userId: UserPrincipal,
    ): ResponseEntity<Unit>{
        likeService.deleteLike(resolutionId, userId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "좋아요 상태 체크")
    @GetMapping
    fun getLikeStatus(
        @RequestParam resolutionId: Long,
        @AuthenticationPrincipal userId: UserPrincipal,
    ): ResponseEntity<LikeStatusResponse>{
        return ResponseEntity.ok(likeService.getLikeStatus(resolutionId, userId))
    }
}