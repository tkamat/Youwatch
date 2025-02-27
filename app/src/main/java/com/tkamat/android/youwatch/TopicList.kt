package com.tkamat.android.youwatch

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.google.gson.Gson
import database.TopicCursorWrapper
import database.TopicDatabaseHelper
import database.TopicDatabaseSchema.TopicTable
import java.util.*

class TopicList private constructor(context: Context) {
    private val context: Context = context.applicationContext
    private var database: SQLiteDatabase? = null

    val topics: List<Topic>
        get() {
            val topics = ArrayList<Topic>()
            val cursor = queryTopics(
                    null, null)
            cursor?.use {
                it.moveToFirst()
                while (!it.isAfterLast) {
                    topics.add(it.topic)
                    it.moveToNext()
                }
            }
            return topics
        }

    val enabledTopics: List<Topic>
        get() {
            val topics = topics
            val enabledTopics = ArrayList<Topic>()
            for (t in topics) {
                if (t.enabled)
                    enabledTopics.add(t)
            }
            return enabledTopics
        }

    init {
        database = TopicDatabaseHelper(context).writableDatabase
    }

    fun getTopic(id: UUID): Topic? {
        val cursor = queryTopics(TopicTable.Cols.UUID + " = ?", arrayOf(id.toString()))

        try {
            if (cursor?.count == 0)
                return null
            cursor?.moveToFirst()
            return cursor?.topic
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return null
    }

    fun addTopic(topic: Topic) {
        val values = getContentValues(topic)
        database?.insert(TopicTable.NAME, null, values)
    }

    fun updateTopic(topic: Topic) {
        val uuidString = topic.id.toString()
        val values = getContentValues(topic)

        database?.update(TopicTable.NAME, values, TopicTable.Cols.UUID + " = ?", arrayOf(uuidString))
    }

    fun deleteTopic(id: UUID) {
        database?.delete(TopicTable.NAME, TopicTable.Cols.UUID + " = ?", arrayOf(id.toString()))
    }

    private fun queryTopics(whereClause: String?, whereArgs: Array<String>?): TopicCursorWrapper? {
        try {
            val cursor = database?.query(TopicTable.NAME, null,
                    whereClause,
                    whereArgs, null, null, null)
            cursor?.let {
                return TopicCursorWrapper(it)
            } ?: return null
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            return null
        }
    }

    companion object {
        @Volatile private var topicList: TopicList? = null

        fun getInstance(context: Context): TopicList? {
            if (topicList == null) {
                synchronized(TopicList::class.java) {
                    if (topicList == null) {
                        topicList = TopicList(context)
                    }
                }
            } else if (topicList?.database?.isOpen != true) {
                topicList?.let {
                    it.database = TopicDatabaseHelper(it.context).writableDatabase
                }
            }
            return topicList
        }

        private fun getContentValues(topic: Topic): ContentValues {
            val values = ContentValues()
            values.put(TopicTable.Cols.TOPIC_TYPE, topic.topicType)
            values.put(TopicTable.Cols.TOPIC, topic.topicName)
            values.put(TopicTable.Cols.UUID, topic.id.toString())
            values.put(TopicTable.Cols.ENABLED, if (topic.enabled) 1 else 0)
            values.put(TopicTable.Cols.FIRST_NOTIFICATION_SHOWN, if (topic.firstNotificationShown) 1 else 0)

            val gson = Gson()
            val previousNotifications = topic.previousNotifications
            val inputString2 = gson.toJson(previousNotifications)
            values.put(TopicTable.Cols.PREVIOUS_NOTIFICATIONS, inputString2)
            when (topic) {
                is YoutubeTopic -> {
                    values.put(TopicTable.Cols.VIEWS, topic.minViews)
                    val videoIDs = topic.youtubeTopicSearcher?.videoIds
                    val inputString1 = gson.toJson(videoIDs)
                    values.put(TopicTable.Cols.TOPIC_IDS, inputString1)
                }
                is TwitterTopic -> {
                    values.put(TopicTable.Cols.RETWEETS, topic.minRetweets)
                    values.put(TopicTable.Cols.TWEET_LIKES, topic.minLikes)
                    val tweetIds = topic.twitterTopicSearcher?.tweetIds
                    val inputString1 = gson.toJson(tweetIds)
                    values.put(TopicTable.Cols.TOPIC_IDS, inputString1)
                }
            }
            return values
        }
    }

}
