package com.tkamat.android.youwatch

import android.app.*
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log

import java.util.ArrayList

class TopicService : JobService() {

    private val isNetworkAvailableAndConnected: Boolean
        get() {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val isNetworkAvailable = cm.activeNetworkInfo != null
            return isNetworkAvailable && cm.activeNetworkInfo?.isConnected ?: false
        }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onStartJob(jobParameters: JobParameters): Boolean {
        if (!isNetworkAvailableAndConnected) {
            Util.scheduleJob(this)
            return false
        }
        val topics = TopicList.getInstance(this)!!.topics
        for (t in topics) {
            if (t.enabled) {
                when (t) {
                    is YoutubeTopic -> {
                        val oldVideoIDs = t.youtubeTopicSearcher?.videoIds
                        t.youtubeTopicSearcher = YoutubeTopicSearcher(t)
                        t.youtubeTopicSearcher?.search(object : TopicCallback {
                            override fun onFinished() {
                                val newVideoIDs = t.youtubeTopicSearcher?.videoIds
                                val newVideoResults = t.youtubeTopicSearcher?.videoResults
                                val uniqueVideoIDs = ArrayList<String>()
                                for (i in (newVideoIDs?.indices ?: return)) {
                                    if (i < (newVideoResults?.size ?: return) &&
                                            !(oldVideoIDs?.contains(newVideoIDs[i]) ?: return) &&
                                            !t.previousNotifications.contains(newVideoIDs[i])) {
                                        uniqueVideoIDs.add(newVideoIDs[i])
                                        val title = "New Youtube Video from " + newVideoResults[i].snippet.channelTitle
                                        val body = newVideoResults[i].snippet.title
                                        t.previousNotifications.add(newVideoIDs[i])
                                        TopicList.getInstance(this@TopicService)?.updateTopic(t)
                                        Util.createYoutubeNotification(newVideoIDs[i], title, body, this@TopicService)
                                    }
                                }
                                Log.i(TAG, "YoutubeTopic refreshed")
                                Log.i(TAG, uniqueVideoIDs.toString())
                            }

                            override fun onStarted() {
                            }
                        })
                    }
                }
            }
        }
        Util.scheduleJob(this)
        return true
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean {
        return true
    }

    companion object {
        const val TAG = "topic_service"
        const val YOUTUBE_CHANNEL_ID = "youtube_topic_channel"

        fun newIntent(context: Context): Intent {
            return Intent(context, TopicService::class.java)
        }
    }
}
