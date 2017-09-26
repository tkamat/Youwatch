package database;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.widget.ArrayAdapter;
import com.google.api.services.youtube.model.Video;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tkamat.android.youwatch.Topic;
import com.tkamat.android.youwatch.TopicSearcher;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
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
        int enabled = getInt(getColumnIndex(TopicTable.Cols.ENABLED));
        String pastResults = getString(getColumnIndex(TopicTable.Cols.TOPIC_SEARCHER));

        Topic topic = new Topic(title, views, UUID.fromString(uuidString));
        topic.setmEnabled(enabled != 0);
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>(){}.getType();
        List<String> finalOutput = gson.fromJson(pastResults, type);
        topic.getmTopicSearcher().setmVideoIDs(finalOutput);

        return topic;
    }
}
