package com.tkamat.android.cyou;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import database.TopicCursorWrapper;
import database.TopicDatabaseHelper;
import database.TopicDatabaseSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static database.TopicDatabaseSchema.*;

public class TopicList {
    private static TopicList sTopicList;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    private TopicList(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new TopicDatabaseHelper(mContext).getWritableDatabase();
    }

    public static TopicList get(Context context) {
        if (sTopicList == null)
            return new TopicList(context);
        else
            return sTopicList;
    }

    public List<Topic> getTopics() {
        List<Topic> topics = new ArrayList<>();
        TopicCursorWrapper cursor = queryCrimes(null, null);

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

    public Topic getTopic(UUID id) {
        TopicCursorWrapper cursor = queryCrimes(TopicTable.Cols.UUID + " = ?", new String[] {id.toString()});

        try {
            if (cursor.getCount() == 0)
                return null;
            cursor.moveToFirst();
            return cursor.getTopic();
        } finally {
            cursor.close();
        }
    }

    public void addTopic(Topic topic) {
        ContentValues values = getContentValues(topic);
        mDatabase.insert(TopicTable.NAME, null, values);
    }

    private static ContentValues getContentValues(Topic topic) {
        ContentValues values = new ContentValues();
        values.put(TopicTable.Cols.UUID, topic.getmID().toString());
        values.put(TopicTable.Cols.TOPIC, topic.getmTopicName());
        values.put(TopicTable.Cols.VIEWS, topic.getmMinViews());
        return values;
    }

    private TopicCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
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
