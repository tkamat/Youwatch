package com.tkamat.android.youwatch;

import android.app.*;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;
import com.google.api.services.youtube.model.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tushaar Kamat
 * @version 9/26/17
 */

public class TopicService extends JobService {
    public static final String TAG = "topic_service";
    public static final String CHANNEL_ID = "topic_channel";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        if (!isNetworkAvaibaleAndConnected()) {
            Util.scheduleJob(this);
            return false;
        }
        List<Topic> topics = TopicList.get(this).getTopics();
        for (Topic t : topics) {
            if (t.ismEnabled()) {
                List<String> oldVideoIDs = t.getmTopicSearcher().getmVideoIDs();
                t.setmTopicSearcher(new TopicSearcher(t));
                t.getmTopicSearcher().searchForIDs().searchForVideos();
                List<String> newVideoIDs = t.getmTopicSearcher().getmVideoIDs();
                List<Video> newVideoResults = t.getmTopicSearcher().getmResults();
                List<String> uniqueVideoIDs = new ArrayList<>();
                for (int i = 0; i < newVideoIDs.size(); i++) {
                    if (!oldVideoIDs.contains(newVideoIDs.get(i)) && !t.getmNotifiedVideos().contains(newVideoIDs.get(i))) {
                        uniqueVideoIDs.add(newVideoIDs.get(i));
                        String title = "New From " + newVideoResults.get(i).getSnippet().getChannelTitle();
                        String body = newVideoResults.get(i).getSnippet().getTitle();
                        t.getmNotifiedVideos().add(newVideoIDs.get(i));
                        TopicList.get(this).updateTopic(t);
                        createNotification(newVideoIDs.get(i), title, body);
                    }
                }
                Log.i(TAG, "Topic refreshed");
                Log.i(TAG, uniqueVideoIDs.toString());
            }
        }
        Util.scheduleJob(this);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }

    @Override
    public void onDestroy() {
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

    private void createNotification(String videoID, String title, String body) {
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videoID));
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Topic Watcher Service", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("test");
            notificationManager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .build();
            notificationManager.notify(0, notification);
        } else {
            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("test")
                    .setContentText("test test test")
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .build();
            notificationManager.notify(0, notification);
        }
    }
}
