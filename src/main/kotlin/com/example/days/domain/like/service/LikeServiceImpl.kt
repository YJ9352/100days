package com.example.days.domain.like.service

import com.example.days.domain.like.dto.response.LikeResponse
import com.example.days.domain.like.dto.response.LikeStatusResponse
import com.example.days.domain.like.repository.LikeRepository
import com.example.days.domain.resolution.model.Resolution
import com.example.days.domain.resolution.repository.ResolutionRepository
import com.example.days.domain.user.model.User
import com.example.days.domain.user.repository.UserRepository
import com.example.days.global.common.exception.common.LikeAlreadyProcessedException
import com.example.days.global.common.exception.common.ModelNotFoundException
import com.example.days.global.infra.security.UserPrincipal
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LikeServiceImpl(
    private val userRepository: UserRepository,
    private val resolutionRepository: ResolutionRepository,
    private val likeRepository: LikeRepository,
): LikeService {
    @Transactional
    override fun insertLike(resolutionId: Long, userId: UserPrincipal) {
        val (user, resolution) = getUserAndResolution(userId.id, resolutionId)
        if (!likeRepository.existsByUserAndResolution(user, resolution)){
            resolution.updateLikeCount(true)
            likeRepository.save(LikeResponse.from(user, resolution))
        }
        else throw LikeAlreadyProcessedException()
    }

    @Transactional
    override fun deleteLike(resolutionId: Long, userId: UserPrincipal) {
        val (user, resolution) = getUserAndResolution(userId.id, resolutionId)
        val canceledLike = likeRepository.findByUserAndResolution(user, resolution) ?: throw LikeAlreadyProcessedException()

        resolution.updateLikeCount(false)
        likeRepository.delete(canceledLike)
    }

    override fun getLikeStatus(resolutionId: Long, userId: UserPrincipal): LikeStatusResponse {
        val (user, resolution) = getUserAndResolution(userId.id, resolutionId)
        return if(likeRepository.existsByUserAndResolution(user, resolution)){
            LikeStatusResponse(status = true)
        } else {
            LikeStatusResponse(status = false)
        }
    }

    fun getUserAndResolution(userId: Long, resolutionId: Long): Pair<User, Resolution>{
        val user = userRepository.findByIdOrNull(userId) ?: throw ModelNotFoundException("User", userId)
        val resolution = resolutionRepository.findByIdOrNull(resolutionId)
            ?: throw ModelNotFoundException("Resolution", resolutionId)
        return Pair(user, resolution)
    }


}