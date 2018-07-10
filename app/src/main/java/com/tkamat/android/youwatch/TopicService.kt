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
        val topics = TopicList[this]!!.topics
        for (t in topics) {
            if (t.enabled) {
                val oldVideoIDs = t.topicSearcher?.videoIDs
                t.topicSearcher = TopicSearcher(t)
                t.topicSearcher?.searchForIDsService()?.searchForVideosService()
                val newVideoIDs = t.topicSearcher?.videoIDs
                val newVideoResults = t.topicSearcher?.videoResults
                val uniqueVideoIDs = ArrayList<String>()
                for (i in (newVideoIDs?.indices ?: return false)) {
                    if (i < (newVideoResults?.size ?: return false) &&
                            !(oldVideoIDs?.contains(newVideoIDs[i]) ?: return false) &&
                            !t.notifiedVideos.contains(newVideoIDs[i])) {
                        uniqueVideoIDs.add(newVideoIDs[i])
                        val title = "New From " + newVideoResults[i].snippet.channelTitle
                        val body = newVideoResults[i].snippet.title
                        t.notifiedVideos.add(newVideoIDs[i])
                        TopicList[this@TopicService]?.updateTopic(t)
                        Util.createNotification(newVideoIDs[i], title, body, this@TopicService)
                    }
                }
                Log.i(TAG, "Topic refreshed")
                Log.i(TAG, uniqueVideoIDs.toString())
            }
        }
        Util.scheduleJob(this)
        return true
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean {
        return true
    }

    override fun onDestroy() {}

    companion object {
        const val TAG = "topic_service"
        const val CHANNEL_ID = "topic_channel"

        fun newIntent(context: Context): Intent {
            return Intent(context, TopicService::class.java)
        }
    }

}
