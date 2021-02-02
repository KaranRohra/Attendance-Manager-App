package com.example.attendancemanager

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.lang.Exception

class SubjectInfoActivity : AppCompatActivity() {
    private lateinit var add: Button
    private lateinit var cancel: Button
    private lateinit var subject: EditText
    private lateinit var attended: EditText
    private lateinit var missed: EditText
    private lateinit var criteria: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.subject_info)
        title="Attendance Information"

        add=findViewById(R.id.update_)
        cancel=findViewById(R.id.cancel)
        subject=findViewById(R.id.subject_name_in_update)
        attended=findViewById(R.id.attended_in_update)
        missed=findViewById(R.id.missed_in_update)
        criteria=findViewById(R.id.criteria_in_update)

        add.setOnClickListener {
            val criteriaInString=criteria.text.toString()
            if(criteriaInString == ""){
                Toast.makeText(this,"Please enter criteria", Toast.LENGTH_LONG).show()
            }
            else if(criteriaInString.toInt()<0 || criteriaInString.toInt()>100){
                Toast.makeText(this,"Criteria should between 0 to 100", Toast.LENGTH_LONG).show()
            }
            else if(subject.text.toString() != "") {
                try {
                    val attendanceManagerDB = AttendanceManagerDB(applicationContext)
                    val db: SQLiteDatabase = attendanceManagerDB.readableDatabase

                    val values = ContentValues()
                    values.put(AttendanceManagerDB.COLUMN_SUBJECT_NAME, subject.text.toString())
                    values.put(AttendanceManagerDB.COLUMN_ATTENDED, attended.text.toString())
                    values.put(AttendanceManagerDB.COLUMN_MISSED, missed.text.toString())
                    values.put(AttendanceManagerDB.COLUMN_CRITERIA, if(criteria.text.toString()=="") "0" else criteria.text.toString())
                    values.put(AttendanceManagerDB.COLUMN_STATUS,"Status: ")

                    db.insertOrThrow(AttendanceManagerDB.TABLE_NAME, null, values)

                    Toast.makeText(this,"${subject.text} is added",Toast.LENGTH_LONG).show()

                    startActivity(Intent(this, AddSubjectActivity::class.java))
                    finish()
                }
                catch (e: Exception) {
                    Toast.makeText(this,"Subject is already present",Toast.LENGTH_LONG).show()
                }
            }
            else{
                //val vibrate=getSystemService(VIBRATOR_SERVICE)
                Toast.makeText(this,"Please enter subject name",Toast.LENGTH_LONG).show()
            }
        }
        cancel.setOnClickListener {
            startActivity(Intent(this,AddSubjectActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this,AddSubjectActivity::class.java))
        finish()
    }
}