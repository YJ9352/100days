package com.example.days.domain.user.service

import com.example.days.domain.mail.dto.request.EmailRequest
import com.example.days.domain.mail.dto.response.EmailResponse
import com.example.days.domain.user.dto.request.LoginRequest
import com.example.days.domain.user.dto.request.ModifyInfoRequest
import com.example.days.domain.user.dto.request.SignUpRequest
import com.example.days.domain.user.dto.request.UserPasswordRequest
import com.example.days.domain.user.dto.response.AccountSearchResponse
import com.example.days.domain.user.dto.response.LoginResponse
import com.example.days.domain.user.dto.response.ModifyInfoResponse
import com.example.days.domain.user.dto.response.SignUpResponse
import com.example.days.global.infra.security.UserPrincipal

interface UserService {

    fun login(request: LoginRequest): LoginResponse

    fun signUp(request: SignUpRequest): SignUpResponse

    fun searchUserEmail(nickname: String): List<EmailResponse>

    fun reissueUserPassword(request: EmailRequest)

    fun getInfo(userId: UserPrincipal): ModifyInfoResponse

    fun modifyInfo(userId: UserPrincipal, request: ModifyInfoRequest): ModifyInfoResponse

    fun withdraw(userId: UserPrincipal, request: UserPasswordRequest)

    fun passwordChange(userId: UserPrincipal, request: UserPasswordRequest)

    // 고유아이디 or 닉네임으로 유저 검색
    fun searchUserAccountId(accountId: String): List<AccountSearchResponse>
}