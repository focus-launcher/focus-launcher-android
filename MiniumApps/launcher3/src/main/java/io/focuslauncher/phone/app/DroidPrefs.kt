package io.focuslauncher.phone.app

import android.graphics.Color
import org.androidannotations.annotations.sharedpreferences.*

/**
 * Created by Shahab on 6/2/2016.
 */
@SharedPref(SharedPref.Scope.UNIQUE)
interface DroidPrefs {
    @DefaultInt(Color.BLACK)
    fun selectedThemeColor(): Int

    @DefaultInt(0)
    fun selectedThemeId(): Int

    @DefaultInt(0)
    fun notificationScheduleIndex(): Int

    @DefaultInt(0)
    fun notificationSchedulerValue(): Int

    @DefaultLong(0)
    fun notificationScheulerNextMillis(): Long

    @DefaultBoolean(true)
    fun notificationSchedulerSupressCalls(): Boolean

    @DefaultBoolean(true)
    fun notificationSchedulerSupressSMS(): Boolean

    // Flow related configurations
    @get:DefaultBoolean(false)
    val isFlowRunning: Boolean

    @DefaultFloat(0F)
    fun flowMaxTimeLimitMillis(): Float

    @DefaultFloat(0F)
    fun flowSegmentDurationMillis(): Float
    // Notification Scheduler configurations
    /**
     * true - Notification Scheduler could not display scheduled notification due to other prioritized action (i.e. Flow)
     * false - otherwise
     *
     * @return
     */
    @get:DefaultBoolean(false)
    val isNotificationSupressed: Boolean

    @get:DefaultBoolean(false)
    val isNotificationSchedulerEnabled: Boolean

    @get:DefaultBoolean(false)
    val isSiempoNotificationServiceRunning: Boolean

    @DefaultBoolean(false)
    fun hasShownIntroScreen(): Boolean

    /**
     * This preference is used for store
     * user preference for Grid/List in Menu Listing View.
     *
     * @return
     */
    @get:DefaultBoolean(false)
    val isMenuGrid: Boolean

    @DefaultString("")
    fun sortedMenu(): String?

    /**
     * This preference is used for store
     * user preference for Grid/List in App Listing View.
     *
     * @return
     */
    @get:DefaultBoolean(true)
    val isGrid: Boolean

    @get:DefaultBoolean(true)
    val isContactUpdate: Boolean

    @get:DefaultBoolean(false)
    val isAppUpdated: Boolean

    @DefaultString("")
    fun callPackage(): String?

    @get:DefaultBoolean(false)
    val isCallClicked: Boolean

    @get:DefaultBoolean(false)
    val isCallClickedFirstTime: Boolean

    @DefaultString("")
    fun messagePackage(): String?

    @get:DefaultBoolean(false)
    val isMessageClicked: Boolean

    @get:DefaultBoolean(false)
    val isMessageClickedFirstTime: Boolean

    @DefaultString("")
    fun calenderPackage(): String?

    @get:DefaultBoolean(false)
    val isCalenderClicked: Boolean

    @DefaultString("")
    fun contactPackage(): String?

    @get:DefaultBoolean(false)
    val isContactClicked: Boolean

    @DefaultString("")
    fun mapPackage(): String?

    @get:DefaultBoolean(false)
    val isMapClicked: Boolean

    @DefaultString("")
    fun photosPackage(): String?

    @get:DefaultBoolean(false)
    val isPhotosClicked: Boolean

    @DefaultString("")
    fun cameraPackage(): String?

    @get:DefaultBoolean(false)
    val isCameraClicked: Boolean

    @DefaultString("")
    fun browserPackage(): String?

    @get:DefaultBoolean(false)
    val isBrowserClicked: Boolean

    @DefaultString("")
    fun clockPackage(): String?

    @get:DefaultBoolean(false)
    val isClockClicked: Boolean

    @DefaultString("")
    fun emailPackage(): String?

    @DefaultString("")
    fun notesPackage(): String?

    @get:DefaultBoolean(false)
    val isEmailClicked: Boolean

    @get:DefaultBoolean(false)
    val isEmailClickedFirstTime: Boolean

    @get:DefaultBoolean(true)
    val isFacebookAllowed: Boolean

    @get:DefaultBoolean(true)
    val isFacebooKMessangerAllowed: Boolean

    @get:DefaultBoolean(true)
    val isFacebooKMessangerLiteAllowed: Boolean

    @get:DefaultBoolean(true)
    val isWhatsAppAllowed: Boolean

    @get:DefaultBoolean(true)
    val isHangOutAllowed: Boolean

    @get:DefaultBoolean(false)
    val isAlphaSettingEnable: Boolean

    @get:DefaultBoolean(true)
    val isFireBaseAnalyticsEnable: Boolean

    @get:DefaultBoolean(false)
    val isTempoNotificationControlsDisabled: Boolean

    // 0 Individual,1 batch,2 Only at.
    @DefaultInt(0)
    fun tempoType(): Int

    // 15 minute,30 minute,1 hour,2 hour,4 hour
    @DefaultInt(15)
    fun batchTime(): Int

    @DefaultString("12:01")
    fun onlyAt(): String?

    // 0 for mute,1 for Sound
    @DefaultInt(0)
    fun tempoSoundProfile(): Int

    @DefaultString("")
    fun userEmailId(): String?

    @get:DefaultBoolean(false)
    val isIntentionEnable: Boolean

    @DefaultString("")
    fun defaultIntention(): String?
}
