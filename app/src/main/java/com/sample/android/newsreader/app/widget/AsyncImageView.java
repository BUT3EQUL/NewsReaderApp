package com.sample.android.newsreader.app.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.sample.android.newsreader.app.R;

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

    private URL mURL = null;

    private Bitmap mBitmap = null;

    private LoadImageTask.Callback mLoadImageTaskCallback = null;

    private Set<URL> mURLSet = null;

    public AsyncImageView(@NonNull Context context) {
        this(context, null);
    }

    public AsyncImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AsyncImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mLoadImageTaskCallback = new ImageLoadCallback();
        mURLSet = new HashSet<>();
    }

    @Override
    public void setImageBitmap(@Nullable Bitmap bitmap) {
        if (mBitmap != null && !mBitmap.equals(bitmap)) {
            setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.image_fade_out));
        }
        super.setImageBitmap(bitmap);

        setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.image_fade_in));
    }

    public void setImageLoadedBitmap(@Nullable Bitmap bitmap) {
        addBitmap(mURL, bitmap);
        setImageBitmap(bitmap);
    }

    public void setImageURL(@Nullable URL url) {
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

    @SuppressWarnings("unused")
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

    private static boolean addBitmap(@NonNull URL url, @Nullable Bitmap bitmap) {
        synchronized (sMapLock) {
            if (BITMAP_MAP.containsKey(url)) {
                return false;
            } else {
                BITMAP_MAP.put(url, bitmap);
                return true;
            }
        }
    }

    private static boolean removeBitmap(@NonNull URL url) {
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

    private static class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {

        interface Callback {
            void onLoadFinished(@Nullable Bitmap bitmap);
            void onLoadCanceled(@Nullable Throwable throwable);
        }

        private Callback mCallback = null;

        private URL mURL = null;

        private Throwable mThrowable = null;

        public LoadImageTask(URL url) {
            mURL = url;
        }

        @Override
        protected Bitmap doInBackground(@Nullable Void... params) {
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
        protected void onPostExecute(@Nullable Bitmap bitmap) {
            if (mCallback != null) {
                if (bitmap != null) {
                    mCallback.onLoadFinished(bitmap);
                } else {
                    mCallback.onLoadCanceled(mThrowable);
                }
            }
        }

        public void setCallback(@Nullable Callback callback) {
            if (mCallback != null && !mCallback.equals(callback)) {
                mCallback = callback;
            }
        }
    }
}
