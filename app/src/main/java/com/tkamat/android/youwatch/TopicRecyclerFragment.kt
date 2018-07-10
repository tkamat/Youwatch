package com.tkamat.android.youwatch

import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.FloatingActionButton
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

class TopicRecyclerFragment : Fragment() {
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var topicRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private var topicAdapter: TopicAdapter? = null

    private val isFirstTime: Boolean
        get() {
            var preferences: SharedPreferences? = null
            activity?.let {
                preferences = activity!!.getPreferences(MODE_PRIVATE)
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
                val packageInfo = activity!!
                        .packageManager
                        .getPackageInfo(activity!!.packageName, 0)
                val currentVersion = packageInfo.versionCode
                val preferences = activity!!.getPreferences(MODE_PRIVATE)
                val lastRunVersion = preferences.getInt("LastRunVersion", 7)
                return if (currentVersion > lastRunVersion) {
                    val editor = preferences.edit()
                    editor.putInt("LastRunVersion", currentVersion)
                    editor.commit()
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
        constraintLayout = v.findViewById<View>(R.id.constraint_layout) as ConstraintLayout
        fab = v.findViewById<View>(R.id.add_button) as FloatingActionButton
        fab.setOnClickListener {
            activity?.let {
               if ((TopicList[it]?.enabledTopics?.size ?: 0) <= 10) {
                    val intent = TopicPickerActivity.newIntent(it, UUID.randomUUID())
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
        topicRecyclerView = v.findViewById<View>(R.id.topic_recycler_view) as RecyclerView
        topicRecyclerView.layoutManager = LinearLayoutManager(activity)
        topicRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && fab.isShown) {
                    fab.hide()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fab.show()
                }

                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        updateUI()
        if (isFirstTime) {
            showHelpDialog()
        }
        if (isFirstTimeAfterUpdate) {
            showUpdateDialog()
        }

        Util.scheduleJob(activity!!)
        return v
    }

    override fun onResume() {
        updateUI()
        super.onResume()
    }

    private fun updateUI() {
        showViews()

        val topicList = TopicList[activity!!]
        val topics = topicList!!.topics

        if (topicAdapter == null) {
            topicAdapter = TopicAdapter(topics)
            topicRecyclerView.adapter = topicAdapter
        } else {
            topicAdapter?.setTopics(topics)
            topicAdapter?.notifyDataSetChanged()
        }
    }

    private fun hideViews() {
        topicRecyclerView.visibility = View.GONE
        fab.visibility = View.GONE
    }

    private fun showViews() {
        topicRecyclerView.visibility = View.VISIBLE
        fab.visibility = View.VISIBLE
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

    private inner class TopicHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_topic, parent, false)) {
        private var topic: Topic? = null
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
                    if (userSelect) {
                        if (TopicList[activity!!]!!.enabledTopics.size <= 10) {
                            topic?.enabled = b
                            if (b)
                                Toast.makeText(activity, getString(R.string.toast_enabled), Toast.LENGTH_SHORT).show()
                            else
                                Toast.makeText(activity, getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show()
                        } else if (b) {
                            Toast.makeText(activity, getString(R.string.limit_reached_toast), Toast.LENGTH_SHORT).show()
                            userSelect = false
                            switch.isChecked = false
                        } else if (!b) {
                            topic?.enabled = false
                            Toast.makeText(activity, getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show()
                        }
                    }
                    activity?.let { a ->
                        topic?.let {t ->
                            TopicList[a]?.updateTopic(t)
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
                        val intent = TopicPickerActivity.newIntent(a, t.id)
                        startActivity(intent)
                    }
                }
            }
        }

        fun bind(topic: Topic) {
            this.topic = topic
            topicText.text = this.topic?.topicName
            minimumViews.text = this.topic?.minViews.toString() + " views minimum"
            switch.isChecked = (this.topic?.enabled ?: true)
        }
    }

    private inner class TopicAdapter(private var topics: List<Topic>?) : RecyclerView.Adapter<TopicHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicHolder {
            val layoutInflater = LayoutInflater.from(activity)
            return TopicHolder(layoutInflater, parent)
        }

        override fun onBindViewHolder(holder: TopicHolder, position: Int) {
            val topic = topics?.get(position)
            holder.bind(topic ?: Topic("", 0))
        }

        override fun getItemCount(): Int {
            return topics?.size ?: 0
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
