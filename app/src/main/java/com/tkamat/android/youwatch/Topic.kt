package com.tkamat.android.youwatch

import java.util.ArrayList
import java.util.UUID

data class Topic @JvmOverloads constructor(var topicName: String,
                                           var minViews: Int,
                                           val id: UUID = UUID.randomUUID(),
                                           var enabled: Boolean = true,
                                           var topVideoNotificationShown: Boolean = false,
                                           var notifiedVideos: ArrayList<String> = ArrayList(),
                                           var topicSearcher: TopicSearcher? = null) {
    init {
        topicSearcher = TopicSearcher(this)
    }
}

