package com.tkamat.android.youwatch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * @author Tushaar Kamat
 * @version 9/26/17
 */

class StartTopicServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Util.scheduleJob(context)
    }
}
