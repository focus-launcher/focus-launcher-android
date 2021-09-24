package io.focuslauncher.phone.app

import io.focuslauncher.phone.app.Constants.DEFAULT_TEMPO_MINUTE
import org.androidannotations.annotations.sharedpreferences.*

/**
 * Created by Shahab on 2/16/2017.
 */
@SharedPref(SharedPref.Scope.UNIQUE)
interface Launcher3Prefs {
    @get:DefaultBoolean(false)
    val isPauseActive: Boolean

    @get:DefaultBoolean(false)
    val isPauseAllowFavoriteChecked: Boolean

    @get:DefaultBoolean(false)
    val isPauseAllowCallsChecked: Boolean

    @get:DefaultBoolean(false)
    val isNotificationBlockerRunning: Boolean

    // Tempo related settings
    @get:DefaultBoolean(false)
    val isTempoActive: Boolean

    @DefaultLong(0)
    fun tempoNextNotificationMillis(): Long

    @DefaultBoolean(false)
    fun tempoAllowFavorites(): Boolean

    @DefaultBoolean(false)
    fun tempoAllowCalls(): Boolean

    @DefaultInt(DEFAULT_TEMPO_MINUTE)
    fun tempoIntervalMinutes(): Int

    @get:DefaultBoolean(false)
    val isAwayChecked: Boolean

    @DefaultString("10:20:1")
    fun time(): String?

    @DefaultString("")
    fun awayMessage(): String?

    @DefaultBoolean(true)
    fun updatePrompt(): Boolean

    @get:DefaultBoolean(true)
    val isAppInstalledFirstTime: Boolean

    @get:DefaultBoolean(false)
    val isKeyBoardDisplay: Boolean

    @get:DefaultInt(0)
    val currentProfile: Int

    @get:DefaultBoolean(false)
    val isAppDefaultOrFront: Boolean

    @get:DefaultInt(0)
    val currentVersion: Int

    @get:DefaultBoolean(false)
    val isPermissionGivenAndContinued: Boolean
}