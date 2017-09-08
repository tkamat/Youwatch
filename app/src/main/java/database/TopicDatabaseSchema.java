package database;

/**
 * @author Tushaar Kamat
 * @version 9/8/17
 */

public class TopicDatabaseSchema {
    public static final class TopicTable {
        public static final String NAME = "topics";

        public static final class Cols {
            public static final String TOPIC = "topic";
            public static final String VIEWS = "views";
        }
    }

}
