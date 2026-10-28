package com.example.midexam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "student.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE = "students";
    public static final String COL_ID = "student_id";
    public static final String COL_NAME = "name";
    public static final String COL_ENROLLED = "enrolled";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + COL_ID + " TEXT PRIMARY KEY, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_ENROLLED + " INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    // ADD
    public boolean addStudent(String id, String name, boolean enrolled) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, id);
        cv.put(COL_NAME, name);
        cv.put(COL_ENROLLED, enrolled ? 1 : 0);
        long result = db.insert(TABLE, null, cv);
        return result != -1; // -1 = gagal (misalnya ID sudah ada)
    }

    // EDIT
    public boolean updateStudent(String id, String name, boolean enrolled) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, name);
        cv.put(COL_ENROLLED, enrolled ? 1 : 0);
        int rows = db.update(TABLE, cv, COL_ID + " = ?", new String[]{id});
        return rows > 0;
    }

    // DELETE
    public boolean deleteStudent(String id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE, COL_ID + " = ?", new String[]{id});
        return rows > 0;
    }

    // VIEW
    public Cursor getAllStudents() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE + " ORDER BY " + COL_NAME, null);
    }
}