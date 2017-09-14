package com.tkamat.android.cyou;


import java.util.UUID;

public class Topic {
    private UUID mID;
    private String mTopicName;
    private int mMinViews;

    public Topic(String mTopicName, int mMinViews) {
        this.mTopicName = mTopicName;
        this.mMinViews = mMinViews;
        this.mID = UUID.randomUUID();
    }

    public Topic(String mTopicName, int mMinViews, UUID mID) {
        this.mID = mID;
        this.mTopicName = mTopicName;
        this.mMinViews = mMinViews;
    }

    public String getmTopicName() {
        return mTopicName;
    }

    public void setmTopicName(String mTopicName) {
        this.mTopicName = mTopicName;
    }

    public int getmMinViews() {
        return mMinViews;
    }

    public void setmMinViews(int mMinViews) {
        this.mMinViews = mMinViews;
    }

    public UUID getmID() {
        return mID;
    }

    public void setmID(UUID mID) {
        this.mID = mID;
    }
}
