package com.tkamat.android.cyou;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import database.TopicDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

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
        List<Topic> test = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            test.add(new Topic("Topic" + i, 1000*i));
        }
        return test;
    }

}
