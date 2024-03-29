package com.example.days.domain.oauth.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType

@Configuration
class KakaoOAuth2ClientConfig {

    // kakao
    @Value("\${oauth2.kakao.client-id}")
    private lateinit var kakaoclientId: String
    @Value("\${oauth2.kakao.client-secret}")
    private lateinit var kakaoclientSecret: String
    @Value("\${oauth2.kakao.redirect-uri}")
    private lateinit var kakaoredirectUri: String

    // google
    @Value("\${oauth2.google.client-id}")
    private lateinit var googleclientId: String
    @Value("\${oauth2.google.client-secret}")
    private lateinit var googleclientSecret: String
    @Value("\${oauth2.google.redirect-uri}")
    private lateinit var googleredirectUri: String

    @Bean
    fun clientRegistrationRepository(): InMemoryClientRegistrationRepository {
        val kakao: ClientRegistration = ClientRegistration.withRegistrationId("kakao")
            .clientId(kakaoclientId)
            .clientSecret(kakaoclientSecret)
            .redirectUri(kakaoredirectUri)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationUri("https://kauth.kakao.com/oauth/authorize")
            .tokenUri("https://kauth.kakao.com/oauth/token")
            .userInfoUri("https://kapi.kakao.com/v2/user/me")
            .clientName("Kakao")
            .build()

        val google: ClientRegistration = ClientRegistration.withRegistrationId("google")
            .clientId(googleclientId)
            .clientSecret(googleclientSecret)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationUri("https://accounts.google.com/o/oauth2/auth")
            .tokenUri("https://oauth2.googleapis.com/token")
            .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
            .redirectUri(googleredirectUri)
            .clientName("Google")
            .build()

        return InMemoryClientRegistrationRepository(kakao, google)
    }
}