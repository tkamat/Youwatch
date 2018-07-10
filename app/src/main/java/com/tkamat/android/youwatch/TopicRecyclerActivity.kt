package com.tkamat.android.youwatch

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity

/**
 * @author Tushaar Kamat
 * @version 9/8/17
 */

class TopicRecyclerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)

        val fm = supportFragmentManager
        var fragment: Fragment? = fm.findFragmentById(R.id.fragment_container)

        fragment ?: run {
            fragment = TopicRecyclerFragment.newInstance()
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit()
        }
    }

}
