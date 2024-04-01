package com.example.days.global.infra.mail

import com.example.days.global.common.exception.common.ModelNotFoundException
import com.example.days.global.infra.redis.RedisUtil
import com.example.days.global.infra.regex.RegexFunc
import com.example.days.global.support.RandomCode
import com.example.days.global.support.MailType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class MailUtility(
    private val passwordEncoder: PasswordEncoder,
    private val regexFunc: RegexFunc,
    private val randomCode: RandomCode,
    private val redisUtil: RedisUtil,
    @Value("\${mail.username}") private val username: String,
    @Autowired val javaMailSender: JavaMailSender
) {

    // 메일전송
    fun emailSender(email: String, type: MailType): String {
        val code = randomCode.generateRandomCode(10) // 10자리 랜덤문자 생성
        val pass = passwordEncoder.encode(regexFunc.regexPassword(code)) // 암호화

        val message = javaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true)

        helper.setTo(email)

        if (type == MailType.VERIFYCODE) {
            // 회원가입용 인증번호 요청일 때
            val redisKey = "verification:$email" // 메일 주소를 기반으로 레디스 키 생성
            redisUtil.setDataExpire(code, redisKey, 300) // redis에 email과 code저장, 인증시간 5분
            helper.setSubject("회원가입을 위한 이메일 인증번호입니다.")
            helper.setText("이메일 인증 번호는 $code 입니다.")
            helper.setFrom(username)
            javaMailSender.send(message)

            return code
        } else if (type == MailType.CHANGEPASSWORD) {
            // 비밀번호 재발급일 때
            helper.setSubject("임시 비밀번호를 발급해드립니다.")
            helper.setText(
                "임시 비밀번호는 $code 입니다. \n " +
                        "로그인 하신 뒤, 반드시 비밀번호를 변경해주세요."
            )
            helper.setFrom(username)
            javaMailSender.send(message)

            return pass

        } else {
            throw ModelNotFoundException("메일 타입")
        }
    }
}