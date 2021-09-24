package io.focuslauncher.phone.app

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.os.UserManager
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.ArrayMap
import android.util.Log
import android.util.LruCache
import androidx.multidex.MultiDexApplication
import com.androidnetworking.AndroidNetworking
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.joanzapata.iconify.Iconify
import com.joanzapata.iconify.fonts.FontAwesomeModule
import de.greenrobot.event.EventBus
import io.focuslauncher.R
import io.focuslauncher.phone.event.AppInstalledEvent
import io.focuslauncher.phone.log.Tracer
import io.focuslauncher.phone.main.MainListItemLoader
import io.focuslauncher.phone.models.AppMenu
import io.focuslauncher.phone.models.CategoryAppList
import io.focuslauncher.phone.models.MainListItem
import io.focuslauncher.phone.utils.LifecycleHandler
import io.focuslauncher.phone.utils.PackageUtil
import io.focuslauncher.phone.utils.PrefSiempo
import io.focuslauncher.phone.utils.UIUtils
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Each application should contain an [Application] class instance
 * All applications of this project should extend their own application from this class
 * This will be first class where we can initialize all necessary first time configurations
 *
 *
 * Created by shahab on 3/17/16.
 */
abstract class CoreApplication : MultiDexApplication() {
    var userManager: UserManager? = null
    var launcherApps: LauncherApps? = null
    var base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1WmJ9sNAoO5o5QGJkZXfqLm8Py95ASb7XCY1NewZF7puJcWMGlv269AY2lqJuR0o/dzMnzo20D259NHPN6zF3TCsXcF8+jhRH5gAqKcNJCoc1p0tZ+rxZ5ETVYjR/OQ90MKStXa8MsArhfL+R6E27IuUELObkjS3XIwcjBj7EhBNVPv2ipj8t7w3bNorql8qPEHhgbc/v54krCMSEF1p82nIbZSvOFcJwLGg/wzmv6YfgsLD5fndoaNPiRLQ1nkWNASOryvgUDZAKqYjAtHY7WAV57FtQGgsViPTE4exzCp9t018GEeI5tbo4+RSw23nygSqmNBZkxv9Ee4jxpw7CQIDAQAB"
    var listApplicationName = ArrayMap<String, String>()
    private var packagesList: MutableSet<String> = HashSet()
    private val disableNotificationApps = ArrayList<String>()
    private var blockedApps: MutableSet<String>? = HashSet()
    private var mMemoryCache: LruCache<String?, Bitmap>? = null
    var junkFoodList = ArrayList<String>()
    var toolItemsList = ArrayList<MainListItem>()
    var toolBottomItemsList = ArrayList<MainListItem>()
    var favoriteItemsList = ArrayList<MainListItem>()
    var isHideIconBranding = true
    var isRandomize = true
    @JvmField
    var categoryAppList: List<CategoryAppList> = ArrayList()
    var runningDownloadigFileList: MutableList<String>? = ArrayList()

    /**
     *
     * Retrieving the third party app usage time when siempo set as launcher.
     *
     * @return
     */
    val thirdpartyAppLogasLauncher: ArrayMap<String, Long>
        get() {
            val storedHashMapString = PrefSiempo.getInstance(this).read(PrefSiempo.THIRD_PARTY_APP_LOG_AS_LAUNCHER, "")
            val type = object : TypeToken<ArrayMap<String?, Long?>?>() {}.type
            return Gson().fromJson(storedHashMapString, type)
        }

    /**
     * Retrieving the third party app usage time when siempo not set as launcher.
     *
     * @return
     */
    val thirdpartyAppLogasnotLauncher: ArrayMap<String, Long>
        get() {
            val storedHashMapString = PrefSiempo.getInstance(this).read(PrefSiempo.THIRD_PARTY_APP_LOG_NOT_AS_LAUNCHER, "")
            val type = object : TypeToken<ArrayMap<String?, Long?>?>() {}.type
            return Gson().fromJson(storedHashMapString, type)
        }

    override fun onCreate() {
        super.onCreate()
        FirebaseAnalytics.getInstance(this)
        userManager = getSystemService(USER_SERVICE) as UserManager
        launcherApps = getSystemService(LAUNCHER_APPS_SERVICE) as LauncherApps
        instance = this
        init()
        initMemoryCatch()
        allApplicationPackageName
    }

    /**
     * This method is used for fetch all installed application package list.
     */
    val allApplicationPackageName: Unit
        get() {
            packagesList.clear()
            LoadApplications().execute()
        }

    @get:SuppressLint("HardwareIds")
    val deviceId: String
        get() {
            var strDeviceId = ""
            return try {
                strDeviceId = Settings.Secure.getString(contentResolver,
                        Settings.Secure.ANDROID_ID)
                strDeviceId
            } catch (e: Exception) {
                strDeviceId = Settings.Secure.ANDROID_ID
                strDeviceId
            }
        }

