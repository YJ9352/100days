package com.example.days.domain.user.repository

import com.example.days.domain.user.model.QUser
import com.example.days.domain.user.model.User
import com.example.days.global.infra.queryDSL.QueryDslSupport
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl : QueryDslSupport(), CustomUserRepository {

    private val user = QUser.user

    override fun searchUserByNickname(nickname: String): List<User> {
        return queryFactory.selectFrom(user)
            .where(user.nickname.containsIgnoreCase(nickname))
            .fetch()
    }

    override fun searchUserByAccountIdAndNickname(nickname: String): List<User> {
        return queryFactory.selectFrom(user)
            .where(user.nickname.containsIgnoreCase(nickname))
            .fetch()
    }
}