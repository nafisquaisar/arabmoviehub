package com.nf.arabmoviehubapp.data.ui;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nf.arabmoviehubapp.R;
import com.nf.arabmoviehubapp.data.web.WebManagerView;

public class WebViewActivity extends AppCompatActivity {

    private WebView webView;

    private WebManagerView webManagerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(
                R.layout.activity_webview
        );

        View main = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(main,
                (view, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        webView =
                findViewById(R.id.webView);

        webManagerView = new WebManagerView(this, webView);

        String url = getIntent().getStringExtra("url");

        if (url != null && !url.isEmpty()) {
            webManagerView.loadUrl(url);
        }
    }

    @Override
    public void onBackPressed() {

        if (webView != null
                && webView.canGoBack()) {

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