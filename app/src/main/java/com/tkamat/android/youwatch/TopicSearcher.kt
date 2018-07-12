package com.tkamat.android.youwatch

import android.content.Context
import android.os.AsyncTask
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView

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
import java.util.concurrent.ExecutionException

const val API_KEY = "AIzaSyB12Ik50RFYt4jixEcpNTSQ7hBT4d-JmjU"
const val NUMBER_OF_VIDEOS: Long = 50

class TopicSearcher(topic: Topic) {
    val searchQuery: String = topic.topicName
    val minViews: Int = topic.minViews
    var notifiedVideoIDs: List<String>? = topic.notifiedVideos
    private var youtube: YouTube? = null
    var searchListResult: MutableList<SearchResult>? = ArrayList()
    var videoIDs: ArrayList<String>? = ArrayList()
    var videoResults: ArrayList<Video>? = ArrayList()
    var hasSearchListFinished: Boolean = false
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
                        videoIDs?.get(i) != null) {
                    it.removeAt(i)
                    videoIDs?.removeAt(i)
                }
            }
        }
    }

    private inner class MakeSearchListRequest : AsyncTask<Void, Void, Void> {
        internal var matches: TextView? = null
        internal var bar: ProgressBar? = null

        constructor()
        constructor(mMatches: TextView, mBar: ProgressBar) {
            this.matches = mMatches
            this.bar = mBar
        }

        override fun doInBackground(vararg params: Void): Void? {
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
                searchListResult = search?.execute()?.items as? ArrayList<SearchResult>
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPreExecute() {
            bar?.visibility = View.VISIBLE
            matches?.visibility = View.GONE
        }

        public override fun onPostExecute(aVoid: Void?) {
            videoIDs?.clear()
            searchListResult?.let {
                for (result in it) {
                    videoIDs?.add(result.id.videoId)
                }
            }
            hasSearchListFinished = true
        }
    }

    private inner class MakeVideoListRequest : AsyncTask<Void, Void, Void> {
        private var matches: TextView? = null
        private var bar: ProgressBar? = null
        private var context: Context? = null
        private var searchVideoListResult: MutableList<Video>? = null

        constructor()
        constructor(matches: TextView, bar: ProgressBar, context: Context) {
            this.matches = matches
            this.bar = bar
            this.context = context
        }

        override fun doInBackground(vararg params: Void): Void? {
            if (context != null) {
                while (!hasSearchListFinished) {
                    //wait for SearchList
                }
            }
            val stringJoiner = Joiner.on(',')
            val videoIds = stringJoiner.join(videoIDs)
            videoResults?.clear()
            try {
                val listVideosQuery = youtube
                        ?.videos()
                        ?.list("snippet, recordingDetails, statistics")
                        ?.setId(videoIds)
                listVideosQuery?.key = API_KEY
                searchVideoListResult = listVideosQuery?.execute()?.items
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }

        public override fun onPostExecute(aVoid: Void?) {
            videoResults = searchVideoListResult as? ArrayList<Video>
            filterResults()

            hasSearchListFinished = false
            bar?.visibility = View.GONE
            matches?.visibility = View.VISIBLE
            val numMatches = numberOfMatches
            val text = if (numMatches == "50+") {
                context?.getString(R.string.text_videos_past_month, numMatches) + " " + context?.getString(R.string.consider_changing)
            } else {
                context?.getString(R.string.text_videos_past_month, numMatches)
            }
            matches?.text = text
        }
    }

    private inner class MakeVideoListRequestForNotifiedVideos(videoAdapter: TopicPickerFragment.VideoAdapter) : AsyncTask<Void, Void, Void>() {
        private var notifiedVideoList: List<Video>? = ArrayList()
        private var videoAdapter: TopicPickerFragment.VideoAdapter? = videoAdapter

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
            videoAdapter?.apply {
                videoIDs = notifiedVideoIDs ?: ArrayList()
                videoTitles = notifiedVideoTitles ?: ArrayList()
                videoCreators = notifiedVideoCreators ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    fun searchForIDs(text: TextView, bar: ProgressBar): TopicSearcher {
        val request = MakeSearchListRequest(text, bar)
        request.execute()
        return this
    }

    fun searchForIDsService(): TopicSearcher {
        val request = MakeSearchListRequest()
        try {
            request.execute().get()
            request.onPostExecute(null)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
        return this
    }

    fun searchForVideos(text: TextView, bar: ProgressBar, context: Context): TopicSearcher {
        val request = MakeVideoListRequest(text, bar, context)
        request.execute()
        return this
    }

    fun searchForVideosService(): TopicSearcher {
        val request = MakeVideoListRequest()
        try {
            request.execute().get()
            request.onPostExecute(null)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
        return this
    }

    fun searchForVideosFromPreviouslyNotified(videoAdapter: TopicPickerFragment.VideoAdapter): TopicSearcher {
        val request = MakeVideoListRequestForNotifiedVideos(videoAdapter)
        request.execute()
        return this
    }

}
