package com.tkamat.android.youwatch;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.List;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

public class TopicRecyclerFragment extends Fragment {
    private ConstraintLayout mConstraintLayout;
    private RecyclerView mTopicRecyclerView;
    private TopicAdapter mTopicAdapter;
    private FloatingActionButton mFAB;
    private ProgressBar mProgressBar;
//    private AdView mAdView;

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
                if (TopicList.get(getActivity()).getEnabledTopics().size() <= 10) {
                    Intent intent = TopicPickerActivity.newIntent(getActivity(), UUID.randomUUID());
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.limit_reached_toast), Toast.LENGTH_SHORT).show();
                }
            }
        });
//        mAdView = (AdView) v.findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().addTestDevice("BA648A93396B1115DEF0054041E7E8EB").build();
//        mAdView.loadAd(adRequest);
//        mAdView.setAdListener(new AdListener() {
//            @Override
//            public void onAdFailedToLoad(int i) {
//                mAdView.setVisibility(View.GONE);
//                super.onAdFailedToLoad(i);
//            }
//
//            @Override
//            public void onAdLoaded() {
//                mAdView.setVisibility(View.VISIBLE);
//                super.onAdLoaded();
//            }
//        });
        mTopicRecyclerView = (RecyclerView) v.findViewById(R.id.topic_recycler_view);
        mTopicRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTopicRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && mFAB.isShown()) {
                    //test
                    mFAB.hide();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mFAB.show();
                }

                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        updateUI();
        if (isFirstTime()) {
            final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle(getString(R.string.first_time_title));
            final TextView textView = new TextView(getActivity());
            textView.setText(R.string.first_time_instructions);
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(12);
            alertDialog.setView(textView, 50, 50, 50, 50);
            alertDialog.show();
        }

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

    private boolean isFirstTime() {
        SharedPreferences preferences = getActivity().getPreferences(MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean("RanBefore", false);
        if (!ranBefore) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("RanBefore", true);
            editor.commit();
        }
        return !ranBefore;
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
                boolean userSelect = false;

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    userSelect = true;
                    return false;
                }

                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (userSelect) {
                        if (TopicList.get(getActivity()).getEnabledTopics().size() <= 10) {
                            mTopic.setmEnabled(b);
                            if (b)
                                Toast.makeText(getActivity(), getString(R.string.toast_enabled), Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getActivity(), getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();
                        } else if (b) {
                            Toast.makeText(getActivity(), getString(R.string.limit_reached_toast), Toast.LENGTH_SHORT).show();
                            userSelect = false;
                            mSwitch.setChecked(false);
                        } else if (!b) {
                            mTopic.setmEnabled(false);
                            Toast.makeText(getActivity(), getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();
                        }
                    }
                    TopicList.get(getActivity()).updateTopic(mTopic);
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