    protected fun init() {
        // set initial configurations here
        configTracer()
        //        configCalligraphy();
        configIconify()
        configureLifecycle()
        configureNetworking()
        if (PrefSiempo.getInstance(this).read(PrefSiempo.INSTALLED_APP_VERSION_CODE, 0) == 0) {
            PrefSiempo.getInstance(this).write(PrefSiempo.INSTALLED_APP_VERSION_CODE,
                    UIUtils.getCurrentVersionCode(this))
        }
        if (toolsSettings == null || toolsSettings!!.isEmpty()) {
            configureToolsPane()
        } else {
            if (toolsSettings!!.size <= 16) {
                PrefSiempo.getInstance(this).write(PrefSiempo.SORTED_MENU, "")
                val oldMap: HashMap<Int, AppMenu>
                val storedHashMapString = PrefSiempo.getInstance(this).read(PrefSiempo.TOOLS_SETTING, "")
                val type = object : TypeToken<HashMap<Int?, AppMenu?>?>() {}.type
                oldMap = Gson().fromJson(storedHashMapString, type)
                PrefSiempo.getInstance(this).write(PrefSiempo.TOOLS_SETTING, "")
                configureToolsPane()
                val newMap = toolsSettings
                for ((key, value) in newMap!!) {
                    if (oldMap.containsKey(key)) {
                        value.applicationName = oldMap[key]!!.applicationName
                    }
                }
                val hashMapToolSettings = Gson().toJson(newMap)
                PrefSiempo.getInstance(this).write(PrefSiempo.TOOLS_SETTING, hashMapToolSettings)
            }
        }
        isHideIconBranding = PrefSiempo.getInstance(instance).read(PrefSiempo.IS_ICON_BRANDING, true)
        isRandomize = PrefSiempo.getInstance(instance).read(PrefSiempo.IS_RANDOMIZE_JUNKFOOD, true)


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
    private fun configureToolsPane() {
        try {
            if (PrefSiempo.getInstance(this).read(PrefSiempo.TOOLS_SETTING, "").equals("", ignoreCase = true)) {
                val map = HashMap<Int, AppMenu>()
                //by default on install, the "Recorder", "Payment", and "Browser" tools are hidden
                // (they may be revealed via the tool-selection screen (see tool-selection below)).
                map[MainListItemLoader.TOOLS_MAP] = AppMenu(true, false, if (instance!!.getApplicationByCategory(1).size == 1) instance!!.getApplicationByCategory(1)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_TRANSPORT] = AppMenu(true, false, if (instance!!.getApplicationByCategory(2).size == 1) instance!!.getApplicationByCategory(2)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_CALENDAR] = AppMenu(true, false, if (instance!!.getApplicationByCategory(3).size == 1) instance!!.getApplicationByCategory(3)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_WEATHER] = AppMenu(true, false, if (instance!!.getApplicationByCategory(4).size == 1) instance!!.getApplicationByCategory(4)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_NOTES] = AppMenu(true, false, if (instance!!.getApplicationByCategory(5).size == 1) getString(R.string.notes) else "")
                map[MainListItemLoader.TOOLS_RECORDER] = AppMenu(false, false, if (instance!!.getApplicationByCategory(6).size == 1) instance!!.getApplicationByCategory(6)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_CAMERA] = AppMenu(true, false, if (instance!!.getApplicationByCategory(7).size == 1) instance!!.getApplicationByCategory(7)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_PHOTOS] = AppMenu(true, false, if (instance!!.getApplicationByCategory(8).size == 1) instance!!.getApplicationByCategory(8)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_PAYMENT] = AppMenu(false, false, if (instance!!.getApplicationByCategory(9).size == 1) instance!!.getApplicationByCategory(9)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_WELLNESS] = AppMenu(true, false, if (instance!!.getApplicationByCategory(10).size == 1) instance!!.getApplicationByCategory(10)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_TODO] = AppMenu(false, false, if (instance!!.getApplicationByCategory(12).size == 1) instance!!.getApplicationByCategory(12)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_BROWSER] = AppMenu(false, false, if (instance!!.getApplicationByCategory(11).size == 1) instance!!.getApplicationByCategory(11)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_MUSIC] = AppMenu(false, false, if (instance!!.getApplicationByCategory(17).size == 1) instance!!.getApplicationByCategory(17)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_PODCAST] = AppMenu(false, false, if (instance!!.getApplicationByCategory(18).size == 1) instance!!.getApplicationByCategory(18)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_FOOD] = AppMenu(false, false, if (instance!!.getApplicationByCategory(19).size == 1) instance!!.getApplicationByCategory(19)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_FITNESS] = AppMenu(false, false, if (instance!!.getApplicationByCategory(20).size == 1) instance!!.getApplicationByCategory(20)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_CALL] = AppMenu(true, true, if (instance!!.getApplicationByCategory(13).size == 1) instance!!.getApplicationByCategory(13)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_CLOCK] = AppMenu(true, true, if (instance!!.getApplicationByCategory(14).size == 1) instance!!.getApplicationByCategory(14)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_MESSAGE] = AppMenu(true, true, if (instance!!.getApplicationByCategory(15).size == 1) instance!!.getApplicationByCategory(15)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_EMAIL] = AppMenu(true, true, if (instance!!.getApplicationByCategory(16).size == 1) instance!!.getApplicationByCategory(16)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_CLOUD] = AppMenu(false, false, if (instance!!.getApplicationByCategory(21).size == 1) instance!!.getApplicationByCategory(21)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_BOOKS] = AppMenu(false, false, if (instance!!.getApplicationByCategory(22).size == 1) instance!!.getApplicationByCategory(22)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_AUTHENTICATION] = AppMenu(false, false, if (instance!!.getApplicationByCategory(23).size == 1) instance!!.getApplicationByCategory(23)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_ASSISTANT] = AppMenu(false, false, if (instance!!.getApplicationByCategory(24).size == 1) instance!!.getApplicationByCategory(24)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_ADDITIONAL_MESSAGE] = AppMenu(false, false, if (instance!!.getApplicationByCategory(25).size == 1) instance!!.getApplicationByCategory(25)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_BANKING] = AppMenu(false, false, if (instance!!.getApplicationByCategory(26).size == 1) instance!!.getApplicationByCategory(26)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_COURCE] = AppMenu(false, false, if (instance!!.getApplicationByCategory(27).size == 1) instance!!.getApplicationByCategory(27)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_DOC] = AppMenu(false, false, if (instance!!.getApplicationByCategory(28).size == 1) instance!!.getApplicationByCategory(28)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_FILES] = AppMenu(false, false, if (instance!!.getApplicationByCategory(29).size == 1) instance!!.getApplicationByCategory(29)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_FLASH] = AppMenu(false, false, if (instance!!.getApplicationByCategory(30).size == 1) instance!!.getApplicationByCategory(30)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_HEALTH] = AppMenu(false, false, if (instance!!.getApplicationByCategory(31).size == 1) instance!!.getApplicationByCategory(31)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_JOURNAL] = AppMenu(false, false, if (instance!!.getApplicationByCategory(32).size == 1) instance!!.getApplicationByCategory(32)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_LANGUAGES] = AppMenu(false, false, if (instance!!.getApplicationByCategory(33).size == 1) instance!!.getApplicationByCategory(33)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_LEARNING] = AppMenu(false, false, if (instance!!.getApplicationByCategory(34).size == 1) instance!!.getApplicationByCategory(34)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_MEDITATION] = AppMenu(false, false, if (instance!!.getApplicationByCategory(35).size == 1) instance!!.getApplicationByCategory(35)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_MICROPHONE] = AppMenu(false, false, if (instance!!.getApplicationByCategory(36).size == 1) instance!!.getApplicationByCategory(36)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_NEWS] = AppMenu(false, false, if (instance!!.getApplicationByCategory(37).size == 1) instance!!.getApplicationByCategory(37)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_SEARCH] = AppMenu(false, false, if (instance!!.getApplicationByCategory(38).size == 1) instance!!.getApplicationByCategory(38)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_SETTINGS] = AppMenu(false, false, if (instance!!.getApplicationByCategory(39).size == 1) instance!!.getApplicationByCategory(39)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_VOICE] = AppMenu(false, false, if (instance!!.getApplicationByCategory(40).size == 1) instance!!.getApplicationByCategory(40)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_SLEEP] = AppMenu(false, false, if (instance!!.getApplicationByCategory(41).size == 1) instance!!.getApplicationByCategory(41)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_CALCULATOR] = AppMenu(false, false, if (instance!!.getApplicationByCategory(42).size == 1) instance!!.getApplicationByCategory(42)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_TRANSLATE] = AppMenu(false, false, if (instance!!.getApplicationByCategory(43).size == 1) instance!!.getApplicationByCategory(43)[0]!!.activityInfo.packageName else "")
                map[MainListItemLoader.TOOLS_VIDEO] = AppMenu(false, false, if (instance!!.getApplicationByCategory(44).size == 1) instance!!.getApplicationByCategory(44)[0]!!.activityInfo.packageName else "")
                val hashMapToolSettings = Gson().toJson(map)
                PrefSiempo.getInstance(this).write(PrefSiempo.TOOLS_SETTING, hashMapToolSettings)


                /*
              SSA-1321: Adding the mentioned apps in junk food by default
             */
                val junkfoodList: MutableSet<String> = HashSet()
                val mainIntent = Intent(Intent.ACTION_MAIN, null)
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
                val pkgAppsList = packageManager.queryIntentActivities(mainIntent, 0)
                for (resolveInfo in pkgAppsList) {
                    val packageName = if (!TextUtils.isEmpty(resolveInfo.activityInfo.packageName)) resolveInfo.activityInfo.packageName else ""
                    if (packageName.contains("com.facebook.katana") || packageName.contains("com.facebook.lite") || packageName
                                    .contains("com.king")) {
                        if (UIUtils.isAppInstalledAndEnabled(applicationContext, packageName)) {
                            junkfoodList.add(packageName)
                        }
                    } else {
                        when (packageName) {
                            Constants.SNAP_PACKAGE, Constants.INSTAGRAM_PACKAGE, Constants.LINKEDIN_PACKAGE, Constants.CLASH_ROYAL_PACKAGE, Constants.HINGE_PACKAGE, Constants.NETFLIX_PACKAGE, Constants.REDDIT_PACKAGE, Constants.TINDER_PACKAGE, Constants.GRINDR_PACKAGE, Constants.YOUTUBE_PACKAGE, Constants.COFFEE_MEETS_PACKAGE, Constants.TWITTER_PACKAGE, Constants.BUMBLE_PACKAGE -> if (UIUtils.isAppInstalledAndEnabled(applicationContext, packageName)) {
                                junkfoodList.add(packageName)
                            }
                        }
                    }
                }
                PrefSiempo.getInstance(this).write(PrefSiempo.JUNKFOOD_APPS, junkfoodList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * HashMap to store tools settings data.
     *
     * @return
     */
    val toolsSettings: HashMap<Int, AppMenu>?
        get() {
            val storedHashMapString = PrefSiempo.getInstance(this).read(PrefSiempo.TOOLS_SETTING, "")
            val type = object : TypeToken<HashMap<Int?, AppMenu?>?>() {}.type
            return Gson().fromJson(storedHashMapString, type)
        }

    private fun configureNetworking() {
        AndroidNetworking.initialize(applicationContext)
    }

    private fun configureLifecycle() {
        registerActivityLifecycleCallbacks(LifecycleHandler())
    }

    private fun configTracer() {
        Tracer.init()
    }

    private fun configCalligraphy() {
//        CalligraphyConfig
//                .initDefault(new CalligraphyConfig.Builder()
//                        .setDefaultFontPath(getString(FontUtils.DEFAULT_FONT_PATH_RES))
//                        .setFontAttrId(R.attr.fontPath)
//                        .build());
    }

    fun logException(e: Throwable?) {
        try {
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun configIconify() {
        Iconify.with(FontAwesomeModule())
    }

    fun getPackagesList(): List<String> {
        return ArrayList(packagesList)
    }

    fun setPackagesList(packagesList: MutableSet<String>) {
        try {
            this.packagesList = packagesList
            blockedApps = PrefSiempo.getInstance(this).read(PrefSiempo.BLOCKED_APPLIST, HashSet())
            if (blockedApps != null && blockedApps!!.size == 0) {
                blockedApps!!.addAll(packagesList)
                PrefSiempo.getInstance(this).write(PrefSiempo.BLOCKED_APPLIST, blockedApps)
            }
        } catch (e: Exception) {
            Tracer.d("Exception e ::$e")
        }
    }

    /**
     * Return the application name by providing it's package name.
     *
     * @param packageName
     * @return application name
     */
    fun getApplicationNameFromPackageName(packageName: String?): String? {
        var applicationname: String? = null
        try {
            if (packageName != null && !packageName.equals("", ignoreCase = true)) {
                if (TextUtils.isEmpty(listApplicationName[packageName])) {
                    val packageManager = packageManager
                    var applicationInfo: ApplicationInfo? = null
                    try {
                        applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                        applicationInfo.loadLabel(getPackageManager())
                    } catch (e: PackageManager.NameNotFoundException) {
                        e.printStackTrace()
                    }
                    applicationname = if (applicationInfo!!.loadLabel(packageManager) == null) {
                        (if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "") as String
                    } else {
                        applicationInfo.loadLabel(packageManager).toString()
                    }
                } else {
                    applicationname = listApplicationName[packageName]
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return applicationname
    }

    /**
     * Return the application icon by providing it's package name.
     *
     * @param packagename
     * @return application name
     */
    fun getApplicationIconFromPackageName(packagename: String?): Drawable? {
        var icon: Drawable? = null
        try {
            icon = packageManager.getApplicationIcon(packagename!!)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return icon
    }

    /**
     * Get List of installed application related to specific category.
     *
     * @param id
     * @return
     */
    fun getApplicationByCategory(id: Int): ArrayList<ResolveInfo?> {
        val list = ArrayList<ResolveInfo?>()
        var packageNames = HashSet<String?>()
        var listTemp = ArrayList<ResolveInfo?>(0)
        when (id) {
            MainListItemLoader.TOOLS_MAP -> {
                val myLatitude = 44.433106
                val myLongitude = 26.103687
                val labelLocation = "Jorgesys @ Bucharest"
                val urlAddress = "http://maps.google.com/maps?q=$myLatitude,$myLongitude($labelLocation)&iwloc=A&hl=es"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress))
                list.addAll(packageManager.queryIntentActivities(intent, 0))
                packageNames = HashSet(0)
                listTemp = ArrayList(0)
                for (resolveInfo in list) {
                    if (!packageNames.contains(resolveInfo!!.activityInfo.packageName)) {
                        packageNames.add(resolveInfo.activityInfo.packageName)
                        listTemp.add(resolveInfo)
                    }
                }
                list.clear()
                list.addAll(listTemp)
            }
            MainListItemLoader.TOOLS_TRANSPORT -> {
            }
            MainListItemLoader.TOOLS_CALENDAR -> {
                val builder = CalendarContract.CONTENT_URI.buildUpon()
                builder.appendPath("time")
                val calenderIntent = Intent(Intent.ACTION_VIEW, builder.build())
                list.addAll(packageManager.queryIntentActivities(calenderIntent, 0))
                packageNames = HashSet(0)
                listTemp = ArrayList(0)
                for (resolveInfo in list) {
                    if (!packageNames.contains(resolveInfo!!.activityInfo.packageName)) {
                        packageNames.add(resolveInfo.activityInfo.packageName)
                        listTemp.add(resolveInfo)
                    }
                }
                list.clear()
                list.addAll(listTemp)
            }
            MainListItemLoader.TOOLS_WEATHER -> {
            }
            MainListItemLoader.TOOLS_NOTES -> {
                val filepath = "mnt/sdcard/doc.txt"
                val file = File(filepath)
                if (!file.exists()) {
                    try {
                        file.createNewFile()
                    } catch (e: IOException) {
                        instance!!.logException(e)
                        e.printStackTrace()
                    }
                }
                val intentNotes = Intent(Intent.ACTION_EDIT)
                intentNotes.setDataAndType(Uri.fromFile(file), "text/plain")
                list.add(null)
                list.addAll(packageManager.queryIntentActivities(intentNotes, 0))
                try {
                    if (UIUtils.isAppInstalledAndEnabled(this, "com.google.android.keep")) {
                        val keepIntent = Intent()
                        keepIntent.setPackage("com.google.android.keep")
                        val resolveInfo = packageManager.queryIntentActivities(keepIntent, 0)
                        if (resolveInfo != null && resolveInfo.size > 0 && !list
                                        .contains(resolveInfo[0])) {
                            list.add(resolveInfo[0])
                        }
                    }
                    if (UIUtils.isAppInstalledAndEnabled(this, "com.evernote")) {
                        val evernote = Intent()
                        evernote.setPackage("com.evernote")
                        val resolveInfoEverNote = packageManager
                                .queryIntentActivities(evernote, 0)
                        if (resolveInfoEverNote != null && resolveInfoEverNote
                                        .size > 0 && !list
                                        .contains(resolveInfoEverNote[0])) {
                            list.add(resolveInfoEverNote[0])
                        }
                    }
                    if (UIUtils.isAppInstalledAndEnabled(this, "com.microsoft.office.onenote")) {
                        val oneNote = Intent()
                        oneNote.setPackage("com.microsoft.office.onenote")
                        val resolveInfoOneNote = packageManager
                                .queryIntentActivities(oneNote, 0)
                        if (resolveInfoOneNote != null && resolveInfoOneNote.size > 0 && !list.contains(resolveInfoOneNote[0])) {
                            list.add(resolveInfoOneNote[0])
                        }
                    }
                    if (UIUtils.isAppInstalledAndEnabled(this, "com.automattic.simplenote")) {
                        val simpleNote = Intent()
                        simpleNote.setPackage("com.automattic.simplenote")
                        val resolveInfoSimpleNote = packageManager
                                .queryIntentActivities(simpleNote, 0)
                        if (resolveInfoSimpleNote != null && resolveInfoSimpleNote.size > 0 && !list.contains(resolveInfoSimpleNote[0])) {
                            list.add(resolveInfoSimpleNote[0])
                        }
                    }
                    if (UIUtils.isAppInstalledAndEnabled(this, "com.socialnmobile.dictapps.notepad.color.note")) {
                        val colorNote = Intent()
                        colorNote.setPackage("com.socialnmobile.dictapps.notepad.color.note")
                        val resolveInfoSimpleNote = packageManager
                                .queryIntentActivities(colorNote, 0)
                        if (resolveInfoSimpleNote != null && resolveInfoSimpleNote.size > 0 && !list.contains(resolveInfoSimpleNote[0])) {
                            list.add(resolveInfoSimpleNote[0])
                        }
                    }
                    if (UIUtils.isAppInstalledAndEnabled(this, "com.task.notes")) {
                        val colorNote = Intent()
                        colorNote.setPackage("com.task.notes")
                        val resolveInfoSimpleNote = packageManager
                                .queryIntentActivities(colorNote, 0)
                        if (resolveInfoSimpleNote != null && resolveInfoSimpleNote.size > 0 && !list.contains(resolveInfoSimpleNote[0])) {
                            list.add(resolveInfoSimpleNote[0])
                        }
                    }
                    if (UIUtils.isAppInstalledAndEnabled(this, "com.edi.masaki.mymemoapp")) {
                        val colorNote = Intent()
                        colorNote.setPackage("com.edi.masaki.mymemoapp")
                        val resolveInfoSimpleNote = packageManager
                                .queryIntentActivities(colorNote, 0)
                        if (resolveInfoSimpleNote != null && resolveInfoSimpleNote.size > 0 && !list.contains(resolveInfoSimpleNote[0])) {
                            list.add(resolveInfoSimpleNote[0])
                        }
                    }
                    if (UIUtils.isAppInstalledAndEnabled(this, "com.dencreak.esmemo")) {
                        val colorNote = Intent()
                        colorNote.setPackage("com.dencreak.esmemo")
                        val resolveInfoSimpleNote = packageManager
                                .queryIntentActivities(colorNote, 0)
                        if (resolveInfoSimpleNote != null && resolveInfoSimpleNote.size > 0 && !list.contains(resolveInfoSimpleNote[0])) {
                            list.add(resolveInfoSimpleNote[0])
                        }
                    }
                    if (UIUtils.isAppInstalledAndEnabled(this, "com.samsung.android.snote")) {
                        val colorNote = Intent()
                        colorNote.setPackage("com.samsung.android.snote")
                        val resolveInfoSimpleNote = packageManager
                                .queryIntentActivities(colorNote, 0)
                        if (resolveInfoSimpleNote != null && resolveInfoSimpleNote.size > 0 && !list.contains(resolveInfoSimpleNote[0])) {
                            list.add(resolveInfoSimpleNote[0])
                        }
                    }
                    if (UIUtils.isAppInstalledAndEnabled(this, "com.samsung.android.app.notes")) {
                        val colorNote = Intent()
                        colorNote.setPackage("com.samsung.android.app.notes")
                        val resolveInfoSimpleNote = packageManager
                                .queryIntentActivities(colorNote, 0)
                        if (resolveInfoSimpleNote != null && resolveInfoSimpleNote.size > 0 && !list.contains(resolveInfoSimpleNote[0])) {
                            list.add(resolveInfoSimpleNote[0])
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            MainListItemLoader.TOOLS_RECORDER -> {
            }
            MainListItemLoader.TOOLS_CAMERA -> {
                val intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                list.addAll(packageManager.queryIntentActivities(intentCamera, 0))
                packageNames = HashSet(0)
                listTemp = ArrayList(0)
                for (resolveInfo in list) {
                    if (!packageNames.contains(resolveInfo!!.activityInfo.packageName)) {
                        packageNames.add(resolveInfo.activityInfo.packageName)
                        listTemp.add(resolveInfo)
                    }
                }
                list.clear()
                list.addAll(listTemp)
            }
            MainListItemLoader.TOOLS_PHOTOS -> {
                val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                pickIntent.type = "image/* video/*"
                list.addAll(packageManager.queryIntentActivities(pickIntent, 0))
                packageNames = HashSet(0)
                listTemp = ArrayList(0)
                for (resolveInfo in list) {
                    if (!packageNames.contains(resolveInfo!!.activityInfo.packageName)) {
                        packageNames.add(resolveInfo.activityInfo.packageName)
                        listTemp.add(resolveInfo)
                    }
                }
                list.clear()
                list.addAll(listTemp)
            }
            MainListItemLoader.TOOLS_PAYMENT -> {
            }
            MainListItemLoader.TOOLS_WELLNESS -> {
            }
            MainListItemLoader.TOOLS_BROWSER -> {
                val intentBrowser = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/"))
                list.addAll(packageManager.queryIntentActivities(intentBrowser, 0))
                packageNames = HashSet(0)
                listTemp = ArrayList(0)
                for (resolveInfo in list) {
                    if (!packageNames.contains(resolveInfo!!.activityInfo.packageName)) {
                        packageNames.add(resolveInfo.activityInfo.packageName)
                        listTemp.add(resolveInfo)
                    }
                }
                list.clear()
                list.addAll(listTemp)
            }
            MainListItemLoader.TOOLS_CALL -> {
                val number = Uri.parse("tel:")
                val dial = Intent(Intent.ACTION_DIAL, number)
                list.addAll(packageManager.queryIntentActivities(dial, 0))
                packageNames = HashSet(0)
                listTemp = ArrayList(0)
                for (resolveInfo in list) {
                    if (!packageNames.contains(resolveInfo!!.activityInfo.packageName)) {
                        packageNames.add(resolveInfo.activityInfo.packageName)
                        listTemp.add(resolveInfo)
                    }
                }
                list.clear()
                list.addAll(listTemp)
            }
            MainListItemLoader.TOOLS_CLOCK -> {
                val intentClock = Intent(AlarmClock.ACTION_SET_ALARM)
                list.addAll(packageManager.queryIntentActivities(intentClock, 0))
                packageNames = HashSet(0)
                listTemp = ArrayList(0)
                for (resolveInfo in list) {
                    if (!packageNames.contains(resolveInfo!!.activityInfo.packageName)) {
                        packageNames.add(resolveInfo.activityInfo.packageName)
                        listTemp.add(resolveInfo)
                    }
                }
                list.clear()
                list.addAll(listTemp)
            }
            MainListItemLoader.TOOLS_MESSAGE -> {
                val message = Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + ""))
                message.putExtra("sms_body", "Test text...")
                list.addAll(packageManager.queryIntentActivities(message, 0))
                try {
                    if (UIUtils.isAppInstalledAndEnabled(this, Constants.WHATSAPP_PACKAGE)) {
                        val keepIntent = Intent()
                        keepIntent.setPackage(Constants.WHATSAPP_PACKAGE)
                        val resolveInfo = packageManager.queryIntentActivities(keepIntent, 0)
                        if (resolveInfo != null && resolveInfo.size > 0 && !list
                                        .contains(resolveInfo[0])) {
                            list.add(resolveInfo[0])
                        }
                    }
                    if (UIUtils.isAppInstalledAndEnabled(this, Constants.LINE_PACKAGE)) {
                        val evernote = Intent()
                        evernote.setPackage(Constants.LINE_PACKAGE)
                        val resolveInfoEverNote = packageManager
                                .queryIntentActivities(evernote, 0)
                        if (resolveInfoEverNote != null && resolveInfoEverNote
                                        .size > 0 && !list
                                        .contains(resolveInfoEverNote[0])) {
                            list.add(resolveInfoEverNote[0])
                        }
                    }
                    if (UIUtils.isAppInstalledAndEnabled(this, Constants.VIBER_PACKAGE)) {
                        val evernote = Intent()
                        evernote.setPackage(Constants.VIBER_PACKAGE)
                        val resolveInfoEverNote = packageManager
                                .queryIntentActivities(evernote, 0)
                        if (resolveInfoEverNote != null && resolveInfoEverNote
                                        .size > 0 && !list
                                        .contains(resolveInfoEverNote[0])) {
                            list.add(resolveInfoEverNote[0])
                        }
                    }
                    if (UIUtils.isAppInstalledAndEnabled(this, Constants.SKYPE_PACKAGE)) {
                        val evernote = Intent()
                        evernote.setPackage(Constants.SKYPE_PACKAGE)
                        val resolveInfoEverNote = packageManager
                                .queryIntentActivities(evernote, 0)
                        if (resolveInfoEverNote != null && resolveInfoEverNote
                                        .size > 0 && !list
                                        .contains(resolveInfoEverNote[0])) {
                            list.add(resolveInfoEverNote[0])
                        }
                    }
                    if (UIUtils.isAppInstalledAndEnabled(this, Constants.WECHAT_PACKAGE)) {
                        val evernote = Intent()
                        evernote.setPackage(Constants.WECHAT_PACKAGE)
                        val resolveInfoEverNote = packageManager
                                .queryIntentActivities(evernote, 0)
                        if (resolveInfoEverNote != null && resolveInfoEverNote
                                        .size > 0 && !list
                                        .contains(resolveInfoEverNote[0])) {
                            list.add(resolveInfoEverNote[0])
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                packageNames = HashSet(0)
                listTemp = ArrayList(0)
                for (resolveInfo in list) {
                    if (!packageNames.contains(resolveInfo!!.activityInfo.packageName)) {
                        packageNames.add(resolveInfo.activityInfo.packageName)
                        listTemp.add(resolveInfo)
                    }
                }
                list.clear()
                list.addAll(listTemp)
            }
            MainListItemLoader.TOOLS_EMAIL -> {
                val intentEmail = Intent(Intent.ACTION_VIEW)
                val data = Uri.parse("mailto:recipient@example.com?subject=" + "" + "&body=" + "")
                intentEmail.data = data
                list.addAll(packageManager.queryIntentActivities(intentEmail, 0))
                packageNames = HashSet(0)
                listTemp = ArrayList(0)
                for (resolveInfo in list) {
                    if (!packageNames.contains(resolveInfo!!.activityInfo.packageName)) {
                        packageNames.add(resolveInfo.activityInfo.packageName)
                        listTemp.add(resolveInfo)
                    }
                }
                list.clear()
                list.addAll(listTemp)
            }
            MainListItemLoader.TOOLS_MUSIC -> {
                val intentMusic = Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER)
                list.addAll(packageManager.queryIntentActivities(intentMusic, 0))
                packageNames = HashSet(0)
                listTemp = ArrayList(0)
                for (resolveInfo in list) {
                    if (!packageNames.contains(resolveInfo!!.activityInfo.packageName)) {
                        packageNames.add(resolveInfo.activityInfo.packageName)
                        listTemp.add(resolveInfo)
                    }
                }
                list.clear()
                list.addAll(listTemp)
            }
            MainListItemLoader.TOOLS_PODCAST -> {
            }
            MainListItemLoader.TOOLS_FOOD -> {
            }
            MainListItemLoader.TOOLS_FITNESS -> {
            }
            MainListItemLoader.TOOLS_TODO -> {
            }
            MainListItemLoader.TOOLS_CLOUD -> {
            }
            MainListItemLoader.TOOLS_BOOKS -> {
            }
            MainListItemLoader.TOOLS_AUTHENTICATION -> {
            }
            MainListItemLoader.TOOLS_ASSISTANT -> {
            }
            MainListItemLoader.TOOLS_ADDITIONAL_MESSAGE -> {
            }
            MainListItemLoader.TOOLS_BANKING -> {
            }
            MainListItemLoader.TOOLS_COURCE -> {
            }
            MainListItemLoader.TOOLS_DOC -> {
            }
            MainListItemLoader.TOOLS_FILES -> {
            }
            MainListItemLoader.TOOLS_FLASH -> {
            }
            MainListItemLoader.TOOLS_HEALTH -> {
            }
            MainListItemLoader.TOOLS_JOURNAL -> {
            }
            MainListItemLoader.TOOLS_LANGUAGES -> {
            }
            MainListItemLoader.TOOLS_LEARNING -> {
            }
            MainListItemLoader.TOOLS_MEDITATION -> {
            }
            MainListItemLoader.TOOLS_MICROPHONE -> {
            }
            MainListItemLoader.TOOLS_NEWS -> {
            }
            MainListItemLoader.TOOLS_SEARCH -> {
            }
            MainListItemLoader.TOOLS_SETTINGS -> {
            }
            MainListItemLoader.TOOLS_VOICE -> {
            }
            MainListItemLoader.TOOLS_SLEEP -> {
            }
            MainListItemLoader.TOOLS_CALCULATOR -> {
            }
            MainListItemLoader.TOOLS_TRANSLATE -> {
            }
            MainListItemLoader.TOOLS_VIDEO -> {
            }
            else -> {
            }
        }
        return list
    }

    fun addOrRemoveApplicationInfo(addingOrDelete: Boolean, packageName: String) {
        try {
            if (addingOrDelete) {
                val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                if (!packagesList.contains(appInfo.packageName)) {
                    packagesList.add(appInfo.packageName)
                    listApplicationName[packageName] = "" + packageManager.getApplicationLabel(appInfo)
                    EventBus.getDefault().post(AppInstalledEvent(true))
                }
            } else {
                if (packagesList.contains(packageName)) {
                    packagesList.remove(packageName)
                    listApplicationName.remove(packageName)
                    EventBus.getDefault().post(AppInstalledEvent(true))
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun initMemoryCatch() {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

        // Use 1/8th of the available memory for this memory cache.
        val cacheSize = maxMemory / 4
        mMemoryCache = object : LruCache<String?, Bitmap>(cacheSize) {
            override fun sizeOf(key: String?, bitmap: Bitmap): Int {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.byteCount / 1024
            }
        }
    }

    fun includeTaskPool(asyncTask: AsyncTask<*, *, *>, `object`: Any?) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, `object` as Nothing?)
    }

    fun addBitmapToMemoryCache(key: String?, bitmap: Bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache!!.put(key, bitmap)
        }
    }

    fun getBitmapFromMemCache(key: String?): Bitmap? {
        return mMemoryCache!![key]
    }

    @Synchronized
    fun downloadSiempoImages() {
        try {
            val folderSiempoImage = File(Environment.getExternalStorageDirectory().toString() +
                    "/Siempo images")
            if (!folderSiempoImage.exists()) {
                folderSiempoImage.mkdirs()
            }
            val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            var activeNetwork: NetworkInfo? = null
            if (connectivityManager != null) {
                activeNetwork = connectivityManager.activeNetworkInfo
            }
            if (activeNetwork != null) {
                if (folderSiempoImage != null && folderSiempoImage.list() != null) {
                    val listImageName = ArrayList(Arrays.asList(*folderSiempoImage.list()))
                    val list = resources.getStringArray(R.array.siempo_images)
                    val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                    for (strUrl in list) {
                        val fileName = strUrl.substring(strUrl.lastIndexOf('/') + 1, strUrl.length)
                        if (listImageName.contains(fileName)) {
                            Log.d("File Exists", fileName)
                        } else {
                            val download_Uri = Uri.parse(strUrl)
                            if (runningDownloadigFileList == null) {
                                runningDownloadigFileList = ArrayList()
                            }
                            if (download_Uri != null && !runningDownloadigFileList!!.contains(fileName)) {
                                runningDownloadigFileList!!.add(fileName)
                                val request = DownloadManager.Request(download_Uri)
                                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                                request.setAllowedOverRoaming(false)
                                request.setTitle(fileName)
                                request.setDescription(fileName)
                                request.setVisibleInDownloadsUi(false)
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                                request.setDestinationInExternalPublicDir("/Siempo images", fileName)
                                if (downloadManager != null) {
                                    val refid = downloadManager.enqueue(request)
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private inner class LoadApplications : AsyncTask<Any?, Any?, MutableSet<String>>() {
        protected override fun doInBackground(vararg params: Any?): MutableSet<String> {
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val pkgAppsList = packageManager.queryIntentActivities(mainIntent, 0)
            val applist: MutableSet<String> = HashSet()
            for (appInfo in pkgAppsList) {
                try {
                    val packageName = appInfo.activityInfo.packageName
                    if (!packageName.equals(getPackageName(), ignoreCase = true)) {
                        var drawable: Drawable? = null
                        try {
                            drawable = appInfo.loadIcon(packageManager)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        if (drawable != null) {
                            val bitmap = PackageUtil.drawableToBitmap(drawable)
                            addBitmapToMemoryCache(packageName, bitmap)
                        }
                        applist.add(packageName)
                        val packageManager = packageManager
                        val applicationName = appInfo.loadLabel(packageManager).toString()
                        if (applicationName == null) {
                            var applicationInfo: ApplicationInfo? = null
                            try {
                                applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                            } catch (e: PackageManager.NameNotFoundException) {
                                e.printStackTrace()
                            }
                            val applicationNameTemp = (if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "") as String
                            listApplicationName[packageName] = applicationNameTemp
                        } else {
                            listApplicationName[packageName] = applicationName
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return applist
        }

        override fun onPostExecute(applicationInfos: MutableSet<String>) {
            super.onPostExecute(applicationInfos)
            setPackagesList(applicationInfos)
            EventBus.getDefault().post(AppInstalledEvent(true))
        }
    }

    companion object {
        @get:Synchronized
        var instance: CoreApplication? = null
            private set
    }
}