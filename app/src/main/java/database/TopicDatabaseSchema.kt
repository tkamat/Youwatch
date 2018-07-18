package database

class TopicDatabaseSchema {
    object TopicTable {
        const val NAME = "topics"

        object Cols {
            const val TOPIC_TYPE = "topic_type"
            const val UUID = "uuid"
            const val TOPIC = "topic"
            const val VIEWS = "views"
            const val ENABLED = "enabled"
            const val TOPIC_IDS = "topic_searcher"
            const val PREVIOUS_NOTIFICATIONS = "notified_videos"
            const val FIRST_NOTIFICATION_SHOWN = "top_video"
            const val RETWEETS = "retweets"
            const val TWEET_LIKES = "tweet_likes"
        }
    }

}
