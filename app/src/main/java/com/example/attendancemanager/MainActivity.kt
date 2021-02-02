package com.example.attendancemanager

import android.annotation.SuppressLint
import android.content.*
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var subjectsView: LinearLayout
    private lateinit var allSubjectViews: ArrayList<View>
    private lateinit var attendanceManagerDB : AttendanceManagerDB
    private lateinit var db: SQLiteDatabase
    private lateinit var addSubject : Button
    private lateinit var overallAttendance: TextView
    private lateinit var overallPercentage: TextView
    private lateinit var goal: TextView
    private var goalPercentage=0
    private var totalAttended=0
    private var totalClasses=0
    private var actualPercentage=0
    private var countSubject=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        subjectsView=findViewById(R.id.subject_list_in_main)
        allSubjectViews= ArrayList()
        attendanceManagerDB= AttendanceManagerDB(applicationContext)
        db=attendanceManagerDB.readableDatabase
        addSubject=findViewById(R.id.add_in_main)
        overallAttendance=findViewById(R.id.overall_attendance)
        overallPercentage=findViewById(R.id.overall_percantage)
        goal=findViewById(R.id.goal)

        displayAllSubjects()

        addSubject.setOnClickListener {
            startActivity(Intent(this,AddSubjectActivity::class.java))
            finish()
        }
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    private fun displayAllSubjects(){

        val cursor=db.rawQuery("SELECT * FROM ${AttendanceManagerDB.TABLE_NAME};",null)
        countSubject=cursor.count
        cursor.moveToFirst()
        while (!cursor.isAfterLast){
            val view=layoutInflater.inflate(R.layout.single_subject_for_main_layout,null)

            val pop=view.findViewById<ImageButton>(R.id.popup_menu)
            pop.setImageResource(R.drawable.menu_white_dots)

            val attended=view.findViewById<ImageButton>(R.id.click_attend)
            val missed=view.findViewById<ImageButton>(R.id.click_miss)

            val subjectName=view.findViewById<TextView>(R.id.subject_name_in_main)
            val percent=view.findViewById<TextView>(R.id.percentage)
            val total=view.findViewById<TextView>(R.id.score)
            val status=view.findViewById<TextView>(R.id.status)

            attended.setImageResource(R.drawable.green_tick_in_circle)
            missed.setImageResource(R.drawable.red_cross_in_circle)

            val tp=cursor.getInt(1)+cursor.getInt(2)
            val p: Int=if(tp!=0)(cursor.getInt(1)*100.0/tp).toInt() else 0

            status.text=cursor.getString(4)
            subjectName.text = cursor.getString(0)
            total.text="${cursor.getInt(1)}/$tp"

            totalClasses+=tp
            totalAttended+=cursor.getInt(1)
            actualPercentage+=p
            percent.text= "$p%"
            if(p<cursor.getInt(3)){
                percent.setTextColor(Color.RED)
            }
            else{
                percent.setTextColor(Color.GREEN)
            }

            allSubjectViews.add(view)

            for(i in 0 until allSubjectViews.size){
                val a=allSubjectViews[i].findViewById<ImageButton>(R.id.click_attend)
                val m=allSubjectViews[i].findViewById<ImageButton>(R.id.click_miss)
                val s=allSubjectViews[i].findViewById<TextView>(R.id.subject_name_in_main)
                val per=allSubjectViews[i].findViewById<TextView>(R.id.percentage)
                val t=allSubjectViews[i].findViewById<TextView>(R.id.score)
                val statusAfterClick=allSubjectViews[i].findViewById<TextView>(R.id.status)

                a.setOnClickListener {
                    val values=ContentValues()
                    val c=db.rawQuery("SELECT * FROM ${AttendanceManagerDB.TABLE_NAME} WHERE ${AttendanceManagerDB.COLUMN_SUBJECT_NAME} LIKE '%${s.text}%'",null)
                    c.moveToNext()

                    val tpe=c.getInt(1)+c.getInt(2)+1
                    t.text="${c.getInt(1)+1}/$tpe"
                    val tempPercent: Int=(c.getInt(1)+1)*100/tpe

                    actualPercentage-=per.text.toString().substring(0,per.text.toString().length-1).toInt()
                    actualPercentage+=tempPercent

                    per.text= "${tempPercent}%"
                    if(tempPercent<c.getInt(3)){
                        per.setTextColor(Color.RED)
                        when (val youHave=youHaveToAttend(c.getInt(1)+1,tpe,c.getInt(3))) {
                            0 -> statusAfterClick.text="Status: On track, You can't miss any class"
                            1 -> statusAfterClick.text="Status: Attend next class to be on track"
                            else -> statusAfterClick.text="Status: Attend next $youHave classes to be on track"
                        }
                    }
                    else{
                        per.setTextColor(Color.GREEN)
                        when (val youCan=youCanMiss(c.getInt(1)+1,tpe,c.getInt(3))) {
                            0 -> statusAfterClick.text="Status: On track, You can't miss any class"
                            1 -> statusAfterClick.text="Status: On track, You can miss next class"
                            else -> statusAfterClick.text="Status: On track, You can miss $youCan classes"
                        }
                    }

                    totalAttended++
                    totalClasses++
                    setGoalText()

                    values.put(AttendanceManagerDB.COLUMN_ATTENDED,c.getInt(1)+1)
                    values.put(AttendanceManagerDB.COLUMN_STATUS,statusAfterClick.text.toString())

                    db.update(AttendanceManagerDB.TABLE_NAME,values,"${AttendanceManagerDB.COLUMN_SUBJECT_NAME} LIKE '%${s.text}%'",null)
                    c.close()
                }
                m.setOnClickListener {
                    val values=ContentValues()
                    val c=db.rawQuery("SELECT * FROM ${AttendanceManagerDB.TABLE_NAME} WHERE ${AttendanceManagerDB.COLUMN_SUBJECT_NAME} LIKE '%${s.text}%'",null)
                    c.moveToNext()

                    val tpe=c.getInt(1)+c.getInt(2)+1
                    t.text="${c.getInt(1)}/$tpe"
                    val tempPercent: Int=(c.getInt(1))*100/tpe

                    actualPercentage-=per.text.toString().substring(0,per.text.toString().length-1).toInt()
                    actualPercentage+=tempPercent

                    per.text= "${tempPercent}%"
                    if(tempPercent<c.getInt(3)){
                        per.setTextColor(Color.RED)
                        when (val youHave=youHaveToAttend(c.getInt(1),tpe,c.getInt(3))) {
                            0 -> statusAfterClick.text="Status: On track, You can't miss any class"
                            1 -> statusAfterClick.text="Status: Attend next class to be on track"
                            else -> statusAfterClick.text="Status: Attend next $youHave classes to be on track"
                        }
                    }
                    else{
                        per.setTextColor(Color.GREEN)
                        when (val youCan=youCanMiss(c.getInt(1),tpe,c.getInt(3))) {
                            0 -> statusAfterClick.text="Status: On track, You can't miss any class"
                            1 -> statusAfterClick.text="Status: On track, You can miss next class"
                            else -> statusAfterClick.text="Status: On track, You can miss $youCan classes"
                        }
                    }

                    totalClasses++
                    setGoalText()

                    values.put(AttendanceManagerDB.COLUMN_MISSED,c.getInt(2)+1)
                    values.put(AttendanceManagerDB.COLUMN_STATUS,statusAfterClick.text.toString())

                    db.update(AttendanceManagerDB.TABLE_NAME,values,"${AttendanceManagerDB.COLUMN_SUBJECT_NAME} LIKE '%${s.text}%'",null)
                    c.close()
                }
            }

            goalPercentage+=cursor.getInt(3)
            subjectsView.addView(view)
            cursor.moveToNext()
        }
        goalPercentage = if(countSubject!=0)goalPercentage/countSubject else 0
        goal.text="Goal: $goalPercentage %"
        setGoalText()
        cursor.close()
    }

    @SuppressLint("SetTextI18n")
    fun setGoalText(){
        overallAttendance.text="Overall Attendance: $totalAttended/$totalClasses"
        val tempActualPercentage = if(countSubject!=0) actualPercentage/countSubject else 0
        overallPercentage.text="$tempActualPercentage%"
        if(tempActualPercentage<goalPercentage)
            overallPercentage.setTextColor(Color.RED)
        else
            overallPercentage.setTextColor(Color.GREEN)
    }

    private fun youCanMiss(attended: Int,t: Int,percentage: Int): Int{
        var count=0
        var tempPercent: Int
        var total=t
        do{
            total++
            tempPercent=attended*100/total
            count++
        }while (percentage<=tempPercent)
        return count-1
    }

    private fun youHaveToAttend(a: Int,t: Int,percentage: Int):Int{
        var count=0
        var tempPercent: Int
        var total=t
        var attended=a

        do{
            total++
            attended++
            tempPercent=attended*100/total
            count++
        }while (percentage>tempPercent)
        return count
    }

    fun textPopUp(view: View){
        var i=0
        while(i<allSubjectViews.size){
            if(view==allSubjectViews[i].findViewById(R.id.popup_menu))
                break
            i++
        }
        val s=allSubjectViews[i].findViewById<TextView>(R.id.subject_name_in_main)
        val popUp=PopupMenu(this,view)
        popUp.menuInflater.inflate(R.menu.pop_up_menu,popUp.menu)
        popUp.setOnMenuItemClickListener {
            val cursor=db.rawQuery("SELECT * FROM ${AttendanceManagerDB.TABLE_NAME} WHERE ${AttendanceManagerDB.COLUMN_SUBJECT_NAME} LIKE '%${s.text}%' ;",null)
            when (it.itemId) {
               R.id.delete -> {
                   val builder=AlertDialog.Builder(this)
                   builder.setMessage("Sure you want to delete attendance record of ${s.text} ?").setCancelable(false)
                           .setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                               cursor.moveToNext()
                               val t2=allSubjectViews[i].findViewById<TextView>(R.id.percentage)
                               val row = db.delete(AttendanceManagerDB.TABLE_NAME, "${AttendanceManagerDB.COLUMN_SUBJECT_NAME} LIKE '%${s.text}%';", null)
                               if (row > 0) {
                                   Toast.makeText(applicationContext, "Deleted Successfully", Toast.LENGTH_LONG).show()
                                   subjectsView.removeView(allSubjectViews[i])
                               } else {
                                   Toast.makeText(applicationContext, "Failed", Toast.LENGTH_LONG).show()
                               }
                               totalAttended-=cursor.getInt(1)
                               totalClasses-=cursor.getInt(1)+cursor.getInt(2)
                               actualPercentage-=t2.text.toString().substring(0, t2.text.length-1).toInt()

                               setGoalText()
                               cursor.close()
                           }
                           .setNegativeButton("No") { _: DialogInterface, _: Int ->
                           }
                   val alert=builder.create()
                   alert.setTitle("Delete")
                   alert.show()
               }
               R.id.reset -> {
                   val builder=AlertDialog.Builder(this)
                   builder.setMessage("Sure you want to reset attendance record of ${s.text} ?").setCancelable(false).setPositiveButton(getString(R.string.yes)){ _: DialogInterface, _: Int ->
                       cursor.moveToNext()
                       val t1=allSubjectViews[i].findViewById<TextView>(R.id.score)
                       val t2=allSubjectViews[i].findViewById<TextView>(R.id.percentage)
                       val status=allSubjectViews[i].findViewById<TextView>(R.id.status)
                       status.text=getString(R.string.status)

                       totalAttended-=cursor.getInt(1)
                       totalClasses-=cursor.getInt(1)+cursor.getInt(2)
                       actualPercentage-=t2.text.toString().substring(0, t2.text.length-1).toInt()

                       t1.text="0/0"
                       t2.text="0%"
                       t2.setTextColor(Color.RED)

                       setGoalText()

                       val values=ContentValues()
                       values.put(AttendanceManagerDB.COLUMN_ATTENDED,0)
                       values.put(AttendanceManagerDB.COLUMN_MISSED,0)
                       values.put(AttendanceManagerDB.COLUMN_STATUS,getString(R.string.status))

                       db.update(AttendanceManagerDB.TABLE_NAME,values,"${AttendanceManagerDB.COLUMN_SUBJECT_NAME} LIKE '%${s.text}%'",null)
                       Toast.makeText(this,getString(R.string.reset_success_msg),Toast.LENGTH_LONG).show()
                       cursor.close()
                   }.setNegativeButton(getString(R.string.no)){ _: DialogInterface, _: Int -> }
                   val alert=builder.create()
                   alert.setTitle(getString(R.string.reset_title))
                   alert.show()
               }
               R.id.update -> {
                   val intent=Intent(this,UpdateActivity::class.java)
                   intent.putExtra("SubjectName",s.text)
                   intent.putExtra("Activity","Main")
                   startActivity(intent)
                   finish()
               }
           }
            true
        }
        popUp.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val builder=AlertDialog.Builder(this)
        if(item.itemId==R.id.delete_all){
            builder.setMessage("Sure you want delete all attendance record ?").setCancelable(false).
                    setPositiveButton("Yes"){ _: DialogInterface, _: Int ->
                        subjectsView.removeAllViews()
                        db.execSQL("DROP TABLE IF EXISTS ${AttendanceManagerDB.TABLE_NAME}")
                        attendanceManagerDB.onCreate(db)

                        totalClasses=0
                        totalAttended=0
                        countSubject=0
                        actualPercentage=0
                        goalPercentage=0
                        goal.text=getString(R.string.goal_reset)
                        setGoalText()

                        Toast.makeText(this,"All subject deleted",Toast.LENGTH_LONG).show()
                    }.setNegativeButton("NO"){_: DialogInterface,_: Int->}
        }
        else{
            builder.setMessage("Sure you want reset all attendance record ?").setCancelable(false).
            setPositiveButton("Yes"){ _: DialogInterface, _: Int ->
                for(i in 0 until allSubjectViews.size){
                    val s=allSubjectViews[i].findViewById<TextView>(R.id.subject_name_in_main)
                    val t1=allSubjectViews[i].findViewById<TextView>(R.id.score)
                    val t2=allSubjectViews[i].findViewById<TextView>(R.id.percentage)
                    val status=allSubjectViews[i].findViewById<TextView>(R.id.status)
                    status.text=getString(R.string.status)

                    t1.text="0/0"
                    t2.text="0%"
                    t2.setTextColor(Color.RED)

                    val values=ContentValues()
                    values.put(AttendanceManagerDB.COLUMN_ATTENDED,0)
                    values.put(AttendanceManagerDB.COLUMN_MISSED,0)
                    values.put(AttendanceManagerDB.COLUMN_STATUS,getString(R.string.status))

                    db.update(AttendanceManagerDB.TABLE_NAME,values,"${AttendanceManagerDB.COLUMN_SUBJECT_NAME} LIKE '%${s.text}%'",null)
                }

                totalClasses=0
                totalAttended=0
                countSubject=0
                actualPercentage=0
                setGoalText()

                Toast.makeText(this,"Reset success",Toast.LENGTH_LONG).show()
            }.setNegativeButton("NO"){_: DialogInterface,_: Int->}
        }
        val alert=builder.create()
        alert.show()
        return true
    }

    override fun onBackPressed() {
        val i=Intent(Intent.ACTION_MAIN)
        i.addCategory(Intent.CATEGORY_HOME)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }
}