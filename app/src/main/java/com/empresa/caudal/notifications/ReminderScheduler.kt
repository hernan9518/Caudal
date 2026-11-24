package com.empresa.caudal.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private const val WORK_NAME = "caudal_daily_expense_reminder"

    fun scheduleDailyReminder(context: Context, hour: Int = 20, minute: Int = 0) {
        val now = LocalDateTime.now()
        var target = now.with(LocalTime.of(hour, minute))
        if (!target.isAfter(now)) {
            target = target.plusDays(1)
        }
        val initialDelayMinutes = Duration.between(now, target).toMinutes().coerceAtLeast(1L)

        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}