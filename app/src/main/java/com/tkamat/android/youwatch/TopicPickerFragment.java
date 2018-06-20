package com.tkamat.android.youwatch;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.*;
import android.widget.*;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

import static android.content.Context.CONNECTIVITY_SERVICE;


public class TopicPickerFragment extends Fragment {
    private Topic mTopic;
    private EditText mTopicText;
    private Spinner mViewSpinner;
    private TextView mMatchesPerMonthText;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private VideoAdapter mVideoAdapter;

    private String mTopVideoID;
    private String mTopVideoTitle;
    private String mTopVideoBody;
//    private InterstitialAd mInterstitialAd;

    private Timer timer;

    private static final String ARG_TOPIC_ID = "arg_topic_id";

    public TopicPickerFragment() {

    }

    public static TopicPickerFragment newInstance(UUID topicID) {
        TopicPickerFragment fragment = new TopicPickerFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TOPIC_ID, topicID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mTopic = TopicList.get(getActivity()).getTopic((UUID) getArguments().getSerializable(ARG_TOPIC_ID));
        if (mTopic == null)
            mTopic = new Topic("", 100000, (UUID) getArguments().getSerializable(ARG_TOPIC_ID));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_topic_picker, container, false);
        setRetainInstance(true);

        mProgressBar = (ProgressBar) v.findViewById(R.id.matches_bar);
        mProgressBar.setVisibility(View.GONE);

