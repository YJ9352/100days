package com.example.days.domain.user.dto.response

import com.example.days.domain.user.model.User
import java.time.LocalDate

data class ModifyMyInfoResponse(
    val email: String,
    val nickname: String,
    val accountId: String,
    val birth: LocalDate
){
    companion object {
        fun from(user: User): ModifyMyInfoResponse {
            return ModifyMyInfoResponse(
                email = user.email,
                nickname = user.nickname,
                accountId = user.accountId,
                birth = user.birth
            )
        }
    }
}
