package com.tkamat.android.youwatch;

import android.app.IntentService;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tushaar Kamat
 * @version 9/26/17
 */

public class TopicService extends JobService {
    public static final String TAG = "topic_service";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        if (!isNetworkAvaibaleAndConnected())
            return false;
        List<Topic> topics = TopicList.get(this).getTopics();
        for (Topic t : topics) {
            List<String> oldVideoIDs = t.getmTopicSearcher().getmVideoIDs();
            t.setmTopicSearcher(new TopicSearcher(t));
            t.getmTopicSearcher().searchForIDs().searchForVideos();
            List<String> newVideoIDs = t.getmTopicSearcher().getmVideoIDs();
            List<String> uniqueVideoIDs = new ArrayList<>();
            for (int i = newVideoIDs.size() - 1; i >= 0; i--) {
                if (!oldVideoIDs.contains(newVideoIDs.get(i))) {
                    uniqueVideoIDs.add(newVideoIDs.get(i));
                }
            }
            Log.i(TAG, "Topic refreshed");
            Log.i(TAG, uniqueVideoIDs.toString());
            Util.scheduleJob(getApplicationContext());
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }

    public static Intent newIntent (Context context) {
        return new Intent(context, TopicService.class);
    }

    private boolean isNetworkAvaibaleAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

        return isNetworkConnected;
    }
}
