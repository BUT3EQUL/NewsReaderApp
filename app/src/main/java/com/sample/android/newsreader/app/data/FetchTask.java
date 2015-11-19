package com.sample.android.newsreader.app.data;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public abstract class FetchTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    public interface Callback<Result> {
        void onPreExecute();
        void onPostExecute(@Nullable Result result);
    }

    public interface OnStatusListener {
        void onComplete();
        void onError(@Nullable String message, @Nullable Throwable exception);
    }

    protected Callback<Result> mCallback = null;

    protected OnStatusListener mOnStatusListener = null;

    private boolean mIsTaskLive = false;

    public void setCallback(@Nullable Callback<Result> callback) {
        mCallback = callback;
    }

    public void setOnStatusListener(@Nullable OnStatusListener listener) {
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

    @SafeVarargs
    @Nullable
    @Override
    protected final Result doInBackground(@Nullable Params... params) {
        try {
            if (params != null) {
                return onBackground(params);
            }
        } catch (Exception e) {
            onError(e);
        }
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected abstract Result onBackground(@NonNull Params... params) throws Exception;

    protected void onError(Exception e) {
        if (mIsTaskLive && mOnStatusListener != null) {
            Log.d("FetchTask", "onError");
            mOnStatusListener.onError(e.getMessage(), e.getCause());
            mIsTaskLive = false;
        }
    }

    @Override
    protected void onPostExecute(@Nullable Result result) {
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