package com.example.days.domain.user.dto.request

import com.example.days.domain.user.repository.UserRepository
import com.example.days.global.common.exception.user.DuplicateNicknameException
import com.example.days.global.infra.regex.RegexFunc
import com.example.days.global.support.RandomCode
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.time.LocalDate

data class ModifyMyInfoRequest(
    @NotBlank
    @Schema(
        description = "비밀번호",
        example = "비밀번호는 8~16자의 영문 대소문자, 숫자, 특수문자로 이루어져야 합니다."
    )
    val password: String,

    @NotBlank
    @Schema(
        description = "이름",
        example = "이름은 영문 또는 한글로 이루어진 2~50자 사이로 작성해 주세요."
    )
    @field:Pattern(regexp = "^([a-zA-Zㄱ-ㅎ가-힣]{2,50})$")
    val nickname: String,

    @NotBlank
    @Schema(
        description = "회원 ID",
        example = "12자리 문자를 입력해주세요. 비워두시면 랜덤하게 생성됩니다."
    )
    @field:Pattern(regexp = "^[a-zA-Z0-9\\p{Punct}]{12}$")
    val accountId: String,
    val birth: LocalDate
)

