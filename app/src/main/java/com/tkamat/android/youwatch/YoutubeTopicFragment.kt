package com.tkamat.android.youwatch

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.*

import java.text.NumberFormat
import java.text.ParseException
import java.util.*

import android.content.Context.CONNECTIVITY_SERVICE
import android.text.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tkamat.android.youwatch.databinding.FragmentYoutubeTopicBinding
import com.tkamat.android.youwatch.databinding.ListItemNotifiedTopicBinding


class YoutubeTopicFragment : Fragment() {

    private var _binding: FragmentYoutubeTopicBinding? = null
    private val binding get() = _binding!!

    private lateinit var topic: YoutubeTopic
    private lateinit var videoAdapter: VideoAdapter

    var timer: Timer? = null
    //    private InterstitialAd mInterstitialAd;


    private val isNetworkAvailableAndConnected: Boolean
        get() {
            activity?.let {
                val cm = it.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                val isNetworkAvailable = cm.activeNetworkInfo != null
                return isNetworkAvailable && cm.activeNetworkInfo?.isConnected ?: false
            } ?: return false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        activity?.let { act ->
            arguments?.let { arg ->
                topic = TopicList.getInstance(act)?.getTopic(arg.getSerializable(ARG_TOPIC_ID) as UUID) as? YoutubeTopic
                        ?: YoutubeTopic("", 100000)
            }
        } ?: YoutubeTopic("", 100000)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentYoutubeTopicBinding.inflate(inflater, container, false)

        binding.matchesBar.visibility = View.GONE

        binding.textTopic.setText(topic.topicName)
        binding.textTopic.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                binding.matchesPerMonth.visibility = View.GONE
                binding.matchesBar.visibility = View.VISIBLE
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                topic.topicName = charSequence.toString()
                topic.youtubeTopicSearcher = YoutubeTopicSearcher(topic)
                timer?.cancel()
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
        binding.spinnerViews.adapter = adapter
        val minViews = NumberFormat.getInstance(Locale.US).format(topic.minViews.toLong())
        if (adapter?.getPosition(minViews) == -1) {
            adapter?.add(minViews)
            binding.spinnerViews.setSelection(adapter?.getPosition(minViews) ?: 0)
        } else {
            binding.spinnerViews.setSelection(adapter?.getPosition(minViews) ?: 0)
        }
        class SpinnerInteractionListener : AdapterView.OnItemSelectedListener, View.OnTouchListener {
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
                            binding.spinnerViews.setSelection(adapter?.getPosition(number) ?: 0)
                            try {
                                topic.minViews = NumberFormat.getNumberInstance(Locale.US).parse(number).toInt()
                                topic.youtubeTopicSearcher = YoutubeTopicSearcher(topic)
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
                            topic.youtubeTopicSearcher = YoutubeTopicSearcher(topic)
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
        binding.spinnerViews.onItemSelectedListener = listener
        binding.spinnerViews.setOnTouchListener(listener)

        if (binding.textTopic.text.toString() != "") {
            updateMatchesAndTopVideo()
        }

        binding.notifiedRecyclerView.layoutManager = LinearLayoutManager(activity)
        videoAdapter = VideoAdapter()
        binding.notifiedRecyclerView.adapter = videoAdapter
        topic.youtubeTopicSearcher?.notifiedVideoIDs = topic.previousNotifications
        topic.youtubeTopicSearcher?.searchNotified(object: TopicCallback {
            override fun onFinished() {
                videoAdapter.apply {
                    videoIDs = topic.youtubeTopicSearcher?.notifiedVideoIDs ?: ArrayList()
                    videoTitles = topic.youtubeTopicSearcher?.notifiedVideoTitles ?: ArrayList()
                    videoCreators = topic.youtubeTopicSearcher?.notifiedVideoCreators ?: ArrayList()
                    notifyDataSetChanged()
                }
            }

            override fun onStarted() {
            }

        })
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.fragment_topic_picker, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.save_button -> {
                if (binding.textTopic.text.toString() == "") {
                    Toast.makeText(activity, R.string.toast_blank, Toast.LENGTH_SHORT).show()
                    return true
                }
                activity?.let {
                    var topVideoID: String? = null
                    var topVideoTitle: String? = null
                    var topVideoBody: String? = null
                    if (TopicList.getInstance(it)?.getTopic(topic.id) == null) {
                        if (!topic.firstNotificationShown && (topic.youtubeTopicSearcher?.videoResults?.size ?: 0) > 0) {
                            topVideoID = topic.youtubeTopicSearcher?.videoIds?.get(0)
                            topVideoTitle = "New YouTube Video from" + topic.youtubeTopicSearcher?.videoResults?.get(0)?.snippet?.channelTitle
                            topVideoBody = topic.youtubeTopicSearcher?.videoResults?.get(0)?.snippet?.title
                        }
                        if (topVideoID != null && !topic.firstNotificationShown) {
                            activity?.let {
                                Util.createYoutubeNotification(topVideoID ?: "", topVideoTitle ?: "", topVideoBody ?: "", it)
                            }
                            topic.firstNotificationShown = true
                            topVideoID.let {
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

    private fun updateMatchesAndTopVideo() {
        if (isNetworkAvailableAndConnected) {
            val searcher = topic.youtubeTopicSearcher
            activity?.let {
                searcher?.search(object: TopicCallback {
                    override fun onFinished() {
                        binding.matchesBar.visibility = View.GONE
                        binding.matchesPerMonth.visibility = View.VISIBLE
                        val numberOfMatches = topic.youtubeTopicSearcher?.numberOfMatches
                        val text = if (numberOfMatches == "50+") {
                            context?.getString(R.string.text_videos_past_month, numberOfMatches) + " " + context?.getString(R.string.consider_changing)
                        } else {
                            context?.getString(R.string.text_videos_past_month, numberOfMatches)
                        }
                        binding.matchesPerMonth.text = text
                    }

                    override fun onStarted() {
                        binding.matchesBar.visibility = View.VISIBLE
                        binding.matchesPerMonth.visibility = View.GONE
                    }
                })
            }
        }
    }

    inner class VideoHolder(private val itemBinding: ListItemNotifiedTopicBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        init {
            itemBinding.topicText.movementMethod = LinkMovementMethod.getInstance()
            itemBinding.topicText.isClickable = true
        }

        @SuppressLint("SetTextI18n")
        fun bind(videoID: String, videoTitle: String, videoCreator: String) {
            itemBinding.topicText.text = Html.fromHtml(
                    "<a href=\"https://www.youtube.com/watch?v=$videoID\">" +
                    videoTitle +
                    "</a>")
            itemBinding.topicPoster.text = "From $videoCreator"
        }
    }

    inner class VideoAdapter : RecyclerView.Adapter<VideoHolder>() {
        var videoIDs: List<String> = ArrayList()
        var videoTitles: List<String> = ArrayList()
        var videoCreators: List<String> = ArrayList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
            val itemBinding = ListItemNotifiedTopicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VideoHolder(itemBinding)
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

        fun newInstance(topicID: UUID): YoutubeTopicFragment {
            return YoutubeTopicFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TOPIC_ID, topicID)
                }
            }
        }
    }
}
