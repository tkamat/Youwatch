package com.tkamat.android.youwatch

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*

import java.util.UUID

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.util.Log
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig

class TopicRecyclerFragment : Fragment() {
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var topicRecyclerView: RecyclerView
    private lateinit var fam: FloatingActionsMenu
    private lateinit var youtubeFAB: com.getbase.floatingactionbutton.FloatingActionButton
    private lateinit var twitterFAB: com.getbase.floatingactionbutton.FloatingActionButton
    private lateinit var emptyScreenHint: TextView
    private var topicAdapter: TopicAdapter? = null

    private val isFirstTime: Boolean
        get() {
            var preferences: SharedPreferences? = null
            activity?.let {
                preferences = it.getPreferences(MODE_PRIVATE)
            }
            val ranBefore = preferences?.getBoolean("RanBefore", false)
            if (ranBefore == false) {
                val editor = preferences?.edit()
                editor?.putBoolean("RanBefore", true)
                editor?.commit()
            }
            return !(ranBefore ?: true)
        }

    private val isFirstTimeAfterUpdate: Boolean
        get() {
            if (isFirstTime) {
                return false
            }
            try {
                var packageInfo: PackageInfo? = null
                var preferences: SharedPreferences? = null
                activity?.let {
                    packageInfo = it
                            .packageManager
                            .getPackageInfo(it.packageName, 0)
                    preferences = it.getPreferences(MODE_PRIVATE)
                }
                val currentVersion = packageInfo?.versionCode ?: 0
                val lastRunVersion = preferences?.getInt("LastRunVersion", 7) ?: 0
                return if (currentVersion > lastRunVersion) {
                    val editor = preferences?.edit()
                    editor?.putInt("LastRunVersion", currentVersion)
                    editor?.commit()
                    true
                } else {
                    false
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            return false
        }
    //    private AdView mAdView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.fragment_topic_recycler, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.help_button -> showHelpDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_topic_list, container, false)
        constraintLayout = v.findViewById(R.id.constraint_layout) as ConstraintLayout
        fam = v.findViewById(R.id.add_button) as FloatingActionsMenu
        youtubeFAB = v.findViewById(R.id.youtubeButton) as com.getbase.floatingactionbutton.FloatingActionButton
        youtubeFAB.setOnClickListener {
            activity?.let {
                if ((TopicList.getInstance(it)?.enabledTopics?.size ?: 0) <= 10) {
                    val intent = TopicPickerActivity.newIntent(it, UUID.randomUUID(), "youtube")
                    startActivity(intent)
                } else {
                    Toast.makeText(it, getString(R.string.limit_reached_toast), Toast.LENGTH_SHORT).show()
                }
            }
        }
        twitterFAB = v.findViewById(R.id.twitterButton) as com.getbase.floatingactionbutton.FloatingActionButton
        twitterFAB.setOnClickListener {
            activity?.let {
                if ((TopicList.getInstance(it)?.enabledTopics?.size ?: 0) <= 10) {
                    val intent = TopicPickerActivity.newIntent(it, UUID.randomUUID(), "twitter")
                    startActivity(intent)
                } else {
                    Toast.makeText(it, getString(R.string.limit_reached_toast), Toast.LENGTH_SHORT).show()
                }
            }
        }
        //        mAdView = (AdView) v.findViewById(R.id.adView);
        //        AdRequest adRequest = new AdRequest.Builder().addTestDevice("BA648A93396B1115DEF0054041E7E8EB").build();
        //        mAdView.loadAd(adRequest);
        //        mAdView.setAdListener(new AdListener() {
        //            @Override
        //            public void onAdFailedToLoad(int i) {
        //                super.onAdFailedToLoad(i);
        //            }
        //
        //            @Override
        //            public void onAdLoaded() {
        //                mAdView.setVisibility(View.VISIBLE);
        //                super.onAdLoaded();
        //            }
        //        });
        topicRecyclerView = v.findViewById(R.id.topic_recycler_view) as RecyclerView
        topicRecyclerView.layoutManager = LinearLayoutManager(activity)
        topicRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && fam.isShown) {
//                    fam.visibility = View.VISIBLE
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    fam.visibility = View.GONE
                }

                super.onScrollStateChanged(recyclerView, newState)
            }
        })
        emptyScreenHint = v.findViewById(R.id.emptyScreenHint) as TextView

        updateUI()

        if (isFirstTime) {
            showHelpDialog()
        }
        if (isFirstTimeAfterUpdate) {
            showUpdateDialog()
        }

        activity?.let {
            Util.scheduleJob(it)
        }
        return v
    }

    override fun onResume() {
        updateUI()
        super.onResume()
    }

    private fun updateUI() {
        showViews()

        var topicList: TopicList? = null
        activity?.let {
            topicList = TopicList.getInstance(it)
        }
        val topics = topicList?.topics

        if (topicAdapter == null) {
            topics?.let {
                topicAdapter = TopicAdapter(it)
                topicRecyclerView.adapter = topicAdapter
            }
        } else {
            topics?.let {
                topicAdapter?.setTopics(it)
                topicAdapter?.notifyDataSetChanged()
            }
        }

        activity?.let {
            if (TopicList.getInstance(it)?.topics?.size == 0) {
                emptyScreenHint.visibility = View.VISIBLE
            } else {
                emptyScreenHint.visibility = View.GONE
            }
        }
    }

    private fun hideViews() {
        topicRecyclerView.visibility = View.GONE
        fam.visibility = View.GONE
    }

    private fun showViews() {
        topicRecyclerView.visibility = View.VISIBLE
        fam.visibility = View.VISIBLE
    }

    private fun showHelpDialog() {
        val textView = TextView(activity)
        textView.apply {
            setText(R.string.first_time_instructions)
            setTextColor(Color.BLACK)
            textSize = 14f
        }
        val alertDialog = AlertDialog.Builder(activity).create()
        alertDialog.apply {
            setTitle(getString(R.string.first_time_title))
            setView(textView, 48, 48, 48, 12)
            setButton(Dialog.BUTTON_POSITIVE, "OK") { _, _ -> }
            show()
        }
    }

    private fun showUpdateDialog() {
        val textView = TextView(activity)
        textView.apply {
            setText(R.string.update_alert_text)
            setTextColor(Color.BLACK)
            textSize = 14f
        }
        val alertDialog = AlertDialog.Builder(activity).create()
        alertDialog.apply {
            setTitle(getString(R.string.update_alert_title))
            setView(textView, 24, 48, 24, 12)
            setButton(Dialog.BUTTON_POSITIVE, "OK") { _, _ -> }
            show()
        }
    }

    private inner class YoutubeTopicHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_youtube_topic, parent, false)) {
        private var topic: YoutubeTopic? = null
        private val topicText: TextView = itemView.findViewById<View>(R.id.topic_text) as TextView
        private val minimumViews: TextView = itemView.findViewById<View>(R.id.minimum_views) as TextView
        private val switch: Switch = itemView.findViewById<View>(R.id.enabled_switch) as Switch
        private val constraintLayout: ConstraintLayout = itemView.findViewById<View>(R.id.constraint_layout) as ConstraintLayout

        init {
            class SwitchListener : CompoundButton.OnCheckedChangeListener, View.OnTouchListener {
                var userSelect = false

                override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                    userSelect = true
                    return false
                }

                override fun onCheckedChanged(compoundButton: CompoundButton, b: Boolean) {
                    activity?.let {
                        if (userSelect) {
                            if (TopicList.getInstance(it)?.enabledTopics?.size ?: 0 <= 10) {
                                topic?.enabled = b
                                if (b)
                                    Toast.makeText(it, getString(R.string.toast_enabled), Toast.LENGTH_SHORT).show()
                                else
                                    Toast.makeText(it, getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show()
                            } else if (b) {
                                Toast.makeText(it, getString(R.string.limit_reached_toast), Toast.LENGTH_SHORT).show()
                                userSelect = false
                                switch.isChecked = false
                            } else if (!b) {
                                topic?.enabled = false
                                Toast.makeText(it, getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    activity?.let { a ->
                        topic?.let {t ->
                            TopicList.getInstance(a)?.updateTopic(t)
                        }
                    }
                    userSelect = false
                }
            }

            val listener = SwitchListener()
            switch.setOnCheckedChangeListener(listener)
            switch.setOnTouchListener(listener)

            constraintLayout.setOnClickListener {
                hideViews()
                activity?.let { a ->
                    topic?.let { t ->
                        val intent = TopicPickerActivity.newIntent(a, t.id, "youtube")
                        startActivity(intent)
                    }
                }
            }
        }


        fun bind(topic: YoutubeTopic) {
            this.topic = topic
            topicText.text = this.topic?.topicName
            minimumViews.text = this.topic?.minViews.toString() + " views minimum"
            switch.isChecked = (this.topic?.enabled ?: true)
        }
    }

    private inner class TwitterTopicHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_twitter_topic, parent, false)) {
        private var topic: TwitterTopic? = null
        private val topicText: TextView = itemView.findViewById<View>(R.id.topic_text) as TextView
        private val minimumLikesAndRetweets: TextView = itemView.findViewById<View>(R.id.minimum_likes_and_retweets) as TextView
        private val switch: Switch = itemView.findViewById<View>(R.id.enabled_switch) as Switch
        private val constraintLayout: ConstraintLayout = itemView.findViewById<View>(R.id.constraint_layout) as ConstraintLayout

        init {
            class SwitchListener : CompoundButton.OnCheckedChangeListener, View.OnTouchListener {
                var userSelect = false

                override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                    userSelect = true
                    return false
                }

                override fun onCheckedChanged(compoundButton: CompoundButton, b: Boolean) {
                    activity?.let {
                        if (userSelect) {
                            if (TopicList.getInstance(it)?.enabledTopics?.size ?: 0 <= 10) {
                                topic?.enabled = b
                                if (b)
                                    Toast.makeText(it, getString(R.string.toast_enabled), Toast.LENGTH_SHORT).show()
                                else
                                    Toast.makeText(it, getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show()
                            } else if (b) {
                                Toast.makeText(it, getString(R.string.limit_reached_toast), Toast.LENGTH_SHORT).show()
                                userSelect = false
                                switch.isChecked = false
                            } else if (!b) {
                                topic?.enabled = false
                                Toast.makeText(it, getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    activity?.let { a ->
                        topic?.let {t ->
                            TopicList.getInstance(a)?.updateTopic(t)
                        }
                    }
                    userSelect = false
                }
            }

            val listener = SwitchListener()
            switch.setOnCheckedChangeListener(listener)
            switch.setOnTouchListener(listener)

            constraintLayout.setOnClickListener {
                hideViews()
                activity?.let { a ->
                    topic?.let { t ->
                        val intent = TopicPickerActivity.newIntent(a, t.id, "twitter")
                        startActivity(intent)
                    }
                }
            }
        }


        @SuppressLint("SetTextI18n")
        fun bind(topic: TwitterTopic) {
            this.topic = topic
            topicText.text = this.topic?.topicName
            minimumLikesAndRetweets.text = "${topic.minLikes} likes, ${topic.minRetweets} retweets minimum"
            switch.isChecked = (this.topic?.enabled ?: true)
        }
    }

    private inner class TopicAdapter(private var topics: List<Topic>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val layoutInflater = LayoutInflater.from(activity)
            return when (viewType) {
                0 -> YoutubeTopicHolder(layoutInflater, parent)
                1 -> TwitterTopicHolder(layoutInflater, parent)
                else -> throw IllegalArgumentException("Invalid view type")
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val topic = topics?.get(position)
            when (holder.itemViewType) {
                0 -> {
                    val holder = holder as? YoutubeTopicHolder
                    holder?.bind(topic as? YoutubeTopic ?: YoutubeTopic("", 0))
                }
                1 -> {
                    val holder = holder as? TwitterTopicHolder
                    holder?.bind(topic as? TwitterTopic ?: TwitterTopic("", 0, 0))
                }
            }
        }

        override fun getItemCount(): Int {
            return topics?.size ?: 0
        }

        override fun getItemViewType(position: Int): Int {
            return when (topics?.get(position)) {
                is YoutubeTopic -> 0
                is TwitterTopic -> 1
                else -> throw IllegalArgumentException("Topic type not supported")
            }
        }

        fun setTopics(topics: List<Topic>) {
            this.topics = topics
        }
    }

    companion object {

        fun newInstance(): TopicRecyclerFragment {
            val fragment = TopicRecyclerFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }


}
