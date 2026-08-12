package com.nf.arabmoviehubapp.data.web;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebManagerView {

    private static final String PREF_NAME =
            "arabmovieshub_prefs";

    private static final String KEY_APP_URL =
            "arabmovieshub";

    private final Context context;
    private final WebView webView;

    public WebManagerView(
            Context context,
            WebView webView
    ) {
        this.context = context.getApplicationContext();
        this.webView = webView;

        setup();
    }

    private void setup() {

        WebSettings settings =
                webView.getSettings();

        settings.setJavaScriptEnabled(true);

        settings.setDomStorageEnabled(true);

        settings.setMediaPlaybackRequiresUserGesture(false);

        CookieManager cookieManager =
                CookieManager.getInstance();

        cookieManager.setAcceptCookie(true);

        cookieManager.setAcceptThirdPartyCookies(
                webView,
                true
        );

        webView.setWebViewClient(
                new WebViewClient() {

                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view,
                            WebResourceRequest request
                    ) {

                        return handleUrl(
                                request.getUrl()
                        );
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view,
                            String url
                    ) {

                        return handleUrl(
                                Uri.parse(url)
                        );
                    }
                }
        );
    }

    private boolean handleUrl(Uri uri) {

        if (uri == null) {
            return true;
        }

        String scheme =
                uri.getScheme();

        if ("http".equalsIgnoreCase(scheme)
                || "https".equalsIgnoreCase(scheme)) {

            /*
             * Return false so WebView handles
             * normal HTTP/HTTPS navigation itself.
             */
            return false;
        }

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            uri
                    );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            context.startActivity(intent);

        } catch (Exception ignored) {
        }

        return true;
    }

    public void loadUrl(String url) {

        if (!isValidHttpUrl(url)) {
            return;
        }

        webView.loadUrl(url);
    }

    private boolean isValidHttpUrl(String url) {

        if (url == null
                || url.trim().isEmpty()) {

            return false;
        }

        String value =
                url.trim().toLowerCase();

        return value.startsWith("https://")
                || value.startsWith("http://");
    }

    // =========================================================
    // Runtime URL Storage
    // =========================================================

    public static String getAppUrl(Context context) {

        return context
                .getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                )
                .getString(
                        KEY_APP_URL,
                        ""
                );
    }

    public static void saveFacebookValue(
            Context context,
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return;
        }

        value = value.trim();

        /*
         * If a complete URL is supplied,
         * remove the existing scheme.
         */
        if (value.contains("://")) {

            value =
                    value.substring(
                            value.indexOf("://") + 3
                    );
        }

        value =
                "https://" +
                        value.replaceFirst(
                                "^/+",
                                ""
                        );

        context
                .getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                )
                .edit()
                .putString(
                        KEY_APP_URL,
                        value
                )
                .apply();
    }

    public static void clearUrl(Context context) {

        context
                .getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                )
                .edit()
                .remove(KEY_APP_URL)
                .apply();
    }
}