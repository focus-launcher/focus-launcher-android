package io.focuslauncher.phone.activities

import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.appcompat.widget.PopupMenu
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.greenrobot.event.Subscribe
import io.focuslauncher.R
import io.focuslauncher.databinding.ActivityFavoriteSelectionBinding
import io.focuslauncher.phone.adapters.FavoriteFlaggingAdapter
import io.focuslauncher.phone.app.CoreApplication
import io.focuslauncher.phone.event.AppInstalledEvent
import io.focuslauncher.phone.helper.FirebaseHelper
import io.focuslauncher.phone.models.AppListInfo
import io.focuslauncher.phone.service.LoadFavoritePane
import io.focuslauncher.phone.service.LoadJunkFoodPane
import io.focuslauncher.phone.utils.*
import java.lang.ref.WeakReference

class FavoritesSelectionActivity : CoreActivity(), AdapterView.OnItemClickListener {
    var list: MutableSet<String> = HashSet()

    var adapterList: MutableSet<String> = HashSet()

    //Junk list removal will be needed here as we need to remove the
    //junk-flagged app from other app list which cn be marked as favorite
    private var junkFoodList: MutableSet<String>? = HashSet()
    private var junkfoodFlaggingAdapter: FavoriteFlaggingAdapter? = null
    private var firstPosition = 0
    private var installedPackageList: List<String>? = null

    private var popup: PopupMenu? = null
    private var favoriteList: MutableList<AppListInfo>? = ArrayList()
    private var unfavoriteList: MutableList<AppListInfo> = ArrayList()
    private var bindingList = ArrayList<AppListInfo>()
    private var startTime: Long = 0

    private var binding: ActivityFavoriteSelectionBinding? by lifecycleProperty()

