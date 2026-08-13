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

import androidx.appcompat.app.AppCompatActivity;

import com.nf.arabmoviehubapp.data.local.AppUrlStorage;
import com.nf.arabmoviehubapp.data.ui.MovieListActivity;
import com.nf.arabmoviehubapp.data.ui.WebViewActivity;
import com.nf.arabmoviehubapp.data.web.WebManagerView;

import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {

    private EmulatorDetector detector;
    private WindowManager windowManager;
    private Consumer<Integer> recordingCallback;

    private RelativeLayout webViewContainer;
    private View recordingFallbackUI;
    private View mainLoading;
    private WebView webView;
    private WebManagerView webManagerView;

    private boolean isProtectedContentVisible = false;

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


        boolean canOpenWebView = !isMirrored && !isInitiallyRecording && !isEmulator && hasValidUrl;


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