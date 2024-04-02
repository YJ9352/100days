package com.example.days.domain.report.dto.response

import com.example.days.domain.report.model.UserReport

data class UserReportResponse(
    val id: Long,
    val reportedUserAccountId: String,
    val content: String,
    val reporter: String
) {
    companion object {
        fun from(userReport: UserReport): UserReportResponse {
            return UserReportResponse(
                id = userReport.id!!,
                content = userReport.content,
                reportedUserAccountId = userReport.reportedUserId.accountId,
                reporter = userReport.reporter.accountId
            )
        }
    }
}
