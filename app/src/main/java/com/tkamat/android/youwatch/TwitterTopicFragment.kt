package com.tkamat.android.youwatch


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            topicID = it.getString(ARG_TOPIC_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_twitter_topic, container, false)
    }


    companion object {
        private const val ARG_TOPIC_ID = "arg_topic_id"

        fun newInstance(topicID: String) =
                TwitterTopicFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_TOPIC_ID, topicID)
                    }
                }
    }
}
