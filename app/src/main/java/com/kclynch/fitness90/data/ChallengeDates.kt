package com.kclynch.fitness90.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Pure date math for a [Challenge], shared by the in-app ViewModel and the
 * home-screen widget so "what day is it" is computed exactly one way.
 */
object ChallengeDates {

    data class Status(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val todayDayNumber: Int,
        val daysRemaining: Int,
        val isFinished: Boolean
    )

    fun status(challenge: Challenge, today: LocalDate = LocalDate.now()): Status {
        val startDate = LocalDate.ofEpochDay(challenge.startEpochDay)
        val endDate = startDate.plusDays((challenge.totalDays - 1).toLong())

        val rawDayNumber = ChronoUnit.DAYS.between(startDate, today).toInt() + 1
        val todayDayNumber = rawDayNumber.coerceIn(1, challenge.totalDays)
        val isFinished = today.isAfter(endDate)
        val daysRemaining = if (isFinished) {
            0
        } else {
            ChronoUnit.DAYS.between(today, endDate).toInt().coerceAtLeast(0)
        }

        return Status(startDate, endDate, todayDayNumber, daysRemaining, isFinished)
    }
}
