package com.example.days.domain.user.model

import com.example.days.domain.oauth.model.OAuth2Provider
import com.example.days.domain.user.dto.request.ModifyMyInfoRequest
import com.example.days.global.entity.BaseEntity
import com.example.days.global.infra.regex.RegexFunc
import com.example.days.global.support.RandomCode
import jakarta.persistence.*
import java.time.LocalDate
import kotlin.random.Random

@Entity
@Table(name = "users")
class User(

    @Column(name = "email")
    var email: String,

    @Column(name = "nickname")
    var nickname: String,

    @Column(name = "password")
    var password: String,

    @Column(name = "birth")
    var birth: LocalDate,

    @Column(name = "isdelete")
    var isDelete: Boolean,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: Status,

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    val role: UserRole,

    @Column(name = "count_report") var countReport: Int = 0,

    // 고유 id
    @Column(name = "account_id")
    var accountId: String,

    // social login ID
    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    val provider: OAuth2Provider?,

    @Column(name = "provider_id")
    val providerId: String,

    ) : BaseEntity() {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "period")
    var period: LocalDate? = null

    fun userDeleteByAdmin() {
        status = Status.WITHDRAW
    }

    fun userBanByAdmin() {
        status = Status.BAN
    }

    fun userIsDeletedByAdmin() {
        isDelete = true
    }

    fun updateUser(request: ModifyMyInfoRequest) {
        // 요청된 accountId가 비어있으면 랜덤 코드를 생성
        val account = request.accountId
            .ifBlank {
                RandomCode(RegexFunc())
                .generateRandomCode(Random.nextInt(5, 15))
            }.let { "@$it" } // 생성문자 앞에 id 식별용 @붙임

        nickname = request.nickname
        accountId = account
        birth = request.birth
    }
}




