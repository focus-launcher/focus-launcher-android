package io.focuslauncher.phone.activities

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import io.focuslauncher.R
import io.focuslauncher.databinding.ActivitySettingsMainBinding
import io.focuslauncher.databinding.ActivityTempoSettingsBinding
import io.focuslauncher.phone.fragments.AppMenuFragment
import io.focuslauncher.phone.fragments.TempoSettingsFragment
import io.focuslauncher.phone.utils.PrefSiempo
import io.focuslauncher.phone.utils.lifecycleProperty

open class SettingsActivity : CoreActivity() {

    private var binding: ActivityTempoSettingsBinding? by lifecycleProperty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTempoSettingsBinding.inflate(LayoutInflater.from(this))
        setContentView(binding?.root)

        if (intent.hasExtra("FlagApp")) {
            loadFragment(AppMenuFragment.newInstance(true), R.id.tempoView, "main")
        } else {
            loadFragment(TempoSettingsFragment(), R.id.tempoView, "main")
        }
    }

    override fun onResume() {
        super.onResume()
        statusBar()
    }

    private fun statusBar() {
        val isEnable = PrefSiempo.getInstance(this).read(PrefSiempo.DEFAULT_NOTIFICATION_ENABLE, false)
        if (isEnable) {
            val decorView = window.decorView
            val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
            decorView.systemUiVisibility = uiOptions
            decorView.fitsSystemWindows = true
            decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    if (PrefSiempo.getInstance(this@SettingsActivity)
                            .read(PrefSiempo.DEFAULT_NOTIFICATION_ENABLE, false)
                    ) {
                        Handler().postDelayed({
                            val decorView = window.decorView
                            val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                            decorView.systemUiVisibility = uiOptions
                            decorView.fitsSystemWindows = true
                        }, 3000)
                    }
                }
            }
        } else {
            val decorView = window.decorView
            val uiOptions = View.SYSTEM_UI_FLAG_VISIBLE
            decorView.systemUiVisibility = uiOptions
        }
        statusBarColor()
    }

    private fun statusBarColor() {

        /* new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Window window = getWindow();
                window.setStatusBarColor(ContextCompat.getColor(SettingsActivity.this ,R.color.green_solid));
                window.setNavigationBarColor(ContextCompat.getColor(SettingsActivity.this ,R.color.green_solid));
            }
        },1000);*/
    }
}