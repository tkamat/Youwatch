package com.tkamat.android.youwatch;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
        mTopicText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mTopic.setmTopicName(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

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
        mViewSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                    final EditText input = new EditText(getActivity());
                    input.setHint("Minimum Views");
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    alertDialog.setView(input);
                    alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String number = NumberFormat.getNumberInstance(Locale.US).format(Integer.parseInt(input.getText().toString()));
                            adapter.add(number);
                            mViewSpinner.setSelection(adapter.getPosition(number));
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


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
                if (TopicList.get(getActivity()).getTopic(mTopic.getmID()) == null) {
                    TopicList.get(getActivity()).addTopic(mTopic);
                } else {
                    TopicList.get(getActivity()).updateTopic(mTopic);
                }
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
}