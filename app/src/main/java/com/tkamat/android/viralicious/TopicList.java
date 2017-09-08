package com.tkamat.android.viralicious;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class TopicList {
    private static TopicList sTopicList;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    private TopicList(Context context) {
        mContext = context.getApplicationContext();
    }

    public static TopicList get(Context context) {
        if (sTopicList == null)
            return new TopicList(context);
        else
            return sTopicList;
    }


}
