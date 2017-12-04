package database;

public class TopicDatabaseSchema {
    public static final class TopicTable {
        public static final String NAME = "topics";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TOPIC = "topic";
            public static final String VIEWS = "views";
            public static final String ENABLED = "enabled";
            public static final String TOPIC_SEARCHER = "topic_searcher";
            public static final String NOTIFIED_VIDEOS = "notified_videos";
            public static final String TOP_VIDEO_NOTIFICATION_SHOWN = "top_video";
        }
    }

}
