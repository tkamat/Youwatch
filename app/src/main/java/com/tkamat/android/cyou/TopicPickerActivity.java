package com.tkamat.android.cyou;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import java.util.UUID;

public class TopicPickerActivity extends AppCompatActivity {
    public static final String EXTRA_TOPIC_ID = "topic_id";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = TopicPickerFragment.newInstance();
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }

    public static Intent newIntent(Context packageContext, UUID topicID) {
        Intent intent = new Intent(packageContext, TopicPickerActivity.class);
        intent.putExtra(EXTRA_TOPIC_ID, topicID);
        return intent;
    }
}
