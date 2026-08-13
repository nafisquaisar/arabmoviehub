package com.nf.arabmoviehubapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.nf.arabmoviehubapp.data.local.AppUrlStorage;
import com.nf.arabmoviehubapp.data.ui.MovieListActivity;
import com.nf.arabmoviehubapp.data.web.WebManagerView;

import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {


    private EmulatorDetector detector;
    private WindowManager windowManager;
    private Consumer<Integer> recordingCallback;

    private RelativeLayout webViewContainer;
    private View recordingFallbackUI;
    private View mainProgressBar;

    private WebView webView;
    private WebManagerView webManagerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TASK: Apply FLAG_SECURE
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_main);

        detector = new EmulatorDetector(this);
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        webViewContainer = findViewById(R.id.webViewContainer);
        recordingFallbackUI = findViewById(R.id.recordingFallbackUI);
        mainProgressBar = findViewById(R.id.mainProgressBar);

        handleIncomingIntent(getIntent());

        performEnvironmentCheck();
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingIntent(intent);

        // Re-evaluate environment and URL
        performEnvironmentCheck();
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent == null) return;

        Uri data = intent.getData();
        if (data == null) return;


        String url = data.getQueryParameter("url");
        if (url != null) {
            AppUrlStorage.saveUrl(this, url);
        }
    }

    private void performEnvironmentCheck() {
        String savedUrl = AppUrlStorage.getUrl(this);
        boolean isEmulator = detector.isEmulator();
        boolean isMirrored = isScreenBeingMirrored();
        boolean hasValidUrl = AppUrlStorage.isValidUrl(savedUrl);

        boolean isInitiallyRecording = false;
        boolean recordingDetectionSupported = Build.VERSION.SDK_INT >= 35;

        if (recordingDetectionSupported && windowManager != null) {
            // Add a temporary callback just to get the current state
            Consumer<Integer> tempCallback = state -> {};
            int state = windowManager.addScreenRecordingCallback(getMainExecutor(), tempCallback);
            isInitiallyRecording = (state == WindowManager.SCREEN_RECORDING_STATE_VISIBLE);
            windowManager.removeScreenRecordingCallback(tempCallback);
        }


        boolean canOpenWebView = !isMirrored && !isInitiallyRecording && !isEmulator && hasValidUrl;


        if (canOpenWebView) {
            setupWebView(savedUrl);
        } else {
            // Hide progress and route to MovieList
            mainProgressBar.setVisibility(View.GONE);
            if (webView != null) {
                webViewContainer.setVisibility(View.GONE);
            }

            Intent intent = new Intent(this, MovieListActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void setupWebView(String url) {
        mainProgressBar.setVisibility(View.GONE);
        webViewContainer.setVisibility(View.VISIBLE);

        if (webView == null) {
            webView = new WebView(this);
            webView.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT));

            webViewContainer.addView(webView);
            webManagerView = new WebManagerView(this, webView);

            setupRecordingDetection();
        }

        webManagerView.loadUrl(url);
    }

    private void setupRecordingDetection() {
        if (Build.VERSION.SDK_INT >= 35 && windowManager != null) {
            if (recordingCallback != null) return;

            recordingCallback = state -> {
                boolean isRecording = (state == WindowManager.SCREEN_RECORDING_STATE_VISIBLE);
                updateRecordingUI(isRecording);
            };
        }
    }

    private void updateRecordingUI(boolean isRecording) {
        runOnUiThread(() -> {
            if (isRecording) {
                webViewContainer.setVisibility(View.GONE);
                recordingFallbackUI.setVisibility(View.VISIBLE);
            } else {
                recordingFallbackUI.setVisibility(View.GONE);
                webViewContainer.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= 35 && windowManager != null && recordingCallback != null) {
            int state = windowManager.addScreenRecordingCallback(getMainExecutor(), recordingCallback);
            updateRecordingUI(state == WindowManager.SCREEN_RECORDING_STATE_VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= 35 && windowManager != null && recordingCallback != null) {
            windowManager.removeScreenRecordingCallback(recordingCallback);
        }
    }

    private boolean isScreenBeingMirrored() {
        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        if (displayManager == null) return false;
        Display[] displays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        return displays != null && displays.length > 0;
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.loadUrl("about:blank");
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}