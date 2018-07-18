package com.tkamat.android.youwatch


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.*
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.list_item_notified_topic.*
import java.text.NumberFormat
import java.text.ParseException
import java.util.*
import kotlin.collections.ArrayList


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [TwitterTopicFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class TwitterTopicFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var topicID: String? = null
    private lateinit var topic: TwitterTopic
    private lateinit var topicText: EditText
    private lateinit var likeSpinner: Spinner
    private lateinit var retweetSpinner: Spinner
    private lateinit var matchesPerWeekText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var tweetAdapter: TweetAdapter

    var timer: Timer? = null
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
                topic = TopicList.getInstance(act)?.getTopic(arg.getSerializable(ARG_TOPIC_ID) as UUID) as? TwitterTopic
                        ?: TwitterTopic("", 10000, 1000)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_twitter_topic, container, false)
        retainInstance = true

        progressBar = v.findViewById(R.id.matches_bar)
        progressBar.visibility = View.GONE

        matchesPerWeekText = v.findViewById(R.id.matches_per_week)

        recyclerView = v.findViewById(R.id.notified_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        tweetAdapter = TweetAdapter()
        recyclerView.adapter = tweetAdapter
        topic.twitterTopicSearcher?.notifiedTweetIds = topic.previousNotifications
        activity?.let {
            topic.twitterTopicSearcher?.searchNotified(it, object: TopicCallback {
                override fun onFinished() {
                    tweetAdapter.apply {
                        tweetIds = topic.twitterTopicSearcher?.notifiedTweetIds ?: ArrayList()
                        tweetTexts = topic.twitterTopicSearcher?.notifiedTweetTexts ?: ArrayList()
                        tweetUsers = topic.twitterTopicSearcher?.notifiedTweetUsers ?: ArrayList()
                        notifyDataSetChanged()
                    }
                }
                override fun onStarted() {
                }
            })
        }

        topicText = v.findViewById(R.id.text_topic)
        topicText.setText(topic.topicName)
        topicText.addTextChangedListener(object: TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                val handler = Handler()
                timer = Timer()
                timer?.schedule(object: TimerTask() {
                    override fun run() {
                        handler.post { updateMatchesAndTopTweet() }
                    }
                }, 600)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                matchesPerWeekText.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                topic.topicName = s.toString()
                topic.twitterTopicSearcher = TwitterTopicSearcher(topic)
                timer?.cancel()
            }
        })

        likeSpinner = v.findViewById(R.id.spinner_likes)
        retweetSpinner = v.findViewById(R.id.spinner_retweets)

        val viewChoices = ArrayList<String>()
        viewChoices.add("Custom")
        viewChoices.add("100")
        viewChoices.add("500")
        viewChoices.add("1,000")
        viewChoices.add("5,000")
        viewChoices.add("10,000")
        viewChoices.add("100,000")
        viewChoices.add("1,000,000")
        var likesAdapter: ArrayAdapter<String>? = null
        var retweetsAdapter: ArrayAdapter<String>? = null
        activity?.let {
            likesAdapter = ArrayAdapter(it, android.R.layout.simple_spinner_item, viewChoices)
            retweetsAdapter = ArrayAdapter(it, android.R.layout.simple_spinner_item, viewChoices)
        }
        likesAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        retweetsAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        likeSpinner.adapter = likesAdapter
        retweetSpinner.adapter = retweetsAdapter
        val minLikes = NumberFormat.getInstance(Locale.US).format(topic.minLikes.toLong())
        val minRetweets = NumberFormat.getInstance(Locale.US).format(topic.minRetweets.toLong())

        if (likesAdapter?.getPosition(minLikes) == -1) {
            likesAdapter?.add(minLikes)
            likeSpinner.setSelection(likesAdapter?.getPosition(minLikes) ?: 0)
        } else {
            likeSpinner.setSelection(likesAdapter?.getPosition(minLikes) ?: 0)
        }

        if (retweetsAdapter?.getPosition(minRetweets) == -1) {
            retweetsAdapter?.add(minRetweets)
            retweetSpinner.setSelection(retweetsAdapter?.getPosition(minRetweets) ?: 0)
        } else {
            retweetSpinner.setSelection(retweetsAdapter?.getPosition(minRetweets) ?: 0)
        }

        class SpinnerInteractionListenerLikes : AdapterView.OnItemSelectedListener, View.OnTouchListener {
            var userSelect = false

            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                userSelect = true
                return false
            }

            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, i: Int, l: Long) {
                if (userSelect) {
                    if (i == 0) {
                        var alertDialog: AlertDialog? = null
                        var input: EditText? = null
                        activity?.let {
                            alertDialog = AlertDialog.Builder(it).create()
                            input = EditText(it)
                        }
                        input?.hint = "Minimum Likes"
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

                            likesAdapter?.add(number)
                            likeSpinner.setSelection(likesAdapter?.getPosition(number) ?: 0)
                            try {
                                topic.minLikes = NumberFormat.getNumberInstance(Locale.US).parse(number).toInt()
                                topic.twitterTopicSearcher = TwitterTopicSearcher(topic)
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            } catch (e: NumberFormatException) {
                                e.printStackTrace()
                            }

                            updateMatchesAndTopTweet()
                        }
                        alertDialog?.show()
                    } else {
                        try {
                            topic.minLikes = NumberFormat.getNumberInstance(Locale.US).parse(adapterView.getItemAtPosition(i).toString()).toInt()
                            topic.twitterTopicSearcher = TwitterTopicSearcher(topic)
                        } catch (e: ParseException) {
                            e.printStackTrace()
                        }

                    }
                    if (topic.topicName != "") {
                        updateMatchesAndTopTweet()
                    }
                }
                userSelect = false
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }

        class SpinnerInteractionListenerRetweets : AdapterView.OnItemSelectedListener, View.OnTouchListener {
            var userSelect = false

            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                userSelect = true
                return false
            }

            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, i: Int, l: Long) {
                if (userSelect) {
                    if (i == 0) {
                        var alertDialog: AlertDialog? = null
                        var input: EditText? = null
                        activity?.let {
                            alertDialog = AlertDialog.Builder(it).create()
                            input = EditText(it)
                        }
                        input?.hint = "Minimum Retweets"
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

                            retweetsAdapter?.add(number)
                            retweetSpinner.setSelection(retweetsAdapter?.getPosition(number) ?: 0)
                            try {
                                topic.minRetweets = NumberFormat.getNumberInstance(Locale.US).parse(number).toInt()
                                topic.twitterTopicSearcher = TwitterTopicSearcher(topic)
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            } catch (e: NumberFormatException) {
                                e.printStackTrace()
                            }
                            updateMatchesAndTopTweet()
                        }
                        alertDialog?.show()
                    } else {
                        try {
                            topic.minRetweets = NumberFormat.getNumberInstance(Locale.US).parse(adapterView.getItemAtPosition(i).toString()).toInt()
                            topic.twitterTopicSearcher = TwitterTopicSearcher(topic)
                        } catch (e: ParseException) {
                            e.printStackTrace()
                        }

                    }
                    if (topic.topicName != "") {
                        updateMatchesAndTopTweet()
                    }
                }
                userSelect = false
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }

        val listenerLikes = SpinnerInteractionListenerLikes()
        likeSpinner.onItemSelectedListener = listenerLikes
        likeSpinner.setOnTouchListener(listenerLikes)

        val listenerRetweets = SpinnerInteractionListenerRetweets()
        retweetSpinner.onItemSelectedListener = listenerRetweets
        retweetSpinner.setOnTouchListener(listenerRetweets)

        if (topicText.text.toString() != "") {
            updateMatchesAndTopTweet()
        }

        return v
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.fragment_topic_picker, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.save_button -> {
                if (topicText.text.toString() == "") {
                    Toast.makeText(activity, R.string.toast_blank, Toast.LENGTH_SHORT).show()
                    return true
                }
                activity?.let {
                    var topTweetId: String? = null
                    var topTweetTitle: String? = null
                    var topTweetBody: String? = null
                    if (TopicList.getInstance(it)?.getTopic(topic.id) == null) {
                        if (!topic.firstNotificationShown && (topic.twitterTopicSearcher?.tweetResults?.size ?: 0) > 0) {
                            topTweetId = topic.twitterTopicSearcher?.tweetIds?.get(0)
                            topTweetTitle = "New Tweet by @" + topic.twitterTopicSearcher?.tweetResults?.get(0)?.user?.screenName
                            topTweetBody = topic.twitterTopicSearcher?.tweetResults?.get(0)?.text
                        }
                        if (topTweetId != null && !topic.firstNotificationShown) {
                            activity?.let {
                                Util.createTwitterNotification(topTweetId, topTweetTitle ?: "", topTweetBody ?: "", it)
                            }
                            topic.firstNotificationShown = true
                            topTweetId.let {
                                topic.previousNotifications.add(it)
                            }
                        }
                        TopicList.getInstance(it)?.addTopic(topic)
                    } else {
                        TopicList.getInstance(it)?.updateTopic(topic)
                    }
                    it.onBackPressed()
                }
                return true
            }
            R.id.delete_button -> {
                activity?.let {
                    if (TopicList.getInstance(it)?.getTopic(topic.id) != null) {
                        TopicList.getInstance(it)?.deleteTopic(topic.id)
                    }
                    it.onBackPressed()
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateMatchesAndTopTweet() {
        if (isNetworkAvailableAndConnected) {
            val searcher = topic.twitterTopicSearcher
            activity?.let {
                searcher?.search(it, object: TopicCallback {
                    override fun onFinished() {
                        progressBar.visibility = View.GONE
                        matchesPerWeekText.visibility = View.VISIBLE
                        val numberOfMatches = topic.twitterTopicSearcher?.numberOfMatches
                        val text = if (numberOfMatches == "50+") {
                            context?.getString(R.string.text_videos_past_month, numberOfMatches) + " " + context?.getString(R.string.consider_changing)
                        } else {
                            context?.getString(R.string.text_videos_past_month, numberOfMatches)
                        }
                        matchesPerWeekText.text = text
                    }

                    override fun onStarted() {
                        progressBar.visibility = View.VISIBLE
                        matchesPerWeekText.visibility = View.GONE
                    }
                })
            }
        }
    }

    inner class TweetHolder(inflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_notified_topic, parent, false)) {
        private val tweetTextView: TextView = itemView.findViewById(R.id.topic_text)
        private val tweetUserTextView: TextView = itemView.findViewById(R.id.topic_poster)

        init {
            tweetTextView.movementMethod = LinkMovementMethod.getInstance()
            tweetTextView.isClickable = true
        }

        @SuppressLint("SetTextI18n")
        fun bind(tweetId: String, tweetUser: String, tweetText: String) {
            tweetTextView.text = Html.fromHtml(
                    "<a href=\"https://twitter.com/anyuser/status/$tweetId\">" +
                    tweetText +
                    "</a>")
            tweetUserTextView.text = "From @$tweetUser"
        }
    }

    inner class TweetAdapter: RecyclerView.Adapter<TweetHolder>() {
        var tweetIds: List<String> = ArrayList()
        var tweetTexts: List<String> = ArrayList()
        var tweetUsers: List<String> = ArrayList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetHolder {
            val layoutInflater = LayoutInflater.from(activity)
            return TweetHolder(layoutInflater, parent)
        }

        override fun getItemCount(): Int {
            return tweetIds.size
        }

        override fun onBindViewHolder(holder: TweetHolder, position: Int) {
            var tweetId = ""
            var tweetText = ""
            var tweetUser = ""
            try {
                tweetId = tweetIds[position]
                tweetText = tweetTexts[position]
                tweetUser = tweetUsers[position]
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTrace()
            }

            holder.bind(tweetId, tweetUser, tweetText)
        }

    }

    companion object {
        private const val ARG_TOPIC_ID = "arg_topic_id"

        fun newInstance(topicID: UUID) =
                TwitterTopicFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(ARG_TOPIC_ID, topicID)
                    }
                }
    }
}
