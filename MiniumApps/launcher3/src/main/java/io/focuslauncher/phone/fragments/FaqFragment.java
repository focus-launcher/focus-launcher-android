package io.focuslauncher.phone.fragments;

import android.graphics.Color;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import io.focuslauncher.R;
import io.focuslauncher.phone.helper.FirebaseHelper;

/**
 * This screen is use to display FAQ link.
 */
@EFragment(R.layout.fragment_faq)
public class FaqFragment extends CoreFragment {

    @ViewById
    WebView web_Faq;

    @ViewById
    Toolbar toolbar;
    private long startTime = 0;

    @AfterViews
    void afterViews() {

        toolbar.setTitle(R.string.faq_section);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        try {
            web_Faq.getSettings().setJavaScriptEnabled(true);
            web_Faq.loadUrl(getString(R.string.faqlink));
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
