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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * AppiraterKit utilities.
 */
@SuppressWarnings("unused")
public class AppiraterUtils {
    private static final String TAG = "AppiraterUtils";

    private static final String STORE_SCHEME = "market";

    private static final String STORE_PATH = "details";

    private static final String STORE_QUERY_ID = "id";

    private AppiraterUtils() {
    }

    /**
     * Launch this app store site.
     *
     * @param context Context
     */
    public static void launchStore(@NonNull Context context) {
        try {
            Uri uri = new Uri.Builder()
                    .scheme(STORE_SCHEME)
                    .appendPath(STORE_PATH)
                    .appendQueryParameter(STORE_QUERY_ID, context.getPackageName())
                    .build();
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "Occurred ActivityNotFoundException.", e);
        }
    }
}
