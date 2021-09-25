package io.focuslauncher.phone.activities

import io.focuslauncher.phone.interfaces.OnFavoriteItemListChangedListener
import io.focuslauncher.phone.main.OnStartDragListener
import io.focuslauncher.phone.models.MainListItem
import io.focuslauncher.phone.adapters.FavoritePositioningAdapter
import androidx.recyclerview.widget.RecyclerView
import io.focuslauncher.phone.customviews.ItemOffsetDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import android.os.Bundle
import io.focuslauncher.R
import io.focuslauncher.phone.utils.PrefSiempo
import android.text.TextUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import androidx.core.content.ContextCompat
import io.focuslauncher.phone.service.LoadFavoritePane
import io.focuslauncher.phone.helper.FirebaseHelper
import io.focuslauncher.phone.utils.PackageUtil
import androidx.recyclerview.widget.GridLayoutManager
import io.focuslauncher.phone.app.CoreApplication
import io.focuslauncher.phone.main.SimpleItemTouchHelperCallback
import android.content.Intent
import android.net.Uri
import android.view.Menu
import com.google.gson.Gson
import io.focuslauncher.databinding.ActivityFavoriteAppsPositioningBinding
import io.focuslauncher.phone.util.AppUtils
import io.focuslauncher.phone.utils.bindView
import io.focuslauncher.phone.utils.lifecycleProperty
import java.io.File
import java.lang.Exception
import java.util.ArrayList

class FavoriteAppsPositionActivity : CoreActivity(), OnFavoriteItemListChangedListener, OnStartDragListener {

    private var items = ArrayList<MainListItem>()

    private var favoritesAdapter: FavoritePositioningAdapter? = null
    private var itemTouchHelper: ItemTouchHelper? = null
    private var startTime: Long = 0

    private var binding: ActivityFavoriteAppsPositioningBinding? by lifecycleProperty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindView(ActivityFavoriteAppsPositioningBinding::inflate)
        val filePath = PrefSiempo.getInstance(this).read(PrefSiempo.DEFAULT_BAG, "")
        try {
            if (!TextUtils.isEmpty(filePath)) {
                Glide.with(this)
                    .load(Uri.fromFile(File(filePath)))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding?.imgBackground)
                binding?.linMain?.setBackgroundColor(ContextCompat.getColor(this, R.color.trans_black_bg))
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_junkfood_flagging, menu)
        val menuItem = menu.findItem(R.id.item_save)
        menuItem.setOnMenuItemClickListener {
            finish()
            false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
        initView()
    }

    override fun onPause() {
        super.onPause()
        LoadFavoritePane(PrefSiempo.getInstance(this)).execute()
        FirebaseHelper.getInstance()
            .logScreenUsageTime(this@FavoriteAppsPositionActivity.javaClass.simpleName, startTime)
    }

    private fun initView() {
        binding?.toolbar?.apply {
            setTitle(R.string.editing_frequently_apps)
            setSupportActionBar(this)
            setOnClickListener { finish() }
        }
        items = ArrayList()
        items = PackageUtil.getFavoriteList(this, false)
        favoritesAdapter = FavoritePositioningAdapter(
            this,
            CoreApplication.getInstance().isHideIconBranding,
            items,
            this,
            this
        )
        val callback: ItemTouchHelper.Callback = SimpleItemTouchHelperCallback(favoritesAdapter, this)
        itemTouchHelper = ItemTouchHelper(callback)
        binding?.recyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 4)
            addItemDecoration(ItemOffsetDecoration(context, R.dimen.dp_10))
            adapter = favoritesAdapter
            itemTouchHelper?.attachToRecyclerView(this)
        }
        binding?.txtSelectTools?.setOnClickListener {
            val intent = Intent(this@FavoriteAppsPositionActivity, FavoritesSelectionActivity::class.java)
            startActivity(intent)
            overridePendingTransition(
                R.anim.fade_in,
                R.anim.fade_out
            )
        }
        binding?.relTop?.setOnClickListener { finish() }
        binding?.relPane?.setOnClickListener { finish() }
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper?.startDrag(viewHolder)
    }

    override fun onFavoriteItemListChanged(customers: ArrayList<MainListItem>) {
        val listOfSortedCustomerId = ArrayList<String>()
        for (customer in customers) {
            listOfSortedCustomerId.add(customer.packageName)
        }
        val gson = Gson()
        val jsonListOfSortedCustomerIds = gson.toJson(listOfSortedCustomerId)
        PrefSiempo.getInstance(this).write(PrefSiempo.FAVORITE_SORTED_MENU, jsonListOfSortedCustomerIds)
    }
}