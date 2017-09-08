package com.tkamat.android.cyou;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class TopicRecyclerFragment extends Fragment {
    private RecyclerView mTopicRecyclerView;
    private TopicAdapter mTopicAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_topic_list, container, false);
        mTopicRecyclerView = (RecyclerView) v.findViewById(R.id.topic_recycler_view);
        mTopicRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();

        return v;
    }

    public static TopicRecyclerFragment newInstance() {
        TopicRecyclerFragment fragment = new TopicRecyclerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void updateUI() {
        TopicList topicList = TopicList.get(getActivity());
        List<Topic> topics = topicList.getTopics();

        if (mTopicAdapter == null) {
            mTopicAdapter = new TopicAdapter(topics);
            mTopicRecyclerView.setAdapter(mTopicAdapter);
        } else {
            mTopicAdapter.setmTopics(topics);
            mTopicAdapter.notifyDataSetChanged();
        }
    }

    private class TopicHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Topic mTopic;
        private TextView mTopicText;
        private TextView mMinimumViews;

        public TopicHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_topic, parent, false));
            mTopicText = (TextView) itemView.findViewById(R.id.topic_text);
            mMinimumViews = (TextView) itemView.findViewById(R.id.minimum_views);
            itemView.setOnClickListener(this);
        }

        public void bind(Topic topic) {
            mTopic = topic;
            mTopicText.setText(mTopic.getmTopicName());
            mMinimumViews.setText(mTopic.getmMinViews() + " views minimum");
        }

        @Override
        public void onClick(View view) {
            Intent intent = TopicPickerActivity.newIntent(getActivity(), mTopic.getmID());
            startActivity(intent);
        }
    }

    private class TopicAdapter extends RecyclerView.Adapter<TopicHolder> {
        private List<Topic> mTopics;

        public TopicAdapter(List<Topic> mTopics) {
            this.mTopics = mTopics;
        }

        @Override
        public TopicHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new TopicHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(TopicHolder holder, int position) {
            Topic topic = mTopics.get(position);
            holder.bind(topic);
        }

        @Override
        public int getItemCount() {
            return mTopics.size();
        }

        public void setmTopics(List<Topic> mTopics) {
            this.mTopics = mTopics;
        }
    }

}
