package com.tkamat.android.youwatch

import android.os.AsyncTask

import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.Joiner
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.SearchResult
import com.google.api.services.youtube.model.Video

import java.io.IOException
import java.math.BigInteger
import java.util.ArrayList
import java.util.Calendar

const val API_KEY = "AIzaSyB12Ik50RFYt4jixEcpNTSQ7hBT4d-JmjU"
const val NUMBER_OF_VIDEOS: Long = 50

class YoutubeTopicSearcher(topic: YoutubeTopic) {
    val searchQuery: String = topic.topicName
    val minViews: Int = topic.minViews
    private var youtube: YouTube? = null
    var videoIds: ArrayList<String>? = ArrayList()
    var videoResults: ArrayList<Video>? = ArrayList()
    var notifiedVideoIDs = topic.previousNotifications
    var notifiedVideoTitles: ArrayList<String>? = ArrayList()
    var notifiedVideoCreators: ArrayList<String>? = ArrayList()

    val numberOfMatches: String
        get() = if (videoResults != null && videoResults?.size == 50) {
            "50+"
        } else if (videoResults != null) {
            videoResults?.size.toString() + ""
        } else {
            ""
        }

    private fun filterResults() {
        videoResults?.let {
            for (i in it.indices.reversed()) {
                if (it[i].statistics.viewCount <= BigInteger.valueOf(minViews.toLong()) &&
                        videoIds?.get(i) != null) {
                    it.removeAt(i)
                    videoIds?.removeAt(i)
                }
            }
        }
    }

    private inner class MakeSearchRequest(val callback: TopicCallback) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            val transport = NetHttpTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            youtube = YouTube.Builder(transport, jsonFactory, HttpRequestInitializer { }).setApplicationName("Youwatch").build()
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -1)

            try {
                val search = youtube?.search()?.list("id,snippet")
                search?.apply {
                    key = API_KEY
                    q = searchQuery
                    type = "video"
                    fields = "items(id/videoId)"
                    maxResults = NUMBER_OF_VIDEOS
                    publishedAfter = DateTime(cal.time)
                    order = "viewCount"
                    relevanceLanguage = "en"
                }
                val searchListResult: MutableList<SearchResult>? = search?.execute()?.items as? ArrayList<SearchResult>
                videoIds?.clear()
                searchListResult?.let {
                    for (result in it) {
                        videoIds?.add(result.id.videoId)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val stringJoiner = Joiner.on(',')
            val videoIds = stringJoiner.join(videoIds)
            videoResults?.clear()
            try {
                val listVideosQuery = youtube
                        ?.videos()
                        ?.list("snippet, recordingDetails, statistics")
                        ?.setId(videoIds)
                listVideosQuery?.key = API_KEY
                videoResults = listVideosQuery?.execute()?.items as? ArrayList<Video>
                filterResults()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPreExecute() {
            callback.onStarted()
        }

        public override fun onPostExecute(aVoid: Void?) {
            callback.onFinished()
        }
    }

    private inner class MakeSearchRequestForNotified(val callback: TopicCallback) : AsyncTask<Void, Void, Void>() {
        var notifiedVideoList: List<Video>? = ArrayList()

        override fun doInBackground(vararg voids: Void): Void? {
            val stringJoiner = Joiner.on(',')
            val videoIds = stringJoiner.join(notifiedVideoIDs)
            try {
                val listVideosQuery = youtube
                        ?.videos()
                        ?.list("snippet, recordingDetails, statistics")
                        ?.setId(videoIds)
                listVideosQuery?.key = API_KEY
                notifiedVideoList = listVideosQuery?.execute()?.items
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            notifiedVideoList?.let {
                for (video in it) {
                    notifiedVideoTitles?.add(video.snippet.title)
                    notifiedVideoCreators?.add(video.snippet.channelTitle)
                }
            }
            callback.onFinished()
        }
    }

    fun search(callback: TopicCallback) {
        val request = MakeSearchRequest(callback)
        request.execute()
    }

    fun searchNotified(callback: TopicCallback) {
        val request = MakeSearchRequestForNotified(callback)
        request.execute()
    }
}
