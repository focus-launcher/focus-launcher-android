package io.focuslauncher.phone.activities;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;

import io.focuslauncher.R;
import io.focuslauncher.phone.helper.FirebaseHelper;

/**
 * This screen is use to display FAQ link.
 */
public class PrivacyPolicyActivity extends CoreActivity {

    WebView web_Faq;
    Toolbar toolbar;
    private long startTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_faq);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp);
        toolbar.setTitle(R.string.privacypolicy);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //Changed for SSA-1761 Fatal Exception: java.lang.RuntimeException: Unable to start activity ComponentInfo
        //web_Faq = findViewById(R.id.web_Faq);
        try {
            web_Faq = findViewById(R.id.web_Faq);
            web_Faq.getSettings().setJavaScriptEnabled(true);
            web_Faq.loadUrl(getString(R.string.url_privicey_policy));
            web_Faq.setBackgroundColor(Color.TRANSPARENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startTime = System.currentTimeMillis();
    }

    @Override
    public void onPause() {
        super.onPause();
        FirebaseHelper.getInstance().logScreenUsageTime(this.getClass().getSimpleName(), startTime);
    }
}
