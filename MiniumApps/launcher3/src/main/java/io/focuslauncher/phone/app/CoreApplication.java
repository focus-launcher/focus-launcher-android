package io.focuslauncher.phone.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.UserManager;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.LruCache;
import com.androidnetworking.AndroidNetworking;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import io.focuslauncher.R;
import io.focuslauncher.phone.event.AppInstalledEvent;
import io.focuslauncher.phone.log.Tracer;
import io.focuslauncher.phone.models.AppMenu;
import io.focuslauncher.phone.models.CategoryAppList;
import io.focuslauncher.phone.models.MainListItem;
import io.focuslauncher.phone.utils.LifecycleHandler;
import io.focuslauncher.phone.utils.PackageUtil;
import io.focuslauncher.phone.utils.PrefSiempo;
import io.focuslauncher.phone.utils.UIUtils;
import de.greenrobot.event.EventBus;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_ADDITIONAL_MESSAGE;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_ASSISTANT;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_AUTHENTICATION;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_BANKING;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_BOOKS;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_BROWSER;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_CALCULATOR;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_CALENDAR;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_CALL;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_CAMERA;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_CLOCK;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_CLOUD;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_COURCE;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_DOC;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_EMAIL;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_FILES;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_FITNESS;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_FLASH;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_FOOD;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_HEALTH;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_JOURNAL;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_LANGUAGES;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_LEARNING;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_MAP;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_MEDITATION;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_MESSAGE;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_MICROPHONE;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_MUSIC;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_NEWS;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_NOTES;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_PAYMENT;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_PHOTOS;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_PODCAST;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_RECORDER;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_SEARCH;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_SETTINGS;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_SLEEP;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_TODO;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_TRANSLATE;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_TRANSPORT;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_VIDEO;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_VOICE;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_WEATHER;
import static io.focuslauncher.phone.main.MainListItemLoader.TOOLS_WELLNESS;
/**
 * Each application should contain an {@link Application} class instance
 * All applications of this project should extend their own application from this class
 * This will be first class where we can initialize all necessary first time configurations
 * <p>
 * Created by shahab on 3/17/16.
 */

public abstract class CoreApplication extends MultiDexApplication {

    private static CoreApplication sInstance;
    UserManager userManager;
    LauncherApps launcherApps;
    String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1WmJ9sNAoO5o5QGJkZXfqLm8Py95ASb7XCY1NewZF7puJcWMGlv269AY2lqJuR0o/dzMnzo20D259NHPN6zF3TCsXcF8+jhRH5gAqKcNJCoc1p0tZ+rxZ5ETVYjR/OQ90MKStXa8MsArhfL+R6E27IuUELObkjS3XIwcjBj7EhBNVPv2ipj8t7w3bNorql8qPEHhgbc/v54krCMSEF1p82nIbZSvOFcJwLGg/wzmv6YfgsLD5fndoaNPiRLQ1nkWNASOryvgUDZAKqYjAtHY7WAV57FtQGgsViPTE4exzCp9t018GEeI5tbo4+RSw23nygSqmNBZkxv9Ee4jxpw7CQIDAQAB";
    private ArrayMap<String, String> listApplicationName = new ArrayMap<>();
    private Set<String> packagesList = new HashSet<>();
    private ArrayList<String> disableNotificationApps = new ArrayList<>();
    private Set<String> blockedApps = new HashSet<>();
    private LruCache<String, Bitmap> mMemoryCache;
    private ArrayList<String> junkFoodList = new ArrayList<>();
    private ArrayList<MainListItem> toolItemsList = new ArrayList<>();
    private ArrayList<MainListItem> toolBottomItemsList = new ArrayList<>();
    private ArrayList<MainListItem> favoriteItemsList = new ArrayList<>();
    private boolean isHideIconBranding = true;
    private boolean isRandomize = true;
    public List<CategoryAppList> categoryAppList = new ArrayList<>();
    private List<String> runningDownloadigFileList = new ArrayList<>();



    public List<CategoryAppList> getCategoryAppList(){
        return categoryAppList;
    }
    /**
     *
     * Retrieving the third party app usage time when siempo set as launcher.
     *
     * @return
     */
    public ArrayMap<String, Long> getThirdpartyAppLogasLauncher() {
        String storedHashMapString = PrefSiempo.getInstance(this).read(PrefSiempo.THIRD_PARTY_APP_LOG_AS_LAUNCHER, "");
        java.lang.reflect.Type type = new TypeToken<ArrayMap<String, Long>>() {
        }.getType();
        return new Gson().fromJson(storedHashMapString, type);
    }

    /**
     * Retrieving the third party app usage time when siempo not set as launcher.
     *
     * @return
     */
    public ArrayMap<String, Long> getThirdpartyAppLogasnotLauncher() {
        String storedHashMapString = PrefSiempo.getInstance(this).read(PrefSiempo.THIRD_PARTY_APP_LOG_NOT_AS_LAUNCHER, "");
        java.lang.reflect.Type type = new TypeToken<ArrayMap<String, Long>>() {
        }.getType();
        return new Gson().fromJson(storedHashMapString, type);
    }

    public static synchronized CoreApplication getInstance() {
        return sInstance;
    }

    public List<String> getRunningDownloadigFileList() {
        return runningDownloadigFileList;
    }

    public void setRunningDownloadigFileList(List<String> runningDownloadigFileList) {
        this.runningDownloadigFileList = runningDownloadigFileList;
    }

    public String getBase64EncodedPublicKey() {
        return base64EncodedPublicKey;
    }

    public boolean isHideIconBranding() {
        return isHideIconBranding;
    }

    public void setHideIconBranding(boolean hideIconBranding) {
        isHideIconBranding = hideIconBranding;
    }

