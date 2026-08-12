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

import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ProctorTest";

    private EmulatorDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        detector = new EmulatorDetector(this);

        handleIncomingUrl();

        openNextScreen();
    }

    private void handleIncomingUrl() {
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data == null) {
            return;
        }

        String url = data.getQueryParameter("url");
        if (url != null) {
            AppUrlStorage.saveUrl(this, url);
        }
    }

    private void openNextScreen() {
        String savedUrl = AppUrlStorage.getUrl(this);
        boolean isEmulator = detector.isEmulator();
        boolean isMirrored = isScreenBeingMirrored();
        boolean hasValidUrl = AppUrlStorage.isValidUrl(savedUrl);

        boolean isInitiallyRecording = false;
        boolean recordingDetectionSupported = Build.VERSION.SDK_INT >= 35;

        if (recordingDetectionSupported) {
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            if (wm != null) {
                Consumer<Integer> tempCallback = state -> {};
                int state = wm.addScreenRecordingCallback(getMainExecutor(), tempCallback);
                isInitiallyRecording = (state == WindowManager.SCREEN_RECORDING_STATE_VISIBLE);
                wm.removeScreenRecordingCallback(tempCallback);
            }
        }

        Log.d(TAG, "========== PROCTOR CHECK ==========");
        Log.d(TAG, "Android Version = " + Build.VERSION.RELEASE);
        Log.d(TAG, "SDK_INT = " + Build.VERSION.SDK_INT);
        Log.d(TAG, "Screen Recording Support = " + (recordingDetectionSupported ? "YES" : "NO (API < 35)"));
        Log.d(TAG, "Screen Recording = " + (recordingDetectionSupported ? isInitiallyRecording : "UNSUPPORTED"));
        Log.d(TAG, "Screen Mirroring = " + isMirrored);
        Log.d(TAG, "Emulator = " + isEmulator);
        Log.d(TAG, "Valid URL = " + hasValidUrl);

        boolean canOpenWebView = !isMirrored && !isInitiallyRecording && !isEmulator && hasValidUrl;

        Log.d(TAG, "CAN OPEN WEBVIEW = " + canOpenWebView);

        if (canOpenWebView) {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra("url", savedUrl);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, MovieListActivity.class);
            startActivity(intent);
        }

        finish();
    }

    private boolean isScreenBeingMirrored() {
        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        if (displayManager == null) {
            return false;
        }
        Display[] displays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        return displays != null && displays.length > 0;
    }
}