    @Subscribe
    fun appInstalledEvent(event: AppInstalledEvent) {
        if (!isFinishing && event.isAppInstalledSuccessfully) {
            loadApps()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindView(ActivityFavoriteSelectionBinding::inflate)
        initView()
        list = PrefSiempo.getInstance(this).read(PrefSiempo.FAVORITE_APPS, HashSet())
        adapterList = HashSet()
        junkFoodList = PrefSiempo.getInstance(this).read(PrefSiempo.JUNKFOOD_APPS, HashSet())
        list.removeAll(junkFoodList.orEmpty())
        adapterList.addAll(list)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {}

    private fun initView() {
        binding?.toolbar?.apply {
            setTitle(R.string.title_flagging_screen)
            setSupportActionBar(this)
        }
        binding?.lstOtherApps?.onItemClickListener = this@FavoritesSelectionActivity
        binding?.edtSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                junkfoodFlaggingAdapter?.filter?.filter(s.toString())
                if (s.toString().isNotEmpty()) {
                    binding?.imgClear?.visibility = View.VISIBLE
                } else {
                    binding?.imgClear?.visibility = View.INVISIBLE
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding?.imgClear?.setOnClickListener { binding?.edtSearch?.setText("") }
    }

    /**
     * load system apps and filter the application for junkfood and normal.
     */
    private fun loadApps() {
        val installedPackageListLocal = CoreApplication.getInstance().packagesList
        val appList: List<String> = ArrayList(installedPackageListLocal)
        installedPackageList = appList
        bindData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_assignment_list, menu)
        val menuItem = menu.findItem(R.id.item_save)
        //        setTextColorForMenuItem(menuItem, R.color.colorAccent);
        menuItem.setOnMenuItemClickListener { item: MenuItem? ->
            junkFoodList?.removeAll(adapterList)
            val jsonListOfSortedFavorites =
                PrefSiempo.getInstance(this@FavoritesSelectionActivity).read(PrefSiempo.FAVORITE_SORTED_MENU, "")
            //convert onNoteListChangedJSON array into a List<Long>
            val gson1 = Gson()
            val listOfSortFavoritesApps = gson1.fromJson<MutableList<String>>(
                jsonListOfSortedFavorites,
                object : TypeToken<List<String>>() {}.type
            )
            val it = listOfSortFavoritesApps.listIterator()
            while (it.hasNext()) {
                val packageName = it.next()
                if (!adapterList.contains(packageName)) {
                    //Used List Iterator to set empty
                    // value for package name retaining
                    // the positions of elements
                    it.set("")
                }
            }
            val gson2 = Gson()
            val jsonListOfFavoriteApps = gson2.toJson(listOfSortFavoritesApps)
            PrefSiempo.getInstance(this@FavoritesSelectionActivity)
                .write(PrefSiempo.FAVORITE_SORTED_MENU, jsonListOfFavoriteApps)
            PrefSiempo.getInstance(this@FavoritesSelectionActivity).write(PrefSiempo.FAVORITE_APPS, adapterList)
            PrefSiempo.getInstance(this@FavoritesSelectionActivity).write(PrefSiempo.JUNKFOOD_APPS, junkFoodList)
            LoadJunkFoodPane(PrefSiempo.getInstance(this@FavoritesSelectionActivity)).execute()
            LoadFavoritePane(PrefSiempo.getInstance(this@FavoritesSelectionActivity)).execute()
            finish()
            false
        }
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * bind the list view of flag app and all apps.
     */
    private fun bindData() {
        try {
            favoriteList = ArrayList()
            unfavoriteList = ArrayList()
            bindingList = ArrayList()
            for (resolveInfo in (installedPackageList ?: emptyList())) {
                if (!resolveInfo.equals(packageName, ignoreCase = true)) {
                    val isEnable = UIUtils.isAppInstalledAndEnabled(this, resolveInfo)
                    if (isEnable) {
                        val applicationname = CoreApplication.getInstance()
                            .listApplicationName[resolveInfo]
                        if (!TextUtils.isEmpty(applicationname)) {
                            if (adapterList.contains(resolveInfo)) {
                                favoriteList?.add(
                                    AppListInfo(
                                        resolveInfo, applicationname,
                                        false, false, true
                                    )
                                )
                            } else {
                                if (junkFoodList?.contains(resolveInfo) == false) {
                                    unfavoriteList.add(
                                        AppListInfo(
                                            resolveInfo,
                                            applicationname, false, false, false
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            setToolBarText(favoriteList?.size ?: 0)
            if (favoriteList?.size == 0) {
                favoriteList?.add(AppListInfo("", "", true, true, true))
            } else {
                favoriteList?.add(0, AppListInfo("", "", true, false, true))
            }
            favoriteList = Sorting.sortApplication(favoriteList)
            bindingList.addAll(favoriteList.orEmpty())
            if (unfavoriteList.size == 0) {
                unfavoriteList.add(AppListInfo("", "", true, true, false))
            } else {
                unfavoriteList.add(0, AppListInfo("", "", true, false, false))
            }
            unfavoriteList = Sorting.sortApplication(unfavoriteList)
            bindingList.addAll(unfavoriteList)
            junkfoodFlaggingAdapter = FavoriteFlaggingAdapter(this, bindingList)
            binding?.lstOtherApps?.adapter = junkfoodFlaggingAdapter
            binding?.lstOtherApps?.onItemClickListener = this
            junkfoodFlaggingAdapter?.filter?.filter(binding?.edtSearch?.text.toString())
            binding?.lstOtherApps?.setSelection(firstPosition)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * show pop dialog on List item click for flag/un-flag and application information.
     */
    fun showPopUp(view: View, packagename: String, isFlagApp: Boolean) {
        popup?.dismiss()
        popup = PopupMenu(this@FavoritesSelectionActivity, view, Gravity.END)
        popup?.menuInflater?.inflate(R.menu.junkfood_popup, popup?.menu)
        val menuItem = popup?.menu?.findItem(R.id.item_Unflag)
        if (isFlagApp) {
            menuItem?.isVisible = favoriteList?.size != 2
        } else {
            menuItem?.isVisible = favoriteList != null && favoriteList?.size?.let { it < 13 } == true
        }
        menuItem?.title = if (isFlagApp) getString(R.string.favorite_menu_unselect)
        else getString(R.string.favorite_menu_select)
        popup?.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.item_Unflag) {
                try {
                    popup?.dismiss()
                    runOnUiThread {
                        if (adapterList.contains(packagename)) {
                            adapterList.remove(packagename)
                            //setToolBarText(favoriteList.size());
                        } else {
                            if (favoriteList?.size?.let { it < 13 } == true) {
                                adapterList.add(packagename)
                            }
                            // setToolBarText(favoriteList.size());
                        }
                        firstPosition = binding?.lstOtherApps?.firstVisiblePosition ?: 0
                        FilterApps(true, this@FavoritesSelectionActivity).execute()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (item.itemId == R.id.item_Info) {
                try {
                    PackageUtil.appSettings(this@FavoritesSelectionActivity, packagename)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            false
        }
        popup?.show()
        popup?.setOnDismissListener { popup = null }
    }

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
        loadApps()
    }

    override fun onPause() {
        super.onPause()
        FirebaseHelper.getInstance().logScreenUsageTime(this.javaClass.simpleName, startTime)
    }

    fun setToolBarText(count: Int) {
        val remainapps = 12 - count
        binding?.toolbar?.title = "Select up to $remainapps more apps"
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (popup != null) {
                popup?.dismiss()
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    internal inner class FilterApps(isNotify: Boolean, context: FavoritesSelectionActivity) :
        AsyncTask<String?, String?, ArrayList<AppListInfo>>() {
        var isNotify: Boolean
        var size = 0
        private val activityReference: WeakReference<FavoritesSelectionActivity>
        override fun onPreExecute() {
            super.onPreExecute()
            favoriteList = ArrayList()
            unfavoriteList = ArrayList()
            bindingList = ArrayList()
        }

        override fun doInBackground(vararg params: String?): ArrayList<AppListInfo> {
            try {
                for (resolveInfo in installedPackageList.orEmpty()) {
                    if (!resolveInfo.equals(packageName, ignoreCase = true)) {
                        val isEnable = UIUtils.isAppInstalledAndEnabled(this@FavoritesSelectionActivity, resolveInfo)
                        if (isEnable) {
                            val applicationname = CoreApplication.getInstance()
                                .listApplicationName[resolveInfo]
                            if (!TextUtils.isEmpty(applicationname)) {
                                if (adapterList.contains(resolveInfo)) {
                                    favoriteList?.add(
                                        AppListInfo(
                                            resolveInfo, applicationname,
                                            false, false,
                                            true
                                        )
                                    )
                                } else {
                                    if (junkFoodList?.contains(resolveInfo) == false
                                    ) {
                                        unfavoriteList.add(
                                            AppListInfo(
                                                resolveInfo,
                                                applicationname, false,
                                                false, false
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                size = favoriteList?.size ?: 0
                if (favoriteList?.size == 0) {
                    favoriteList?.add(AppListInfo("", "", true, true, true))
                } else {
                    favoriteList?.add(0, AppListInfo("", "", true, false, true))
                }
                favoriteList = Sorting.sortApplication(favoriteList)
                bindingList.addAll(favoriteList.orEmpty())
                if (unfavoriteList.size == 0) {
                    unfavoriteList.add(AppListInfo("", "", true, true, false))
                } else {
                    unfavoriteList.add(0, AppListInfo("", "", true, false, false))
                }
                unfavoriteList = Sorting.sortApplication(unfavoriteList)
                bindingList.addAll(unfavoriteList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return bindingList
        }

        override fun onPostExecute(s: ArrayList<AppListInfo>) {
            super.onPostExecute(s)
            try {
                val activity = activityReference.get()
                if (activity == null || activity.isFinishing) return
                if (binding?.toolbar != null) {
                    setToolBarText(size)
                    junkfoodFlaggingAdapter = FavoriteFlaggingAdapter(this@FavoritesSelectionActivity, bindingList)
                    binding?.lstOtherApps?.adapter = junkfoodFlaggingAdapter
                    binding?.lstOtherApps?.onItemClickListener = this@FavoritesSelectionActivity
                    if (isNotify) {
                        junkfoodFlaggingAdapter?.notifyDataSetChanged()
                        binding?.edtSearch?.setText("")
                        binding?.lstOtherApps?.setSelection(firstPosition)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        init {
            activityReference = WeakReference(context)
            this.isNotify = isNotify
            binding?.lstOtherApps?.onItemClickListener = null
        }
    }
}