package com.example.days.domain.user.dto.request

import java.time.LocalDate

data class ModifyMyInfoRequest(
    val password: String,
    val nickname: String,
    val accountId: String,
    val birth: LocalDate
)