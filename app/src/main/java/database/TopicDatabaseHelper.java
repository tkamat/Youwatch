package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static database.TopicDatabaseSchema.*;

public class TopicDatabaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 10;
    private static final String DATABASE_NAME = "topicDatabase.db";

    public TopicDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + TopicTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                TopicTable.Cols.UUID + ", " +
                TopicTable.Cols.TOPIC + ", " +
                TopicTable.Cols.VIEWS + ", " +
                TopicTable.Cols.ENABLED + ", " +
                TopicTable.Cols.TOPIC_SEARCHER +  ", " +
                TopicTable.Cols.NOTIFIED_VIDEOS + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TopicTable.NAME);
        onCreate(sqLiteDatabase);
    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
}
