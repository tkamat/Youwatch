package com.tkamat.android.viralicious;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import database.TopicDatabaseHelper;

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


}
