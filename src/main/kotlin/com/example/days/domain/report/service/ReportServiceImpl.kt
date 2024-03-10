package com.example.days.domain.report.service

import com.example.days.domain.report.dto.request.UserReportRequest
import com.example.days.domain.report.dto.response.UserReportResponse
import com.example.days.domain.report.model.UserReport
import com.example.days.domain.report.repository.ReportRepository
import com.example.days.domain.user.model.Status
import com.example.days.domain.user.repository.UserRepository
import com.example.days.global.common.exception.ModelNotFoundException
import com.example.days.global.common.exception.NotReportException
import com.example.days.global.common.exception.NotSelfReportException
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ReportServiceImpl(
    private val userRepository: UserRepository,
    private val reportRepository: ReportRepository
) : ReportService {
    @Transactional
    override fun reportUser(req: UserReportRequest, userId: Long): UserReportResponse {
        val reportedUserNickname = userRepository.findByNickname(req.reportedUserNickname) ?: TODO()
        val user = userRepository.findByIdOrNull(userId) ?: throw ModelNotFoundException("User", userId)

        if (reportedUserNickname.status == Status.BAN || reportedUserNickname.status == Status.WITHDRAW || user.status == Status.BAN || user.status == Status.WITHDRAW) {
            throw NotReportException("이 닉네임은 이미 밴이나 탈퇴처리되어 있어 신고할 수 없습니다")
        }

        if (user.nickname == req.reportedUserNickname) {
            throw NotSelfReportException("본인은 본인을 신고할 수 없습니다.")
        }

        val report = reportRepository.save(
            UserReport(
                reporter = user,
                reportedUserId = reportedUserNickname,
                content = req.content,

                )
        )
        report.reportUser()
        report.reportedUserId.countReport++
        userRepository.save(user)
        return UserReportResponse.from(report)
    }
}