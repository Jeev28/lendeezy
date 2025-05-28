package com.example.lendeezy.data.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.example.lendeezy.NotificationReceiver
import java.util.Calendar

/**
 * Schedules reminder using alarm manager
 */
fun scheduleNotification(context: Context, calendar: Calendar, title: String, message: String) {
    // create intent to be broadcast when alarm triggers
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        // pass title and message
        putExtra("title", title)
        putExtra("message", message)
    }

    // create pending intent which is fired by alarm manager at scheduled time
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        calendar.timeInMillis.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // schedule alarm which wakes the alarm if needed
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExact(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
}

/**
 * Check if app has permission to set exact alarms
 */
fun hasExactAlarmPermission(context: Context): Boolean {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
        // returns true if granted
    } else {
        true
    }
}

/**
 * requests user to grant exact alarm permission to app if not allowed
 */
fun requestExactAlarmPermission(context: Context) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:" + context.packageName)
        }
        context.startActivity(intent) // launch settings activity
    }
}