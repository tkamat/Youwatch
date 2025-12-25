package com.tkamat.android.youwatch

import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.tkamat.android.youwatch.databinding.FragmentTopicListBinding
import com.tkamat.android.youwatch.databinding.ListItemYoutubeTopicBinding

class TopicRecyclerFragment : Fragment() {
    private var _binding: FragmentTopicListBinding? = null
    private val binding get() = _binding!!
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_topic_recycler, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.help_button -> showHelpDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTopicListBinding.inflate(inflater, container, false)

        binding.addButton.setOnClickListener {
            activity?.let {
                if ((TopicList.getInstance(it)?.enabledTopics?.size ?: 0) <= 10) {
                    val intent = TopicPickerActivity.newIntent(it, UUID.randomUUID())
                    startActivity(intent)
                } else {
                    Toast.makeText(it, getString(R.string.limit_reached_toast), Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.topicRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.topicRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    binding.addButton.hide()
                } else if (dy < 0) {
                    binding.addButton.show()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    binding.addButton.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        // Setup swipe-to-delete
        setupSwipeToDelete()

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
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private val deleteBackground = ColorDrawable(ContextCompat.getColor(requireContext(), R.color.deleteRed))
            private val deleteIcon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_delete)

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val topics = TopicList.getInstance(requireContext())?.topics
                topics?.getOrNull(position)?.let { topic ->
                    TopicList.getInstance(requireContext())?.deleteTopic(topic.id)
                    updateUI()
                    Toast.makeText(context, "Topic deleted", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - (deleteIcon?.intrinsicHeight ?: 0)) / 2

                if (dX < 0) {
                    // Swiping left - draw red background
                    deleteBackground.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    deleteBackground.draw(c)

                    // Draw delete icon
                    deleteIcon?.let {
                        val iconTop = itemView.top + iconMargin
                        val iconBottom = iconTop + it.intrinsicHeight
                        val iconRight = itemView.right - iconMargin
                        val iconLeft = iconRight - it.intrinsicWidth
                        it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        it.setTint(Color.WHITE)
                        it.draw(c)
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.topicRecyclerView)
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
                binding.topicRecyclerView.adapter = topicAdapter
            }
        } else {
            topics?.let {
                topicAdapter?.setTopics(it)
                topicAdapter?.notifyDataSetChanged()
            }
        }

        activity?.let {
            if (TopicList.getInstance(it)?.topics?.size == 0) {
                binding.emptyScreenHint.visibility = View.VISIBLE
            } else {
                binding.emptyScreenHint.visibility = View.GONE
            }
        }
    }

    private fun hideViews() {
        binding.topicRecyclerView.visibility = View.GONE
        binding.addButton.visibility = View.GONE
    }

    private fun showViews() {
        binding.topicRecyclerView.visibility = View.VISIBLE
        binding.addButton.visibility = View.VISIBLE
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

    private inner class YoutubeTopicHolder(private val itemBinding: ListItemYoutubeTopicBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        private var topic: YoutubeTopic? = null

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
                            } else if (b) {
                                Toast.makeText(it, getString(R.string.limit_reached_toast), Toast.LENGTH_SHORT).show()
                                userSelect = false
                                itemBinding.enabledSwitch.isChecked = false
                            } else if (!b) {
                                topic?.enabled = false
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
            itemBinding.enabledSwitch.setOnCheckedChangeListener(listener)
            itemBinding.enabledSwitch.setOnTouchListener(listener)

            itemBinding.constraintLayout.setOnClickListener {
                hideViews()
                activity?.let { a ->
                    topic?.let { t ->
                        val intent = TopicPickerActivity.newIntent(a, t.id)
                        startActivity(intent)
                    }
                }
            }
        }

        fun bind(topic: YoutubeTopic) {
            this.topic = topic
            itemBinding.topicText.text = this.topic?.topicName
            itemBinding.minimumViews.text = "${this.topic?.minViews} views minimum"
            itemBinding.enabledSwitch.isChecked = (this.topic?.enabled ?: true)
        }
    }

    private inner class TopicAdapter(private var topics: List<Topic>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val itemBinding = ListItemYoutubeTopicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return YoutubeTopicHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val topic = topics?.get(position)
            val youtubeHolder = holder as? YoutubeTopicHolder
            youtubeHolder?.bind(topic as? YoutubeTopic ?: YoutubeTopic("", 0))
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
