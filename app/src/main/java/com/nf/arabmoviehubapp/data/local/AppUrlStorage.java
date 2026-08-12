package com.nf.arabmoviehubapp.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

public class AppUrlStorage {

    private static final String PREF_NAME = "arabmovieshub_prefs";
    private static final String KEY_URL = "arabmovieshub";

    private AppUrlStorage() {
    }

    public static void saveUrl(Context context, String url) {
        if (!isValidUrl(url)) {
            return;
        }

        String normalizedUrl = normalizeUrl(url);

        getPreferences(context)
                .edit()
                .putString(KEY_URL, normalizedUrl)
                .apply();
    }

    public static String getUrl(Context context) {
        return getPreferences(context).getString(KEY_URL, "");
    }

    public static void clearUrl(Context context) {
        getPreferences(context)
                .edit()
                .remove(KEY_URL)
                .apply();
    }

    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        url = url.trim();
        Uri uri = Uri.parse(url);

        
        String scheme = uri.getScheme();
        if (scheme == null) {
            // Try normalizing first to see if it becomes valid
            uri = Uri.parse(normalizeUrl(url));
            scheme = uri.getScheme();
        }

        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            return false;
        }

        String host = uri.getHost();
        return host != null && !host.trim().isEmpty();
    }

    private static String normalizeUrl(String url) {
        if (url == null) return "";
        url = url.trim();
        if (url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://")) {
            return url;
        }
        url = url.replaceFirst("^/+", "");
        
        return "https://" + url;
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}