package com.example.attendancemanager

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AddSubjectActivity : AppCompatActivity() {
    lateinit var subjectsView: LinearLayout
    lateinit var allSubjectViews: ArrayList<View>
    lateinit var addSubject: Button
    lateinit var attendanceManagerDB : AttendanceManagerDB
    lateinit var db: SQLiteDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.all_subject_list)
        title = "Subjects"

        subjectsView=findViewById(R.id.subject_list_view)
        allSubjectViews= ArrayList()
        attendanceManagerDB = AttendanceManagerDB(applicationContext)
        db=attendanceManagerDB.readableDatabase

        displayAllSubjects()

        addSubject=findViewById(R.id.add_in_list)
        addSubject.setOnClickListener {
            startActivity(Intent(this,SubjectInfoActivity::class.java))
            finish()
        }
    }

    @SuppressLint("Recycle")
    fun displayAllSubjects(){
        val cursor=db.rawQuery("SELECT * FROM ${AttendanceManagerDB.TABLE_NAME};",null)
        cursor.moveToFirst()
        while (!cursor.isAfterLast){
            val view=layoutInflater.inflate(R.layout.single_subject_for_suject_layout,null)

            val delete=view.findViewById<ImageButton>(R.id.delete_list)
            val reset=view.findViewById<ImageButton>(R.id.reset_list)
            val update=view.findViewById<ImageButton>(R.id.edit_info_list)
            
            val subjectName=view.findViewById<TextView>(R.id.subject_name_list)
            val attended=view.findViewById<TextView>(R.id.attended_list)
            val total=view.findViewById<TextView>(R.id.total_classes_list)

            delete.setImageResource(R.drawable.ic_menu_delete)
            reset.setImageResource(R.drawable.ic_menu_rotate)
            update.setImageResource(R.drawable.ic_menu_edit)

            subjectName.text = cursor.getString(0)
            attended.text=" Attended: ${cursor.getInt(1)}  "
            total.text="Total Classes: ${cursor.getInt(1)+cursor.getInt(2)}"

            allSubjectViews.add(view)

            for(i in 0 until allSubjectViews.size){
                val d=allSubjectViews[i].findViewById<ImageButton>(R.id.delete_list)
                val s=allSubjectViews[i].findViewById<TextView>(R.id.subject_name_list)
                val u=allSubjectViews[i].findViewById<ImageButton>(R.id.edit_info_list)
                val r=allSubjectViews[i].findViewById<ImageButton>(R.id.reset_list)

                d.setOnClickListener {
                    val builder= AlertDialog.Builder(this)
                    builder.setMessage("Sure you want to delete attendance record of ${s.text} ?").setCancelable(false)
                            .setPositiveButton("Yes", DialogInterface.OnClickListener(){ _: DialogInterface, _: Int ->
                                val row=db.delete(AttendanceManagerDB.TABLE_NAME, "${AttendanceManagerDB.COLUMN_SUBJECT_NAME} LIKE '%${s.text}%';",null)
                                if(row>0){
                                    Toast.makeText(applicationContext,"Deleted Successfully",Toast.LENGTH_LONG).show()
                                    subjectsView.removeView(allSubjectViews[i])
                                }
                                else{
                                    Toast.makeText(applicationContext,"Failed",Toast.LENGTH_LONG).show()
                                }
                            })
                            .setNegativeButton("No") { _: DialogInterface, _: Int ->
                            }
                    val alert=builder.create()
                    alert.setTitle("Delete")
                    alert.show()
                }

                u.setOnClickListener {
                    val intent=Intent(this,UpdateActivity::class.java)
                    intent.putExtra("SubjectName",s.text)
                    intent.putExtra("Activity","AddSubject")
                    startActivity(intent)
                    finish()
                }

                r.setOnClickListener {
                    val builder=AlertDialog.Builder(this)
                    builder.setMessage("Sure you want to reset attendance record of ${s.text} ?").setCancelable(false).
                    setPositiveButton("Yes"){ _: DialogInterface, _: Int ->
                        val t1=allSubjectViews[i].findViewById<TextView>(R.id.total_classes_list)
                        val t2=allSubjectViews[i].findViewById<TextView>(R.id.attended_list)
                        t1.text="Attended: 0  "
                        t2.text="Total Classes: 0"

                        val values= ContentValues()
                        values.put(AttendanceManagerDB.COLUMN_ATTENDED,0)
                        values.put(AttendanceManagerDB.COLUMN_MISSED,0)

                        db.update(AttendanceManagerDB.TABLE_NAME,values,"${AttendanceManagerDB.COLUMN_SUBJECT_NAME} LIKE '%${s.text}%'",null)
                        Toast.makeText(this,"Reset Success",Toast.LENGTH_LONG).show()
                    }.setNegativeButton("No"){ _: DialogInterface, _: Int -> }
                    val alert=builder.create()
                    alert.setTitle("Reset")
                    alert.show()
                }
            }

            subjectsView.addView(view)
            cursor.moveToNext()
        }
        cursor.close()
    }

    override fun onBackPressed() {
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }
}