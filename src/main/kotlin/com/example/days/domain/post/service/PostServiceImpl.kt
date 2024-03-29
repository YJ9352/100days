package com.example.days.domain.post.service

import com.example.days.domain.category.repository.CategoryRepository
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
import com.example.days.global.common.exception.common.ModelNotFoundException
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
    private val categoryRepository: CategoryRepository,
    private val commentRepository: CommentRepository
) : PostService {

    // post 전체조회 (내림차순), comment x
    override fun getAllPostList(): List<PostListResponse> {
        // 게시글 작성시간 특정 부분까지만 표시
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return postRepository.findAll()
            .sortedByDescending { it.createdAt }
            .map {
                PostListResponse(
                    id = it.id!!,
                    title = it.title,
                    createdAt = it.createdAt.format(formatter)
                )
            }
    }

    // post 개별조회, comment o
    @Transactional
    override fun getPostById(postId: Long): PostWithCommentResponse {
        val post = postRepository.findByIdOrNull(postId) ?: throw ModelNotFoundException("게시글", postId)
        val comments: List<Comment> = commentRepository.findByPostId(post)
        post.comments.addAll(comments)

        return PostWithCommentResponse.from(post)
    }

    // post 작성 > 데일리 체크에서 달성도 체크 후 이쪽으로 넘어옴
    @Transactional
    override fun createPost(userId: UserPrincipal,
                            resolutionId: Long,
                            type: PostType,
                            request: PostRequest
    ): PostResponse {
        val user = userRepository.findByIdOrNull(userId.id) ?: throw UserNotFoundException()
        val resolution = resolutionRepository.findByIdOrNull(resolutionId) ?: throw ModelNotFoundException("목표", resolutionId)
        val post = Post(
            title = request.title,
            content = request.content,
            imageUrl = request.imageUrl,
            type = type,
            userId = user,
            resolutionId = resolution
        )
            // check 로 선택하면 제목만 입력가능, 나머지는 입력 x
            if (type == PostType.CHECK) {
                post.content = ""
                post.imageUrl = ""
            }

        return postRepository.save(post).let { PostResponse.from(post) }
    }

    // post 수정
    @Transactional
    override fun updatePost(userId: UserPrincipal, type: PostType, postId: Long, request: PostRequest): PostResponse {
        userRepository.findByIdOrNull(userId.id) ?: throw UserNotFoundException()
        val post = postRepository.findByIdOrNull(postId) ?: throw ModelNotFoundException("게시글", postId)

        // 작성 포스트 타입 확인
        if (type == post.type) {
            // 작성자 확인
            if (post.userId?.id == userId.id) {
                // 작성 타입별 입력 폼 구분
                if (post.type == PostType.CHECK) {
                    val (title) = request
                    post.title = title
                    post.content = ""
                    post.imageUrl = ""

                } else if (post.type == PostType.APPEND) {
                    val (title, content, imageUrl) = request
                    post.title = title
                    post.content = content
                    post.imageUrl = imageUrl
                }

            } else {
                throw PermissionDeniedException()
            }
        } else {
            throw TypeNotFoundException()
        }

        return postRepository.save(post).let { PostResponse.from(post) }
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