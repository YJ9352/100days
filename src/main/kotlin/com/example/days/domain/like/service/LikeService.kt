package com.example.days.domain.like.service

import com.example.days.domain.like.dto.response.LikeStatusResponse
import com.example.days.global.infra.security.UserPrincipal

interface LikeService {
    fun insertLike(resolutionId: Long, userId: UserPrincipal)
    fun deleteLike(resolutionId: Long, userId: UserPrincipal)
    fun getLikeStatus(resolutionId: Long, userId: UserPrincipal): LikeStatusResponse
}