package com.tkamat.android.youwatch

import android.app.Dialog
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.Html
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.*

import java.text.NumberFormat
import java.text.ParseException
import java.util.*

import android.content.Context.CONNECTIVITY_SERVICE
import kotlin.collections.ArrayList


class TopicPickerFragment : Fragment() {
    private lateinit var topic: Topic
    private lateinit var topicText: EditText
    private lateinit var viewSpinner: Spinner
    private lateinit var matchesPerMonthText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var videoAdapter: VideoAdapter

    private var topVideoID: String? = null
    private var topVideoTitle: String? = null
    private var topVideoBody: String? = null
    //    private InterstitialAd mInterstitialAd;

    private var timer: Timer? = null

    private val isNetworkAvailableAndConnected: Boolean
        get() {
            activity?.let {
                val cm = it.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                val isNetworkAvailable = cm.activeNetworkInfo != null
                return isNetworkAvailable && cm.activeNetworkInfo.isConnected
            } ?: return false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        activity?.let { act ->
            arguments?.let { arg ->
                topic = TopicList[act]?.getTopic(arg.getSerializable(ARG_TOPIC_ID) as UUID) ?: Topic("", 0)
            }
        } ?: Topic("", 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_topic_picker, container, false)
        retainInstance = true

        progressBar = v.findViewById<View>(R.id.matches_bar) as ProgressBar
        progressBar.visibility = View.GONE

        topicText = v.findViewById<View>(R.id.text_topic) as EditText
        topicText.setText(topic.topicName)
        topicText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                matchesPerMonthText.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                topic.topicName = charSequence.toString()
                topic.topicSearcher = TopicSearcher(topic)
                timer?.let {
                    it.cancel()
                }
            }

