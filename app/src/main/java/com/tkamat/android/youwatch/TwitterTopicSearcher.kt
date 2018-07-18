package com.tkamat.android.youwatch

import android.content.Context
import android.util.Log
import com.google.common.base.Joiner
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.models.Search
import com.twitter.sdk.android.core.models.Tweet

class TwitterTopicSearcher(topic: TwitterTopic) {
    val searchQuery = topic.topicName
    val minLikes = topic.minLikes
    val minRetweets = topic.minRetweets
    var tweetResults: ArrayList<Tweet> = ArrayList()
    var tweetIds: ArrayList<String> = ArrayList()
    var notifiedTweetIds = topic.previousNotifications
    var notifiedTweetUsers: ArrayList<String> = ArrayList()
    var notifiedTweetTexts: ArrayList<String> = ArrayList()
    val numberOfMatches: String
        get() = if (tweetResults.size >= 50) {
            "50+"
        } else {
            tweetResults.size.toString()
        }

    fun search(context: Context, topicCallback: TopicCallback) {
        topicCallback.onStarted()
        val config = TwitterConfig.Builder(context)
                .logger(DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(TwitterAuthConfig("QQk6jyCVJ1qg1xA22wYGTjxrR", "oKtOwAxGbpgFyDr159oQ5SziXaiaDiFFna0kMAsrj8OMog0Gbo"))
                .build()
        Twitter.initialize(config)
        val twitterApiClient = TwitterCore.getInstance().guestApiClient
        val searchService = twitterApiClient.searchService
        val tweets = searchService.tweets(searchQuery, null, "en", null, "popular", 100, null, null, null, null)
        tweets.enqueue(object: Callback<Search>() {
            override fun success(result: Result<Search>?) {
                val tweetData = result?.data?.tweets
                tweetResults.clear()
                tweetData?.let {
                    for (tweet in tweetData) {
                        if (tweet.retweetCount >= minRetweets && tweet.favoriteCount >= minLikes) {
                            tweetResults.add(tweet)
                        }
                    }
                }
                tweetIds.clear()
                for (tweet in tweetResults) {
                    tweetIds.add(tweet.idStr)
                }

                topicCallback.onFinished()
            }

            override fun failure(exception: TwitterException?) {
            }
        })
    }

    fun searchNotified(context: Context, topicCallback: TopicCallback) {
        topicCallback.onStarted()
        val config = TwitterConfig.Builder(context)
                .logger(DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(TwitterAuthConfig("QQk6jyCVJ1qg1xA22wYGTjxrR", "oKtOwAxGbpgFyDr159oQ5SziXaiaDiFFna0kMAsrj8OMog0Gbo"))
                .build()
        Twitter.initialize(config)
        val stringJoiner = Joiner.on(',')
        val tweetIds = stringJoiner.join(notifiedTweetIds)
        val twitterApiClient = TwitterCore.getInstance().guestApiClient
        val statusesService = twitterApiClient.statusesService
        val tweets = statusesService.lookup(tweetIds, null, null, null)
        tweets.enqueue(object: Callback<MutableList<Tweet>>() {
            override fun success(result: Result<MutableList<Tweet>>?) {
                val tweetData = result?.data
                notifiedTweetIds.clear()
                notifiedTweetUsers.clear()
                notifiedTweetTexts.clear()
                tweetData?.let {
                    for (tweet in tweetData) {
                        notifiedTweetIds.add(tweet.idStr)
                        notifiedTweetUsers.add(tweet.user.screenName)
                        notifiedTweetTexts.add(tweet.text)
                    }
                }
                topicCallback.onFinished()
            }

            override fun failure(exception: TwitterException?) {
            }
        })
    }

}