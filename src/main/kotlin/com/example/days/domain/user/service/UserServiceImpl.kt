package com.example.days.domain.user.service

import com.example.days.domain.mail.dto.EmailRequest
import com.example.days.domain.user.dto.request.*
import com.example.days.domain.user.dto.response.*
import com.example.days.domain.user.model.Status
import com.example.days.domain.user.model.User
import com.example.days.domain.user.model.UserRole
import com.example.days.domain.user.repository.UserRepository
import com.example.days.global.common.exception.common.ModelNotFoundException
import com.example.days.global.common.exception.user.*
import com.example.days.global.infra.mail.MailUtility
import com.example.days.global.infra.regex.RegexFunc
import com.example.days.global.infra.security.UserPrincipal
import com.example.days.global.infra.security.jwt.JwtPlugin
import com.example.days.global.support.MailType
import com.example.days.global.support.RandomCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val mailUtility: MailUtility,
    private val encoder: PasswordEncoder,
    private val jwtPlugin: JwtPlugin,
    private val regexFunc: RegexFunc
) : UserService {

    // 로그인
    override fun login(request: LoginRequest): LoginResponse {
        // 유저, 비밀번호 존재 확인
        val user = userRepository.findUserByEmail(regexFunc.regexUserEmail(request.email))
            ?: throw NoSearchUserByEmailException(request.email)

        if (!encoder.matches(regexFunc.regexPassword(request.password), user.password)
        ) throw MismatchPasswordException()

        // 해당 회원이 탈퇴 상태인데 7일이 지나지 않았을 때, 다시 활동 상태로 되돌림
        if (user.status == Status.BAN) throw UserSuspendedException()
        if (user.status == Status.WITHDRAW) {
            user.isDelete = false
            user.status = Status.ACTIVE
            userRepository.save(user)
        }

        // accessToken
        val accessToken = jwtPlugin.accessToken(
            id = user.id!!,
            email = user.email,
            role = user.role
        )

        return LoginResponse(accessToken)
    }

    // 회원가입
    override fun signUp(request: SignUpRequest): SignUpResponse {
        if (userRepository.existsByEmail(regexFunc.regexUserEmail(request.email)))
            throw DuplicateEmailException(request.email)

//        // 닉네임 중복허용 x
//        if (userRepository.existsByNickname(request.nickname))
//            throw DuplicateNicknameException(request.nickname) RandomCode(RegexFunc()).generateRandomCode(12)

        val pass =
            if (request.password == request.newPassword) encoder.encode(regexFunc.regexPassword(request.password))
            else throw MismatchPasswordException()

        // 계정이 존재하는지 확인하고, 이미 존재한다면 예외처리
        if (userRepository.existsByAccountId(request.accountId)) {
            throw DuplicateNicknameException(request.accountId)
        }

        // 요청된 accountId가 비어있으면 랜덤 코드를 생성
        val account = request.accountId
            .ifBlank { RandomCode(RegexFunc()).generateRandomCode(Random.nextInt(5, 15)) }
            .let { "@$it" } // 생성문자 앞에 id 식별용 @붙임

        return User(
            email = regexFunc.regexUserEmail(request.email),
            nickname = request.nickname,
            pass,
            birth = request.birth,
            isDelete = false,
            status = Status.ACTIVE,
            role = UserRole.USER,
            accountId = account,
            provider = null,
            providerId = null.toString()
        ).let {
            userRepository.save(it)
        }.let { SignUpResponse.from(it) }
    }

    // 회원의 가입된 email 검색 (분실시)
    override fun searchUserEmail(nickname: String): List<EmailResponse> {
        return userRepository.searchUserByNickname(nickname).map { EmailResponse.from(it) }
    }

    // 회원의 비밀번호 분실 시 재발급
    @Transactional
    override fun reissueUserPassword(request: EmailRequest) {
        val user = userRepository.findUserByEmail(request.email)

        // 이메일이 null 이 아니고, 가입된 사용자일때 메일 발송
        if (user != null && user.email == request.email) {
            val pass = mailUtility.emailSender(request.email, MailType.CHANGEPASSWORD)
            user.email = request.email
            user.password = pass
            userRepository.save(user)
        } else {
            throw NoSearchUserByEmailException(request.email)
        }
    }

    // 회원정보 조회
    override fun getMyInfo(userId: UserPrincipal): ModifyMyInfoResponse {
        val user = userRepository.findByIdOrNull(userId.id) ?: throw ModelNotFoundException("User", userId.id)
        return user.let { ModifyMyInfoResponse.from(it) }
    }

    // 회원정보 수정
    @Transactional
    override fun modifyMyInfo(userId: UserPrincipal, request: ModifyMyInfoRequest): ModifyMyInfoResponse {
        val user = userRepository.findByIdOrNull(userId.id) ?: throw ModelNotFoundException("user", userId.id)

        // accountId가 존재하는지 확인하고, 이미 존재한다면 예외처리
        if (userRepository.existsByAccountId(request.accountId)) {
            throw DuplicateNicknameException(request.accountId)
        }

        // 기존 비밀번호 재입력 후 일치 시 수정 가능
        if (encoder.matches(regexFunc.regexPassword(request.password), user.password)) {
            user.updateUser(request)
            userRepository.save(user)

        } else {
            throw MismatchPasswordException()
        }

        return ModifyMyInfoResponse(user.email, user.nickname, user.accountId, user.birth)
    }

    // 회원탈퇴 (상태 변경 후 7일 뒤 삭제)
    override fun withdraw(userId: UserPrincipal, request: WithdrawRequest) {
        val user = userRepository.findByIdOrNull(userId.id) ?: throw ModelNotFoundException("user", userId.id)
        if (encoder.matches(regexFunc.regexPassword(request.password), user.password)) {
            user.isDelete = true
            user.status = Status.WITHDRAW
            userRepository.save(user)
        } else {
            throw MismatchPasswordException()
        }
    }

    // 비밀번호 변경 (회원정보 내부에서 변경)
    @Transactional
    override fun passwordChangeInMyInfo(userId: UserPrincipal, request: UserPasswordRequest) {
        val user = userRepository.findByIdOrNull(userId.id) ?: throw ModelNotFoundException("user", userId.id)

        if (encoder.matches(request.password, user.password))
            throw InvalidPasswordError()

        if (request.password == request.newPassword) {
            user.password = encoder.encode(regexFunc.regexPassword(request.newPassword))
            userRepository.save(user)
        } else {
            throw MismatchPasswordException()
        }
    }

    // 고유아이디 or 닉네임으로 유저 검색 > 닉네임의 경우 동일아이디 전부 출력
    override fun searchUserAccountId(accountId: String): List<AccountSearchResponse> {
        return userRepository.seacrhUserByAccountIdAndNickname(accountId).map { AccountSearchResponse.from(it) }
    }

    @Scheduled(cron = "0 0 12 * * ?")
    fun userDeletedAuto() {
        val nowTime = LocalDateTime.now()
        val userDeleteAuto = nowTime.minusDays(7)
        userRepository.deleteUsersByStatusAndUpdatedAtIsLessThanEqualBatch(Status.WITHDRAW, userDeleteAuto)
    }

}
