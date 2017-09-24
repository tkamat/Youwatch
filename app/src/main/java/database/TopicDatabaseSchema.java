package database;

public class TopicDatabaseSchema {
    public static final class TopicTable {
        public static final String NAME = "topics";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TOPIC = "topic";
            public static final String VIEWS = "views";
            public static final String ENABLED = "enabled";
        }
    }

}
