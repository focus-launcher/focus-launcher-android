package io.focuslauncher.phone.app;

import android.graphics.Color;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultFloat;
import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.DefaultLong;
import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * Created by Shahab on 6/2/2016.
 */
@SharedPref(SharedPref.Scope.UNIQUE)
public interface DroidPrefs {

    @DefaultInt(Color.BLACK)
    int selectedThemeColor();

    @DefaultInt(0)
    int selectedThemeId();

    @DefaultInt(0)
    int notificationScheduleIndex();

    @DefaultInt(0)
    int notificationSchedulerValue();

    @DefaultLong(0)
    long notificationScheulerNextMillis();

    @DefaultBoolean(true)
    boolean notificationSchedulerSupressCalls();

    @DefaultBoolean(true)
    boolean notificationSchedulerSupressSMS();

    // Flow related configurations
    @DefaultBoolean(false)
    boolean isFlowRunning();

    @DefaultFloat(0)
    float flowMaxTimeLimitMillis();

    @DefaultFloat(0)
    float flowSegmentDurationMillis();

    // Notification Scheduler configurations

    /**
     * true - Notification Scheduler could not display scheduled notification due to other prioritized action (i.e. Flow)
     * false - otherwise
     *
     * @return
     */
    @DefaultBoolean(false)
    boolean isNotificationSupressed();

    @DefaultBoolean(false)
    boolean isNotificationSchedulerEnabled();

    @DefaultBoolean(false)
    boolean isSiempoNotificationServiceRunning();

    @DefaultBoolean(false)
    boolean hasShownIntroScreen();

    /**
     * This preference is used for store
     * user preference for Grid/List in Menu Listing View.
     *
     * @return
     */
    @DefaultBoolean(false)
    boolean isMenuGrid();


    @DefaultString("")
    String sortedMenu();

    /**
     * This preference is used for store
     * user preference for Grid/List in App Listing View.
     *
     * @return
     */
    @DefaultBoolean(true)
    boolean isGrid();

    @DefaultBoolean(true)
    boolean isContactUpdate();

    @DefaultBoolean(false)
    boolean isAppUpdated();

    @DefaultString("")
    String callPackage();

    @DefaultBoolean(false)
    boolean isCallClicked();

    @DefaultBoolean(false)
    boolean isCallClickedFirstTime();

    @DefaultString("")
    String messagePackage();

    @DefaultBoolean(false)
    boolean isMessageClicked();

    @DefaultBoolean(false)
    boolean isMessageClickedFirstTime();

    @DefaultString("")
    String calenderPackage();

    @DefaultBoolean(false)
    boolean isCalenderClicked();

    @DefaultString("")
    String contactPackage();

    @DefaultBoolean(false)
    boolean isContactClicked();

    @DefaultString("")
    String mapPackage();

    @DefaultBoolean(false)
    boolean isMapClicked();

    @DefaultString("")
    String photosPackage();

    @DefaultBoolean(false)
    boolean isPhotosClicked();

    @DefaultString("")
    String cameraPackage();

    @DefaultBoolean(false)
    boolean isCameraClicked();

    @DefaultString("")
    String browserPackage();

    @DefaultBoolean(false)
    boolean isBrowserClicked();

    @DefaultString("")
    String clockPackage();

    @DefaultBoolean(false)
    boolean isClockClicked();

    @DefaultString("")
    String emailPackage();

    @DefaultString("")
    String notesPackage();

    @DefaultBoolean(false)
    boolean isEmailClicked();

    @DefaultBoolean(false)
    boolean isEmailClickedFirstTime();

    @DefaultBoolean(true)
    boolean isFacebookAllowed();

    @DefaultBoolean(true)
    boolean isFacebooKMessangerAllowed();

    @DefaultBoolean(true)
    boolean isFacebooKMessangerLiteAllowed();

    @DefaultBoolean(true)
    boolean isWhatsAppAllowed();

    @DefaultBoolean(true)
    boolean isHangOutAllowed();

    @DefaultBoolean(false)
    boolean isAlphaSettingEnable();

    @DefaultBoolean(true)
    boolean isFireBaseAnalyticsEnable();

    @DefaultBoolean(false)
    boolean isTempoNotificationControlsDisabled();

    //0 Individual,1 batch,2 Only at.
    @DefaultInt(0)
    int tempoType();

    //15 minute,30 minute,1 hour,2 hour,4 hour
    @DefaultInt(15)
    int batchTime();

    @DefaultString("12:01")
    String onlyAt();

    // 0 for mute,1 for Sound
    @DefaultInt(0)
    int tempoSoundProfile();

    @DefaultString("")
    String userEmailId();

    @DefaultBoolean(false)
    boolean isIntentionEnable();

    @DefaultString("")
    String defaultIntention();

}
