package io.focuslauncher.phone.fragments;

import android.graphics.Color;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import io.focuslauncher.R;

/**
 * This screen is use to display privacy policy which is load from assets folder.
 */
@EFragment(R.layout.fragment_privacy_policy)
public class PrivacyPolicyFragment extends CoreFragment {

    @ViewById
    WebView web_PrivacyPolicy;

    @ViewById
    Toolbar toolbar;


    @AfterViews
    void afterViews() {

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp);
        toolbar.setTitle(R.string.privacypolicy);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        try {
            web_PrivacyPolicy.setHorizontalScrollBarEnabled(false);

            web_PrivacyPolicy.getSettings().setJavaScriptEnabled(true);
            web_PrivacyPolicy.loadUrl(getString(R.string.url_privicey_policy));


            web_PrivacyPolicy.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // do your handling codes here, which url is the requested url
                    // probably you need to open that url rather than redirect:
                    view.loadUrl(url);
                    return false; // then it is not handled by default action
                }
            });
            web_PrivacyPolicy.setBackgroundColor(Color.TRANSPARENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
