package io.focuslauncher.phone.screenfilter;

import android.content.SharedPreferences;
import android.content.res.Resources;

import android.util.Log;

public class SettingsModel  implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SettingsModel";
    private static final boolean DEBUG = true;

    private SharedPreferences mSharedPreferences;
    private OnSettingsChangedListener mSettingsChangedListener;

    private String mPowerStatePrefKey = "prefPower";
    private String mPauseStatePrefKey = "prefState";
    private String mDimPrefKey = "prefDim";
    private String mColorPrefKey = "prefColor";
    private String mOpenOnBootPrefKey = "prefBoot";
    private String mKeepRunningAfterRebootPrefKey = "prefReboot";

    public SettingsModel(Resources resources, SharedPreferences sharedPreferences) {
        mSharedPreferences = sharedPreferences;
    }

    public boolean getShadesPowerState() {
        return mSharedPreferences.getBoolean(mPowerStatePrefKey, false);
    }

    public void setShadesPowerState(boolean state) {
        mSharedPreferences.edit().putBoolean(mPowerStatePrefKey, state).apply();
    }

    public boolean getShadesPauseState() {
        return mSharedPreferences.getBoolean(mPauseStatePrefKey, false);
    }

    public void setShadesPauseState(boolean state) {
        mSharedPreferences.edit().putBoolean(mPauseStatePrefKey, state).apply();
    }

    public int getShadesDimLevel() {
        return mSharedPreferences.getInt(mDimPrefKey, 60);
    }

    public int getShadesColor() {
        return mSharedPreferences.getInt(mColorPrefKey, 0xFF91a7ff);
    }

    public boolean getOpenOnBootFlag() {
        return mSharedPreferences.getBoolean(mOpenOnBootPrefKey, false);
    }

    public boolean getResumeAfterRebootFlag() {
        return mSharedPreferences.getBoolean(mKeepRunningAfterRebootPrefKey, false);
    }

    public void setOnSettingsChangedListener(OnSettingsChangedListener listener) {
        mSettingsChangedListener = listener;
    }

    public void openSettingsChangeListener() {
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        if (DEBUG) Log.d(TAG, "Opened Settings change listener");
    }

    public void closeSettingsChangeListener() {
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);

        if (DEBUG) Log.d(TAG, "Closed Settings change listener");
    }

    //region OnSharedPreferenceChangeListener
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (mSettingsChangedListener == null) {
            return;
        }

        if (key.equals(mPowerStatePrefKey))
        {
            boolean powerState = getShadesPowerState();
            mSettingsChangedListener.onShadesPowerStateChanged(powerState);
        }
        else if (key.equals(mPauseStatePrefKey))
        {
            boolean pauseState = getShadesPauseState();
            mSettingsChangedListener.onShadesPauseStateChanged(pauseState);
        }
        else if (key.equals(mDimPrefKey))
        {
            int dimLevel = getShadesDimLevel();
            mSettingsChangedListener.onShadesDimLevelChanged(dimLevel);
        }
        else if (key.equals(mColorPrefKey))
        {
            int color = getShadesColor();
            mSettingsChangedListener.onShadesColorChanged(color);
        }
    }
    //endregion

    public interface OnSettingsChangedListener {
        void onShadesPowerStateChanged(boolean powerState);
        void onShadesPauseStateChanged(boolean pauseState);
        void onShadesDimLevelChanged(int dimLevel);
        void onShadesColorChanged(int color);
    }
}

