package com.nf.arabmoviehubapp.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class AppUrlStorage {

    private static final String PREF_NAME =
            "arabmovieshub_prefs";

    private static final String KEY_URL =
            "arabmovieshub";

    private AppUrlStorage() {
    }

    public static void saveUrl(
            Context context,
            String url
    ) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }

        getPreferences(context)
                .edit()
                .putString(KEY_URL, url.trim())
                .apply();
    }

    public static String getUrl(Context context) {

        return getPreferences(context)
                .getString(KEY_URL, "");
    }

    public static void clearUrl(Context context) {

        getPreferences(context)
                .edit()
                .remove(KEY_URL)
                .apply();
    }

    private static SharedPreferences getPreferences(
            Context context
    ) {
        return context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }
}