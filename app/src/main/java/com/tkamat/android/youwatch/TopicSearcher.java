package com.tkamat.android.youwatch;

import android.os.AsyncTask;
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
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TopicSearcher {
    private static final String API_KEY = "AIzaSyB12Ik50RFYt4jixEcpNTSQ7hBT4d-JmjU";
    private static final long NUMBER_OF_VIDEOS = 50;

    private String searchQuery;
    private int minViews;
    private YouTube mYoutube;
    private List<String> mVideoIDs;
    private List<Video> mResults;

    public TopicSearcher(Topic topic) {
        searchQuery = topic.getmTopicName();
        minViews = topic.getmMinViews();


        try {
            List<SearchResult> searchResults = new MakeSearchListRequest().execute().get();

            mVideoIDs = new ArrayList<>();
            if (searchResults != null) {
                for (SearchResult result  : searchResults) {
                    mVideoIDs.add(result.getId().getVideoId());
                }
            }

            Joiner stringJoiner = Joiner.on(',');
            String videoId = stringJoiner.join(mVideoIDs);

            mResults = new MakeVideoListRequest().execute(videoId).get();

        } catch (ExecutionException e) {

        } catch (InterruptedException e) {

        }
    }

    public String getNumberOfMatches() {
        int matches = 0;
        for (Video result : mResults) {
            if (result.getStatistics().getViewCount().compareTo(BigInteger.valueOf(minViews)) >= 0) {
                matches++;
            }
        }
        return matches + "";
    }

    public List<String> getVideoIDs() {
        return mVideoIDs;
    }

    private class MakeSearchListRequest extends AsyncTask<Void, Void, List<SearchResult>> {

        @Override
        protected List<SearchResult> doInBackground(Void... params) {
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
                return search.execute().getItems();
            } catch (IOException e) {

            }
            return null;
        }
    }

    private class MakeVideoListRequest extends AsyncTask<String, Void, List<Video>> {

        @Override
        protected List<Video> doInBackground(String... videoId) {
            try {
                YouTube.Videos.List listVideosQuery = mYoutube.videos().list("snippet, recordingDetails, statistics").setId(videoId[0]);
                listVideosQuery.setKey(API_KEY);
                return listVideosQuery.execute().getItems();
            } catch (IOException e) {

            }
            return null;
        }
    }


}
