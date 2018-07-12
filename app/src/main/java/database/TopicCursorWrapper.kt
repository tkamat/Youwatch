package database

import android.database.Cursor
import android.database.CursorWrapper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tkamat.android.youwatch.Topic
import database.TopicDatabaseSchema.TopicTable
import java.util.*

class TopicCursorWrapper(cursor: Cursor) : CursorWrapper(cursor) {

    val topic: Topic
        get() {
            val uuidString = getString(getColumnIndex(TopicTable.Cols.UUID))
            val title = getString(getColumnIndex(TopicTable.Cols.TOPIC))
            val views = getInt(getColumnIndex(TopicTable.Cols.VIEWS))
            val enabled = getInt(getColumnIndex(TopicTable.Cols.ENABLED))
            val topVideo = getInt(getColumnIndex(TopicTable.Cols.TOP_VIDEO_NOTIFICATION_SHOWN))
            val videoIDsString = getString(getColumnIndex(TopicTable.Cols.TOPIC_SEARCHER))
            val notifiedVideosString = getString(getColumnIndex(TopicTable.Cols.NOTIFIED_VIDEOS))

            val type = object : TypeToken<List<String>>() {

            }.type
            val gson = Gson()
            val videoIDs = gson.fromJson<List<String>>(videoIDsString, type)
            val notifiedVideos = gson.fromJson<List<String>>(notifiedVideosString, type)

            val topic = Topic(title, views, UUID.fromString(uuidString))
            topic.enabled = enabled != 0
            topic.topicSearcher?.videoIDs = videoIDs as ArrayList<String>
            topic.notifiedVideos = notifiedVideos as ArrayList<String>
            topic.topVideoNotificationShown = topVideo != 0

            return topic
        }
}
