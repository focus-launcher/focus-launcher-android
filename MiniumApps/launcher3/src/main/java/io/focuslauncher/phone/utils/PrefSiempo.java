package io.focuslauncher.phone.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by rajeshjadi on 2/2/18.
 * This class is used to store the local preference data.
 */

public class PrefSiempo {
    //Key Names

    // This field is used for show/hide the IF
    public static final String IS_INTENTION_ENABLE = "isIntentionEnable";

    // This field is used to store IF data.
    public static final String DEFAULT_INTENTION = "defaultIntention";

    // This field is used to store check application installed first time or not.
    public static final String IS_APP_INSTALLED_FIRSTTIME = "is_app_installed_firsttime";

    // This field is used to store check application installed first time or not.
    public static final String IS_AUTOSCROLL = "is_autoscroll";

    // This field is used to store check application installed first time or not.
    public static final String IS_APP_INSTALLED_FIRSTTIME_SHOW_TOOLTIP = "is_app_installed_firsttime_show_tooltip";

    // This field is used to store check application installed first time or not.
    public static final String IS_JUNKFOOD_FIRSTTIME = "is_junkfood_firsttime";

    // This field is used for icon branding : True to show siempo icon and False for default application icon
    public static final String IS_ICON_BRANDING = "is_icon_branding";

    // This field is used for App icons will have a different position each time you open the junk-food menu
    public static final String IS_RANDOMIZE_JUNKFOOD = "is_randomize_junkfood";

    // This field is used for to store tools pane visible/hide and its connected application.
    public static final String TOOLS_SETTING = "tools_setting";
    public static final String INSTALLED_APP_VERSION_CODE = "installed_app_version_code";

    // This field is used for to store junkfood application package name.
    public static final String JUNKFOOD_APPS = "junkfood_apps";

    // This field is used for to store favorites application package name.
    public static final String FAVORITE_APPS = "favorite_apps";

    // This field is used for to store search List.
    public static final String RECENT_ITEM_LIST = "recentItemList";

    // used for sorting of tools menu.
    public static final String SORTED_MENU = "sortedMenu";

    public static final String FAVORITE_SORTED_MENU = "favoriteSortedMenu";

    // used for Allow peaking.
    public static final String ALLOW_PEAKING = "Allowpeaking";
    public static final String IS_DARK_THEME = "isDarkTheme";


    // used for default background.
    public static final String DEFAULT_BAG = "default_back";
    public static final String DEFAULT_BAG_ENABLE = "default_e";

    public static final String IS_ASK_HINT = "IS_ASK_HINT";

    //Launcher 3 preferences
    public static final String UPDATE_PROMPT = "updatePrompt";
    public static final String CALL_RUNNING = "CALLRUNNING";
    public static final String IS_APP_DEFAULT_OR_FRONT = "isAppDefaultOrFront";
    //DroidPrefs
    public static final String SELECTED_THEME_ID = "selectedThemeId";
    public static final String FLOW_MAX_TIME_LIMIT_MILLIS =
            "flowMaxTimeLimitMillis";
    public static final String FLOW_SEGMENT_DURATION_MILLIS =
            "flowSegmentDurationMillis";
    public static final String IS_APP_UPDATED = "isAppUpdated";
    public static final String IS_ALPHA_SETTING_ENABLE = "isAlphaSettingEnable";
    public static final String IS_FIREBASE_ANALYTICS_ENABLE =
            "isFireBaseAnalyticsEnable";
    public static final String TEMPO_TYPE = "tempoType";
    public static final String BATCH_TIME = "batchTime";
    public static final String ONLY_AT = "onlyAt";
    public static final String USER_EMAILID = "userEmailId";
    public static final String IS_CONTACT_UPDATE = "isContactUpdate";


    public static final String LOCK_COUNTER_STATUS = "LOCK_COUNTER_STATUS";
    public static final String LOCATION_TIMER_TIME = "LOCATION_TIMER_TIME";
    //Preference for DeterUser
    public static final String DETER_AFTER = "deterAfter";
    public static final String BREAK_PERIOD = "break_period";
    public static final String GRACE_TIME = "grace_time";
    public static final String COVER_TIME = "cover_time";
    public static final String BREAK_TIME = "break_time";
    public static final String IS_SETTINGS_PRESSED = "is_settings_pressed";
    public static final String IS_BREAK_TIME_PRESSED = "is_settings_pressed";
    private static final PrefSiempo ourInstance = new PrefSiempo();
    public static String HELPFUL_ROBOTS = "HELPFUL_ROBOTS";
    public static String BLOCKED_APPLIST = "BLOCKED_APPLIST";
    public static String MESSENGER_DISABLE_COUNT = "MESSENGER_DISABLE_COUNT";
    public static String APP_DISABLE_COUNT = "APP_DISABLE_COUNT";
    public static String HEADER_APPLIST = "HEADER_APPLIST";
    public static String TOGGLE_LEFTMENU = "toggle_leftmenu";
    public static String USER_SEEN_EMAIL_REQUEST = "user_seen_email_request";
    public static String APPLAND_TOUR_SEEN = "appland_tour_seen";
    public static String JUNK_RESTRICTED = "junk_restricted";
    public static String USER_VOLUME = "user_volume";
    public static String LOCATION_STATUS = "location_status";
    public static String JUNKFOOD_USAGE_TIME = "junkfood_usage_time";
    public static String JUNKFOOD_USAGE_COVER_TIME = "junkfood_usage_cover_time";
    public static String CURRENT_DATE = "current_date";

