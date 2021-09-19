package io.focuslauncher.phone.activities

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import de.greenrobot.event.EventBus
import de.greenrobot.event.Subscribe
import io.focuslauncher.R
import io.focuslauncher.databinding.ActivityAppAssignementBinding
import io.focuslauncher.phone.adapters.viewholder.AppAssignmentAdapter
import io.focuslauncher.phone.app.Constants
import io.focuslauncher.phone.app.CoreApplication
import io.focuslauncher.phone.event.AppInstalledEvent
import io.focuslauncher.phone.event.NotifySearchRefresh
import io.focuslauncher.phone.helper.FirebaseHelper
import io.focuslauncher.phone.models.MainListItem
import io.focuslauncher.phone.utils.NetworkUtil
import io.focuslauncher.phone.utils.PrefSiempo
import io.focuslauncher.phone.utils.Sorting
import io.focuslauncher.phone.utils.lifecycleProperty

class AppAssignmentActivity : CoreActivity() {
    var appList: MutableList<ResolveInfo> = ArrayList()
    var mainListItem: MainListItem? = null
    private var toolsItem: MenuItem? = null

    var set: Set<String> = HashSet()
    var appListAll: MutableList<ResolveInfo> = ArrayList()
    private var resolvedMimeList = ArrayList<ResolveInfo>()
    private var binding: ActivityAppAssignementBinding? by lifecycleProperty()
    private var appAssignmentAdapter: AppAssignmentAdapter? = null
    private var startTime: Long = 0
    private var className: String? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_assign, menu)
        toolsItem = menu.findItem(R.id.item_tools)
        val mainListItem = mainListItem
        if (toolsItem != null && mainListItem != null) {
            toolsItem?.setIcon(mainListItem.drawable)
            toolsItem?.title = mainListItem.title
        }
        return super.onCreateOptionsMenu(menu)
    }

    @Subscribe
    fun appInstalledEvent(event: AppInstalledEvent) {
        if (event.isAppInstalledSuccessfully) {
            filterList()
            initView()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppAssignementBinding.inflate(LayoutInflater.from(this))
        setContentView(binding?.root)
        mainListItem = intent.getSerializableExtra(Constants.INTENT_MAINLISTITEM) as MainListItem?
        className = intent.getStringExtra("class_name")
        if (mainListItem != null) {
            set = PrefSiempo.getInstance(this).read(PrefSiempo.JUNKFOOD_APPS, HashSet())
            initView()
        } else {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
        //Added to refresh if app is marked as non-junk by navigating to Flag
        // Junk Apps directly from this screen
        filterList()
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().postSticky(NotifySearchRefresh(true))
        FirebaseHelper.getInstance().logScreenUsageTime(this@AppAssignmentActivity.javaClass.simpleName, startTime)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mainListItem = savedInstanceState.getSerializable("MainListItem") as MainListItem?
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("MainListItem", mainListItem)
    }

    private fun filterList() {
        appList = ArrayList()
        if (mainListItem != null) {
            if (NetworkUtil.isOnline(this)) {
                showListByCategory()
            } else {
                showListByMimeType()
            }
        } else {
            finish()
        }
    }

    fun showListByCategory() {
        var isCategoryAvailable = false
        val categoryAppList = CoreApplication.getInstance().categoryAppList
        val mainListItem = mainListItem
        if (categoryAppList != null && categoryAppList.size > 0) {
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val installedPackageList = packageManager?.queryIntentActivities(mainIntent, 0).orEmpty()
            for (resolveInfo in installedPackageList) {
                if (resolveInfo.activityInfo.packageName != null
                    && !resolveInfo.activityInfo.packageName.equals(packageName, ignoreCase = true)
                ) {
                    for (googleAppPackages in googleApps) {
                        if (mainListItem != null
                            && resolveInfo.activityInfo.packageName.equals(googleAppPackages, ignoreCase = true)
                        ) {
                            isCategoryAvailable = true
                            appList.add(resolveInfo)
                        }
                    }
                }
            }
            for (resolveInfo in installedPackageList) {
                if (resolveInfo?.activityInfo?.packageName != null
                    && !resolveInfo.activityInfo.packageName.equals(packageName, ignoreCase = true)
                ) {
                    var appName = ""
                    try {
                        appName = resolveInfo.loadLabel(packageManager).toString()
                    } catch (e: Exception) {
                    }
                    for (category in categoryAppList) {
                        if (mainListItem?.category.equals("Travel & Local", ignoreCase = true)
                            && resolveInfo.activityInfo.packageName.equals("me.lyft.android", ignoreCase = true)
                        ) {
                            isCategoryAvailable = true
                            appList.add(resolveInfo)
                        }
                        if (mainListItem != null
                            && resolveInfo.activityInfo.packageName.equals(category.getPackageName(), ignoreCase = true)
                            && mainListItem.category.equals(category.getCategoryName(), ignoreCase = true)
                            || mainListItem != null && appName.contains(mainListItem.title)
                            || mainListItem?.title?.contains(appName) == true
                        ) {
                            isCategoryAvailable = true
                            appList.add(resolveInfo)
                        }
                    }
                }
            }
            appList = removeDuplicates(appList)
            if (isCategoryAvailable) {
                bindList(appList)
            } else {
                showListByMimeType()
            }
        } else {
            showListByMimeType()
        }
    }

    fun showListByMimeType() {
        val installedPackageList: List<ResolveInfo>
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        val mainListItem = mainListItem
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        installedPackageList = packageManager.queryIntentActivities(mainIntent, 0)
        if (idList.contains(mainListItem?.id)) {
            resolvedMimeList = getMimeList()
            for (resolveInfo in installedPackageList) {
                if (!resolveInfo.activityInfo.packageName.equals(packageName, ignoreCase = true)) {
                    appList.add(resolveInfo)
                }
            }
            binding?.txtViewAllapps?.visibility = View.GONE
        } else if (mainListItem != null) {
            appList = CoreApplication.getInstance().getApplicationByCategory(mainListItem.id)
        }
        appListAll = ArrayList()
        for (resolveInfo in installedPackageList) {
            if (!resolveInfo.activityInfo.packageName.equals(packageName, ignoreCase = true)) {
                appListAll.add(resolveInfo)
            }
        }
        appListAll = Sorting.sortAppAssignment(this@AppAssignmentActivity, appListAll)
        appListAll = removeDuplicates(appListAll)
        appList = removeDuplicates(appList)
        if (binding?.txtViewAllapps?.visibility != View.VISIBLE) {
            bindList(appListAll)
        } else {
            bindList(appList)
        }
    }

    private fun getMimeList(): ArrayList<ResolveInfo> {
        val mimeListLocal = ArrayList<ResolveInfo>()
        for (i in 1..20) {
            mimeListLocal.addAll(CoreApplication.getInstance().getApplicationByCategory(i))
        }
        return mimeListLocal
    }

    private fun initView() {
        val mainListItem = mainListItem
        binding?.toolbar?.apply {
            setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp)
            if (mainListItem != null) {
                title = getString(R.string.assign_an_app) + " " + mainListItem.title
            }
            setNavigationOnClickListener { finish() }
            setSupportActionBar(this)
        }

        binding?.txtViewAllapps?.setOnClickListener {
            binding?.txtViewAllapps?.visibility = View.GONE
            bindList(appListAll)
        }

        //Added for searchbar
        binding?.edtSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (appAssignmentAdapter != null) {
                    appAssignmentAdapter?.filter?.filter(s.toString())
                }
                if (s.toString().isNotEmpty()) {
                    binding?.imgClear?.visibility = View.VISIBLE
                } else {
                    binding?.imgClear?.visibility = View.INVISIBLE
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding?.imgClear?.setOnClickListener { binding?.edtSearch?.setText("") }
        allInstallApps
    }

    private val allInstallApps: Unit
        get() {
            val installedPackageList: List<ResolveInfo>
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            installedPackageList = packageManager.queryIntentActivities(mainIntent, 0)
            appListAll = ArrayList()
            for (resolveInfo in installedPackageList) {
                if (!resolveInfo.activityInfo.packageName.equals(packageName, ignoreCase = true)) {
                    appListAll.add(resolveInfo)
                }
            }
            appListAll = Sorting.sortAppAssignment(this@AppAssignmentActivity, appListAll)
        }

    private fun bindList(inputAppList: List<ResolveInfo>) {
        var appList: List<ResolveInfo?> = inputAppList
        val mainListItem = mainListItem
        if (appList.isNotEmpty()) {
            binding?.recyclerView?.visibility = View.VISIBLE
            binding?.txtErrorMessage?.visibility = View.INVISIBLE
            appList = Sorting.sortAppAssignment(this, appList)
            val mLayoutManager = LinearLayoutManager(applicationContext)
            binding?.recyclerView?.layoutManager = mLayoutManager
            binding?.recyclerView?.addItemDecoration(
                DividerItemDecoration(this, mLayoutManager.orientation)
            )
            if (mainListItem != null) {
                appAssignmentAdapter = AppAssignmentAdapter(
                    this, mainListItem.id,
                    appList, className
                )
                binding?.recyclerView?.adapter = appAssignmentAdapter
                appAssignmentAdapter?.filter?.filter(binding?.edtSearch?.text?.toString()?.trim { it <= ' ' } ?: "")
            }
        } else {
            binding?.recyclerView?.visibility = View.INVISIBLE
            binding?.txtErrorMessage?.visibility = View.VISIBLE
            if (mainListItem != null) {
                binding?.txtErrorMessage?.text = "No " + mainListItem.title + " apps are installed."
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    fun hideOrShowMessage(isShow: Boolean) {
        binding?.apply {
            if (isShow) {
                recyclerView.visibility = View.VISIBLE
                txtErrorMessage.visibility = View.INVISIBLE
            } else {
                recyclerView.visibility = View.INVISIBLE
                txtErrorMessage.visibility = View.VISIBLE
                txtErrorMessage.setText(R.string.no_mattched_text)
            }
        }
    }

    companion object {
        fun <T> removeDuplicates(list: List<T>): MutableList<T> {
            val newList: MutableList<T> = ArrayList()
            for (element in list) {
                if (!newList.contains(element)) {
                    newList.add(element)
                }
            }
            return newList
        }

        val googleApps = listOf(
            "com.google.android.gm",
            "com.google.android.googlequicksearchbox",
            "com.android.chrome",
            "com.google.android.apps.photos",
            "com.google.android.apps.googleassistant",
            "com.google.android.calendar",
            "com.google.android.apps.docs.editors.docs",
            "com.google.android.apps.docs.editors.sheets",
            "com.google.android.apps.docs.editors.slides",
            "com.google.android.apps.docs",
            "com.google.android.apps.tachyon",
            "com.google.earth",
            "com.google.android.apps.fitness",
            "com.google.android.apps.chromecast.app",
            "com.google.android.keep",
            "com.google.android.apps.maps",
            "com.google.android.apps.nbu.paisa.user",
            "com.google.android.music",
            "com.google.android.apps.podcasts"
        )

        /**
         * some mystery numbers: 2, 4, 6, 9, 10, 12, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44
         * title from initial variant: //8 Photos
         */
        val idList = listOf(2, 4, 6, 9, 10, 12) + (18..44)
    }
}