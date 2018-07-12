package database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import database.TopicDatabaseSchema.*

private const val VERSION = 10
private const val DATABASE_NAME = "topicDatabase.db"

class TopicDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, VERSION) {

    private var openConnections = 0

    override fun getReadableDatabase(): SQLiteDatabase {
        openConnections++
        return super.getReadableDatabase()
    }

    override fun getWritableDatabase(): SQLiteDatabase {
        openConnections++
        return super.getWritableDatabase()
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + TopicTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                TopicTable.Cols.UUID + ", " +
                TopicTable.Cols.TOPIC + ", " +
                TopicTable.Cols.VIEWS + ", " +
                TopicTable.Cols.ENABLED + ", " +
                TopicTable.Cols.TOPIC_SEARCHER + ", " +
                TopicTable.Cols.NOTIFIED_VIDEOS + ", " +
                TopicTable.Cols.TOP_VIDEO_NOTIFICATION_SHOWN + ")"
        )
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TopicTable.NAME)
        onCreate(sqLiteDatabase)
    }

    @Synchronized
    override fun close() {
        openConnections--
        if (openConnections == 0) {
            super.close()
        }
    }

}
