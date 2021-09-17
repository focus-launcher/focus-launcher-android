package io.focuslauncher.phone.app;

import android.Manifest;

/**
 * Created by tkb on 2017-04-26.
 */

public class Constants {


    public static final String WHATSAPP_PACKAGE = "com.whatsapp";
    public static final String VIBER_PACKAGE = "com.viber.voip";
    public static final String SKYPE_PACKAGE = "com.skype.raider";
    public static final String WECHAT_PACKAGE = "com.tencent.mm";
    public static final String LINE_PACKAGE = "jp.naver.line.android";


    public static final String GOOGLE_CALENDAR_PACKAGES = "com.google.android.calendar";
    public static final String TELEGRAM_PACKAGES = "org.telegram.messenger";
    //Packages to be added by default in junk food app
    public static final String FACEBOOK_PACKAGE = "com.facebook.katana";
    public static final String SNAP_PACKAGE = "com.snapchat.android";
    public static final String INSTAGRAM_PACKAGE = "com.instagram.android";
    public static final String TWITTER_PACKAGE = "com.twitter.android";
    public static final String LINKEDIN_PACKAGE = "com.linkedin.android";
    public static final String CLASH_ROYAL_PACKAGE = "com.supercell.clashroyale";
    public static final String COFFEE_MEETS_PACKAGE = "com.coffeemeetsbagel";
    public static final String HINGE_PACKAGE = "co.hinge.app";
    public static final String NETFLIX_PACKAGE = "com.netflix.mediaclient";
    public static final String REDDIT_PACKAGE = "com.reddit.frontpage";
    public static final String TINDER_PACKAGE = "com.tinder";
    public static final String CANDY_SAGA_PACKAGE = "com.king.candycrushsaga";
    public static final String GRINDR_PACKAGE = "com.grindrapp.android";
    public static final String YOUTUBE_PACKAGE = "com.google.android.youtube";
    public static final String BUMBLE_PACKAGE = "com.bumble.app";


    public static final String FACEBOOK_MESSENGER_PACKAGE = "com.facebook.orca";
    public static final String FACEBOOK_LITE_PACKAGE = "com.facebook.mlite";
    public static final String GOOGLE_HANGOUTS_PACKAGES = "com.google.android.talk";
    public static final String GOOGLE_MAP_PACKAGE = "com.google.android.apps.maps";
    public static final String GOOGLE_PHOTOS = "com.google.android.apps.photos";
    public static final String GOOGLE_CAMERA = " com.android.camera";
    public static final String CALL_APP_PACKAGE = "com.google.android.dialer";
    public static final String GOOGLE_GMAIL_PACKAGE = "com.google.android.gm";
    public static final String CONTACT_APP_PACKAGE = "com.android.contacts";
    public static final String SETTINGS_APP_PACKAGE = "com.android.settings";
    public static final int DEFAULT_TEMPO_MINUTE = 15;
    public static final String[] CALL_APP_PACKAGES = new String[]{"com.google.android.dialer", "com.android.dialer"};
    public static final String[] CALENDAR_APP_PACKAGES = new String[]{"com.google.android.calendar"};
    public static final String[] CLOCK_APP_PACKAGES = new String[]{"com.google.android.deskclock", "com.asus.deskclock", "com.sonyericsson.organizer", "com.sonyericsson.organizer.Organizer_WorldClock",
            "com.sec.android.app.clockpackage", "com.motorola.blur.alarmclock.AlarmClock", "com.android.deskclock.DeskClock", "com.android.deskclock",
            "com.htc.android.worldclock", "com.htc.android.worldclock.WorldClockTabControl"};
    public static final int CALL_PACKAGE = 2;
    public static final int MESSAGE_PACKAGE = 1;
    public static final int CALENDER_PACKAGE = 20;
    public static final int CONTACT_PACKAGE = 3;
    public static final int MAP_PACKAGE = 11;
    public static final int NOTES_PACKAGE = 6;
    public static final int PHOTOS_PACKAGE = 22;
    public static final int CAMERA_PACKAGE = 23;
    public static final int BROWSER_PACKAGE = 24;
    public static final int CLOCK_PACKAGE = 21;
    public static final int EMAIL_PACKAGE = 16;
    public static final int ERROR_VERSION_EVENT = -1000;
    public static final String WHATSAPP = "WhatsApp";
    public static final String ALPHA_SETTING = "0x0p9o8i7u6y";
    public static final String HUAWEI = "huawei";
    public static String[] PERMISSIONS = {
            //Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            //Manifest.permission.RECEIVE_SMS,
            //Manifest.permission.SEND_SMS,
            //Manifest.permission.READ_SMS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    public static String HELPFUL_ROBOTS = "HELPFUL_ROBOTS";
    public static String BLOCKED_APPLIST = "BLOCKED_APPLIST";
    public static String SOCIAL_DISABLE_COUNT = "SOCIAL_DISABLE_COUNT";
    public static String MESSENGER_DISABLE_COUNT = "MESSENGER_DISABLE_COUNT";
    public static String APP_DISABLE_COUNT = "APP_DISABLE_COUNT";
    public static String HEADER_APPLIST = "HEADER_APPLIST";
    public static String CALL_RUNNING = "CALLRUNNING";
    public static String INTENT_MAINLISTITEM = "MainListItem";

    public static int STATUSBAR_SERVICE_ID = 1000;
    public static int NOTIFICIONLISTENER_SERVICE_ID = 1001;
    public static int ALARM_SERVICE_ID = 1002;
}
