package me.capcom.smsgateway.modules.keepalive

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import me.capcom.smsgateway.modules.orchestrator.OrchestratorService
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

// Держит приложение живым как SemySMS: доза-стойкий будильник раз в минуту,
// который переподключает сервисы к серверу и планирует следующий тик.
class KeepAliveReceiver : BroadcastReceiver(), KoinComponent {
    override fun onReceive(context: Context, intent: Intent) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "smsgateway:keepalive")
        try {
            wl.acquire(30_000L)
            try {
                get<OrchestratorService>().start(context, true)
            } catch (_: Throwable) {
            }
        } finally {
            try {
                if (wl.isHeld) wl.release()
            } catch (_: Throwable) {
            }
            schedule(context)
        }
    }

    companion object {
        const val ACTION = "me.capcom.smsgateway.KEEPALIVE_TICK"
        private const val INTERVAL_MS = 60_000L

        fun schedule(context: Context) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            val pi = PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, KeepAliveReceiver::class.java).setAction(ACTION),
                flags
            )
            val triggerAt = System.currentTimeMillis() + INTERVAL_MS
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
                } else {
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
                }
            } catch (_: SecurityException) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        }
    }
}