    public boolean isRandomize() {
        return isRandomize;
    }

    public void setRandomize(boolean randomize) {
        this.isRandomize = randomize;
    }

    public ArrayList<String> getJunkFoodList() {
        return junkFoodList;
    }

    public void setJunkFoodList(ArrayList<String> junkFoodList) {
        this.junkFoodList = junkFoodList;
    }

    public ArrayList<MainListItem> getToolItemsList() {
        return toolItemsList;
    }

    public void setToolItemsList(ArrayList<MainListItem> toolItemsList) {
        this.toolItemsList = toolItemsList;
    }

    public ArrayList<MainListItem> getFavoriteItemsList() {
        return favoriteItemsList;
    }

    public void setFavoriteItemsList(ArrayList<MainListItem> favoriteItemsList) {
        this.favoriteItemsList = favoriteItemsList;
    }

    public ArrayList<MainListItem> getToolBottomItemsList() {
        return toolBottomItemsList;
    }

    public void setToolBottomItemsList(ArrayList<MainListItem> toolBottomItemsList) {
        this.toolBottomItemsList = toolBottomItemsList;
    }

    public ArrayMap<String, String> getListApplicationName() {
        return listApplicationName;
    }

    public void setListApplicationName(ArrayMap<String, String> listApplicationName) {
        this.listApplicationName = listApplicationName;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseAnalytics.getInstance(this);
        userManager = (UserManager) getSystemService(Context.USER_SERVICE);
        launcherApps = (LauncherApps) getSystemService(Context.LAUNCHER_APPS_SERVICE);
        sInstance = this;
        init();
        initMemoryCatch();
        getAllApplicationPackageName();
    }

    /**
     * This method is used for fetch all installed application package list.
     */
    public void getAllApplicationPackageName() {
        packagesList.clear();
        new LoadApplications().execute();
    }

    @SuppressLint("HardwareIds")
    public String getDeviceId() {
        String strDeviceId = "";
        try {
            strDeviceId = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            return strDeviceId;
        } catch (Exception e) {
            strDeviceId = android.provider.Settings.Secure.ANDROID_ID;
            return strDeviceId;
        }
    }

    protected void init() {
        // set initial configurations here
        configTracer();
//        configCalligraphy();
        configIconify();
        configureLifecycle();
        configureNetworking();
        if (PrefSiempo.getInstance(this).read(PrefSiempo.INSTALLED_APP_VERSION_CODE, 0) == 0) {
            PrefSiempo.getInstance(this).write(PrefSiempo.INSTALLED_APP_VERSION_CODE,
                    UIUtils.getCurrentVersionCode(this));
        }
        if (getToolsSettings() == null || getToolsSettings().isEmpty()) {
            configureToolsPane();
        } else {
            if (getToolsSettings().size() <= 16) {
                PrefSiempo.getInstance(this).write(PrefSiempo.SORTED_MENU, "");
                HashMap<Integer, AppMenu> oldMap;
                String storedHashMapString = PrefSiempo.getInstance(this).read(PrefSiempo.TOOLS_SETTING, "");
                java.lang.reflect.Type type = new TypeToken<HashMap<Integer, AppMenu>>() {
                }.getType();
                oldMap = new Gson().fromJson(storedHashMapString, type);
                PrefSiempo.getInstance(this).write(PrefSiempo.TOOLS_SETTING, "");
                configureToolsPane();
                HashMap<Integer, AppMenu> newMap = getToolsSettings();
                for (Map.Entry<Integer, AppMenu> newMenuEntry : newMap.entrySet()) {
                    if (oldMap.containsKey(newMenuEntry.getKey())) {
                        newMenuEntry.getValue().setApplicationName(oldMap.get(newMenuEntry.getKey()).getApplicationName());
                    }
                }
                String hashMapToolSettings = new Gson().toJson(newMap);
                PrefSiempo.getInstance(this).write(PrefSiempo.TOOLS_SETTING, hashMapToolSettings);
            }


        }
        setHideIconBranding(PrefSiempo.getInstance(sInstance).read(PrefSiempo.IS_ICON_BRANDING, true));
        setRandomize(PrefSiempo.getInstance(sInstance).read(PrefSiempo.IS_RANDOMIZE_JUNKFOOD, true));


//        /**
//         * Fetch Category App List
//         */
//        Intent intent = new Intent(this, CategoriesApp.class);
//        startService(intent);

    }

