package com.example.days.domain.post.service

import com.example.days.domain.comment.model.Comment
import com.example.days.domain.comment.repository.CommentRepository
import com.example.days.domain.post.dto.request.PostRequest
import com.example.days.domain.post.dto.response.DeleteResponse
import com.example.days.domain.post.dto.response.PostListResponse
import com.example.days.domain.post.dto.response.PostResponse
import com.example.days.domain.post.dto.response.PostWithCommentResponse
import com.example.days.domain.post.model.Post
import com.example.days.domain.post.model.PostType
import com.example.days.domain.post.repository.PostRepository
import com.example.days.domain.resolution.repository.ResolutionRepository
import com.example.days.domain.user.repository.UserRepository
import com.example.days.global.common.exception.auth.PermissionDeniedException
import com.example.days.global.common.exception.common.CheckAlreadyCompletedException
import com.example.days.global.common.exception.common.ModelNotFoundException
import com.example.days.global.common.exception.common.ResolutionAlreadyCompletedException
import com.example.days.global.common.exception.common.TypeNotFoundException
import com.example.days.global.common.exception.user.UserNotFoundException
import com.example.days.global.infra.security.UserPrincipal
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

@Service
class PostServiceImpl(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val resolutionRepository: ResolutionRepository,
    private val commentRepository: CommentRepository
) : PostService {

    // post 전체조회 (내림차순), comment x
    override fun getAllPostList(resolutionId: Long): List<PostListResponse> {
        val resolution = resolutionRepository.findByIdOrNull(resolutionId)
            ?: throw ModelNotFoundException("목표", resolutionId)
        return postRepository.findByResolutionId(resolution)
            .sortedByDescending { it.createdAt }
            .map { PostListResponse.from(it) }
    }

    // post 개별조회, comment o
    @Transactional
    override fun getPostById(resolutionId: Long, postId: Long): PostWithCommentResponse {
        val resolution = resolutionRepository.findByIdOrNull(resolutionId)
            ?: throw ModelNotFoundException("목표", resolutionId)
        val post = postRepository.findByIdOrNull(postId) ?: throw ModelNotFoundException("게시글", postId)
        if(post.resolutionId?.id == resolution.id){
            val comments: List<Comment> = commentRepository.findByPostId(post)
            post.comments.addAll(comments)
            return PostWithCommentResponse.from(post)
        }
        else throw PermissionDeniedException()
    }

    // post 작성 > 데일리 체크에서 달성도 체크 후 이쪽으로 넘어옴
    @Transactional
    override fun createPost(
        userId: UserPrincipal,
        resolutionId: Long,
        type: PostType,
        request: PostRequest
    ): PostResponse {
        val resolution = resolutionRepository.findByIdOrNull(resolutionId)
            ?: throw ModelNotFoundException("목표", resolutionId)

        if (userId.id == resolution.author.id){
            val user = userRepository.findByIdOrNull(userId.id) ?: throw UserNotFoundException()
            when {
                resolution.dailyStatus -> throw CheckAlreadyCompletedException()
                resolution.completeStatus -> throw ResolutionAlreadyCompletedException()
                else -> return resolution
                    .let {
                        it.updateProgress()
                        postRepository.save(PostRequest.of(request, resolution, type, user))
                    }
                    .let { PostResponse.from(it)}
            }
        }
        else throw PermissionDeniedException()
    }

    // post 수정
    @Transactional
    override fun updatePost(
        userId: UserPrincipal,
        resolutionId: Long,
        type: PostType,
        postId: Long,
        request: PostRequest
    ): PostResponse {
        val post = postRepository.findByIdOrNull(postId) ?: throw ModelNotFoundException("게시글", postId)
        val resolution = resolutionRepository.findByIdOrNull(resolutionId)
            ?: throw ModelNotFoundException("목표", resolutionId)

        if (userId.id == post.userId?.id && post.resolutionId?.id == resolution.id) {
            post.updatePost(request.title, request.content, request.imageUrl, type)
            return PostResponse.from(post)
        }
        else throw PermissionDeniedException()
    }

    // post 삭제
    @Transactional
    override fun deletePost(userId: UserPrincipal, postId: Long): DeleteResponse {
        val user = userRepository.findByIdOrNull(userId.id) ?: throw UserNotFoundException()
        val post = postRepository.findByIdOrNull(postId) ?: throw ModelNotFoundException("게시글", postId)

        // 작성자 확인
        if (post.userId?.id == userId.id) {
            postRepository.delete(post)
        } else {
            throw PermissionDeniedException()
        }

        return DeleteResponse("${user.nickname} 님 게시글이 삭제 처리되었습니다.")
    }
}