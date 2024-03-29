package com.aziz.linkedinauth.sample

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.aziz.linkedinauth.LinkedInIntent
import com.aziz.linkedinauth.R
import com.aziz.linkedinauth.Utils

class SampleLibraryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

       val intent = LinkedInIntent.Builder(this)
            .setAppKey("sdf")
            .setAppSecret("sdfsdf")
            .setRedirectURL("www.google.com")
            .build()

        startActivityForResult(intent,111)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        Log.d("SampleLibraryActivity", "onActivityResult: ${LinkedInIntent.UserDetail(data)?.data}")

        super.onActivityResult(requestCode, resultCode, data)
    }
}
