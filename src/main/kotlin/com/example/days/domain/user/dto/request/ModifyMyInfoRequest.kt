package com.example.days.domain.user.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
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
        example = "5~15자리 사이로 작성해주세요. 비워두시면 랜덤하게 생성됩니다."
    )
    @Size(min = 5, max = 15)
    val accountId: String,
    val birth: LocalDate
)

