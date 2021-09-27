package io.focuslauncher.phone.activities

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.greenrobot.event.EventBus
import de.greenrobot.event.Subscribe
import io.focuslauncher.R
import io.focuslauncher.databinding.ActivityJunkfoodFlaggingBinding
import io.focuslauncher.phone.adapters.JunkfoodFlaggingAdapter
import io.focuslauncher.phone.app.Constants
import io.focuslauncher.phone.app.CoreApplication
import io.focuslauncher.phone.event.AppInstalledEvent
import io.focuslauncher.phone.event.NotifySearchRefresh
import io.focuslauncher.phone.helper.FirebaseHelper
import io.focuslauncher.phone.models.AppListInfo
import io.focuslauncher.phone.models.AppMenu
import io.focuslauncher.phone.service.LoadFavoritePane
import io.focuslauncher.phone.service.LoadJunkFoodPane
import io.focuslauncher.phone.service.LoadToolPane
import io.focuslauncher.phone.utils.*

class JunkfoodFlaggingActivity : CoreActivity(), AdapterView.OnItemClickListener {

    private var binding: ActivityJunkfoodFlaggingBinding? by lifecycleProperty()

    var list: Set<String> = HashSet()
    var isLoadFirstTime = true
    var adapterlist: MutableSet<String> = HashSet()
        private set
    var favoriteList: MutableSet<String> = HashSet()
    var junkfoodFlaggingAdapter: JunkfoodFlaggingAdapter? = null
    var installedPackageList: List<String>? = null
    var firstPosition = 0
    var isClickOnView = true

    private var flagAppList: MutableList<AppListInfo> = ArrayList()
    private var unflageAppList: MutableList<AppListInfo> = ArrayList()
    private var bindingList = ArrayList<AppListInfo>()
    private var startTime: Long = 0
    private var isFromAppMenu = false
    private var positionPopUP = 0

    private var popup: PopupMenu? = null

