package com.sample.android.newsreader.app.data

import android.os.AsyncTask
import android.util.Log

abstract class FetchTask<Params, Progress, Result> : AsyncTask<Params, Progress, Result>() {

    interface Callback<Result> {
        fun onPreExecute()
        fun onPostExecute(result: Result)
    }

    interface OnStatusListener {
        fun onComplete()
        fun onError(message: String?, exception: Throwable?)
    }

    protected var mCallback: Callback<Result>? = null

    protected var mOnStatusListener: OnStatusListener? = null

    private var mIsTaskLive = false

    fun setCallback(callback: Callback<Result>?) {
        mCallback = callback
    }

    fun setOnStatusListener(listener: OnStatusListener?) {
        mOnStatusListener = listener
    }

    override fun onPreExecute() {
        mIsTaskLive = true
        if (mCallback != null) {
            mCallback!!.onPreExecute()
        }
        Log.d("FetchTask", "onPreExecute flag=" + mIsTaskLive)
    }

    override fun doInBackground(vararg params: Params): Result? {
        try {
            return onBackground(*params)
        } catch (e: Exception) {
            onError(e)
            return null
        }

    }

    @Throws(Exception::class)
    protected abstract fun onBackground(vararg params: Params): Result?

    protected fun onError(e: Exception) {
        if (mIsTaskLive && mOnStatusListener != null) {
            Log.d("FetchTask", "onError")
            mOnStatusListener!!.onError(e.message, e.cause)
            mIsTaskLive = false
        }
    }

    override fun onPostExecute(result: Result) {
        if (mIsTaskLive && mOnStatusListener != null) {
            Log.d("FetchTask", "onComplete")
            mOnStatusListener!!.onComplete()
            mIsTaskLive = false
        }
        if (mCallback != null) {
            mCallback!!.onPostExecute(result)
        }
    }
}