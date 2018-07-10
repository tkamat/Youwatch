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
        int topVideo = getInt(getColumnIndex(TopicTable.Cols.TOP_VIDEO_NOTIFICATION_SHOWN));
        String videoIDsString = getString(getColumnIndex(TopicTable.Cols.TOPIC_SEARCHER));
        String notifiedVideosString = getString(getColumnIndex(TopicTable.Cols.NOTIFIED_VIDEOS));

        Type type = new TypeToken<List<String>>(){}.getType();
        Gson gson = new Gson();
        List<String> videoIDs = gson.fromJson(videoIDsString, type);
        List<String> notifiedVideos = gson.fromJson(notifiedVideosString, type);

        Topic topic = new Topic(title, views, UUID.fromString(uuidString));
        topic.setEnabled(enabled != 0);
        topic.getTopicSearcher().setVideoIDs((ArrayList<String>) videoIDs);
        topic.setNotifiedVideos((ArrayList<String>) notifiedVideos);
        topic.setTopVideoNotificationShown(enabled != 0);

        return topic;
    }
}
