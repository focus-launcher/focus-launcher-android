package io.focuslauncher.phone.activities

import android.graphics.Color
import io.focuslauncher.phone.activities.CoreActivity
import android.webkit.WebView
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import io.focuslauncher.R
import io.focuslauncher.databinding.FragmentPrivacyPolicyBinding
import io.focuslauncher.phone.helper.FirebaseHelper
import io.focuslauncher.phone.utils.lifecycleProperty
import java.lang.Exception

/**
 * This screen is use to display FAQ link.
 */
class PrivacyPolicyActivity : CoreActivity() {

    private var binding: FragmentPrivacyPolicyBinding? by lifecycleProperty()

    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_faq)
        binding?.toolbar?.apply {
            setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp)
            setTitle(R.string.privacypolicy)
            setNavigationOnClickListener { onBackPressed() }
        }
        //Changed for SSA-1761 Fatal Exception: java.lang.RuntimeException: Unable to start activity ComponentInfo
        //web_Faq = findViewById(R.id.web_Faq);
        try {
            binding?.webPrivacyPolicy?.apply {
                getSettings().javaScriptEnabled = true
                loadUrl(getString(R.string.url_privicey_policy))
                setBackgroundColor(Color.TRANSPARENT)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
    }

    public override fun onPause() {
        super.onPause()
        FirebaseHelper.getInstance().logScreenUsageTime(this.javaClass.simpleName, startTime)
    }
}