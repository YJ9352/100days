package com.example.days.domain.oauth.controller

import com.example.days.domain.oauth.model.OAuth2Provider
import com.example.days.domain.oauth.service.OAuth2ClientService
import com.example.days.domain.oauth.service.OAuth2LoginService
import com.example.days.domain.user.dto.response.LoginResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("")
class OAuth2Controller(
    private val oAuth2LoginService: OAuth2LoginService,
    private val oauth2ClientService: OAuth2ClientService
) {

    // login 페이지로 redirect
    @PreAuthorize("isAnonymous()")
    @GetMapping("/oauth2/login/code/{provider}")
    fun redirectLoginPage(
        @PathVariable provider: OAuth2Provider,
        response: HttpServletResponse
    ) {
        val loginPageUrl = oauth2ClientService.generateLoginPageUrl(provider)
        response.sendRedirect(loginPageUrl)
    }

    // AuthorizationCode 로 사용자 인증 처리 해주는 api
    @PreAuthorize("isAnonymous()")
    @GetMapping("/oauth2/callback/{provider}")
    fun callback(
        @PathVariable provider: OAuth2Provider,
        response: HttpServletResponse,
        @RequestParam(name = "code") authorizationCode: String
    ): LoginResponse {
        return oAuth2LoginService.login(provider, response, authorizationCode)
    }

}