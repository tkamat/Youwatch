package com.tkamat.android.youwatch

import java.util.*
import kotlin.collections.ArrayList

abstract class Topic(val id: UUID,
                     var topicName: String,
                     val topicType: String,
                     var enabled: Boolean = true,
                     var firstNotificationShown: Boolean = false,
                     var previousNotifications: ArrayList<String> = ArrayList()) {
}