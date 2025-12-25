package com.tkamat.android.youwatch

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tkamat.android.youwatch.TopicService.Companion.YOUTUBE_CHANNEL_ID
import java.text.SimpleDateFormat
import java.util.*

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

    fun scheduleJob(context: Context) {
        val serviceComponent = ComponentName(context, TopicService::class.java)
        val builder = JobInfo.Builder(0, serviceComponent)
        builder.apply {
            setMinimumLatency((60 * 120 * 1000).toLong())
            setOverrideDeadline((60 * 180 * 1000).toLong())
            setPersisted(true)
            setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        }
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(builder.build())
    }

    fun createYoutubeNotification(videoID: String, title: String, body: String, context: Context) {
        val notificationIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoID"))
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val id = createID()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(YOUTUBE_CHANNEL_ID, "Youwatch Youtube Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Youtube notifications for Youwatch topics"
            notificationManager.createNotificationChannel(channel)
            val notification = Notification.Builder(context, YOUTUBE_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .build()
            notificationManager.notify(id, notification)
        } else {
            val notification = NotificationCompat.Builder(context, YOUTUBE_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .build()
            notificationManager.notify(id, notification)
        }
    }
}
