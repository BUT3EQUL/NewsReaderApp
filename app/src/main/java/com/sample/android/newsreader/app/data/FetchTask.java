package com.sample.android.newsreader.app.data;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

public abstract class FetchTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    public interface Callback<Result> {
        void onPreExecute();
        void onPostExecute(Result result);
    }

    public interface OnStatusListener {
        void onComplete();
        void onError(String message, Throwable exception);
    }

    protected Callback<Result> mCallback = null;

    protected OnStatusListener mOnStatusListener = null;

    private boolean mIsTaskLive = false;

    public void setCallback(Callback<Result> callback) {
        mCallback = callback;
    }

    public void setOnStatusListener(OnStatusListener listener) {
        mOnStatusListener = listener;
    }

    @Override
    protected void onPreExecute() {
        mIsTaskLive = true;
        if (mCallback != null) {
            mCallback.onPreExecute();
        }
        Log.d("FetchTask", "onPreExecute flag=" + mIsTaskLive);
    }

    @Override
    protected final Result doInBackground(Params... params) {
        try {
            return onBackground(params);
        } catch (Exception e) {
            onError(e);
            return null;
        }
    }

    @Nullable
    protected abstract Result onBackground(Params... params) throws Exception;

    protected void onError(Exception e) {
        if (mIsTaskLive && mOnStatusListener != null) {
            Log.d("FetchTask", "onError");
            mOnStatusListener.onError(e.getMessage(), e.getCause());
            mIsTaskLive = false;
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        if (mIsTaskLive && mOnStatusListener != null) {
            Log.d("FetchTask", "onComplete");
            mOnStatusListener.onComplete();
            mIsTaskLive = false;
        }
        if (mCallback != null) {
            mCallback.onPostExecute(result);
        }
    }
}