    public static String THIRD_PARTY_APP_LOG_AS_LAUNCHER= "third_party_app_log_as_launcher";
    public static String THIRD_PARTY_APP_LOG_NOT_AS_LAUNCHER= "third_party_app_log_not_as_launcher";

    public static final String DEFAULT_ICON_TOOLS_TEXT_VISIBILITY_ENABLE = "default_tools_o";
    public static final String DEFAULT_ICON_FAVORITE_TEXT_VISIBILITY_ENABLE = "default_favorite_o";
    public static final String DEFAULT_ICON_JUNKFOOD_TEXT_VISIBILITY_ENABLE = "default_junkfood_o";

    public static final String DEFAULT_NOTIFICATION_ENABLE = "default_notification";
    public static final String DEFAULT_SCREEN_OVERLAY = "default_screen_overlay";

    /*This preferences are for double tap coontrols*/
    public static final String IS_SLEEP_ENABLE = "isSleepEnable";
    public static final String IS_DND_ENABLE = "isDnDEnable";
    
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private PrefSiempo() {
        //prevent creating multiple instances by making the constructor private
    }

    //The context passed into the getInstance should be application level context.
    public static PrefSiempo getInstance(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
        return ourInstance;
    }

    /**
     * Store the boolean data in local preference
     * e.g PrefSiempo.getInstance(this).write(PrefSiempo.Key,value);
     *
     * @param key   name to store in preference
     * @param value user provided value
     */
    public void write(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * Retrieve the boolean data from local preference
     * e.g PrefSiempo.getInstance(this).read(PrefSiempo.Key,defValue);
     *
     * @param key      name of retrieve from preference
     * @param defValue user provided default value
     */
    public boolean read(String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    /**
     * Store the boolean data in local preference
     * e.g PrefSiempo.getInstance(this).write(PrefSiempo.Key,value);
     *
     * @param key   name to store in preference
     * @param value user provided value
     */
    public void write(String key, float value) {
        editor.putFloat(key, value);
        editor.apply();
    }

    /**
     * Retrieve the float data from local preference
     * e.g PrefSiempo.getInstance(this).read(PrefSiempo.Key,defValue);
     *
     * @param key      name of retrieve from preference
     * @param defValue user provided default value
     */
    public float read(String key, float defValue) {
        return sharedPreferences.getFloat(key, defValue);
    }

    /**
     * Store the boolean data in local preference
     * e.g PrefSiempo.getInstance(this).write(PrefSiempo.Key,value);
     *
     * @param key   name to store in preference
     * @param value user provided value
     */
    public void write(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * Retrieve the int data from local preference
     * e.g PrefSiempo.getInstance(this).read(PrefSiempo.Key,defValue);
     *
     * @param key      name of retrieve from preference
     * @param defValue user provided default value
     */
    public int read(String key, int defValue) {
        return sharedPreferences.getInt(key, defValue);
    }


    /**
     * Store the boolean data in local preference
     * e.g PrefSiempo.getInstance(this).write(PrefSiempo.Key,value);
     *
     * @param key   name to store in preference
     * @param value user provided value
     */
    public synchronized void write(String key, long value) {
        editor.putLong(key, value);
        editor.apply();
    }

    /**
     * Retrieve the long data from local preference
     * e.g PrefSiempo.getInstance(this).read(PrefSiempo.Key,defValue);
     *
     * @param key      name of retrieve from preference
     * @param defValue user provided default value
     */
    public long read(String key, long defValue) {
        return sharedPreferences.getLong(key, defValue);
    }

    /**
     * Store the boolean data in local preference
     * e.g PrefSiempo.getInstance(this).write(PrefSiempo.Key,value);
     *
     * @param key   name to store in preference
     * @param value user provided value
     */
    public void write(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Retrieve the String data from local preference
     * e.g PrefSiempo.getInstance(this).read(PrefSiempo.Key,defValue);
     *
     * @param key      name of retrieve from preference
     * @param defValue user provided default value
     */
    public String read(String key, String defValue) {
        return sharedPreferences.getString(key, defValue);
    }

    /**
     * Store the boolean data in local preference
     * e.g PrefSiempo.getInstance(this).write(PrefSiempo.Key,value);
     *
     * @param key   name to store in preference
     * @param value user provided value
     */
    public void write(String key, Set<String> value) {
        editor.putStringSet(key, value);
        editor.apply();
    }

    /**
     * Retrieve the Set<String> data from local preference
     * e.g PrefSiempo.getInstance(this).read(PrefSiempo.Key,defValue);
     *
     * @param key      name of retrieve from preference
     * @param defValue user provided default value
     */
    public Set<String> read(String key, Set<String> defValue) {

        Set<String> sharedSet = new HashSet<>(sharedPreferences.getStringSet
                (key, defValue));

        return sharedSet;
    }

    /**
     * This method is used to delete object from preference.
     * e.g PrefSiempo.getInstance(this).remove(PrefSiempo.Key);
     *
     * @param key user provided key.
     */
    public void remove(String key) {
        editor.remove(key);
        editor.commit();
    }

    /**
     * User to clear all local preference data.
     * e.g PrefSiempo.getInstance(this).clearAll();
     */
    public void clearAll() {
        editor.clear();
        editor.commit();
    }



}
