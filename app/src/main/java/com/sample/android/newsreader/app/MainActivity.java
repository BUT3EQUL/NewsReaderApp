package com.sample.android.newsreader.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private boolean mIsInitialized;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mIsInitialized = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsInitialized) {
            updateNews();
            mIsInitialized = true;
        }
    }

    private void updateNews() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (fragment instanceof MainFragment) {
            ((MainFragment) fragment).updateNews();
        }
    }
}
