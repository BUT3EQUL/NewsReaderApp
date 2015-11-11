package com.sample.android.newsreader.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

class MainActivity : AppCompatActivity() {

    private var mIsInitialized: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        mIsInitialized = false
    }

    override fun onResume() {
        super.onResume()
        if (!mIsInitialized) {
            updateNews()
            mIsInitialized = true
        }
    }

    private fun updateNews() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment)
        if (fragment is MainFragment) {
            fragment.updateNews()
        }
    }
}
