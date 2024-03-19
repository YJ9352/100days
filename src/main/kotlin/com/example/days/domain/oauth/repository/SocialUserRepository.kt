package com.example.days.domain.oauth.repository

import com.example.days.domain.oauth.model.OAuth2Provider
import com.example.days.domain.oauth.model.SocialUser
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SocialUserRepository: CrudRepository<SocialUser, Long> {
    fun existsByProviderAndProviderId(kakao: OAuth2Provider, toString: String): Boolean
    fun findByProviderAndProviderId(kakao: OAuth2Provider, toString: String): SocialUser
}