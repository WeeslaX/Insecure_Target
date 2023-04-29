package sg.insecure.insecuretarget.database;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

public class EncryptedDatabaseHelper extends SQLiteOpenHelper {
    // declaring name of the database
    static final String DATABASE_NAME = "EncUserDB";

    // declaring table name of the database
    static final String TABLE_NAME = "Users";

    // declaring version of the database
    static final int DATABASE_VERSION = 1;

    // sql query to create the table
    private static final String CREATE_DB_TABLE = " CREATE TABLE " + TABLE_NAME
            + " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + " name TEXT NOT NULL);";
    private static final String TAG = "EncryptedDatabaseHelper";

    public EncryptedDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        System.loadLibrary("sqlcipher");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables and set encryption key here
        Log.d(TAG, "CREATE_DB_TABLE SQL: " + CREATE_DB_TABLE );
        db.execSQL(CREATE_DB_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // sql query to drop a table
        // having similar name
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}
