package com.example.days.domain.resolution.service

import com.example.days.domain.category.repository.CategoryRepository
import com.example.days.domain.resolution.dto.request.ResolutionRequest
import com.example.days.domain.resolution.dto.response.ResolutionResponse
import com.example.days.domain.resolution.dto.response.SimpleResolutionResponse
import com.example.days.domain.resolution.repository.ResolutionRepository
import com.example.days.domain.user.repository.UserRepository
import com.example.days.global.common.SortOrder
import com.example.days.global.common.exception.common.ModelNotFoundException
import com.example.days.global.common.exception.auth.PermissionDeniedException
import com.example.days.global.infra.security.UserPrincipal
import org.springframework.data.domain.Page
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate


@Service
class ResolutionServiceImpl(
    private val resolutionRepository: ResolutionRepository,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
    private val redisTemplate: RedisTemplate<Any, SimpleResolutionResponse>,
) : ResolutionService {
    @Transactional
    override fun createResolution(request: ResolutionRequest, userId: Long): ResolutionResponse {
        val user = userRepository.findByIdOrNull(userId) ?: throw ModelNotFoundException("User", userId)
        val category = categoryRepository.findByName(request.category) ?: throw ModelNotFoundException("Category")
        val resolution = resolutionRepository.save(ResolutionRequest.of(request, category, user))
        return ResolutionResponse.from(resolution, true)
    }

    override fun getResolutionById(resolutionId: Long, userPrincipal: UserPrincipal): ResolutionResponse {
        val resolution = getByIdOrNull(resolutionId)
        if (resolution.author.id == userPrincipal.id){
            return ResolutionResponse.from(resolution, true)
        }else{
            return ResolutionResponse.from(resolution, false)
        }

    }

    override fun getResolutionListPaginated(page: Int, sortOrder: SortOrder?): Page<SimpleResolutionResponse> {
        val resolutionList = resolutionRepository.findByPageable(page, sortOrder)
        return resolutionList.map { SimpleResolutionResponse.from(it) }
    }

    @Transactional
    override fun updateResolution(resolutionId: Long, userId: Long, request: ResolutionRequest): ResolutionResponse {
        val category = categoryRepository.findByName(request.category) ?: throw ModelNotFoundException("Category")
        val updatedResolution = getByIdOrNull(resolutionId)
        if (updatedResolution.author.id == userId) {
            updatedResolution.updateResolution(request.title, request.description, category)
            return ResolutionResponse.from(updatedResolution, true)
        } else throw PermissionDeniedException()

    }

    @Transactional
    override fun deleteResolution(resolutionId: Long, userId: Long) {
        val resolution = getByIdOrNull(resolutionId)
        if (resolution.author.id == userId) {
            resolutionRepository.delete(resolution)
        } else throw PermissionDeniedException()
    }

    override fun getResolutionRanking(): List<SimpleResolutionResponse> {
        val resolutionRanking = redisTemplate.opsForList().range("ranking", 0, -1)
        return resolutionRanking ?: emptyList()
    }

    // 테스트 시 ( 3분에 한번 동작 )
//    @Scheduled(fixedRate = 180000)
    @Scheduled(cron = "0 0 0 * * *")
    fun resetResolutionStatus() {
        resolutionRepository.resetResolutionDailyStatus2()
        val today: LocalDate = LocalDate.now()
        resolutionRepository.checkResolutionDeadline(today)
    }
    @Scheduled(fixedRate = 120000)
    fun getResolutionTop10(){
        redisTemplate.delete("ranking")
        resolutionRepository.getResolutionRanking()
            .forEach{
                redisTemplate.opsForList().rightPush("ranking", SimpleResolutionResponse.from(it))
            }
    }

    override fun getMyResolutionById(resolutionId: Long, userId: Long): ResolutionResponse {
        val resolution = getByIdOrNull(resolutionId)
        if (resolution.author.id == userId){
            return ResolutionResponse.from(resolution, true)
        }
        else throw PermissionDeniedException()
    }

    override fun getMyResolutionListPaginated(page: Int, sortOrder: SortOrder?, userId: Long): Page<SimpleResolutionResponse> {
        val resolutionList = resolutionRepository.findByMyResolutionPageable(page, sortOrder, userId)
        return resolutionList.map { SimpleResolutionResponse.from(it) }
    }

    fun getByIdOrNull(id: Long) = resolutionRepository.findByIdOrNull(id) ?: throw ModelNotFoundException("Resolution", id)
}