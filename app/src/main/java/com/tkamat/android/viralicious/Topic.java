package com.tkamat.android.viralicious;


public class Topic {
    private String mTopicName;
    private int mMinViews;

    public Topic(String mTopicName, int mMinViews) {
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
}
