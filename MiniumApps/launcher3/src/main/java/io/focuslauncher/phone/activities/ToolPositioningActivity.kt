package io.focuslauncher.phone.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.Gson
import io.focuslauncher.R
import io.focuslauncher.databinding.ActivityToolPositioningBinding
import io.focuslauncher.phone.adapters.ToolPositioningAdapter
import io.focuslauncher.phone.app.CoreApplication
import io.focuslauncher.phone.customviews.ItemOffsetDecoration
import io.focuslauncher.phone.helper.FirebaseHelper
import io.focuslauncher.phone.interfaces.OnToolItemListChangedListener
import io.focuslauncher.phone.main.MainListItemLoader
import io.focuslauncher.phone.main.OnStartDragListener
import io.focuslauncher.phone.main.SimpleItemTouchHelperCallback
import io.focuslauncher.phone.models.AppMenu
import io.focuslauncher.phone.models.MainListItem
import io.focuslauncher.phone.service.LoadToolPane
import io.focuslauncher.phone.util.AppUtils
import io.focuslauncher.phone.utils.PackageUtil
import io.focuslauncher.phone.utils.PrefSiempo
import io.focuslauncher.phone.utils.bindView
import io.focuslauncher.phone.utils.lifecycleProperty
import java.io.File

class ToolPositioningActivity : CoreActivity(), OnToolItemListChangedListener, OnStartDragListener {
    var map = HashMap<Int, AppMenu>()
    private var items = ArrayList<MainListItem>()
    private var sortedList = ArrayList<MainListItem>()

    private var binding: ActivityToolPositioningBinding? by lifecycleProperty()

    private var toolsAdapter: ToolPositioningAdapter? = null
    private var mItemTouchHelper: ItemTouchHelper? = null
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindView(ActivityToolPositioningBinding::inflate)
        val filePath = PrefSiempo.getInstance(this).read(PrefSiempo.DEFAULT_BAG, "")
        try {
            if (!TextUtils.isEmpty(filePath)) {
                Glide.with(this)
                    .load(Uri.fromFile(File(filePath)))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding?.imgBackground)
                binding?.linMain?.setBackgroundColor(ContextCompat.getColor(this, R.color.trans_black_bg))
            } else {
                binding?.imgBackground?.setImageBitmap(null)
                binding?.imgBackground?.background = null
                binding?.linMain?.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        AppUtils.notificationBarManaged(this, null)
        AppUtils.statusBarManaged(this)
        AppUtils.statusbarColor0(this, 1)
    }

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
        map = CoreApplication.getInstance().toolsSettings
        initView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_junkfood_flagging, menu)
        val menuItem = menu.findItem(R.id.item_save)
        menuItem.setOnMenuItemClickListener { item: MenuItem? ->
            finish()
            false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPause() {
        super.onPause()
        for (i in sortedList.indices) {
            map[sortedList[i].id]?.isBottomDoc = i >= 40
        }
        val hashMapToolSettings = Gson().toJson(map)
        PrefSiempo.getInstance(this).write(PrefSiempo.TOOLS_SETTING, hashMapToolSettings)
        LoadToolPane().execute()
        FirebaseHelper.getInstance().logScreenUsageTime(this.javaClass.simpleName, startTime)
    }

    private fun initView() {
        binding?.toolbar?.apply {
            setTitle(R.string.editing_tools)
            setSupportActionBar(this)
            setOnClickListener { finish() }
        }
        items = ArrayList()
        MainListItemLoader().loadItemsDefaultApp(items)
        items = PackageUtil.getToolsMenuData(this, items)

        binding?.recyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 4)
            addItemDecoration(ItemOffsetDecoration(context, R.dimen.dp_10))
            toolsAdapter = ToolPositioningAdapter(
                this@ToolPositioningActivity,
                items,
                this@ToolPositioningActivity,
                this@ToolPositioningActivity,
                CoreApplication.getInstance().isHideIconBranding
            )
            val callback: ItemTouchHelper.Callback = SimpleItemTouchHelperCallback(toolsAdapter, context)
            mItemTouchHelper = ItemTouchHelper(callback)
            mItemTouchHelper?.attachToRecyclerView(this)
            adapter = toolsAdapter
        }

        binding?.txtSelectTools?.setOnClickListener {
            val intent = Intent(this@ToolPositioningActivity, ToolSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        binding?.linearTop?.setOnClickListener { finish() }
        binding?.relTop?.setOnClickListener { finish() }
    }

    override fun onToolItemListChanged(customers: ArrayList<MainListItem>, toposition: Int) {
        val listOfSortedCustomerId = ArrayList<Long>()
        for (customer in customers) {
            listOfSortedCustomerId.add(customer.id.toLong())
        }
        sortedList = customers
        val gson = Gson()
        val jsonListOfSortedCustomerIds = gson.toJson(listOfSortedCustomerId)
        PrefSiempo.getInstance(this).write(PrefSiempo.SORTED_MENU, jsonListOfSortedCustomerIds)
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        mItemTouchHelper?.startDrag(viewHolder)
    }
}