    @Subscribe
    fun appInstalledEvent(event: AppInstalledEvent) {
        if (!isFinishing && event.isAppInstalledSuccessfully) {
            loadApps()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindView(ActivityJunkfoodFlaggingBinding::inflate)
        initView()
        list = PrefSiempo.getInstance(this).read(PrefSiempo.JUNKFOOD_APPS, HashSet())
        favoriteList = PrefSiempo.getInstance(this).read(PrefSiempo.FAVORITE_APPS, HashSet())
        favoriteList.removeAll(list)
        PrefSiempo.getInstance(this@JunkfoodFlaggingActivity).write(
            PrefSiempo.FAVORITE_APPS,
            favoriteList
        )
        adapterlist.addAll(list)
        val intent = intent
        if (intent.extras != null && intent.hasExtra("FromAppMenu")) {
            isFromAppMenu = intent.getBooleanExtra("FromAppMenu", false)
        }
    }

    private fun initView() {
        binding?.toolbar?.apply {
            setTitle(R.string.title_flagging_screen)
            setSupportActionBar(this)
        }
        try {
            val myTypeface = Typeface.createFromAsset(assets, "fonts/robotoregular.ttf")
            binding?.edtSearch?.setTypeface(myTypeface)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding?.listAllApps?.onItemClickListener = this@JunkfoodFlaggingActivity
        binding?.edtSearch?.clearFocus()
        binding?.edtSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (junkfoodFlaggingAdapter != null) {
                    junkfoodFlaggingAdapter?.filter?.filter(s.toString())
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
    }

    private fun loadApps() {
        if (PrefSiempo.getInstance(this).read(PrefSiempo.IS_JUNKFOOD_FIRSTTIME, true)) {
            PrefSiempo.getInstance(this).write(PrefSiempo.IS_JUNKFOOD_FIRSTTIME, false)
            showFirstTimeDialog()
        }
        val installedPackageListLocal = CoreApplication.getInstance().packagesList
        Log.d("Junkfood", "" + installedPackageListLocal.size)
        installedPackageListLocal.remove(Constants.SETTINGS_APP_PACKAGE)
        installedPackageList = ArrayList()
        val appList: MutableList<String> = ArrayList(installedPackageListLocal)
        favoriteList = PrefSiempo.getInstance(this).read(PrefSiempo.FAVORITE_APPS, HashSet())
        appList.removeAll(favoriteList)
        installedPackageList = appList
        bindData()
    }

    private val toolsAppList: MutableList<String>
        get() {
            val assignedToolListSet: MutableSet<AppMenu> = HashSet()
            if (null != CoreApplication.getInstance() && null != CoreApplication
                    .getInstance().toolsSettings
            ) {
                for ((_, value) in CoreApplication.getInstance().toolsSettings) {
                    assignedToolListSet.add(value)
                }
            }
            val appToolList: MutableList<String> = ArrayList()
            for (toolsMenuApp in assignedToolListSet) {
                if (toolsMenuApp.applicationName != null) {
                    appToolList.add(toolsMenuApp.applicationName)
                }
            }
            return appToolList
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_assignment_list, menu)
        val menuItem = menu.findItem(R.id.item_save)
        menuItem.setOnMenuItemClickListener { item: MenuItem? ->
            showSaveDialog()
            false
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun showSaveDialog() {
        val builder = AlertDialog.Builder(this@JunkfoodFlaggingActivity)
        builder.setTitle(getString(R.string.msg_congratulations))
        builder.setMessage(R.string.msg_flage_save_dialog)
        builder.setPositiveButton(R.string.strcontinue) { dialog, which ->
            dialog.dismiss()
            runOnUiThread(object : Runnable {
                override fun run() {
                    favoriteList.removeAll(adapterlist)
                    toolsAppList.removeAll(adapterlist)
                    val newMap = CoreApplication.getInstance().toolsSettings.orEmpty()
                    for (i in 0 until newMap.size) {
                        if (newMap[i] != null && !TextUtils.isEmpty(newMap[i]?.applicationName)) {
                            if (adapterlist.contains(newMap[i]?.applicationName)) {
                                newMap[i]?.applicationName = ""
                            }
                        }
                    }
                    val hashMapToolSettings = Gson().toJson(newMap)
                    PrefSiempo.getInstance(this@JunkfoodFlaggingActivity)
                        .write(PrefSiempo.TOOLS_SETTING, hashMapToolSettings)
                    PrefSiempo.getInstance(this@JunkfoodFlaggingActivity).write(PrefSiempo.FAVORITE_APPS, favoriteList)
                    PrefSiempo.getInstance(this@JunkfoodFlaggingActivity).write(PrefSiempo.JUNKFOOD_APPS, adapterlist)
                    if (adapterlist.size == 0 && !DashboardActivity.isJunkFoodOpen) {
                        DashboardActivity.isJunkFoodOpen = true
                    }
                    LoadFavoritePane(PrefSiempo.getInstance(this@JunkfoodFlaggingActivity)).execute()
                    LoadJunkFoodPane(PrefSiempo.getInstance(this@JunkfoodFlaggingActivity)).execute()
                    LoadToolPane().execute()
                    EventBus.getDefault().postSticky(NotifySearchRefresh(true))
                    FirebaseHelper.getInstance().logScreenUsageTime(this.javaClass.simpleName, startTime)
                    finish()
                    overridePendingTransition(R.anim.in_from_right_email, R.anim.out_to_left_email)
                }
            })
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.dialog_blue))
    }

    private fun showFirstTimeDialog() {
        val builder = AlertDialog.Builder(this@JunkfoodFlaggingActivity)
        builder.setTitle(getString(R.string.flag_app_first_time))
        builder.setMessage(R.string.flag_first_time_install)
        builder.setPositiveButton(R.string.gotit) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.dialog_blue))
    }

    override fun onBackPressed() {

        //Added this code as part of SSA-1333, to save the list on backpress
        super.onBackPressed()
        if (isFromAppMenu) {
            return
        }
        if (list.size == 0 && !DashboardActivity.isJunkFoodOpen) {
            DashboardActivity.isJunkFoodOpen = true
        }
        overridePendingTransition(R.anim.in_from_right_email, R.anim.out_to_left_email)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {}
    private fun bindData() {
        try {
            flagAppList = ArrayList()
            unflageAppList = ArrayList()
            bindingList = ArrayList()
            junkfoodFlaggingAdapter = JunkfoodFlaggingAdapter(this, bindingList)
            binding?.listAllApps?.adapter = junkfoodFlaggingAdapter
            for (resolveInfo in installedPackageList.orEmpty()) {
                if (!resolveInfo.equals(packageName, ignoreCase = true)) {
                    val applicationname = CoreApplication.getInstance()
                        .listApplicationName[resolveInfo]
                    if (!TextUtils.isEmpty(applicationname)) {
                        if (adapterlist.contains(resolveInfo)) {
                            flagAppList.add(AppListInfo(resolveInfo, applicationname, false, false, true))
                        } else {
                            unflageAppList.add(AppListInfo(resolveInfo, applicationname, false, false, false))
                        }
                    }
                }
            }
            removeJunkAppsFromFavorites()
            if (flagAppList.size == 0) {
                flagAppList.add(AppListInfo("", "", true, true, true))
            } else {
                flagAppList.add(0, AppListInfo("", "", true, false, true))
            }
            flagAppList = Sorting.sortApplication(flagAppList)
            bindingList.addAll(flagAppList)
            if (unflageAppList.size == 0) {
                unflageAppList.add(AppListInfo("", "", true, true, false))
            } else {
                unflageAppList.add(0, AppListInfo("", "", true, false, false))
            }
            unflageAppList = Sorting.sortApplication(unflageAppList)
            bindingList.addAll(unflageAppList)
            if (junkfoodFlaggingAdapter != null) {
                junkfoodFlaggingAdapter?.setData(bindingList)
                junkfoodFlaggingAdapter?.filter?.filter(binding?.edtSearch?.text?.toString().orEmpty())
                binding?.listAllApps?.setSelection(firstPosition)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeJunkAppsFromFavorites() {
        val jsonListOfSortedFavorites = PrefSiempo.getInstance(this@JunkfoodFlaggingActivity)
            .read(PrefSiempo.FAVORITE_SORTED_MENU, "")
        val favlist = PrefSiempo.getInstance(this).read(PrefSiempo.FAVORITE_APPS, HashSet())
        //convert onNoteListChangedJSON array into a List<Long>
        val gson1 = Gson()
        val listOfSortFavoritesApps =
            gson1.fromJson<MutableList<String>>(jsonListOfSortedFavorites, object : TypeToken<List<String?>?>() {}.type)
        for (junkString in adapterlist) {
            if (favlist != null && favlist.contains(junkString)) {
                val it = listOfSortFavoritesApps.listIterator()
                while (it.hasNext()) {
                    val packageName = it.next()
                    if (junkString.equals(packageName, ignoreCase = true)) {
                        //Used List Iterator to set empty
                        // value for package name retaining
                        // the positions of elements
                        it.set("")
                    }
                }
            }
        }
        val gson2 = Gson()
        val jsonListOfFavoriteApps = gson2.toJson(listOfSortFavoritesApps)
        PrefSiempo.getInstance(this@JunkfoodFlaggingActivity)
            .write(PrefSiempo.FAVORITE_SORTED_MENU, jsonListOfFavoriteApps)
        PrefSiempo.getInstance(this@JunkfoodFlaggingActivity).write(
            PrefSiempo.FAVORITE_APPS,
            favlist
        )
    }

    fun showPopUp(view: View, packagename: String, isFlagApp: Boolean) {
        positionPopUP = 0
        for (i in bindingList.indices) {
            if (bindingList[i].packageName.equals(packagename, ignoreCase = true)) {
                positionPopUP = i
                break
            }
        }
        if (popup != null) {
            popup?.dismiss()
        }
        popup = PopupMenu(this@JunkfoodFlaggingActivity, view, Gravity.END)
        popup?.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.item_Unflag) {
                try {
                    if (isLoadFirstTime && isFlagApp) {
                        showAlertForFirstTime(positionPopUP, view)
                    } else {
                        popup?.dismiss()
                        runOnUiThread {
                            showEmptyRowBeforeDelete(view)
                            val handler = Handler()
                            handler.postDelayed({
                                if (adapterlist.contains(packagename)) {
                                    adapterlist.remove(packagename)
                                } else {
                                    adapterlist.add(packagename)
                                }
                                firstPosition = binding?.listAllApps?.firstVisiblePosition ?: 0
                                FilterApps().execute()
                                binding?.listAllApps?.isEnabled = true
                                binding?.listAllApps?.isClickable = true
                            }, 300)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (item.itemId == R.id.item_Info) {
                try {
                    PackageUtil.appSettings(this@JunkfoodFlaggingActivity, packagename)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            false
        }
        popup?.menuInflater?.inflate(R.menu.junkfood_popup, popup?.menu)
        val menuItem = popup?.menu?.findItem(R.id.item_Unflag)
        menuItem?.title =
            if (adapterlist.contains(packagename)) getString(R.string.unflagapp) else getString(R.string.flag_app)
        popup?.show()
        popup?.setOnDismissListener { popup = null }
    }

    private fun showEmptyRowBeforeDelete(view: View) {
        binding?.listAllApps?.isEnabled = false
        binding?.listAllApps?.isClickable = false
        val textView = view.findViewById<TextView>(R.id.txtAppName)
        val imageView = view.findViewById<ImageView>(R.id.imgAppIcon)
        val imageViewChevron = view
            .findViewById<ImageView>(R.id.imgChevron)
        if (null != textView) {
            textView.text = ""
        }
        imageView?.setImageDrawable(null)
        imageViewChevron?.setImageDrawable(null)
    }

    private fun showAlertForFirstTime(position: Int, itemView: View?) {
        val builder = AlertDialog.Builder(this@JunkfoodFlaggingActivity)
        builder.setTitle(getString(R.string.are_you_sure))
        builder.setMessage(R.string.msg_flag_first_time)
        builder.setPositiveButton(getString(R.string.yes_unhide)) { dialog, which ->
            dialog.dismiss()
            runOnUiThread {
                isLoadFirstTime = false
                itemView?.let { showEmptyRowBeforeDelete(it) }
                val handler = Handler()
                handler.postDelayed({
                    if (adapterlist.contains(bindingList[position].packageName)) {
                        adapterlist.remove(bindingList[position].packageName)
                    } else {
                        adapterlist.add(bindingList[position].packageName)
                    }
                    firstPosition = binding?.listAllApps?.firstVisiblePosition ?: 0
                    FilterApps().execute()
                    binding?.listAllApps?.isEnabled = true
                    binding?.listAllApps?.isClickable = true
                }, 200)
            }
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, which ->
            isLoadFirstTime = false
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.dialog_blue))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.dialog_red))
    }

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
        loadApps()
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            popup?.dismiss()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    internal inner class FilterApps : AsyncTask<String?, String?, ArrayList<AppListInfo>>() {
        override fun onPreExecute() {
            super.onPreExecute()
            isClickOnView = false
            flagAppList = ArrayList()
            unflageAppList = ArrayList()
            bindingList = ArrayList()
        }

        override fun doInBackground(vararg params: String?): ArrayList<AppListInfo> {
            try {
                for (resolveInfo in installedPackageList.orEmpty()) {
                    if (!resolveInfo.equals(packageName, ignoreCase = true)) {
                        val applicationname = CoreApplication.getInstance()
                            .listApplicationName[resolveInfo]
                        if (!TextUtils.isEmpty(applicationname)) {
                            if (adapterlist.contains(resolveInfo)) {
                                flagAppList.add(AppListInfo(resolveInfo, applicationname, false, false, true))
                            } else {
                                unflageAppList.add(AppListInfo(resolveInfo, applicationname, false, false, false))
                            }
                        }
                    }
                }
                //Code for removing the junk app from Favorite Sorted Menu and
                //Favorite List
                removeJunkAppsFromFavorites()
                if (flagAppList.size == 0) {
                    flagAppList.add(AppListInfo("", "", true, true, true))
                } else {
                    flagAppList.add(0, AppListInfo("", "", true, false, true))
                }
                flagAppList = Sorting.sortApplication(flagAppList)
                bindingList.addAll(flagAppList)
                if (unflageAppList.size == 0) {
                    unflageAppList.add(AppListInfo("", "", true, true, false))
                } else {
                    unflageAppList.add(0, AppListInfo("", "", true, false, false))
                }
                unflageAppList = Sorting.sortApplication(unflageAppList)
                bindingList.addAll(unflageAppList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return bindingList
        }

        override fun onPostExecute(s: ArrayList<AppListInfo>) {
            super.onPostExecute(s)
            try {
                binding?.listAllApps?.apply {
                    if (binding?.listAllApps != null) {
                        bindingList = s
                        onItemClickListener = this@JunkfoodFlaggingActivity
                        if (junkfoodFlaggingAdapter != null) {
                            junkfoodFlaggingAdapter?.setData(bindingList)
                            binding?.edtSearch?.setText("")
                            setSelection(firstPosition)
                        }
                        isClickOnView = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        init {
            binding?.listAllApps?.onItemClickListener = null
        }
    }
}