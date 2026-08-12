package com.nf.arabmoviehubapp.data.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nf.arabmoviehubapp.R;
import com.nf.arabmoviehubapp.data.web.WebManagerView;

import java.util.function.Consumer;

public class WebViewActivity extends AppCompatActivity {

    private static final String TAG = "ProctorTest";

    private WebView webView;
    private View fallbackUI;
    private WebManagerView webManagerView;
    private WindowManager windowManager;
    private Consumer<Integer> recordingCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TASK 3: FLAG_SECURE protection
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_webview);

        View main = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(main, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        webView = findViewById(R.id.webView);
        fallbackUI = findViewById(R.id.fallbackUI);
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        webManagerView = new WebManagerView(this, webView);

        String url = getIntent().getStringExtra("url");
        if (url != null && !url.isEmpty()) {
            webManagerView.loadUrl(url);
        }

        setupRecordingDetection();
    }

    private void setupRecordingDetection() {
        if (Build.VERSION.SDK_INT >= 35) {
            recordingCallback = state -> {
                boolean isRecording = (state == WindowManager.SCREEN_RECORDING_STATE_VISIBLE);
                updateRecordingUI(isRecording);
                Log.d(TAG, "SCREEN RECORDING = " + (isRecording ? "ON" : "OFF"));
            };
        } else {
            Log.d(TAG, "Screen Recording Support = NO (API < 35)");
        }
    }

    private void updateRecordingUI(boolean isRecording) {
        runOnUiThread(() -> {
            if (isRecording) {
                webView.setVisibility(View.GONE);
                fallbackUI.setVisibility(View.VISIBLE);
                Log.d(TAG, "WEBVIEW CONTENT HIDDEN — RECORDING DETECTED");
            } else {
                fallbackUI.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= 35 && windowManager != null && recordingCallback != null) {
            int currentState = windowManager.addScreenRecordingCallback(getMainExecutor(), recordingCallback);
            updateRecordingUI(currentState == WindowManager.SCREEN_RECORDING_STATE_VISIBLE);
            Log.d(TAG, "SCREEN RECORDING CALLBACK REGISTERED");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= 35 && windowManager != null && recordingCallback != null) {
            windowManager.removeScreenRecordingCallback(recordingCallback);
            Log.d(TAG, "SCREEN RECORDING CALLBACK REMOVED");
        }
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
        }
        super.onDestroy();
    }
}