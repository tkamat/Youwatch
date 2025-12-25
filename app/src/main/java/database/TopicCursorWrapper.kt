package database

import android.database.Cursor
import android.database.CursorWrapper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tkamat.android.youwatch.Topic
import com.tkamat.android.youwatch.YoutubeTopic
import database.TopicDatabaseSchema.TopicTable
import java.util.*
import kotlin.collections.ArrayList

class TopicCursorWrapper(cursor: Cursor) : CursorWrapper(cursor) {

    val topic: Topic?
        get() {
            val topicTypeString = getString(getColumnIndex(TopicTable.Cols.TOPIC_TYPE))
            val uuidString = getString(getColumnIndex(TopicTable.Cols.UUID))
            val titleString = getString(getColumnIndex(TopicTable.Cols.TOPIC))
            val viewsInt = getInt(getColumnIndex(TopicTable.Cols.VIEWS))
            val enabledInt = getInt(getColumnIndex(TopicTable.Cols.ENABLED))
            val previousNotificationInt = getInt(getColumnIndex(TopicTable.Cols.FIRST_NOTIFICATION_SHOWN))
            val topicIDsString = getString(getColumnIndex(TopicTable.Cols.TOPIC_IDS))
            val notifiedVideosString = getString(getColumnIndex(TopicTable.Cols.PREVIOUS_NOTIFICATIONS))

            val type = object : TypeToken<List<String>>() {}.type
            val gson = Gson()
            val topicIDs = gson.fromJson<List<String>>(topicIDsString, type)
            val previousNotificationsList = gson.fromJson<List<String>>(notifiedVideosString, type)

            return when (topicTypeString) {
                "youtube" -> {
                    val topic = YoutubeTopic(titleString, viewsInt, UUID.fromString(uuidString))
                    topic.apply {
                        enabled = enabledInt != 0
                        youtubeTopicSearcher?.videoIds = topicIDs as ArrayList<String>
                        previousNotifications = previousNotificationsList as ArrayList<String>
                        firstNotificationShown = previousNotificationInt != 0
                    }
                }
                else -> {
                    // Skip unsupported topic types (like old twitter topics)
                    null
                }
            }
        }
}
