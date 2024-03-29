package com.example.days.domain.mail.service

import com.example.days.domain.mail.dto.EmailRequest

interface MailService {
    fun sendVerificationEmail(request: EmailRequest) // 인증메일 발송
    fun verifyCode(code: String, email: String): String // 코드 일치 확인
}