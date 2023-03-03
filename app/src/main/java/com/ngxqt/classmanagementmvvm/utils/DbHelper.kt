package com.ngxqt.classmanagementmvvm.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.sql.SQLException

class DbHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, VERSION) {

    var rowExists: Boolean = false

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREAT_CLASS_TABLE)
        db?.execSQL(CREAT_STUDENT_TABLE)
        db?.execSQL(CREAT_STATUS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        try {
            db?.execSQL(DROP_CLASS_TABLE)
            db?.execSQL(DROP_STUDENT_TABLE)
            db?.execSQL(DROP_STATUS_TABLE)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun addClass(className: String, subjectName: String): Long {
        val database: SQLiteDatabase = this.writableDatabase
        val values = ContentValues()
        values.put(CLASS_NAME_KEY, className)
        values.put(SUBJECT_NAME_KEY, subjectName)

        return database.insert(CLASS_TABLE_NAME, null, values)
    }

    fun getClassTabale(): Cursor {
        val database: SQLiteDatabase = this.readableDatabase

        return database.rawQuery(SELECT_CLASS_TABLE, null)
    }

    fun deleteClass(cid: Long): Int {
        val database: SQLiteDatabase = this.readableDatabase
        return database.delete(CLASS_TABLE_NAME, C_ID + "=?", arrayOf(cid.toString()))
    }

    fun updateClass(cid: Long, className: String, subjectName: String): Int {
        val database: SQLiteDatabase = this.writableDatabase
        val values = ContentValues()
        values.put(CLASS_NAME_KEY, className)
        values.put(SUBJECT_NAME_KEY, subjectName)

        return database.update(CLASS_TABLE_NAME, values, C_ID + "=?", arrayOf(cid.toString()))
    }

    fun addStudent(cid: Long, roll: Int, name: String): Long {
        val database: SQLiteDatabase = this.writableDatabase
        val values = ContentValues()
        values.put(C_ID, cid)
        values.put(STUDENT_ROLL_KEY, roll)
        values.put(STUDENT_NAME_KEY, name)

        return database.insert(STUDENT_TABLE_NAME, null, values)
    }

    fun getStudentTabale(cid: Long): Cursor {
        val database: SQLiteDatabase = this.readableDatabase

        return database.query(
            STUDENT_TABLE_NAME, null, C_ID + "=?",
            arrayOf(cid.toString()), null, null, STUDENT_ROLL_KEY
        )
    }

    fun deleteStudent(sid: Long): Int {
        val database: SQLiteDatabase = this.readableDatabase
        return database.delete(STUDENT_TABLE_NAME, S_ID + "=?", arrayOf(sid.toString()))
    }

    fun updateStudent(sid: Long,roll:Int, name: String): Int {
        val database: SQLiteDatabase = this.writableDatabase
        val values = ContentValues()
        values.put(STUDENT_NAME_KEY, name)
        values.put(STUDENT_ROLL_KEY, roll)

        return database.update(STUDENT_TABLE_NAME, values, S_ID + "=?", arrayOf(sid.toString()))
    }

    fun addStatus(sid: Long, cid: Long, date: String, status: String?): Long {
        val database: SQLiteDatabase = this.writableDatabase
        val values = ContentValues()
        values.put(S_ID, sid)
        values.put(C_ID, cid)
        values.put(DATE_KEY, date)
        values.put(STATUS_KEY, status)
        return database.insert(STATUS_TABLE_NAME, null, values)
    }

    fun updateStatus(sid: Long, date: String, status: String?): Int {
        val database: SQLiteDatabase = this.writableDatabase
        val values = ContentValues()
        values.put(STATUS_KEY, status)
        val whereClause = DATE_KEY + "='" + date + "' AND " + S_ID + "=" + sid
        return database.update(STATUS_TABLE_NAME, values, whereClause, null)
    }

    fun getStatus(sid: Long, date: String): String? {
        var status: String? = null
        val database: SQLiteDatabase = this.readableDatabase
        val whereClause = DATE_KEY + "='" + date + "' AND " + S_ID + "=" + sid
        val cursor = database.query(STATUS_TABLE_NAME, null, whereClause, null, null, null, null)
        if (cursor.moveToFirst()) {
            status = cursor.getString(cursor.getColumnIndex(STATUS_KEY))
        }

        return status
    }

    fun getDistincMonths(cid: Long): Cursor {
        val database: SQLiteDatabase = this.readableDatabase
        return database.query(
            STATUS_TABLE_NAME,
            arrayOf(DATE_KEY),
            C_ID + "=" + cid,
            null,
            "substr(" + DATE_KEY + ",4,7)",
            null,
            null
        )
    }

    companion object {
        private val DATABASE_NAME = "Attendance.db"
        private val VERSION = 1

        /**CLASS TABLE*/
        private val CLASS_TABLE_NAME = "CLASS_TABLE"
        val C_ID = "_CID"
        val CLASS_NAME_KEY = "CLASS_NAME"
        val SUBJECT_NAME_KEY = "SUBJECT_TABLE"

        private val CREAT_CLASS_TABLE = "CREATE TABLE " + CLASS_TABLE_NAME + " (" +
                C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                CLASS_NAME_KEY + " TEXT NOT NULL," +
                SUBJECT_NAME_KEY + " TEXT NOT NULL," +
                "UNIQUE (" + CLASS_NAME_KEY + ", " + SUBJECT_NAME_KEY + ")" +
                ")"

        private val DROP_CLASS_TABLE = "DROP TABLE IF EXISTS " + CLASS_TABLE_NAME
        private val SELECT_CLASS_TABLE = "SELECT * FROM " + CLASS_TABLE_NAME

        /**STUDENT TABLE*/
        private val STUDENT_TABLE_NAME = "STUDENT_TABLE"
        val S_ID = "_SID"
        val STUDENT_NAME_KEY = "STUDENT_NAME"
        val STUDENT_ROLL_KEY = "ROLL"

        private val CREAT_STUDENT_TABLE = "CREATE TABLE " + STUDENT_TABLE_NAME + "( " +
                S_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                C_ID + " INTEGER NOT NULL, " +
                STUDENT_NAME_KEY + " TEXT NOT NULL, " +
                STUDENT_ROLL_KEY + " INTEGER," +
                " FOREIGN KEY ( " + C_ID + ") REFERENCES " + CLASS_TABLE_NAME + "(" + C_ID + ")" +
                ");"

        private val DROP_STUDENT_TABLE = "DROP TABLE IF EXISTS " + STUDENT_TABLE_NAME
        private val SELECT_STUDENT_TABLE = "SELECT * FROM " + STUDENT_TABLE_NAME

        /**STATUS TABLEe*/
        private val STATUS_TABLE_NAME = "STATUS_TABLE"
        val STATUS_ID = "_STATUS_ID"
        val DATE_KEY = "STATUS_DATE"
        val STATUS_KEY = "STATUS"

        private val CREAT_STATUS_TABLE = "CREATE TABLE " + STATUS_TABLE_NAME + "( " +
                STATUS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                S_ID + " INTEGER NOT NULL, " +
                C_ID + " INTEGER NOT NULL, " +
                DATE_KEY + " DATE NOT NULL, " +
                STATUS_KEY + " TEXT NOT NULL, " +
                " UNIQUE (" + S_ID + "," + DATE_KEY + ")," +
                " FOREIGN KEY ( " + S_ID + ") REFERENCES " + STUDENT_TABLE_NAME + "( " + S_ID + ")" +
                " FOREIGN KEY ( " + C_ID + ") REFERENCES " + STUDENT_TABLE_NAME + "( " + C_ID + ")" +
                ");"

        private val DROP_STATUS_TABLE = "DROP TABLE IF EXISTS " + STATUS_TABLE_NAME
        private val SELECT_STATUS_TABLE = "SELECT * FROM " + STATUS_TABLE_NAME

    }
}