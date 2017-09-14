package com.tkamat.android.cyou;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.UUID;


public class TopicPickerFragment extends Fragment {
    private Topic mTopic;
    private EditText mTopicText;
    private Spinner mViewSpinner;
    private TextView mMatchesPerMonthText;
    private FloatingActionButton mSaveButton;

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

        mTopicText = (EditText) v.findViewById(R.id.text_topic);
        mTopicText.setText(mTopic.getmTopicName());
        mViewSpinner = (Spinner) v.findViewById(R.id.spinner_views);
        mMatchesPerMonthText = (TextView) v.findViewById(R.id.matches_per_month) ;
        mSaveButton = (FloatingActionButton) v.findViewById(R.id.save_button);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.planets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mViewSpinner.setAdapter(adapter);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_topic_picker, menu);
    }
}
