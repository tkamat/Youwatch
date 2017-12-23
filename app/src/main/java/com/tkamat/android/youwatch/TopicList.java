package com.tkamat.android.youwatch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import database.TopicCursorWrapper;
import database.TopicDatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static database.TopicDatabaseSchema.*;

public class TopicList {
    private static volatile TopicList sTopicList;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    private TopicList(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new TopicDatabaseHelper(mContext).getWritableDatabase();
    }

    public static TopicList get(Context context) {
        if (sTopicList == null) {
            synchronized (TopicList.class) {
                if (sTopicList == null) {
                    sTopicList = new TopicList(context);
                }
            }
        } else if (!sTopicList.getmDatabase().isOpen()) {
            sTopicList.setmDatabase(new TopicDatabaseHelper(sTopicList.getmContext()).getWritableDatabase());
        }
        return sTopicList;
    }

    private SQLiteDatabase getmDatabase() {
        return mDatabase;
    }

    private void setmDatabase(SQLiteDatabase db) {
        mDatabase = db;
    }

    private Context getmContext() {
        return mContext;
    }

    public List<Topic> getTopics() {
        List<Topic> topics = new ArrayList<>();
        TopicCursorWrapper cursor = queryTopics(null, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                topics.add(cursor.getTopic());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return topics;
    }

    public List<Topic> getEnabledTopics() {
        List<Topic> topics = getTopics();
        List<Topic> enabledTopics = new ArrayList<>();
        for (Topic t : topics) {
            if (t.ismEnabled())
                enabledTopics.add(t);
        }
        return enabledTopics;
    }

    public Topic getTopic(UUID id) {
        TopicCursorWrapper cursor = queryTopics(TopicTable.Cols.UUID + " = ?", new String[] {id.toString()});

        try {
            if (cursor.getCount() == 0)
                return null;
            cursor.moveToFirst();
            return cursor.getTopic();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return new Topic("", 10000);
    }

    public void addTopic(Topic topic) {
        ContentValues values = getContentValues(topic);
        mDatabase.insert(TopicTable.NAME, null, values);
    }

    public void updateTopic(Topic topic) {
        String uuidString = topic.getmID().toString();
        ContentValues values = getContentValues(topic);

        mDatabase.update(TopicTable.NAME, values, TopicTable.Cols.UUID + " = ?", new String[] {uuidString});
    }

    public void deleteTopic(UUID id) {
        mDatabase.delete(TopicTable.NAME, TopicTable.Cols.UUID + " = ?", new String[] {id.toString()});
    }

    private static ContentValues getContentValues(Topic topic) {
        ContentValues values = new ContentValues();
        values.put(TopicTable.Cols.UUID, topic.getmID().toString());
        values.put(TopicTable.Cols.TOPIC, topic.getmTopicName());
        values.put(TopicTable.Cols.VIEWS, topic.getmMinViews());
        values.put(TopicTable.Cols.ENABLED, topic.ismEnabled() ? 1 : 0);
        values.put(TopicTable.Cols.TOP_VIDEO_NOTIFICATION_SHOWN, topic.ismTopVideoNotificationShown() ? 1 : 0);

        Gson gson = new Gson();

        List<String> videoIDs = topic.getmTopicSearcher().getmVideoIDs();
        String inputString1 = gson.toJson(videoIDs);
        values.put(TopicTable.Cols.TOPIC_SEARCHER, inputString1);

        List<String> notifiedVideos = topic.getmNotifiedVideos();
        String inputString2 = gson.toJson(notifiedVideos);
        values.put(TopicTable.Cols.NOTIFIED_VIDEOS, inputString2);

        return values;
    }

    private TopicCursorWrapper queryTopics(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(TopicTable.NAME,
                    null,
                    whereClause,
                    whereArgs,
                    null,
                    null,
                    null);
        return new TopicCursorWrapper(cursor);
    }

}
