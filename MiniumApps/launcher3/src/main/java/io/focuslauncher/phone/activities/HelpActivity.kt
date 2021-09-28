package io.focuslauncher.phone.activities

import android.os.Bundle
import io.focuslauncher.R
import io.focuslauncher.phone.fragments.HelpFragment
import io.focuslauncher.phone.helper.FirebaseHelper

class HelpActivity : CoreActivity() {
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        loadFragment(HelpFragment(), R.id.helpView, "main")
    }

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()
        FirebaseHelper.getInstance().logScreenUsageTime(this.javaClass.simpleName, startTime)
    }
}