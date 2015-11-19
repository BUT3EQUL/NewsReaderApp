package jp.co.recruit_mp.android.rmp_appiraterkit;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

class AppiraterMeasureImpl extends AppiraterMeasure {

    private static final String PACKAGE = AppiraterMeasure.class.getPackage().getName();

    private static final String PREF_PREFIX = PACKAGE + ".preference.";

    private static final String PREF_KEY_APP_LAUNCH_COUNT =
            PREF_PREFIX + "APP_LAUNCH_COUNT";

    private static final String PREF_KEY_APP_THIS_VERSION_CODE_LAUNCH_COUNT =
            PREF_PREFIX + "APP_THIS_VERSION_CODE_LAUNCH_COUNT";

    private static final String PREF_KEY_APP_FIRST_LAUNCHED_DATE =
            PREF_PREFIX + "APP_FIRST_LAUNCHED_DATE";

    private static final String PREF_KEY_APP_VERSION_CODE =
            PREF_PREFIX + "APP_VERSION_CODE";

    private static final String PREFERENCE_NAME = "RmpAppiraterKit";

    private Result mLastResult = null;

    @Override
    public Result getLastAppLaunchedResult() {
        return mLastResult;
    }

    @Override
    public Result appLaunched(@NonNull Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        Result previousResult = makeResult(preferences);

        long appLaunchCount = previousResult.appLaunchCount + 1;

        int prevAppVersionCode = previousResult.appVersionCode;
        int appVersionCode = Integer.MIN_VALUE;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appVersionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        long appThisVersionCodeLaunchCount = previousResult.appThisVersionCodeLaunchCount;
        if (appVersionCode != prevAppVersionCode) {
            appThisVersionCodeLaunchCount = 0;
        }
        ++appThisVersionCodeLaunchCount;

        long firstLaunchDate = previousResult.firstLaunchDate;

        Result result = new Result.Builder()
                .setAppLaunchCount(appLaunchCount)
                .setAppThisVersionCodeLaunchCount(appThisVersionCodeLaunchCount)
                .setFirstLaunchDate(firstLaunchDate)
                .setAppVersionCode(appVersionCode, prevAppVersionCode)
                .create();
        applyNewResult(preferences, result);
        return result;
    }

    private Result makeResult(@NonNull SharedPreferences preferences) {
        long appLaunchCount = preferences.getLong(PREF_KEY_APP_LAUNCH_COUNT, 0);
        long appThisVersionLaunchCount = preferences.getLong(PREF_KEY_APP_THIS_VERSION_CODE_LAUNCH_COUNT, 0);
        long firstLaunchDate = preferences.getLong(PREF_KEY_APP_FIRST_LAUNCHED_DATE, System.currentTimeMillis());
        int appVersionCode = preferences.getInt(PREF_KEY_APP_VERSION_CODE, Integer.MIN_VALUE);
        int prevAppVersionCode = Integer.MIN_VALUE;

        return new Result.Builder()
                .setAppLaunchCount(appLaunchCount)
                .setAppThisVersionCodeLaunchCount(appThisVersionLaunchCount)
                .setFirstLaunchDate(firstLaunchDate)
                .setAppVersionCode(appVersionCode, prevAppVersionCode)
                .create();
    }

    private void applyNewResult(@NonNull SharedPreferences preferences, @NonNull Result result) {
        SharedPreferences.Editor editor = preferences.edit();
        try {
            editor.putLong(PREF_KEY_APP_LAUNCH_COUNT, result.appLaunchCount);
            editor.putLong(PREF_KEY_APP_THIS_VERSION_CODE_LAUNCH_COUNT, result.appThisVersionCodeLaunchCount);
            editor.putLong(PREF_KEY_APP_FIRST_LAUNCHED_DATE, result.firstLaunchDate);
            if (result.appVersionCode != Integer.MIN_VALUE) {
                editor.putInt(PREF_KEY_APP_VERSION_CODE, result.appVersionCode);
            }
        } finally {
            editor.apply();
        }
    }

    private SharedPreferences getSharedPreferences(@NonNull Context context) {
        ComponentName componentName = new ComponentName(context, PREFERENCE_NAME);
        return context.getSharedPreferences(componentName.getClassName(), Context.MODE_PRIVATE);
    }

}