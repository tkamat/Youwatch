package com.tkamat.android.youwatch

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.app.NotificationCompat

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.content.Context.NOTIFICATION_SERVICE
import com.tkamat.android.youwatch.TopicService.Companion.CHANNEL_ID

object Util {
    private fun createID(): Int {
        val now = Date()
        var id = 0
        try {
            id = Integer.parseInt(SimpleDateFormat("ddHHmmss", Locale.US).format(now))
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        return id
    }

    // schedule the start of the service every 10 - 30 seconds
    fun scheduleJob(context: Context) {
        val serviceComponent = ComponentName(context, TopicService::class.java)
        val builder = JobInfo.Builder(0, serviceComponent)
        builder.apply {
            setMinimumLatency((60 * 120 * 1000).toLong())
            setOverrideDeadline((60 * 180 * 1000).toLong())
            setPersisted(true)
            setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // require network
            //setRequiresDeviceIdle(true) // device should be idle
            //setRequiresCharging(false) // we don't care if the device is charging or not
        }
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(builder.build())
    }

    fun createNotification(videoID: String, title: String, body: String, context: Context) {
        val notificationIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoID"))
        val contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val id = createID()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Topic Watcher Service", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "test"
            notificationManager.createNotificationChannel(channel)
            val notification = Notification.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .build()
            notificationManager.notify(id, notification)
        } else {
            val notification = NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .build()
            notificationManager.notify(id, notification)
        }
    }

}