        mTopicText = (EditText) v.findViewById(R.id.text_topic);
        mTopicText.setText(mTopic.getmTopicName());
        mTopicText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mMatchesPerMonthText.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mTopic.setmTopicName(charSequence.toString());
                if (timer != null) {
                    timer.cancel();
                }
            }

            @Override
            public void afterTextChanged(final Editable editable) {
                final Handler handler = new Handler();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateMatchesAndTopVideo();
                            }
                        });
                    }
                }, 600);
            }
        });

        mMatchesPerMonthText = (TextView) v.findViewById(R.id.matches_per_month) ;

        mViewSpinner = (Spinner) v.findViewById(R.id.spinner_views);
        List<String> viewChoices = new ArrayList<String>();
        viewChoices.add("Custom");
        viewChoices.add("1,000");
        viewChoices.add("10,000");
        viewChoices.add("100,000");
        viewChoices.add("500,000");
        viewChoices.add("1,000,000");
        viewChoices.add("5,000,000");
        viewChoices.add("10,000,000");
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, viewChoices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mViewSpinner.setAdapter(adapter);
        String minViews = NumberFormat.getInstance(Locale.US).format(mTopic.getmMinViews());
        if (adapter.getPosition(minViews) == -1) {
            adapter.add(minViews);
            mViewSpinner.setSelection(adapter.getPosition(minViews));
        } else {
            mViewSpinner.setSelection(adapter.getPosition(minViews));
        }
        class SpinnerInteractionListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
            boolean userSelect = false;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                userSelect = true;
                return false;
            }

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (userSelect) {
                    if (i == 0) {
                        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                        final EditText input = new EditText(getActivity());
                        input.setHint("Minimum Views");
                        input.setInputType(InputType.TYPE_CLASS_NUMBER);
                        int maxLength = 9;
                        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
                        alertDialog.setView(input, 50, 50, 50, 50);
                        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String number = "";
                                try {
                                    number = NumberFormat.getNumberInstance(Locale.US).format(Integer.parseInt(input.getText().toString()));
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                                adapter.add(number);
                                mViewSpinner.setSelection(adapter.getPosition(number));
                                try {
                                    mTopic.setmMinViews((NumberFormat.getNumberInstance(Locale.US).parse(number)).intValue());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                                updateMatchesAndTopVideo();
                            }
                        });
                        alertDialog.show();
                    } else {
                        try {
                            mTopic.setmMinViews((NumberFormat.getNumberInstance(Locale.US).parse(adapterView.getItemAtPosition(i).toString())).intValue());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    if (mTopic.getmTopicName() != "") {
                        updateMatchesAndTopVideo();
                    }
                }
                userSelect = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        }
        SpinnerInteractionListener listener = new SpinnerInteractionListener();
        mViewSpinner.setOnItemSelectedListener(listener);
        mViewSpinner.setOnTouchListener(listener);

        mMatchesPerMonthText = (TextView) v.findViewById(R.id.matches_per_month);
        if (!mTopicText.getText().toString().equals(""))
            updateMatchesAndTopVideo();

        mRecyclerView = (RecyclerView) v.findViewById(R.id.notified_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mVideoAdapter = new VideoAdapter();
        mRecyclerView.setAdapter(mVideoAdapter);
        mTopic.getmTopicSearcher().setmNotifiedVideoIDs(mTopic.getmNotifiedVideos());
        mTopic.getmTopicSearcher().searchForVideosFromPreviouslyNotified(mVideoAdapter);
//        mInterstitialAd = new InterstitialAd(getActivity());
//        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
//        mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("BA648A93396B1115DEF0054041E7E8EB").build());
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_topic_picker, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_button:
                if (mTopicText.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), R.string.toast_blank, Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (TopicList.get(getActivity()).getTopic(mTopic.getmID()) == null) {
                    if (!mTopic.ismTopVideoNotificationShown() && mTopic.getmTopicSearcher().getmResults().size() > 0) {
                        mTopVideoID = mTopic.getmTopicSearcher().getmVideoIDs().get(0);
                        mTopVideoTitle = "New from " + mTopic.getmTopicSearcher().getmResults().get(0).getSnippet().getChannelTitle();
                        mTopVideoBody = mTopic.getmTopicSearcher().getmResults().get(0).getSnippet().getTitle();
                    }
                    if (mTopVideoID != null && !mTopic.ismTopVideoNotificationShown()) {
                        Util.createNotification(mTopVideoID, mTopVideoTitle, mTopVideoBody, getActivity());
                        mTopic.setmTopVideoNotificationShown(true);
                        mTopic.getmNotifiedVideos().add(mTopVideoID);
                    }
                    TopicList.get(getActivity()).addTopic(mTopic);
                } else {
                    TopicList.get(getActivity()).updateTopic(mTopic);
                }
//                if (mInterstitialAd.isLoaded())
//                    mInterstitialAd.show();
                getActivity().onBackPressed();
                return true;
            case R.id.delete_button:
                if (TopicList.get(getActivity()).getTopic(mTopic.getmID()) == null) {
                } else {
                    TopicList.get(getActivity()).deleteTopic(mTopic.getmID());
                }
                getActivity().onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateMatchesAndTopVideo() {
        if (isNetworkAvaibaleAndConnected()) {
            TopicSearcher searcher = mTopic.getmTopicSearcher();
            searcher.searchForIDs(mMatchesPerMonthText, mProgressBar).searchForVideos(mMatchesPerMonthText, mProgressBar, getActivity());
        }
    }

    private void setText(final TextView text,final String value){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

    private void disableBarAndEnableText(final ProgressBar bar, final TextView text) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bar.setVisibility(View.GONE);
                text.setVisibility(View.VISIBLE);
            }
        });
    }

    private boolean isNetworkAvaibaleAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

        return isNetworkConnected;
    }

   private class VideoHolder extends RecyclerView.ViewHolder {
        private TextView mVideoTitleText;
        private TextView mVideoCreatorText;

        public VideoHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_notified_video, parent, false));
            mVideoTitleText = (TextView) itemView.findViewById(R.id.video_title);
            mVideoTitleText.setMovementMethod(LinkMovementMethod.getInstance());
            mVideoTitleText.setClickable(true);
            mVideoCreatorText = (TextView) itemView.findViewById(R.id.channel_name);
        }

        @SuppressLint("SetTextI18n")
        public void bind(String videoID, String videoTitle, String videoCreator) {
            mVideoTitleText.setText(
                    Html.fromHtml(
                    "<a href=\"https://www.youtube.com/watch?v=" +
                            videoID +
                            "\">" +
                            videoTitle +
                            "</a>"));
            mVideoCreatorText.setText("From " + videoCreator);
        }
   }

   public class VideoAdapter extends RecyclerView.Adapter<VideoHolder> {
       private List<String> mVideoIDs;
       private List<String> mVideoTitles;
       private List<String> mVideoCreators;

       public VideoAdapter() {
           mVideoIDs = new ArrayList<>();
           mVideoTitles = new ArrayList<>();
           mVideoCreators = new ArrayList<>();
       }

       @NonNull
       @Override
       public VideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
           LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
           return new VideoHolder(layoutInflater, parent);
       }

       @Override
       public void onBindViewHolder(@NonNull VideoHolder holder, int position) {
           String videoID = "";
           String videoTitle = "";
           String videoCreator = "";
           try {
               videoID = mVideoIDs.get(position);
               videoTitle = mVideoTitles.get(position);
               videoCreator = mVideoCreators.get(position);
           } catch (IndexOutOfBoundsException e) {
               e.printStackTrace();
           }
           holder.bind(videoID, videoTitle, videoCreator);
       }

       @Override
       public int getItemCount() {
           if (mVideoIDs == null) {
               return 0;
           } else {
               return mVideoIDs.size();
           }
       }

       public void setmVideoIDs(List<String> mVideoIDs) {
           this.mVideoIDs = mVideoIDs;
       }

       public void setmVideoTitles(List<String> mVideoTitles) {
           this.mVideoTitles = mVideoTitles;
       }

       public void setmVideoCreators(List<String> mVideoCreators) {
           this.mVideoCreators = mVideoCreators;
       }
   }


    public String getmTopVideoID() {
        return mTopVideoID;
    }

    public void setmTopVideoID(String mTopVideoID) {
        this.mTopVideoID = mTopVideoID;
    }

    public String getmTopVideoTitle() {
        return mTopVideoTitle;
    }

    public void setmTopVideoTitle(String mTopVideoTitle) {
        this.mTopVideoTitle = mTopVideoTitle;
    }

    public String getmTopVideoBody() {
        return mTopVideoBody;
    }

    public void setmTopVideoBody(String mTopVideoBody) {
        this.mTopVideoBody = mTopVideoBody;
    }

}
