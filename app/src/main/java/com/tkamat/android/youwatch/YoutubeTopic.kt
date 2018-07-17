package com.tkamat.android.youwatch

import java.util.UUID

class YoutubeTopic (topicName: String,
                    var minViews: Int,
                    id: UUID = UUID.randomUUID()) : Topic(topicName = topicName, id = id, topicType = "youtube") {
    var youtubeTopicSearcher: YoutubeTopicSearcher? = null
    init {
        youtubeTopicSearcher = YoutubeTopicSearcher(this)
    }
}

