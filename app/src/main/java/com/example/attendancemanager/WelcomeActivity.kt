package com.example.attendancemanager

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class WelcomeActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_layout)

        val thread=Thread {
            Thread.sleep(1000)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        thread.start()
    }
}