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
import android.support.annotation.NonNull;

@SuppressWarnings("unused")
public abstract class AppiraterMeasure {

    private static AppiraterMeasure sInstance = null;

    /**
     * Constructs a new instance of the #AppiraterMeasure.
     */
    public static synchronized AppiraterMeasure getInstance() {
        if (sInstance == null) {
            sInstance = new AppiraterMeasureImpl();
        }
        return sInstance;
    }

    protected AppiraterMeasure() {
    }

    /**
     * Notify application launch to RmpAppiraterKit.
     *
     * @param context Context
     * @return Application launch information.
     */
    public abstract Result appLaunched(@NonNull Context context);

    /**
     * Return the same result as the previous result of {@link #appLaunched(Context)} calling.
     *
     * @return Last {@link #appLaunched(Context)} result.
     */
    public abstract Result getLastAppLaunchedResult();

    /**
     * App launch information
     */
    public static class Result {

        /** Launch count of This application. */
        public final long appLaunchCount;

        /** Launch count of This application current version. */
        public final long appThisVersionCodeLaunchCount;

        /** First launch date. */
        public final long firstLaunchDate;

        /** This application version code. */
        public final int appVersionCode;

        /** This application version code of when it's launched last. */
        public final int previousAppVersionCode;

        private Result(long appLaunchCount, long appThisVersionCodeLaunchCount, long firstLaunchDate,
                       int appVersionCode, int previousAppVersionCode) {
            this.appLaunchCount = appLaunchCount;
            this.appThisVersionCodeLaunchCount = appThisVersionCodeLaunchCount;
            this.firstLaunchDate = firstLaunchDate;
            this.appVersionCode = appVersionCode;
            this.previousAppVersionCode = previousAppVersionCode;
        }

        static class Builder {

            private long mAppLaunchCount;

            private long mAppThisVersionCodeLaunchCount;

            private long mFirstLaunchDate;

            private int mAppVersionCode;

            private int mPreviousAppVersionCode;

            public Builder() {
                mAppLaunchCount = 0L;
                mAppThisVersionCodeLaunchCount = 0L;
                mFirstLaunchDate = 0L;
                mAppVersionCode = Integer.MIN_VALUE;
                mPreviousAppVersionCode = Integer.MIN_VALUE;
            }

            Builder setAppLaunchCount(long appLaunchCount) {
                mAppLaunchCount = appLaunchCount;
                return this;
            }

            Builder setAppThisVersionCodeLaunchCount(long appThisVersionCodeLaunchCount) {
                mAppThisVersionCodeLaunchCount = appThisVersionCodeLaunchCount;
                return this;
            }

            Builder setFirstLaunchDate(long firstLaunchDate) {
                mFirstLaunchDate = firstLaunchDate;
                return this;
            }

            Builder setAppVersionCode(int appVersionCode, int previousAppVersionCode) {
                mAppVersionCode = appVersionCode;
                mPreviousAppVersionCode = previousAppVersionCode;
                return this;
            }

            Builder setAppVersionCode(int appVersionCode) {
                mAppVersionCode = appVersionCode;
                mPreviousAppVersionCode = Integer.MIN_VALUE;
                return this;
            }

            Result create() {
                return new Result(mAppLaunchCount,
                        mAppThisVersionCodeLaunchCount,
                        mFirstLaunchDate,
                        mAppVersionCode,
                        mPreviousAppVersionCode);
            }
        }
    }
}