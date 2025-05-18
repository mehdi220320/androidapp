package com.example.mobileproj;
import android.database.Cursor;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class dbconnect extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "users.db";


    public dbconnect(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {



        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY, username TEXT, password TEXT, role TEXT)");
        db.execSQL("INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'admin')");
// tabel infraction :
        db.execSQL("CREATE TABLE infractions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "photo BLOB," +          // Stores image data
                "numserie TEXT," +       // Serial number
                "date TEXT," +           // Human-readable date
                "location TEXT)");       // Location string
        // admin add
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Only drop users table if needed
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS infractions");
        }
        onCreate(db);
    }
    public void addUser(String username, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        values.put("role", role);
        db.insert("users", null, values);
    }
//    public boolean insertUser(String username, String password, String role) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(COL_USERNAME, username);
//        contentValues.put(COL_PASSWORD, password);
//        contentValues.put(COL_ROLE, role);
//        long result = db.insert(TABLE_NAME, null, contentValues);
//        return result != -1;
//    }

    public Cursor login(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM users WHERE username=? AND password=?", new String[]{username, password});
    }
}