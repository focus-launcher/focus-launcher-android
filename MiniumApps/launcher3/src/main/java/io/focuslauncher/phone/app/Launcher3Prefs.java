package io.focuslauncher.phone.app;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.DefaultLong;
import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

import static io.focuslauncher.phone.app.Constants.DEFAULT_TEMPO_MINUTE;

/**
 * Created by Shahab on 2/16/2017.
 */
@SharedPref(SharedPref.Scope.UNIQUE)
public interface Launcher3Prefs {

    @DefaultBoolean(false)
    boolean isPauseActive();

    @DefaultBoolean(false)
    boolean isPauseAllowFavoriteChecked();

    @DefaultBoolean(false)
    boolean isPauseAllowCallsChecked();

    @DefaultBoolean(false)
    boolean isNotificationBlockerRunning();

    // Tempo related settings

    @DefaultBoolean(false)
    boolean isTempoActive();

    @DefaultLong(0)
    long tempoNextNotificationMillis();

    @DefaultBoolean(false)
    boolean tempoAllowFavorites();

    @DefaultBoolean(false)
    boolean tempoAllowCalls();

    @DefaultInt(DEFAULT_TEMPO_MINUTE)
    int tempoIntervalMinutes();

    @DefaultBoolean(false)
    boolean isAwayChecked();

    @DefaultString("10:20:1")
    String time();

    @DefaultString("")
    String awayMessage();

    @DefaultBoolean(true)
    boolean updatePrompt();

    @DefaultBoolean(true)
    boolean isAppInstalledFirstTime();

    @DefaultBoolean(false)
    boolean isKeyBoardDisplay();

    @DefaultInt(0)
    int getCurrentProfile();

    @DefaultBoolean(false)
    boolean isAppDefaultOrFront();

    @DefaultInt(0)
    int getCurrentVersion();

    @DefaultBoolean(false)
    boolean isPermissionGivenAndContinued();

}
