package com.empresa.caudal.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.empresa.caudal.MainActivity
import com.empresa.caudal.R
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private const val WORK_NAME = "caudal_daily_expense_reminder"

    fun scheduleDailyReminder(context: Context, hour: Int = 20, minute: Int = 0): Boolean {
        return try {
            val now = LocalDateTime.now()
            var target = now.with(LocalTime.of(hour, minute))
            if (!target.isAfter(now)) {
                target = target.plusDays(1)
            }
            val initialDelayMinutes = Duration.between(now, target).toMinutes().coerceAtLeast(1L)

            val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
            true
        } catch (t: Throwable) {
            t.printStackTrace()
            false
        }
    }

    fun cancelDailyReminder(context: Context) {
        try {
            WorkManager.getInstance(context.applicationContext).cancelUniqueWork(WORK_NAME)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun sendConfirmationNotification(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) return
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                DailyReminderWorker.CHANNEL_ID,
                "Recordatorios diarios",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorio diario para registrar ingresos y gastos"
            }
            manager.createNotificationChannel(channel)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, DailyReminderWorker.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Recordatorio diario activado")
                .setContentText("Caudal te avisará cada día a las 8:00 p. m. para registrar tus movimientos.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(DailyReminderWorker.NOTIFICATION_ID, notification)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}