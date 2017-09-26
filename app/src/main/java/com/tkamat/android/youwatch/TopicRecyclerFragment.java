package com.tkamat.android.youwatch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.List;
import java.util.UUID;

public class TopicRecyclerFragment extends Fragment {
    private ConstraintLayout mConstraintLayout;
    private RecyclerView mTopicRecyclerView;
    private TopicAdapter mTopicAdapter;
    private FloatingActionButton mFAB;
    private ProgressBar mProgressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_topic_list, container, false);
        mConstraintLayout = (ConstraintLayout) v.findViewById(R.id.constraint_layout);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        mFAB = (FloatingActionButton) v.findViewById(R.id.add_button);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = TopicPickerActivity.newIntent(getActivity(), UUID.randomUUID());
                startActivity(intent);
            }
        });
        mTopicRecyclerView = (RecyclerView) v.findViewById(R.id.topic_recycler_view);
        mTopicRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTopicRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if (dy > 0 ||dy<0 && mFAB.isShown())
                {
                    mFAB.hide();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState)
            {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                {
                    mFAB.show();
                }

                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        updateUI();
        Util.scheduleJob(getActivity());
        return v;
    }

    @Override
    public void onResume() {
        updateUI();
        super.onResume();
    }

    public static TopicRecyclerFragment newInstance() {
        TopicRecyclerFragment fragment = new TopicRecyclerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void updateUI() {
        showViews();

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

    private void hideViews() {
        mTopicRecyclerView.setVisibility(View.GONE);
        mFAB.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void showViews() {
        mTopicRecyclerView.setVisibility(View.VISIBLE);
        mFAB.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    private class TopicHolder extends RecyclerView.ViewHolder {
        private Topic mTopic;
        private TextView mTopicText;
        private TextView mMinimumViews;
        private Switch mSwitch;
        private ConstraintLayout mConstraintLayout;

        public TopicHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_topic, parent, false));
            mTopicText = (TextView) itemView.findViewById(R.id.topic_text);
            mMinimumViews = (TextView) itemView.findViewById(R.id.minimum_views);
            mSwitch = (Switch) itemView.findViewById(R.id.enabled_switch);
            class SwitchListener implements CompoundButton.OnCheckedChangeListener, View.OnTouchListener {
                boolean userSelect;

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    userSelect = true;
                    return false;
                }

                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (userSelect) {
                        mTopic.setmEnabled(b);
                        if (b)
                            Toast.makeText(getActivity(), getString(R.string.toast_enabled), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getActivity(), getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();
                    }
                    userSelect = false;
                }
            }
            SwitchListener listener = new SwitchListener();
            mSwitch.setOnCheckedChangeListener(listener);
            mSwitch.setOnTouchListener(listener);

            mConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.constraint_layout);
            mConstraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideViews();
                    Intent intent = TopicPickerActivity.newIntent(getActivity(), mTopic.getmID());
                    startActivity(intent);
                }
            });
        }

        public void bind(Topic topic) {
            mTopic = topic;
            mTopicText.setText(mTopic.getmTopicName());
            mMinimumViews.setText(mTopic.getmMinViews() + " views minimum");
            mSwitch.setChecked(mTopic.ismEnabled());
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
