package io.focuslauncher.phone.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.focuslauncher.BuildConfig
import io.focuslauncher.R
import io.focuslauncher.databinding.FragmentTempoSettingsBinding
import io.focuslauncher.phone.activities.CoreActivity
import io.focuslauncher.phone.helper.ActivityHelper
import io.focuslauncher.phone.utils.PrefSiempo

open class TempoSettingsFragment : CoreFragment() {

    private var binding: FragmentTempoSettingsBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTempoSettingsBinding.inflate(inflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding?.toolbar?.apply {
            setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp)
            setTitle(R.string.settings)
            setNavigationOnClickListener { activity.onBackPressed() }
        }
        if (BuildConfig.FLAVOR.equals(context.getString(R.string.alpha), ignoreCase = true)) {
            binding?.relAlphaSettings?.visibility = View.VISIBLE
        } else {
            if (PrefSiempo.getInstance(context).read(PrefSiempo.IS_ALPHA_SETTING_ENABLE, false)) {
                binding?.relAlphaSettings?.visibility = View.VISIBLE
            } else {
                binding?.relAlphaSettings?.visibility = View.GONE
            }
        }

        binding?.relHome?.setOnClickListener {
            (activity as CoreActivity).loadChildFragment(
                TempoHomeFragment_.builder()
                    .build(), R.id.tempoView
            )
        }
        binding?.relAppMenu?.setOnClickListener {
            (activity as CoreActivity).loadChildFragment(AppMenuFragment.newInstance(false), R.id.tempoView)
        }

        binding?.relNotification?.setOnClickListener {
            (activity as CoreActivity).loadChildFragment(TempoNotificationFragment_.builder().build(), R.id.tempoView)
        }
        binding?.relDoubleTap?.setOnClickListener {
            (activity as CoreActivity).loadChildFragment(DoubleTapControlsFragment_.builder().build(), R.id.tempoView)
        }
        binding?.relAccount?.setOnClickListener {
            (activity as CoreActivity).loadChildFragment(AccountSettingFragment_.builder().build(), R.id.tempoView)
        }
        binding?.relAlphaSettings?.setOnClickListener {
            ActivityHelper(context).openSiempoAlphaSettingsApp()
        }
    }
}