    /**
     * first time called when user launch the application to set the default value for the
     * application show/hide bind tools to specific package name.
     */
    private void configureToolsPane() {
        try {
            if (PrefSiempo.getInstance(this).read(PrefSiempo.TOOLS_SETTING, "").equalsIgnoreCase("")) {
                HashMap<Integer, AppMenu> map = new HashMap<>();
                //by default on install, the "Recorder", "Payment", and "Browser" tools are hidden
                // (they may be revealed via the tool-selection screen (see tool-selection below)).
                map.put(TOOLS_MAP, new AppMenu(true, false, CoreApplication.getInstance
                        ().getApplicationByCategory(1).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(1).get(0).activityInfo.packageName : ""));
                map.put(TOOLS_TRANSPORT, new AppMenu(true, false, CoreApplication.getInstance
                        ().getApplicationByCategory(2).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(2).get(0).activityInfo.packageName : ""));
                map.put(TOOLS_CALENDAR, new AppMenu(true, false, CoreApplication.getInstance
                        ().getApplicationByCategory(3).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(3).get(0).activityInfo.packageName : ""));
                map.put(TOOLS_WEATHER, new AppMenu(true, false, CoreApplication.getInstance
                        ().getApplicationByCategory(4).size() == 1 ?
                        CoreApplication.getInstance().getApplicationByCategory(4).get(0).activityInfo.packageName : ""));
                map.put(TOOLS_NOTES, new AppMenu(true, false, CoreApplication.getInstance
                        ().getApplicationByCategory(5).size() == 1 ?
                        getString(R.string.notes) : ""));
                map.put(TOOLS_RECORDER, new AppMenu(false, false, CoreApplication
                        .getInstance().getApplicationByCategory(6).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(6).get(0).activityInfo.packageName : ""));
                map.put(TOOLS_CAMERA, new AppMenu(true, false, CoreApplication.getInstance
                        ().getApplicationByCategory(7).size() == 1 ?
                        CoreApplication.getInstance().getApplicationByCategory(7).get(0).activityInfo.packageName : ""));
                map.put(TOOLS_PHOTOS, new AppMenu(true, false, CoreApplication.getInstance
                        ().getApplicationByCategory(8).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(8).get(0).activityInfo.packageName : ""));
                map.put(TOOLS_PAYMENT, new AppMenu(false, false, CoreApplication
                        .getInstance().getApplicationByCategory(9).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(9).get(0).activityInfo.packageName : ""));
                map.put(TOOLS_WELLNESS, new AppMenu(true, false, CoreApplication
                        .getInstance().getApplicationByCategory(10).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(10).get(0).activityInfo.packageName : ""));

                map.put(TOOLS_TODO, new AppMenu(false, false, CoreApplication
                        .getInstance().getApplicationByCategory(12).size() == 1 ?
                        CoreApplication.getInstance().getApplicationByCategory
                                (12).get(0).activityInfo.packageName : ""));
                map.put(TOOLS_BROWSER, new AppMenu(false, false, CoreApplication
                        .getInstance().getApplicationByCategory(11).size() == 1
                        ? CoreApplication.getInstance().getApplicationByCategory
                        (11).get(0).activityInfo.packageName : ""));
                map.put(TOOLS_MUSIC, new AppMenu(false, false, CoreApplication
                        .getInstance().getApplicationByCategory(17).size() == 1 ?
                        CoreApplication.getInstance().getApplicationByCategory
                                (17).get(0).activityInfo.packageName : ""));
                map.put(TOOLS_PODCAST, new AppMenu(false, false, CoreApplication
                        .getInstance().getApplicationByCategory(18).size() == 1 ?
                        CoreApplication.getInstance().getApplicationByCategory
                                (18).get(0).activityInfo.packageName : ""));

                map.put(TOOLS_FOOD, new AppMenu(false, false, CoreApplication
                        .getInstance().getApplicationByCategory(19).size() == 1 ?
                        CoreApplication.getInstance().getApplicationByCategory
                                (19).get(0).activityInfo.packageName : ""));

                map.put(TOOLS_FITNESS, new AppMenu(false, false, CoreApplication
                        .getInstance().getApplicationByCategory(20).size() == 1 ?
                        CoreApplication.getInstance().getApplicationByCategory
                                (20).get(0).activityInfo.packageName : ""));

                map.put(TOOLS_CALL, new AppMenu(true, true, CoreApplication.getInstance
                        ().getApplicationByCategory(13).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(13).get(0).activityInfo.packageName : ""));
                map.put(TOOLS_CLOCK, new AppMenu(true, true, CoreApplication.getInstance
                        ().getApplicationByCategory(14).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(14).get(0).activityInfo.packageName : ""));
                map.put(TOOLS_MESSAGE, new AppMenu(true, true, CoreApplication.getInstance
                        ().getApplicationByCategory(15).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(15).get(0).activityInfo.packageName : ""));
                map.put(TOOLS_EMAIL, new AppMenu(true, true, CoreApplication.getInstance
                        ().getApplicationByCategory(16).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(16).get(0).activityInfo.packageName : ""));

                map.put(TOOLS_CLOUD, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(21).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(21).get(0).activityInfo.packageName : ""));


                map.put(TOOLS_BOOKS, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(22).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(22).get(0).activityInfo.packageName : ""));

                map.put(TOOLS_AUTHENTICATION, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(23).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(23).get(0).activityInfo.packageName : ""));


