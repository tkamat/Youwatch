package database

class TopicDatabaseSchema {
    object TopicTable {
        const val NAME = "topics"

        object Cols {
            const val UUID = "uuid"
            const val TOPIC = "topic"
            const val VIEWS = "views"
            const val ENABLED = "enabled"
            const val TOPIC_SEARCHER = "topic_searcher"
            const val NOTIFIED_VIDEOS = "notified_videos"
            const val TOP_VIDEO_NOTIFICATION_SHOWN = "top_video"
        }
    }

}
