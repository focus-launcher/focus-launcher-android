package io.focuslauncher.phone.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

import io.focuslauncher.R;
import io.focuslauncher.phone.adapters.FavoritePositioningAdapter;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.customviews.ItemOffsetDecoration;
import io.focuslauncher.phone.helper.FirebaseHelper;
import io.focuslauncher.phone.interfaces.OnFavoriteItemListChangedListener;
import io.focuslauncher.phone.main.OnStartDragListener;
import io.focuslauncher.phone.main.SimpleItemTouchHelperCallback;
import io.focuslauncher.phone.models.MainListItem;
import io.focuslauncher.phone.service.LoadFavoritePane;
import io.focuslauncher.phone.util.AppUtils;
import io.focuslauncher.phone.utils.PackageUtil;
import io.focuslauncher.phone.utils.PrefSiempo;

public class FavoriteAppsPositionActivity extends CoreActivity implements OnFavoriteItemListChangedListener,
        OnStartDragListener {
    private ArrayList<MainListItem> items = new ArrayList<>();
    private ArrayList<MainListItem> sortedList = new ArrayList<>();
    private FavoritePositioningAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ItemOffsetDecoration itemDecoration;
    private ItemTouchHelper mItemTouchHelper;
    private Parcelable mListState;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private TextView txtSelectTools;
    private RelativeLayout relTop;
    private RelativeLayout relPane;
    private long startTime = 0;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
    private LinearLayout linMain;
    private RelativeLayout relMain;
    private ImageView imgBackground;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_apps_positioning);
        String filePath = PrefSiempo.getInstance(this).read(PrefSiempo
                .DEFAULT_BAG, "");
        linMain = findViewById(R.id.linMain);
        relMain = findViewById(R.id.relMain);
        imgBackground = findViewById(R.id.imgBackground);

        try {
            if (!TextUtils.isEmpty(filePath)) {
//                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
//
//                BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
//
//                //Code for Applying background
//                relMain.setBackground(ob);

                Glide.with(this)
                        .load(Uri.fromFile(new File(filePath))) // Uri of the
                        // picture
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imgBackground);
                linMain.setBackgroundColor(ContextCompat.getColor(this, R.color
                        .trans_black_bg));

                linMain.setBackgroundColor(ContextCompat.getColor(this, R.color.trans_black_bg));

            } else {

                imgBackground.setImageBitmap(null);
                imgBackground.setBackground(null);
                linMain.setBackgroundColor(ContextCompat.getColor(this, R.color
                        .transparent));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
       /*StatusBarUtil.setTranslucent(this);
        boolean read = PrefSiempo.getInstance(this).read(PrefSiempo.IS_DARK_THEME, false);
        if (read) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.white));
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }*/
        AppUtils.notificationBarManaged(this, null);
        AppUtils.statusBarManaged(this);
        AppUtils.statusbarColor0(this, 1);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_junkfood_flagging, menu);
        MenuItem menuItem = menu.findItem(R.id.item_save);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                finish();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTime = System.currentTimeMillis();
        initView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        new LoadFavoritePane(PrefSiempo.getInstance(this)).execute();
        FirebaseHelper.getInstance().logScreenUsageTime(FavoriteAppsPositionActivity.this.getClass().getSimpleName(), startTime);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void initView() {

        toolbar = findViewById(R.id.toolbar);
        relTop = findViewById(R.id.relTop);
        relPane = findViewById(R.id.relPane);
        toolbar.setTitle(R.string.editing_frequently_apps);
        setSupportActionBar(toolbar);
        items = new ArrayList<>();
        //Changed for SSA-1770 binding items for not empty FavList.
        items = PackageUtil.getFavoriteList(this, false);

        recyclerView = findViewById(R.id.recyclerView);
        txtSelectTools = findViewById(R.id.txtSelectTools);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(mLayoutManager);
        if (itemDecoration != null) {
            recyclerView.removeItemDecoration(itemDecoration);
        }
        itemDecoration = new ItemOffsetDecoration(this, R.dimen.dp_10);
        recyclerView.addItemDecoration(itemDecoration);


        mAdapter = new FavoritePositioningAdapter(this, CoreApplication.getInstance().isHideIconBranding(), items, this, this);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter, this);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(mAdapter);
        txtSelectTools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FavoriteAppsPositionActivity.this, FavoritesSelectionActivity.class);
                startActivity(intent);
                FavoriteAppsPositionActivity.this
                        .overridePendingTransition(R.anim
                                        .fade_in,
                                R.anim.fade_out);
            }
        });


        relTop.setOnClickListener(onClickListener);

        toolbar.setOnClickListener(onClickListener);

        relPane.setOnClickListener(onClickListener);

    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onFavoriteItemListChanged(ArrayList<MainListItem> customers) {
        ArrayList<String> listOfSortedCustomerId = new ArrayList<>();

        for (MainListItem customer : customers) {
            listOfSortedCustomerId.add(customer.getPackageName());
        }
        sortedList = customers;
        Gson gson = new Gson();
        String jsonListOfSortedCustomerIds = gson.toJson(listOfSortedCustomerId);
        PrefSiempo.getInstance(this).write(PrefSiempo.FAVORITE_SORTED_MENU, jsonListOfSortedCustomerIds);
    }


}
