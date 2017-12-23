package com.tkamat.android.youwatch;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.Joiner;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class TopicSearcher {
    private static final String API_KEY = "AIzaSyB12Ik50RFYt4jixEcpNTSQ7hBT4d-JmjU";
    private static final long NUMBER_OF_VIDEOS = 50;

    private String searchQuery;
    private int minViews;
    private YouTube mYoutube;
    private List<SearchResult> mSearchListResult;
    private List<Video> mSearchVideoListResult;
    private List<String> mVideoIDs;
    private List<Video> mResults;
    private boolean mhasSearchListFinished;

    public TopicSearcher(Topic topic) {
        searchQuery = topic.getmTopicName();
        minViews = topic.getmMinViews();
        mVideoIDs = new ArrayList<>();
        mResults = new ArrayList<>();
        mhasSearchListFinished = false;
    }

    public String getNumberOfMatches() {
        if (mResults != null && mResults.size() == 50) {
            return "50+";
        } else if (mResults != null) {
            return mResults.size() + "";
        } else {
            return "";
        }
    }

    private void filterResults() {
        for (int i = mResults.size() - 1; i >= 0; i--) {
            if (mResults.get(i).getStatistics().getViewCount().compareTo(BigInteger.valueOf(minViews)) <= 0 && mVideoIDs.get(i) != null) {
                mResults.remove(i);
                mVideoIDs.remove(i);
            }
        }
    }

    public List<Video> getmResults() {
        return mResults;
    }

    private class MakeSearchListRequest extends AsyncTask<Void, Void, Void> {
        TextView mMatches;
        ProgressBar mBar;

        public MakeSearchListRequest() {
        }
        public MakeSearchListRequest(TextView mMatches, ProgressBar mBar) {
            this.mMatches = mMatches;
            this.mBar = mBar;
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpTransport transport = new NetHttpTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mYoutube = new YouTube.Builder(transport, jsonFactory, new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("Youwatch").build();

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1);

            try {
                YouTube.Search.List search = mYoutube.search().list("id,snippet");
                search.setKey(API_KEY);
                search.setQ(searchQuery);
                search.setType("video");
                search.setFields("items(id/videoId)");
                search.setMaxResults(NUMBER_OF_VIDEOS);
                search.setPublishedAfter(new DateTime(cal.getTime()));
                search.setOrder("viewCount");
                search.setRelevanceLanguage("en");
                mSearchListResult = search.execute().getItems();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            if (mBar != null) {
                mBar.setVisibility(View.VISIBLE);
            }
            if (mMatches != null) {
                mMatches.setVisibility(View.GONE);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mVideoIDs.clear();
            if (mSearchListResult != null) {
                for (SearchResult result  : mSearchListResult) {
                    mVideoIDs.add(result.getId().getVideoId());
                }
            }
            mhasSearchListFinished = true;
        }
    }

    private class MakeVideoListRequest extends AsyncTask<Void, Void, Void> {
        private TextView mMatches;
        private ProgressBar mBar;
        private Context mContext;

        public MakeVideoListRequest() {
        }
        public MakeVideoListRequest(TextView mMatches, ProgressBar mBar, Context mContext) {
            this.mMatches = mMatches;
            this.mBar = mBar;
            this.mContext = mContext;
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (!mhasSearchListFinished) {
            }
            Joiner stringJoiner = Joiner.on(',');
            final String videoId = stringJoiner.join(mVideoIDs);
            mResults.clear();
            try {
                YouTube.Videos.List listVideosQuery = mYoutube.videos().list("snippet, recordingDetails, statistics").setId(videoId);
                listVideosQuery.setKey(API_KEY);
                mSearchVideoListResult = listVideosQuery.execute().getItems();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mResults = mSearchVideoListResult;
            if (mResults != null) {
                filterResults();
            }

            mhasSearchListFinished = false;
            if (mBar != null) {
                mBar.setVisibility(View.GONE);
            }
            if (mMatches != null && mContext != null) {
                mMatches.setVisibility(View.VISIBLE);
                String text = "";
                String matches = getNumberOfMatches();
                if (matches.equals("50+")) {
                    text = mContext.getString(R.string.text_videos_past_month, matches) + " " + mContext.getString(R.string.consider_changing);
                } else {
                    text = mContext.getString(R.string.text_videos_past_month, matches);
                }
                mMatches.setText(text);
            }

        }
    }

    public TopicSearcher searchForIDs() {
        MakeSearchListRequest request = new MakeSearchListRequest();
        request.execute();
        return this;
    }
    public TopicSearcher searchForIDs(TextView text, ProgressBar bar) {
        MakeSearchListRequest request = new MakeSearchListRequest(text, bar);
        request.execute();
        return this;
    }
    public TopicSearcher searchForVideos() {
        MakeVideoListRequest request = new MakeVideoListRequest();
        request.execute();
        return this;
    }
    public TopicSearcher searchForVideos(TextView text, ProgressBar bar, Context context) {
        MakeVideoListRequest request = new MakeVideoListRequest(text, bar, context);
        request.execute();
        return this;
    }

    public List<String> getmVideoIDs() {
        return mVideoIDs;
    }

    public void setmVideoIDs(List<String> mVideoIDs) {
        this.mVideoIDs = mVideoIDs;
    }
}
