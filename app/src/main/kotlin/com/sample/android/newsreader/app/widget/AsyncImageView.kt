package com.sample.android.newsreader.app.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.AttributeSet
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import java.io.IOException
import java.net.URL
import java.util.*

class AsyncImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : ImageView(context, attrs, defStyle) {

    var imageURL: URL? = null
        set(url) {
            if (url == null || url == imageURL) {
                return
            }
            imageURL = url
            mURLSet!!.add(url)

            val bitmap = BITMAP_MAP.getRaw(imageURL)
            if (bitmap == null) {
                val task = LoadImageTask(url)
                task.setCallback(mLoadImageTaskCallback)
                task.execute()
            } else {
                setImageBitmap(bitmap)
            }
        }

    private val mBitmap: Bitmap? = null

    private var mLoadImageTaskCallback: LoadImageTask.Callback? = null

    private var mURLSet: MutableSet<URL>? = null

    init {

        mLoadImageTaskCallback = ImageLoadCallback()
        mURLSet = HashSet<URL>()
    }

    override fun setImageBitmap(bitmap: Bitmap) {
        if (mBitmap != null && mBitmap != bitmap) {
            val animation = AlphaAnimation(LEVEL_VISIBLE, LEVEL_INVISIBLE)
            animation.duration = DURATION
            setAnimation(animation)
        }
        super.setImageBitmap(bitmap)

        val animation = AlphaAnimation(LEVEL_INVISIBLE, LEVEL_VISIBLE)
        animation.duration = DURATION
        setAnimation(animation)
    }

    fun setImageLoadedBitmap(bitmap: Bitmap) {
        addBitmap(imageURL!!, bitmap)
        setImageBitmap(bitmap)
    }

    private inner class ImageLoadCallback : LoadImageTask.Callback {
        override fun onLoadFinished(bitmap: Bitmap) {
            setImageLoadedBitmap(bitmap)
        }

        override fun onLoadCanceled(throwable: Throwable?) {
            throwable?.printStackTrace()
        }
    }

    private class LoadImageTask(url: URL) : AsyncTask<Bitmap, Void, Bitmap>() {

        internal interface Callback {
            fun onLoadFinished(bitmap: Bitmap)
            fun onLoadCanceled(throwable: Throwable?)
        }

        private var mCallback: Callback? = null

        private var mURL: URL? = null

        private var mThrowable: Throwable? = null

        init {
            mURL = url
        }

        override fun doInBackground(vararg params: Bitmap): Bitmap? {
            var bitmap: Bitmap? = null
            try {
                val inputStream = mURL!!.openStream()
                bitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: IOException) {
                mThrowable = e
            }

            return bitmap
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            if (mCallback != null) {
                if (bitmap != null) {
                    mCallback!!.onLoadFinished(bitmap)
                } else {
                    mCallback!!.onLoadCanceled(mThrowable)
                }
            }
        }

        fun setCallback(callback: Callback?) {
            mCallback = callback
        }
    }

    companion object {

        private val BITMAP_MAP = HashMap<URL, Bitmap>()

        private val sMapLock = Object()

        private val LEVEL_VISIBLE = 1.0f

        private val LEVEL_INVISIBLE = 0.0f

        private val DURATION = 500L

        private fun addBitmap(url: URL, bitmap: Bitmap): Boolean {
            synchronized (sMapLock) {
                if (BITMAP_MAP.containsKey(url)) {
                    return false
                } else {
                    BITMAP_MAP.put(url, bitmap)
                    return true
                }
            }
        }
    }
}
