package com.tkamat.android.youwatch

import android.app.*
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log

import java.util.ArrayList

/**
 * @author Tushaar Kamat
 * @version 9/26/17
 */

class TopicService : JobService() {

    private val isNetworkAvailableAndConnected: Boolean
        get() {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val isNetworkAvailable = cm.activeNetworkInfo != null

            return isNetworkAvailable && cm.activeNetworkInfo.isConnected
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
                    is TwitterTopic -> {
                        val oldTweetIds = t.twitterTopicSearcher?.tweetIds
                        t.twitterTopicSearcher = TwitterTopicSearcher(t)
                        t.twitterTopicSearcher?.search(this, object: TopicCallback {
                            override fun onFinished() {
                                val newTweetIds = t.twitterTopicSearcher?.tweetIds
                                val newTweetResults = t.twitterTopicSearcher?.tweetResults
                                val uniqueTweetIds = ArrayList<String>()
                                for (i in (newTweetIds?.indices ?: return)) {
                                    if (i < (newTweetResults?.size ?: return) &&
                                            !(oldTweetIds?.contains(newTweetIds[i]) ?: return) &&
                                            !t.previousNotifications.contains(newTweetIds[i])) {
                                        uniqueTweetIds.add(newTweetIds[i])
                                        val title = "New Tweet by @" + newTweetResults[i].user.screenName
                                        val body = newTweetResults[i].text
                                        t.previousNotifications.add(newTweetIds[i])
                                        TopicList.getInstance(this@TopicService)?.updateTopic(t)
                                        Util.createTwitterNotification(newTweetIds[i], title, body, this@TopicService)
                                    }
                                }
                                Log.i(TAG, "YoutubeTopic refreshed")
                                Log.i(TAG, uniqueTweetIds.toString())
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
        const val TWITTER_CHANNEL_ID = "twitter_topic_channel"

        fun newIntent(context: Context): Intent {
            return Intent(context, TopicService::class.java)
        }
    }
}
