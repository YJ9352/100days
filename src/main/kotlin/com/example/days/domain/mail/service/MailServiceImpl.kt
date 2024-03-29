package com.example.days.domain.mail.service

import com.example.days.domain.mail.dto.EmailRequest
import com.example.days.global.common.exception.user.AuthCodeMismatchException
import com.example.days.global.infra.mail.MailUtility
import com.example.days.global.infra.redis.RedisUtil
import com.example.days.global.support.MailType
import org.springframework.stereotype.Service

@Service
class MailServiceImpl(
    val mailUtility: MailUtility,
    val redisUtil: RedisUtil
) : MailService {

    // 인증메일 발송
    override fun sendVerificationEmail(request: EmailRequest) {
        mailUtility.emailSender(request.email, MailType.VERIFYCODE)
    }

    // 코드 일치 확인
    override fun verifyCode(code: String, email: String): String {
        return redisUtil.getDataMatch(code, email, 300) ?: throw AuthCodeMismatchException()
    }
}
