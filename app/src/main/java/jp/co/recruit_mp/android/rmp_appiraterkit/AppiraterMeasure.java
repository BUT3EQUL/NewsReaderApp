/*
 * Copyright (C) 2015 Recruit Marketing Partners Co.,Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.recruit_mp.android.rmp_appiraterkit;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

public class AppiraterMeasure {
    private static final String TAG = "AppiraterMeasure";

    private static final String PREF_KEY_APP_LAUNCH_COUNT = "PREF_KEY_APP_LAUNCH_COUNT";
    private static final String PREF_KEY_APP_THIS_VERSION_CODE_LAUNCH_COUNT = "PREF_KEY_APP_THIS_VERSION_CODE_LAUNCH_COUNT";
    private static final String PREF_KEY_APP_FIRST_LAUNCHED_DATE = "PREF_KEY_APP_FIRST_LAUNCHED_DATE";
    private static final String PREF_KEY_APP_VERSION_CODE = "PREF_KEY_APP_VERSION_CODE";

    private static final String PREFS_PACKAGE_NAME_SUFFIX = ".RmpAppiraterKit";

    /**
     * App launch information
     */
    public static class Result {
        private long mAppLaunchCount;
        private long mAppThisVersionCodeLaunchCount;
        private long mFirstLaunchDate;
        private int mAppVersionCode;
        private int mPreviousAppVersionCode;

        /*package*/ Result(long appLaunchCount, long appThisVersionCodeLaunchCount, long firstLaunchDate,
                           int appVersionCode, int previousAppVersionCode) {
            mAppLaunchCount = appLaunchCount;
            mAppThisVersionCodeLaunchCount = appThisVersionCodeLaunchCount;
            mFirstLaunchDate = firstLaunchDate;
            mAppVersionCode = appVersionCode;
            mPreviousAppVersionCode = previousAppVersionCode;
        }

        /**
         * Gets launch count of This application.
         *
         * @return Launch count of This application.
         */
        public long getAppLaunchCount() {
            return mAppLaunchCount;
        }

        /**
         * Gets launch count of This application current version.
         *
         * @return Launch count of This application current version.
         */
        public long getAppThisVersionCodeLaunchCount() {
            return mAppThisVersionCodeLaunchCount;
        }

        /**
         * Gets first launch date.
         *
         * @return First launch date.
         */
        public long getFirstLaunchDate() {
            return mFirstLaunchDate;
        }

        /**
         * Gets this application version code.
         *
         * @return This application version code.
         */
        public int getAppVersionCode() {
            return mAppVersionCode;
        }

        /**
         * Gets this application version code of when it's launched last.
         *
         * @return this application version code of when it's launched last.
         */
        public int getPreviousAppVersionCode() {
            return mPreviousAppVersionCode;
        }
    }

    private AppiraterMeasure() {}

    private static Result mLastResult;

    /**
     * Notify app launch to RmpAppiraterKit.
     *
     * @param context Context
     * @return App launch information
     */
    public static Result appLaunched(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        // Load appThisVersionCodeLaunchCount
        long appLaunchCount = prefs.getLong(PREF_KEY_APP_LAUNCH_COUNT, 0);
        // Load appThisVersionCodeLaunchCount
        long appThisVersionCodeLaunchCount = prefs.getLong(PREF_KEY_APP_THIS_VERSION_CODE_LAUNCH_COUNT, 0);
        // Load firstLaunchDate
        long firstLaunchDate = prefs.getLong(PREF_KEY_APP_FIRST_LAUNCHED_DATE, 0);
        // Load appVersionCode and prefsAppVersionCode
        int appVersionCode = Integer.MIN_VALUE;
        final int previousAppVersionCode = prefs.getInt(PREF_KEY_APP_VERSION_CODE, Integer.MIN_VALUE);
        try {
            appVersionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            if (previousAppVersionCode != appVersionCode) {
                // Reset appThisVersionCodeLaunchCount
                appThisVersionCodeLaunchCount = 0;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Occurred PackageManager.NameNotFoundException", e);
        }

        // Increment appLaunchCount
        ++appLaunchCount;
        prefsEditor.putLong(PREF_KEY_APP_LAUNCH_COUNT, appLaunchCount);

        // Increment appThisVersionCodeLaunchCount
        ++appThisVersionCodeLaunchCount;
        prefsEditor.putLong(PREF_KEY_APP_THIS_VERSION_CODE_LAUNCH_COUNT, appThisVersionCodeLaunchCount);

        // Set app first launch date.
        if (firstLaunchDate == 0) {
            firstLaunchDate = System.currentTimeMillis();
            prefsEditor.putLong(PREF_KEY_APP_FIRST_LAUNCHED_DATE, firstLaunchDate);
        }

        // Set app version code
        if (appVersionCode != Integer.MIN_VALUE) {
            prefsEditor.putInt(PREF_KEY_APP_VERSION_CODE, appVersionCode);
        }

        prefsEditor.commit();

        mLastResult = new Result(appLaunchCount, appThisVersionCodeLaunchCount, firstLaunchDate, appVersionCode, previousAppVersionCode);
        return mLastResult;
    }

    /**
     * Gets last {@link #appLaunched(android.content.Context)} result.
     * Return the same result as the previous result of {@link #appLaunched(android.content.Context)} calling.
     *
     * @return Last {@link #appLaunched(android.content.Context)} result
     */
    public static Result getLastAppLaunchedResult() {
        return mLastResult;
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(context.getPackageName() + PREFS_PACKAGE_NAME_SUFFIX, Context.MODE_PRIVATE);
    }
}
