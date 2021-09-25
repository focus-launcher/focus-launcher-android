package io.focuslauncher.phone.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import de.greenrobot.event.EventBus
import io.focuslauncher.R
import io.focuslauncher.databinding.ActivityToolSelectionBinding
import io.focuslauncher.phone.adapters.ToolsListAdapter
import io.focuslauncher.phone.app.CoreApplication
import io.focuslauncher.phone.event.NotifyBottomView
import io.focuslauncher.phone.event.NotifySearchRefresh
import io.focuslauncher.phone.event.NotifyToolView
import io.focuslauncher.phone.helper.FirebaseHelper
import io.focuslauncher.phone.main.MainListItemLoader
import io.focuslauncher.phone.models.AppMenu
import io.focuslauncher.phone.models.MainListItem
import io.focuslauncher.phone.service.LoadToolPane
import io.focuslauncher.phone.utils.PrefSiempo
import io.focuslauncher.phone.utils.Sorting
import io.focuslauncher.phone.utils.bindView
import io.focuslauncher.phone.utils.lifecycleProperty

class ToolSelectionActivity : CoreActivity() {

    private var binding: ActivityToolSelectionBinding? by lifecycleProperty()

    private val map: HashMap<Int, AppMenu> by lazy { CoreApplication.getInstance().toolsSettings }
    private var items = ArrayList<MainListItem>()
    private var startTime: Long = 0
    private var adapterList: ArrayList<MainListItem>? = null

    private var toolsAdapter: ToolsListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindView(ActivityToolSelectionBinding::inflate)
        initView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_assignment_list, menu)
        val menuItem = menu.findItem(R.id.item_save)
        menuItem.setOnMenuItemClickListener {
            if (toolsAdapter != null) {
                for (mainListItem in adapterList.orEmpty()) {
                    map[mainListItem.id]?.isVisible = mainListItem.isVisable
                }
                PrefSiempo.getInstance(this@ToolSelectionActivity).write(PrefSiempo.TOOLS_SETTING, Gson().toJson(map))
                EventBus.getDefault().postSticky(NotifyBottomView(true))
                EventBus.getDefault().postSticky(NotifyToolView(true))
                finish()
            }
            false
        }
        return super.onCreateOptionsMenu(menu)
    }


    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()
        if (toolsAdapter != null) {
            PrefSiempo.getInstance(this@ToolSelectionActivity)
                .write(PrefSiempo.TOOLS_SETTING, Gson().toJson(toolsAdapter?.map.orEmpty()))
            EventBus.getDefault().postSticky(NotifyBottomView(true))
            EventBus.getDefault().postSticky(NotifyToolView(true))
        }
        LoadToolPane().execute()
        EventBus.getDefault().postSticky(NotifySearchRefresh(true))
        FirebaseHelper.getInstance().logScreenUsageTime(this.javaClass.simpleName, startTime)
    }

    private fun initView() {
        binding?.toolbar?.apply {
            setTitle(R.string.select_tools)
            setSupportActionBar(this)
        }
        filterListData()
        toolsAdapter = ToolsListAdapter(this, adapterList, map)
        binding?.recyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = toolsAdapter
        }
        toolsAdapter?.notifyDataSetChanged()
    }

    private fun filterListData() {
        //Copy List
        items = ArrayList()
        MainListItemLoader().loadItemsDefaultApp(items)
        for (i in items.indices) {
            map[items[i].id]!!.isVisible.also { items[i].isVisable = it }
        }

        //original list which will be edited
        val adapterList = ArrayList<MainListItem>()
        MainListItemLoader().loadItemsDefaultApp(adapterList)
        val size = adapterList.size
        for (i in 0 until size) {
            map[adapterList[i].id]?.isVisible?.also { adapterList[i].isVisable = it }
        }
        this.adapterList = Sorting.sortToolAppAssignment(this, adapterList)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int, data: Intent?
    ) {
        if (requestCode == TOOL_SELECTION) {
            if (resultCode == RESULT_OK) {
                toolsAdapter?.refreshEvents(adapterList)
            } else if (resultCode == RESULT_CANCELED) {
                toolsAdapter?.changeClickble(true)
                toolsAdapter?.notifyDataSetChanged()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onBackPressed() {
        val map = toolsAdapter?.map.orEmpty()
        for (mainListItem in items) {
            map[mainListItem.id]?.isVisible = mainListItem.isVisable
        }
        PrefSiempo.getInstance(this@ToolSelectionActivity).write(PrefSiempo.TOOLS_SETTING, Gson().toJson(map))
        super.onBackPressed()
    }

    companion object {
        const val TOOL_SELECTION = 100
    }
}