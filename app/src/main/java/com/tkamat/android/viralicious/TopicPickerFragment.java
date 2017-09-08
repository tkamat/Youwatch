package com.tkamat.android.viralicious;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


public class TopicPickerFragment extends Fragment {

    private EditText mTopicText;
    private Spinner mViewSpinner;
    private TextView mMatchesPerMonthText;

    public TopicPickerFragment() {

    }

    public static TopicPickerFragment newInstance() {
        TopicPickerFragment fragment = new TopicPickerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_topic_picker, container, false);

        mTopicText = (EditText) v.findViewById(R.id.text_topic);
        mViewSpinner = (Spinner) v.findViewById(R.id.spinner_views);
        mMatchesPerMonthText = (TextView) v.findViewById(R.id.matches_per_month) ;

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.planets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mViewSpinner.setAdapter(adapter);
        return v;
    }

}
