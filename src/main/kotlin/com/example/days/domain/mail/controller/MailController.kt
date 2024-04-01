package com.example.days.domain.mail.controller

import com.example.days.domain.mail.dto.EmailRequest
import com.example.days.domain.mail.service.MailService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/mail")
class MailController(
    private val mailService: MailService
) {

    @Operation(summary = "인증번호 보내기")
    @PostMapping("/sendmail")
    fun mailSend(
        @RequestBody request: EmailRequest
    ): ResponseEntity<Unit> {
        mailService.sendVerificationEmail(request)
        return ResponseEntity.status(HttpStatus.OK).build()
    }

    @Operation(summary = "인증번호 확인")
    @GetMapping("/verifycode")
    fun verifyCode(
        @RequestParam("code") code: String,
        @RequestParam("email") email: String
    ): ResponseEntity<Unit> {
        mailService.verifyCode(code, email)
        return ResponseEntity.status(HttpStatus.OK).build()
    }
}