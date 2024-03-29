package com.example.days.domain.user.service

import com.example.days.domain.mail.dto.EmailRequest
import com.example.days.domain.user.dto.response.EmailResponse
import com.example.days.domain.user.dto.request.LoginRequest
import com.example.days.domain.user.dto.request.ModifyMyInfoRequest
import com.example.days.domain.user.dto.request.SignUpRequest
import com.example.days.domain.user.dto.request.UserPasswordRequest
import com.example.days.domain.user.dto.response.AccountSearchResponse
import com.example.days.domain.user.dto.response.LoginResponse
import com.example.days.domain.user.dto.response.ModifyMyInfoResponse
import com.example.days.domain.user.dto.response.SignUpResponse
import com.example.days.global.infra.security.UserPrincipal

interface UserService {


    fun login(request: LoginRequest): LoginResponse // 로그인
    fun signUp(request: SignUpRequest): SignUpResponse // 회원가입

    fun searchUserEmail(nickname: String): List<EmailResponse> // 회원의 가입된 email 검색 (분실시)
    fun reissueUserPassword(request: EmailRequest) // 회원의 비밀번호 분실 시 재발급

    fun getMyInfo(userId: UserPrincipal): ModifyMyInfoResponse // 회원정보 조회
    fun modifyMyInfo(userId: UserPrincipal, request: ModifyMyInfoRequest): ModifyMyInfoResponse // 회원정보 수정
    fun withdraw(userId: UserPrincipal, request: UserPasswordRequest) // 회원탈퇴 (상태 변경 후 7일 뒤 삭제)
    fun passwordChangeInMyInfo(userId: UserPrincipal, request: UserPasswordRequest) // 비밀번호 변경 (회원정보 내부에서 변경)

    fun searchUserAccountId(accountId: String): List<AccountSearchResponse> // 고유아이디 or 닉네임으로 유저 검색
}