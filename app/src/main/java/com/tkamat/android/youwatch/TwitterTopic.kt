package com.tkamat.android.youwatch

import java.util.*

class TwitterTopic(topicName: String,
                   var minLikes: Int,
                   var minRetweets: Int,
                   id: UUID = UUID.randomUUID()) : Topic(topicName = topicName, id = id, topicType = "twitter") {
    var twitterTopicSearcher: TwitterTopicSearcher? = null
    init {
        twitterTopicSearcher = TwitterTopicSearcher(this)
    }
}