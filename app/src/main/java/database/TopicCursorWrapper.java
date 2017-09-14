package database;

import android.database.Cursor;
import android.database.CursorWrapper;
import com.tkamat.android.cyou.Topic;

import java.util.UUID;

import static database.TopicDatabaseSchema.*;

public class TopicCursorWrapper extends CursorWrapper {

    public TopicCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Topic getTopic() {
        String uuidString = getString(getColumnIndex(TopicTable.Cols.UUID));
        String title = getString(getColumnIndex(TopicTable.Cols.TOPIC));
        int views = getInt(getColumnIndex(TopicTable.Cols.VIEWS));

        Topic topic = new Topic(title, views, UUID.fromString(uuidString));

        return topic;
    }
}
