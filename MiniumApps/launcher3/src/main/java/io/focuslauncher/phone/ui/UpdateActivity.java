package io.focuslauncher.phone.ui;

import android.annotation.SuppressLint;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import io.focuslauncher.R;
import io.focuslauncher.phone.activities.CoreActivity;

import static android.view.View.GONE;

@EActivity(R.layout.activity_update)
public class UpdateActivity extends CoreActivity {

    String HOME_PAGE = "https://play.google.com/apps/testing/io.focuslauncher";

    @ViewById
    WebView webView;

    @ViewById
    ProgressBar pBar;


    @SuppressLint("SetJavaScriptEnabled")
    @AfterViews
    void afterViews() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                pBar.setVisibility(GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        webView.loadUrl(HOME_PAGE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
