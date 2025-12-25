package com.tkamat.android.youwatch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.util.*

class TopicPickerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)

        val fm = supportFragmentManager
        var fragment: Fragment? = fm.findFragmentById(R.id.fragment_container)

        fragment ?: run {
            fragment = YoutubeTopicFragment.newInstance(intent.getSerializableExtra(EXTRA_TOPIC_ID) as UUID)
            fm.beginTransaction().add(R.id.fragment_container, fragment as YoutubeTopicFragment).commit()
        }
    }

    companion object {
        const val EXTRA_TOPIC_ID = "extra_topic_ID"

        fun newIntent(packageContext: Context, topicID: UUID): Intent {
            val intent = Intent(packageContext, TopicPickerActivity::class.java)
            intent.putExtra(EXTRA_TOPIC_ID, topicID)
            return intent
        }
    }
}
