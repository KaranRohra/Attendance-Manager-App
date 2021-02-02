package com.example.attendancemanager

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.lang.Exception

class UpdateActivity: AppCompatActivity() {
    private lateinit var activityName: String
    private lateinit var update: Button
    private lateinit var cancel: Button
    private lateinit var subjectName: String
    private lateinit var subject: EditText
    private lateinit var attended: EditText
    private lateinit var missed: EditText
    private lateinit var criteria: EditText
    private lateinit var attendanceManagerDB : AttendanceManagerDB
    private lateinit var db: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.update_subject_info)
        title=getString(R.string.update_activity_title)

        subjectName=intent.getStringExtra("SubjectName").toString()
        activityName=intent.getStringExtra("Activity").toString()

        attendanceManagerDB= AttendanceManagerDB(applicationContext)
        db= attendanceManagerDB.readableDatabase

        val cursor=db.rawQuery("SELECT * FROM ${AttendanceManagerDB.TABLE_NAME} WHERE ${AttendanceManagerDB.COLUMN_SUBJECT_NAME} LIKE '%$subjectName%' ;",null)

        cursor.moveToNext()
        subject=findViewById(R.id.subject_name_in_update)
        attended=findViewById(R.id.attended_in_update)
        missed=findViewById(R.id.missed_in_update)
        criteria=findViewById(R.id.criteria_in_update)
        update=findViewById(R.id.update_)
        cancel=findViewById(R.id.cancel1)

        attended.setText(cursor.getInt(1).toString())
        missed.setText(cursor.getInt(2).toString())
        criteria.setText(cursor.getInt(3).toString())
        subject.setText(subjectName)

        cursor.close()

        update.setOnClickListener {
            val criteriaInString=criteria.text.toString()
            if(criteriaInString == ""){
                Toast.makeText(this,"Please enter criteria", Toast.LENGTH_LONG).show()
            }
            else if(criteriaInString.toInt()<0 || criteriaInString.toInt()>100){
                Toast.makeText(this,"Criteria should between 0 to 100", Toast.LENGTH_LONG).show()
            }
            else if(subject.text.toString() != "") {
                try {
                    val values = ContentValues()
                    values.put(AttendanceManagerDB.COLUMN_SUBJECT_NAME, subject.text.toString())
                    values.put(AttendanceManagerDB.COLUMN_ATTENDED, attended.text.toString())
                    values.put(AttendanceManagerDB.COLUMN_MISSED, missed.text.toString())
                    values.put(AttendanceManagerDB.COLUMN_CRITERIA, criteria.text.toString())

                    db.update(AttendanceManagerDB.TABLE_NAME, values,"${AttendanceManagerDB.COLUMN_SUBJECT_NAME} LIKE '%${subjectName}%';",null)

                    Toast.makeText(this,"${subject.text} is updated", Toast.LENGTH_LONG).show()

                    startActivityByName()
                    finish()
                }
                catch (e: Exception){
                    Toast.makeText(this,getString(R.string.subject_is_already_present_warning), Toast.LENGTH_LONG).show()
                }
            }
            else{
                //val vibrate=getSystemService(VIBRATOR_SERVICE)
                Toast.makeText(this,getString(R.string.enter_subject_name_error), Toast.LENGTH_LONG).show()
            }
        }
        cancel.setOnClickListener {
            startActivityByName()
            finish()
        }
    }

    override fun onBackPressed() {
        startActivityByName()
        finish()
    }

    private fun startActivityByName(){
        if(activityName==getString(R.string.main))
            startActivity(Intent(this, MainActivity::class.java))
        else
            startActivity(Intent(this, AddSubjectActivity::class.java))
    }
}