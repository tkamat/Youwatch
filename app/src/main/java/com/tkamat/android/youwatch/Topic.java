package com.tkamat.android.youwatch;


import com.google.api.services.youtube.model.Video;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Topic {
    private UUID mID;
    private String mTopicName;
    private int mMinViews;
    private boolean mEnabled;
    private boolean mTopVideoNotificationShown;
    private TopicSearcher mTopicSearcher;
    private List<String> mNotifiedVideos;

    public Topic(String mTopicName, int mMinViews) {
        this.mTopicName = mTopicName;
        this.mMinViews = mMinViews;
        this.mID = UUID.randomUUID();
        this.mEnabled = true;
        this.mTopicSearcher = new TopicSearcher(this);
        this.mNotifiedVideos = new ArrayList<>();
        this.mTopVideoNotificationShown = false;
    }

    public Topic(String mTopicName, int mMinViews, UUID mID) {
        this.mID = mID;
        this.mTopicName = mTopicName;
        this.mMinViews = mMinViews;
        this.mEnabled = true;
        this.mTopicSearcher = new TopicSearcher(this);
        this.mNotifiedVideos = new ArrayList<>();
        this.mTopVideoNotificationShown = false;
    }

    public String getmTopicName() {
        return mTopicName;
    }

    public void setmTopicName(String mTopicName) {
        this.mTopicName = mTopicName;
        mTopicSearcher = new TopicSearcher(this);
    }

    public int getmMinViews() {
        return mMinViews;
    }

    public void setmMinViews(int mMinViews) {
        this.mMinViews = mMinViews;
        mTopicSearcher = new TopicSearcher(this);
    }

    public UUID getmID() {
        return mID;
    }

    public void setmID(UUID mID) {
        this.mID = mID;
    }

    public boolean ismEnabled() {
        return mEnabled;
    }

    public void setmEnabled(boolean mEnabled) {
        this.mEnabled = mEnabled;
    }

    public TopicSearcher getmTopicSearcher() {
        return mTopicSearcher;
    }

    public void setmTopicSearcher(TopicSearcher mTopicSearcher) {
        this.mTopicSearcher = mTopicSearcher;
    }

    public List<String> getmNotifiedVideos() {
        return mNotifiedVideos;
    }

    public void setmNotifiedVideos(List<String> mNotifiedVideos) {
        this.mNotifiedVideos = mNotifiedVideos;
    }

    public boolean ismTopVideoNotificationShown() {
        return mTopVideoNotificationShown;
    }

    public void setmTopVideoNotificationShown(boolean mTopVideoNotificationShown) {
        this.mTopVideoNotificationShown = mTopVideoNotificationShown;
    }
}