                map.put(TOOLS_ASSISTANT, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(24).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(24).get(0).activityInfo.packageName : ""));


                map.put(TOOLS_ADDITIONAL_MESSAGE, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(25).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(25).get(0).activityInfo.packageName : ""));

                map.put(TOOLS_BANKING, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(26).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(26).get(0).activityInfo.packageName : ""));


                map.put(TOOLS_COURCE, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(27).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(27).get(0).activityInfo.packageName : ""));



                map.put(TOOLS_DOC, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(28).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(28).get(0).activityInfo.packageName : ""));




                map.put(TOOLS_FILES, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(29).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(29).get(0).activityInfo.packageName : ""));


                map.put(TOOLS_FLASH, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(30).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(30).get(0).activityInfo.packageName : ""));



                map.put(TOOLS_HEALTH, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(31).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(31).get(0).activityInfo.packageName : ""));



                map.put(TOOLS_JOURNAL, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(32).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(32).get(0).activityInfo.packageName : ""));



                map.put(TOOLS_LANGUAGES, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(33).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(33).get(0).activityInfo.packageName : ""));




                map.put(TOOLS_LEARNING, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(34).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(34).get(0).activityInfo.packageName : ""));



                map.put(TOOLS_MEDITATION, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(35).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(35).get(0).activityInfo.packageName : ""));



                map.put(TOOLS_MICROPHONE, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(36).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(36).get(0).activityInfo.packageName : ""));





                map.put(TOOLS_NEWS, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(37).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(37).get(0).activityInfo.packageName : ""));



                map.put(TOOLS_SEARCH, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(38).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(38).get(0).activityInfo.packageName : ""));




                map.put(TOOLS_SETTINGS, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(39).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(39).get(0).activityInfo.packageName : ""));




                map.put(TOOLS_VOICE, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(40).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(40).get(0).activityInfo.packageName : ""));


                map.put(TOOLS_SLEEP, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(41).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(41).get(0).activityInfo.packageName : ""));


                map.put(TOOLS_CALCULATOR, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(42).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(42).get(0).activityInfo.packageName : ""));


                map.put(TOOLS_TRANSLATE, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(43).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(43).get(0).activityInfo.packageName : ""));

                map.put(TOOLS_VIDEO, new AppMenu(false, false, CoreApplication.getInstance
                        ().getApplicationByCategory(44).size() == 1 ? CoreApplication.getInstance().getApplicationByCategory(44).get(0).activityInfo.packageName : ""));












                String hashMapToolSettings = new Gson().toJson(map);
                PrefSiempo.getInstance(this).write(PrefSiempo.TOOLS_SETTING, hashMapToolSettings);


            /*
              SSA-1321: Adding the mentioned apps in junk food by default
             */
                Set<String> junkfoodList = new HashSet<>();

                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> pkgAppsList = getPackageManager().queryIntentActivities(mainIntent, 0);


                for (ResolveInfo resolveInfo : pkgAppsList) {

                    String packageName = !TextUtils.isEmpty(resolveInfo.activityInfo
                            .packageName) ? resolveInfo.activityInfo
                            .packageName : "";
                    if (packageName.contains("com.facebook.katana") || packageName.contains("com.facebook.lite") || packageName
                            .contains("com.king")) {
                        if (UIUtils.isAppInstalledAndEnabled(getApplicationContext(), packageName)) {
                            junkfoodList.add(packageName);
                        }
                    } else {

                        switch (packageName) {
                            case Constants.SNAP_PACKAGE:
                            case Constants.INSTAGRAM_PACKAGE:
                            case Constants.LINKEDIN_PACKAGE:
                            case Constants.CLASH_ROYAL_PACKAGE:
                            case Constants.HINGE_PACKAGE:
                            case Constants.NETFLIX_PACKAGE:
                            case Constants.REDDIT_PACKAGE:
                            case Constants.TINDER_PACKAGE:
                            case Constants.GRINDR_PACKAGE:
                            case Constants.YOUTUBE_PACKAGE:
                            case Constants.COFFEE_MEETS_PACKAGE:
                            case Constants.TWITTER_PACKAGE:
                            case Constants.BUMBLE_PACKAGE:
                                if (UIUtils.isAppInstalledAndEnabled(getApplicationContext(), packageName)) {
                                    junkfoodList.add(packageName);
                                }
                                break;
                        }

                    }
                }


                PrefSiempo.getInstance(this).write(PrefSiempo.JUNKFOOD_APPS, junkfoodList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * HashMap to store tools settings data.
     *
     * @return
     */
    public HashMap<Integer, AppMenu> getToolsSettings() {
        String storedHashMapString = PrefSiempo.getInstance(this).read(PrefSiempo.TOOLS_SETTING, "");
        java.lang.reflect.Type type = new TypeToken<HashMap<Integer, AppMenu>>() {
        }.getType();
        return new Gson().fromJson(storedHashMapString, type);
    }

    private void configureNetworking() {
        AndroidNetworking.initialize(getApplicationContext());
    }

    private void configureLifecycle() {
        registerActivityLifecycleCallbacks(new LifecycleHandler());
    }

    private void configTracer() {
        Tracer.init();
    }

    private void configCalligraphy() {
//        CalligraphyConfig
//                .initDefault(new CalligraphyConfig.Builder()
//                        .setDefaultFontPath(getString(FontUtils.DEFAULT_FONT_PATH_RES))
//                        .setFontAttrId(R.attr.fontPath)
//                        .build());
    }

    public void logException(Throwable e) {
        try {

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void configIconify() {
        Iconify.with(new FontAwesomeModule());
    }

    public List<String> getPackagesList() {
        return new ArrayList<>(packagesList);
    }

    public void setPackagesList(Set<String> packagesList) {
        try {
            this.packagesList = packagesList;
            blockedApps = PrefSiempo.getInstance(this).read(PrefSiempo
                    .BLOCKED_APPLIST, new HashSet<String>());


            if (blockedApps != null && blockedApps.size() == 0) {
                blockedApps.addAll(packagesList);
                PrefSiempo.getInstance(this).write(PrefSiempo
                        .BLOCKED_APPLIST, blockedApps);
            }

        } catch (Exception e) {
            Tracer.d("Exception e ::" + e.toString());
        }
    }

    /**
     * Return the application name by providing it's package name.
     *
     * @param packageName
     * @return application name
     */
    public String getApplicationNameFromPackageName(String packageName) {
        String applicationname = null;
        try {
            if (packageName != null && !packageName.equalsIgnoreCase("")) {
                if (TextUtils.isEmpty(getListApplicationName().get(packageName))) {
                    PackageManager packageManager = getPackageManager();
                    ApplicationInfo applicationInfo = null;
                    try {
                        applicationInfo = packageManager.getApplicationInfo(packageName, 0);
                        applicationInfo.loadLabel(getPackageManager());
                    } catch (final PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (applicationInfo.loadLabel(packageManager) == null) {
                        applicationname = (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "");
                    } else {
                        applicationname = applicationInfo.loadLabel(packageManager).toString();
                    }
                } else {
                    applicationname = getListApplicationName().get(packageName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return applicationname;
    }

    /**
     * Return the application icon by providing it's package name.
     *
     * @param packagename
     * @return application name
     */
    public Drawable getApplicationIconFromPackageName(String packagename) {
        Drawable icon = null;
        try {
            icon = getPackageManager().getApplicationIcon(packagename);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return icon;
    }

    /**
     * Get List of installed application related to specific category.
     *
     * @param id
     * @return
     */
    public ArrayList<ResolveInfo> getApplicationByCategory(int id) {
        ArrayList<ResolveInfo> list = new ArrayList<>();
        HashSet<String> packageNames = new HashSet<>();
        ArrayList<ResolveInfo> listTemp = new ArrayList<ResolveInfo>(0);
        switch (id) {
            case TOOLS_MAP:// Map
                Double myLatitude = 44.433106;
                Double myLongitude = 26.103687;
                String labelLocation = "Jorgesys @ Bucharest";
                String urlAddress = "http://maps.google.com/maps?q=" + myLatitude + "," + myLongitude + "(" + labelLocation + ")&iwloc=A&hl=es";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress));
                list.addAll(getPackageManager().queryIntentActivities(intent, 0));
                packageNames = new HashSet<String>(0);
                listTemp = new ArrayList<ResolveInfo>(0);
                for (ResolveInfo resolveInfo : list) {
                    if (!packageNames.contains(resolveInfo.activityInfo.packageName)) {
                        packageNames.add(resolveInfo.activityInfo.packageName);
                        listTemp.add(resolveInfo);
                    }
                }
                list.clear();
                list.addAll(listTemp);
                break;
            case TOOLS_TRANSPORT:// Transport
                break;
            case TOOLS_CALENDAR://Calender
                Uri.Builder builder =
                        CalendarContract.CONTENT_URI.buildUpon();
                builder.appendPath("time");
                Intent calenderIntent =
                        new Intent(Intent.ACTION_VIEW, builder.build());
                list.addAll(getPackageManager().queryIntentActivities(calenderIntent, 0));
                packageNames = new HashSet<String>(0);
                listTemp = new ArrayList<ResolveInfo>(0);
                for (ResolveInfo resolveInfo : list) {
                    if (!packageNames.contains(resolveInfo.activityInfo.packageName)) {
                        packageNames.add(resolveInfo.activityInfo.packageName);
                        listTemp.add(resolveInfo);
                    }
                }
                list.clear();
                list.addAll(listTemp);
                break;
            case TOOLS_WEATHER://Weather
                break;
            case TOOLS_NOTES://Notes
                String filepath = "mnt/sdcard/doc.txt";
                File file = new File(filepath);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        CoreApplication.getInstance().logException(e);
                        e.printStackTrace();
                    }
                }

                Intent intentNotes = new Intent(Intent.ACTION_EDIT);
                intentNotes.setDataAndType(Uri.fromFile(file), "text/plain");
                list.add(null);
                list.addAll(getPackageManager().queryIntentActivities(intentNotes, 0));

                try {

                    if (UIUtils.isAppInstalledAndEnabled(this, "com.google.android.keep")) {
                        Intent keepIntent = new Intent();
                        keepIntent.setPackage("com.google.android.keep");
                        List<ResolveInfo> resolveInfo = getPackageManager().queryIntentActivities(keepIntent, 0);
                        if (resolveInfo != null && resolveInfo.size() > 0 && !list
                                .contains(resolveInfo.get(0))) {
                            list.add(resolveInfo.get(0));
                        }
                    }

                    if (UIUtils.isAppInstalledAndEnabled(this, "com.evernote")) {
                        Intent evernote = new Intent();
                        evernote.setPackage("com.evernote");
                        List<ResolveInfo> resolveInfoEverNote = getPackageManager()
                                .queryIntentActivities(evernote, 0);
                        if (resolveInfoEverNote != null && resolveInfoEverNote
                                .size() > 0 && !list
                                .contains(resolveInfoEverNote.get(0))) {
                            list.add(resolveInfoEverNote.get(0));
                        }
                    }

                    if (UIUtils.isAppInstalledAndEnabled(this, "com.microsoft.office.onenote")) {
                        Intent oneNote = new Intent();
                        oneNote.setPackage("com.microsoft.office.onenote");
                        List<ResolveInfo> resolveInfoOneNote = getPackageManager()
                                .queryIntentActivities(oneNote, 0);
                        if (resolveInfoOneNote != null && resolveInfoOneNote.size
                                () > 0 && !list.contains(resolveInfoOneNote.get
                                (0))) {
                            list.add(resolveInfoOneNote.get(0));
                        }
                    }
                    if (UIUtils.isAppInstalledAndEnabled(this, "com.automattic.simplenote")) {
                        Intent simpleNote = new Intent();
                        simpleNote.setPackage("com.automattic.simplenote");
                        List<ResolveInfo> resolveInfoSimpleNote =
                                getPackageManager()
                                        .queryIntentActivities(simpleNote, 0);
                        if (resolveInfoSimpleNote != null && resolveInfoSimpleNote.size
                                () > 0 && !list.contains(resolveInfoSimpleNote.get
                                (0))) {
                            list.add(resolveInfoSimpleNote.get(0));
                        }
                    }
                    if (UIUtils.isAppInstalledAndEnabled(this, "com.socialnmobile.dictapps.notepad.color.note")) {
                        Intent colorNote = new Intent();
                        colorNote.setPackage("com.socialnmobile.dictapps.notepad.color.note");
                        List<ResolveInfo> resolveInfoSimpleNote =
                                getPackageManager()
                                        .queryIntentActivities(colorNote, 0);
                        if (resolveInfoSimpleNote != null && resolveInfoSimpleNote.size
                                () > 0 && !list.contains(resolveInfoSimpleNote.get
                                (0))) {
                            list.add(resolveInfoSimpleNote.get(0));
                        }
                    }

                    if (UIUtils.isAppInstalledAndEnabled(this, "com.task.notes")) {
                        Intent colorNote = new Intent();
                        colorNote.setPackage("com.task.notes");
                        List<ResolveInfo> resolveInfoSimpleNote =
                                getPackageManager()
                                        .queryIntentActivities(colorNote, 0);
                        if (resolveInfoSimpleNote != null && resolveInfoSimpleNote.size
                                () > 0 && !list.contains(resolveInfoSimpleNote.get
                                (0))) {
                            list.add(resolveInfoSimpleNote.get(0));
                        }
                    }

                    if (UIUtils.isAppInstalledAndEnabled(this, "com.edi.masaki.mymemoapp")) {
                        Intent colorNote = new Intent();
                        colorNote.setPackage("com.edi.masaki.mymemoapp");
                        List<ResolveInfo> resolveInfoSimpleNote =
                                getPackageManager()
                                        .queryIntentActivities(colorNote, 0);
                        if (resolveInfoSimpleNote != null && resolveInfoSimpleNote.size
                                () > 0 && !list.contains(resolveInfoSimpleNote.get
                                (0))) {
                            list.add(resolveInfoSimpleNote.get(0));
                        }
                    }

                    if (UIUtils.isAppInstalledAndEnabled(this, "com.dencreak.esmemo")) {
                        Intent colorNote = new Intent();
                        colorNote.setPackage("com.dencreak.esmemo");
                        List<ResolveInfo> resolveInfoSimpleNote =
                                getPackageManager()
                                        .queryIntentActivities(colorNote, 0);
                        if (resolveInfoSimpleNote != null && resolveInfoSimpleNote.size
                                () > 0 && !list.contains(resolveInfoSimpleNote.get
                                (0))) {
                            list.add(resolveInfoSimpleNote.get(0));
                        }
                    }
                    if (UIUtils.isAppInstalledAndEnabled(this, "com.samsung.android.snote")) {
                        Intent colorNote = new Intent();
                        colorNote.setPackage("com.samsung.android.snote");
                        List<ResolveInfo> resolveInfoSimpleNote =
                                getPackageManager()
                                        .queryIntentActivities(colorNote, 0);
                        if (resolveInfoSimpleNote != null && resolveInfoSimpleNote.size
                                () > 0 && !list.contains(resolveInfoSimpleNote.get
                                (0))) {
                            list.add(resolveInfoSimpleNote.get(0));
                        }
                    }
                    if (UIUtils.isAppInstalledAndEnabled(this, "com.samsung.android.app.notes")) {
                        Intent colorNote = new Intent();
                        colorNote.setPackage("com.samsung.android.app.notes");
                        List<ResolveInfo> resolveInfoSimpleNote =
                                getPackageManager()
                                        .queryIntentActivities(colorNote, 0);
                        if (resolveInfoSimpleNote != null && resolveInfoSimpleNote.size
                                () > 0 && !list.contains(resolveInfoSimpleNote.get
                                (0))) {
                            list.add(resolveInfoSimpleNote.get(0));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                break;
            case TOOLS_RECORDER://Recorder
                break;
            case TOOLS_CAMERA://Camera
                Intent intentCamera = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                list.addAll(getPackageManager().queryIntentActivities(intentCamera, 0));
                packageNames = new HashSet<String>(0);
                listTemp = new ArrayList<ResolveInfo>(0);
                for (ResolveInfo resolveInfo : list) {
                    if (!packageNames.contains(resolveInfo.activityInfo.packageName)) {
                        packageNames.add(resolveInfo.activityInfo.packageName);
                        listTemp.add(resolveInfo);
                    }
                }
                list.clear();
                list.addAll(listTemp);
                break;
            case TOOLS_PHOTOS://Photos
                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/* video/*");
                list.addAll(getPackageManager().queryIntentActivities(pickIntent, 0));
                packageNames = new HashSet<String>(0);
                listTemp = new ArrayList<ResolveInfo>(0);
                for (ResolveInfo resolveInfo : list) {
                    if (!packageNames.contains(resolveInfo.activityInfo.packageName)) {
                        packageNames.add(resolveInfo.activityInfo.packageName);
                        listTemp.add(resolveInfo);
                    }
                }
                list.clear();
                list.addAll(listTemp);
                break;
            case TOOLS_PAYMENT://Payment
                break;
            case TOOLS_WELLNESS://Wellness
                break;
            case TOOLS_BROWSER://Browser
                Intent intentBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/"));
                list.addAll(getPackageManager().queryIntentActivities(intentBrowser, 0));
                packageNames = new HashSet<String>(0);
                listTemp = new ArrayList<ResolveInfo>(0);
                for (ResolveInfo resolveInfo : list) {
                    if (!packageNames.contains(resolveInfo.activityInfo.packageName)) {
                        packageNames.add(resolveInfo.activityInfo.packageName);
                        listTemp.add(resolveInfo);
                    }
                }
                list.clear();
                list.addAll(listTemp);
                break;

            case TOOLS_CALL://Call
                Uri number = Uri.parse("tel:");
                Intent dial = new Intent(Intent.ACTION_DIAL, number);
                list.addAll(getPackageManager().queryIntentActivities(dial, 0));
                packageNames = new HashSet<String>(0);
                listTemp = new ArrayList<ResolveInfo>(0);
                for (ResolveInfo resolveInfo : list) {
                    if (!packageNames.contains(resolveInfo.activityInfo.packageName)) {
                        packageNames.add(resolveInfo.activityInfo.packageName);
                        listTemp.add(resolveInfo);
                    }
                }
                list.clear();
                list.addAll(listTemp);
                break;
            case TOOLS_CLOCK://Clock
                Intent intentClock = new Intent(AlarmClock.ACTION_SET_ALARM);
                list.addAll(getPackageManager().queryIntentActivities(intentClock, 0));
                packageNames = new HashSet<String>(0);
                listTemp = new ArrayList<ResolveInfo>(0);
                for (ResolveInfo resolveInfo : list) {
                    if (!packageNames.contains(resolveInfo.activityInfo.packageName)) {
                        packageNames.add(resolveInfo.activityInfo.packageName);
                        listTemp.add(resolveInfo);
                    }
                }
                list.clear();
                list.addAll(listTemp);
                break;
            case TOOLS_MESSAGE://message
                Intent message = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + ""));
                message.putExtra("sms_body", "Test text...");
                list.addAll(getPackageManager().queryIntentActivities(message, 0));

                try {
                    if (UIUtils.isAppInstalledAndEnabled(this, Constants
                            .WHATSAPP_PACKAGE)) {
                        Intent keepIntent = new Intent();
                        keepIntent.setPackage(Constants
                                .WHATSAPP_PACKAGE);
                        List<ResolveInfo> resolveInfo = getPackageManager().queryIntentActivities(keepIntent, 0);
                        if (resolveInfo != null && resolveInfo.size() > 0 && !list
                                .contains(resolveInfo.get(0))) {
                            list.add(resolveInfo.get(0));
                        }
                    }

                    if (UIUtils.isAppInstalledAndEnabled(this, Constants
                            .LINE_PACKAGE)) {
                        Intent evernote = new Intent();
                        evernote.setPackage(Constants
                                .LINE_PACKAGE);
                        List<ResolveInfo> resolveInfoEverNote = getPackageManager()
                                .queryIntentActivities(evernote, 0);
                        if (resolveInfoEverNote != null && resolveInfoEverNote
                                .size() > 0 && !list
                                .contains(resolveInfoEverNote.get(0))) {
                            list.add(resolveInfoEverNote.get(0));
                        }
                    }

                    if (UIUtils.isAppInstalledAndEnabled(this, Constants
                            .VIBER_PACKAGE)) {
                        Intent evernote = new Intent();
                        evernote.setPackage(Constants
                                .VIBER_PACKAGE);
                        List<ResolveInfo> resolveInfoEverNote = getPackageManager()
                                .queryIntentActivities(evernote, 0);
                        if (resolveInfoEverNote != null && resolveInfoEverNote
                                .size() > 0 && !list
                                .contains(resolveInfoEverNote.get(0))) {
                            list.add(resolveInfoEverNote.get(0));
                        }
                    }

                    if (UIUtils.isAppInstalledAndEnabled(this, Constants
                            .SKYPE_PACKAGE)) {
                        Intent evernote = new Intent();
                        evernote.setPackage(Constants
                                .SKYPE_PACKAGE);
                        List<ResolveInfo> resolveInfoEverNote = getPackageManager()
                                .queryIntentActivities(evernote, 0);
                        if (resolveInfoEverNote != null && resolveInfoEverNote
                                .size() > 0 && !list
                                .contains(resolveInfoEverNote.get(0))) {
                            list.add(resolveInfoEverNote.get(0));
                        }
                    }
                    if (UIUtils.isAppInstalledAndEnabled(this, Constants
                            .WECHAT_PACKAGE)) {
                        Intent evernote = new Intent();
                        evernote.setPackage(Constants
                                .WECHAT_PACKAGE);
                        List<ResolveInfo> resolveInfoEverNote = getPackageManager()
                                .queryIntentActivities(evernote, 0);
                        if (resolveInfoEverNote != null && resolveInfoEverNote
                                .size() > 0 && !list
                                .contains(resolveInfoEverNote.get(0))) {
                            list.add(resolveInfoEverNote.get(0));
                        }
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
                packageNames = new HashSet<String>(0);
                listTemp = new ArrayList<ResolveInfo>(0);
                for (ResolveInfo resolveInfo : list) {
                    if (!packageNames.contains(resolveInfo.activityInfo.packageName)) {
                        packageNames.add(resolveInfo.activityInfo.packageName);
                        listTemp.add(resolveInfo);
                    }
                }
                list.clear();
                list.addAll(listTemp);

                break;
            case TOOLS_EMAIL://email
                Intent intentEmail = new Intent(Intent.ACTION_VIEW);
                Uri data = Uri.parse("mailto:recipient@example.com?subject=" + "" + "&body=" + "");
                intentEmail.setData(data);
                list.addAll(getPackageManager().queryIntentActivities(intentEmail, 0));
                packageNames = new HashSet<String>(0);
                listTemp = new ArrayList<ResolveInfo>(0);
                for (ResolveInfo resolveInfo : list) {
                    if (!packageNames.contains(resolveInfo.activityInfo.packageName)) {
                        packageNames.add(resolveInfo.activityInfo.packageName);
                        listTemp.add(resolveInfo);
                    }
                }
                list.clear();
                list.addAll(listTemp);
                break;

            case TOOLS_MUSIC:// Music
                Intent intentMusic = new Intent(MediaStore
                        .INTENT_ACTION_MUSIC_PLAYER);
                list.addAll(getPackageManager().queryIntentActivities(intentMusic, 0));
                packageNames = new HashSet<String>(0);
                listTemp = new ArrayList<ResolveInfo>(0);
                for (ResolveInfo resolveInfo : list) {
                    if (!packageNames.contains(resolveInfo.activityInfo.packageName)) {
                        packageNames.add(resolveInfo.activityInfo.packageName);
                        listTemp.add(resolveInfo);
                    }
                }
                list.clear();
                list.addAll(listTemp);
                break;

            case TOOLS_PODCAST://PODCAST
                break;

            case TOOLS_FOOD://Food
                break;

            case TOOLS_FITNESS://Fitness
                break;

            case TOOLS_TODO://
                break;

            case TOOLS_CLOUD://Recorder
                break;

            case TOOLS_BOOKS://Recorder
                break;

            case TOOLS_AUTHENTICATION://Recorder
                break;

            case TOOLS_ASSISTANT://Recorder
                break;

            case TOOLS_ADDITIONAL_MESSAGE://Recorder
                break;

            case TOOLS_BANKING://Recorder
                break;

            case TOOLS_COURCE://Recorder
                break;

            case TOOLS_DOC://Recorder
                break;

            case TOOLS_FILES://Recorder
                break;

            case TOOLS_FLASH://Recorder
                break;

            case TOOLS_HEALTH://Recorder
                break;

            case TOOLS_JOURNAL://Recorder
                break;

            case TOOLS_LANGUAGES://Recorder
                break;


            case TOOLS_LEARNING://Recorder
                break;

            case TOOLS_MEDITATION://Recorder
                break;

            case TOOLS_MICROPHONE://Recorder
                break;

            case TOOLS_NEWS://Recorder
                break;

            case TOOLS_SEARCH://Recorder
                break;

            case TOOLS_SETTINGS://Recorder
                break;

            case TOOLS_VOICE://Recorder
                break;

            case TOOLS_SLEEP://Recorder
                break;

            case TOOLS_CALCULATOR://Recorder
                break;

            case TOOLS_TRANSLATE://Recorder
                break;

            case TOOLS_VIDEO://Recorder
                break;

            default:
                break;
        }
        return list;
    }

    public void addOrRemoveApplicationInfo(boolean addingOrDelete, String
            packageName) {
        try {
            if (addingOrDelete) {
                ApplicationInfo appInfo = getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                if (!packagesList.contains(appInfo.packageName)) {
                    packagesList.add(appInfo.packageName);
                    getListApplicationName().put(packageName, "" + getPackageManager().getApplicationLabel(appInfo));
                    EventBus.getDefault().post(new AppInstalledEvent(true));
                }

            } else {
                if (packagesList.contains(packageName)) {
                    packagesList.remove(packageName);
                    getListApplicationName().remove(packageName);
                    EventBus.getDefault().post(new AppInstalledEvent(true));
                }
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initMemoryCatch() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 4;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void includeTaskPool(AsyncTask asyncTask, Object object) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, object);
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public synchronized void downloadSiempoImages() {
        try {
            File folderSiempoImage = new File(Environment.getExternalStorageDirectory() +
                    "/Siempo images");
            if (!folderSiempoImage.exists()) {
                folderSiempoImage.mkdirs();
            }
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context
                    .CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = null;
            if (connectivityManager != null) {
                activeNetwork = connectivityManager.getActiveNetworkInfo();
            }
            if (activeNetwork != null) {
                if (folderSiempoImage != null && folderSiempoImage.list() != null) {
                    ArrayList<String> listImageName = new ArrayList<>(Arrays.asList(folderSiempoImage.list()));
                    String[] list = getResources().getStringArray(R.array.siempo_images);
                    DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    for (String strUrl : list) {
                        String fileName = strUrl.substring(strUrl.lastIndexOf('/') + 1, strUrl.length());
                        if (listImageName.contains(fileName)) {
                            Log.d("File Exists", fileName);
                        } else {
                            Uri download_Uri = Uri.parse(strUrl);
                            if (runningDownloadigFileList == null) {
                                runningDownloadigFileList = new ArrayList<>();
                            }
                            if (download_Uri != null && !getRunningDownloadigFileList().contains
                                    (fileName)) {
                                getRunningDownloadigFileList().add(fileName);
                                DownloadManager.Request request = new DownloadManager.Request(download_Uri);
                                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                                request.setAllowedOverRoaming(false);
                                request.setTitle(fileName);
                                request.setDescription(fileName);
                                request.setVisibleInDownloadsUi(false);
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                                request.setDestinationInExternalPublicDir("/Siempo images", fileName);
                                if (downloadManager != null) {
                                    long refid = downloadManager.enqueue(request);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class LoadApplications extends AsyncTask<Object, Object, Set<String>> {

        @Override
        protected Set<String> doInBackground(Object... params) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> pkgAppsList = getPackageManager().queryIntentActivities(mainIntent, 0);
            Set<String> applist = new HashSet<>();
            for (ResolveInfo appInfo : pkgAppsList) {
                try {
                    String packageName = appInfo.activityInfo.packageName;
                    if (!packageName.equalsIgnoreCase(getPackageName())) {
                        Drawable drawable = null;
                        try {
                            drawable = appInfo.loadIcon
                                    (getPackageManager());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (drawable != null) {
                            Bitmap bitmap = PackageUtil.drawableToBitmap(drawable);
                            addBitmapToMemoryCache(packageName, bitmap);
                        }
                        applist.add(packageName);

                        PackageManager packageManager = getPackageManager();
                        String applicationName = appInfo.loadLabel(packageManager).toString();
                        if (applicationName == null) {
                            ApplicationInfo applicationInfo = null;
                            try {
                                applicationInfo = packageManager.getApplicationInfo(packageName, 0);
                            } catch (final PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                            String applicationNameTemp = (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "");
                            getListApplicationName().put(packageName, applicationNameTemp);
                        } else {
                            getListApplicationName().put(packageName, applicationName);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            return applist;
        }


        @Override
        protected void onPostExecute(Set<String> applicationInfos) {
            super.onPostExecute(applicationInfos);
            setPackagesList(applicationInfos);
            EventBus.getDefault().post(new AppInstalledEvent(true));
        }
    }
}
