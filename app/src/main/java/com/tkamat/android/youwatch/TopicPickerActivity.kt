package com.tkamat.android.youwatch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import java.util.*

class TopicPickerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)

        val fm = supportFragmentManager
        var fragment: Fragment? = fm.findFragmentById(R.id.fragment_container)

        fragment ?: run {
            when (intent.getSerializableExtra(EXTRA_TOPIC_TYPE) as String) {
                "youtube" -> {
                    fragment = YoutubeTopicFragment.newInstance(intent.getSerializableExtra(EXTRA_TOPIC_ID) as UUID)
                    fm.beginTransaction().add(R.id.fragment_container, fragment).commit()
                }
                "twitter" -> {
                    fragment = TwitterTopicFragment.newInstance(intent.getSerializableExtra(EXTRA_TOPIC_ID) as UUID)
                    fm.beginTransaction().add(R.id.fragment_container, fragment).commit()
                }
                else -> throw IllegalArgumentException("Topic type must be youtube or twitter")
            }
        }
    }

    companion object {

        const val EXTRA_TOPIC_ID = "extra_topic_ID"
        const val EXTRA_TOPIC_TYPE = "extra_topic_type"


        fun newIntent(packageContext: Context, topicID: UUID, topicType: String): Intent {
            val intent = Intent(packageContext, TopicPickerActivity::class.java)
            intent.putExtra(EXTRA_TOPIC_ID, topicID)
            intent.putExtra(EXTRA_TOPIC_TYPE, topicType)
            return intent
        }
    }
}
