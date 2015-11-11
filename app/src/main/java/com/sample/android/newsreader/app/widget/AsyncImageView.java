package com.sample.android.newsreader.app.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AsyncImageView extends ImageView {

    private static final Map<URL, Bitmap> BITMAP_MAP = new HashMap<>();

    private static final Object sMapLock = new Object();

    private static final float LEVEL_VISIBLE = 1.0f;

    private static final float LEVEL_INVISIBLE = 0.0f;

    private static final long DURATION = 500L;

    private URL mURL = null;

    private Bitmap mBitmap = null;

    private LoadImageTask.Callback mLoadImageTaskCallback = null;

    private Set<URL> mURLSet = null;

    public AsyncImageView(Context context) {
        this(context, null);
    }

    public AsyncImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AsyncImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mLoadImageTaskCallback = new ImageLoadCallback();
        mURLSet = new HashSet<>();
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        if (mBitmap != null && !mBitmap.equals(bitmap)) {
            Animation animation = new AlphaAnimation(LEVEL_VISIBLE, LEVEL_INVISIBLE);
            animation.setDuration(DURATION);
            setAnimation(animation);
        }
        super.setImageBitmap(bitmap);

        Animation animation = new AlphaAnimation(LEVEL_INVISIBLE, LEVEL_VISIBLE);
        animation.setDuration(DURATION);
        setAnimation(animation);
    }

    public void setImageLoadedBitmap(Bitmap bitmap) {
        addBitmap(mURL, bitmap);
        setImageBitmap(bitmap);
    }

    public void setImageURL(URL url) {
        if (url == null || url.equals(mURL)) {
            return;
        }
        mURL = url;
        mURLSet.add(mURL);

        Bitmap bitmap = BITMAP_MAP.get(mURL);
        if (bitmap == null) {
            LoadImageTask task = new LoadImageTask(mURL);
            task.setCallback(mLoadImageTaskCallback);
            task.execute();
        } else {
            setImageBitmap(bitmap);
        }
    }

    public URL getImageURL() {
        return mURL;
    }

    @Override
    protected void finalize() throws Throwable {
        if (mURLSet != null && !mURLSet.isEmpty()) {
            for (URL url : mURLSet) {
                removeBitmap(url);
            }
            mURLSet = null;
        }
        super.finalize();
    }

    private static boolean addBitmap(URL url, Bitmap bitmap) {
        synchronized (sMapLock) {
            if (BITMAP_MAP.containsKey(url)) {
                return false;
            } else {
                BITMAP_MAP.put(url, bitmap);
                return true;
            }
        }
    }

    private static boolean removeBitmap(URL url) {
        synchronized (sMapLock) {
            if (!BITMAP_MAP.containsKey(url)) {
                return false;
            } else {
                BITMAP_MAP.remove(url);
                return true;
            }
        }
    }

    private class ImageLoadCallback implements LoadImageTask.Callback {
        @Override
        public void onLoadFinished(Bitmap bitmap) {
            setImageLoadedBitmap(bitmap);
        }

        @Override
        public void onLoadCanceled(Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private static class LoadImageTask extends AsyncTask<Bitmap, Void, Bitmap> {

        interface Callback {
            void onLoadFinished(Bitmap bitmap);
            void onLoadCanceled(Throwable throwable);
        }

        private Callback mCallback = null;

        private URL mURL = null;

        private Throwable mThrowable = null;

        public LoadImageTask(URL url) {
            mURL = url;
        }

        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            Bitmap bitmap = null;
            try {
                InputStream inputStream = mURL.openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                mThrowable = e;
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (mCallback != null) {
                if (bitmap != null) {
                    mCallback.onLoadFinished(bitmap);
                } else {
                    mCallback.onLoadCanceled(mThrowable);
                }
            }
        }

        public void setCallback(Callback callback) {
            mCallback = callback;
        }
    }
}