            override fun afterTextChanged(editable: Editable) {
                val handler = Handler()
                timer = Timer()
                timer?.schedule(object : TimerTask() {
                    override fun run() {
                        handler.post { updateMatchesAndTopVideo() }
                    }
                }, 600)
            }
        })

        matchesPerMonthText = v.findViewById<View>(R.id.matches_per_month) as TextView

        viewSpinner = v.findViewById<View>(R.id.spinner_views) as Spinner
        val viewChoices = ArrayList<String>()
        viewChoices.add("Custom")
        viewChoices.add("1,000")
        viewChoices.add("10,000")
        viewChoices.add("100,000")
        viewChoices.add("500,000")
        viewChoices.add("1,000,000")
        viewChoices.add("5,000,000")
        viewChoices.add("10,000,000")
        var adapter: ArrayAdapter<String>? = null
        activity?.let {
            adapter = ArrayAdapter(it, android.R.layout.simple_spinner_item, viewChoices)
        }
        adapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        viewSpinner.adapter = adapter
        val minViews = NumberFormat.getInstance(Locale.US).format(topic.minViews.toLong())
        if (adapter?.getPosition(minViews) == -1) {
            adapter?.add(minViews)
            viewSpinner.setSelection(adapter?.getPosition(minViews) ?: 0)
        } else {
            viewSpinner.setSelection(adapter?.getPosition(minViews) ?: 0)
        }
        class SpinnerInteractionListener : AdapterView.OnItemSelectedListener, View.OnTouchListener {
            var userSelect = false

            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                userSelect = true
                return false
            }

            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                if (userSelect) {
                    if (i == 0) {
                        var alertDialog: AlertDialog? = null
                        var input: EditText? = null
                        activity?.let {
                            alertDialog = AlertDialog.Builder(it).create()
                            input = EditText(it)
                        }
                        input?.hint = "Minimum Views"
                        input?.inputType = InputType.TYPE_CLASS_NUMBER
                        val maxLength = 9
                        input?.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
                        alertDialog?.setView(input, 50, 50, 50, 50)
                        alertDialog?.setButton(Dialog.BUTTON_POSITIVE, "Ok") { _, _ ->
                            var number = ""
                            try {
                                number = NumberFormat.getNumberInstance(Locale.US).format(Integer.parseInt(input?.text.toString()).toLong())
                            } catch (e: NumberFormatException) {
                                e.printStackTrace()
                            }

                            adapter?.add(number)
                            viewSpinner.setSelection(adapter?.getPosition(number) ?: 0)
                            try {
                                topic.minViews = NumberFormat.getNumberInstance(Locale.US).parse(number).toInt()
                                topic.topicSearcher = TopicSearcher(topic)
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            } catch (e: NumberFormatException) {
                                e.printStackTrace()
                            }

                            updateMatchesAndTopVideo()
                        }
                        alertDialog?.show()
                    } else {
                        try {
                            topic.minViews = NumberFormat.getNumberInstance(Locale.US).parse(adapterView.getItemAtPosition(i).toString()).toInt()
                            topic.topicSearcher = TopicSearcher(topic)
                        } catch (e: ParseException) {
                            e.printStackTrace()
                        }

                    }
                    if (topic.topicName != "") {
                        updateMatchesAndTopVideo()
                    }
                }
                userSelect = false
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }

        val listener = SpinnerInteractionListener()
        viewSpinner.onItemSelectedListener = listener
        viewSpinner.setOnTouchListener(listener)

        matchesPerMonthText = v.findViewById<View>(R.id.matches_per_month) as TextView
        if (topicText.text.toString() != "")
            updateMatchesAndTopVideo()

        recyclerView = v.findViewById<View>(R.id.notified_recycler_view) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        videoAdapter = VideoAdapter()
        recyclerView.adapter = videoAdapter
        topic.topicSearcher?.notifiedVideoIDs = topic.notifiedVideos
        topic.topicSearcher?.searchForVideosFromPreviouslyNotified(videoAdapter)
        //        mInterstitialAd = new InterstitialAd(getActivity());
        //        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        //        mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("BA648A93396B1115DEF0054041E7E8EB").build());
        return v
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.fragment_topic_picker, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.save_button -> {
                if (topicText.text.toString() == "") {
                    Toast.makeText(activity, R.string.toast_blank, Toast.LENGTH_SHORT).show()
                    return true
                }
                activity?.let {
                    if (TopicList[it]?.getTopic(topic.id) == null) {
                        if (!topic.topVideoNotificationShown && (topic.topicSearcher?.videoResults?.size ?: 0) > 0) {
                            topVideoID = topic.topicSearcher?.videoIDs?.get(0)
                            topVideoTitle = "New from " + topic.topicSearcher?.videoResults?.get(0)?.snippet?.channelTitle
                            topVideoBody = topic.topicSearcher?.videoResults?.get(0)?.snippet?.title
                        }
                        if (topVideoID != null && !topic.topVideoNotificationShown) {
                            Util.createNotification(topVideoID!!, topVideoTitle!!, topVideoBody!!, activity!!)
                            topic.topVideoNotificationShown = true
                            topVideoID?.let {
                                topic.notifiedVideos.add(it)
                            }
                        }
                        TopicList[it]?.addTopic(topic)
                    } else {
                        TopicList[it]?.updateTopic(topic)
                    }
                    it.onBackPressed()
                }
                return true
            }
            R.id.delete_button -> {
                activity?.let {
                    if (TopicList[it]?.getTopic(topic.id) != null) {
                        TopicList[it]?.deleteTopic(topic.id)
                    }
                    it.onBackPressed()
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateMatchesAndTopVideo() {
        if (isNetworkAvailableAndConnected) {
            val searcher = topic.topicSearcher
            activity?.let {
                searcher?.searchForIDs(matchesPerMonthText, progressBar)?.searchForVideos(matchesPerMonthText, progressBar, it)
            }
        }
    }

    private fun setText(text: TextView, value: String) {
        activity?.runOnUiThread { text.text = value }
    }

    private fun disableBarAndEnableText(bar: ProgressBar, text: TextView) {
        activity?.runOnUiThread {
            bar.visibility = View.GONE
            text.visibility = View.VISIBLE
        }
    }

    private inner class VideoHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_notified_video, parent, false)) {
        private val videoTitleText: TextView = itemView.findViewById<View>(R.id.video_title) as TextView
        private val videoCreatorText: TextView = itemView.findViewById<View>(R.id.channel_name) as TextView

        init {
            videoTitleText.movementMethod = LinkMovementMethod.getInstance()
            videoTitleText.isClickable = true
        }

        fun bind(videoID: String, videoTitle: String, videoCreator: String) {
            videoTitleText.text = Html.fromHtml(
                    "<a href=\"https://www.youtube.com/watch?v=" +
                            videoID +
                            "\">" +
                            videoTitle +
                            "</a>")
            videoCreatorText.text = "From $videoCreator"
        }
    }

    private inner class VideoAdapter : RecyclerView.Adapter<VideoHolder>() {
        private var videoIDs: List<String> = ArrayList()
        private var videoTitles: List<String> = ArrayList()
        private var videoCreators: List<String> = ArrayList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
            val layoutInflater = LayoutInflater.from(activity)
            return VideoHolder(layoutInflater, parent)
        }

        override fun onBindViewHolder(holder: VideoHolder, position: Int) {
            var videoID = ""
            var videoTitle = ""
            var videoCreator = ""
            try {
                videoID = videoIDs[position]
                videoTitle = videoTitles[position]
                videoCreator = videoCreators[position]
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTrace()
            }

            holder.bind(videoID, videoTitle, videoCreator)
        }

        override fun getItemCount(): Int {
            return videoIDs.size
        }
    }


    companion object {

        private const val ARG_TOPIC_ID = "arg_topic_id"

        fun newInstance(topicID: UUID): TopicPickerFragment {
            val fragment = TopicPickerFragment()
            val args = Bundle()
            args.putSerializable(ARG_TOPIC_ID, topicID)
            fragment.arguments = args
            return fragment
        }
    }

}
