package com.example.attendancemanager

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AttendanceManagerDB(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME,null, DATABASE_VERSION) {

    companion object{
        const val DATABASE_NAME="attendance_info.db"
        const val TABLE_NAME="attendance_record"
        const val COLUMN_SUBJECT_NAME="subject_name"
        const val COLUMN_ATTENDED="total_attended"
        const val COLUMN_MISSED="total_missed"
        const val COLUMN_CRITERIA="min_criteria"
        const val COLUMN_STATUS="status"
        const val DATABASE_VERSION=2
    }
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_NAME ($COLUMN_SUBJECT_NAME TEXT PRIMARY KEY COLLATE NOCASE, $COLUMN_ATTENDED INTEGER, $COLUMN_MISSED INTEGER, $COLUMN_CRITERIA INTEGER, $COLUMN_STATUS TEXT);" )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_STATUS")
    }
}