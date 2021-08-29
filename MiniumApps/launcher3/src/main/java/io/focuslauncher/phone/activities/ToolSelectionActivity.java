package io.focuslauncher.phone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import io.focuslauncher.R;
import io.focuslauncher.phone.adapters.ToolsListAdapter;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.event.NotifyBottomView;
import io.focuslauncher.phone.event.NotifySearchRefresh;
import io.focuslauncher.phone.event.NotifyToolView;
import io.focuslauncher.phone.helper.FirebaseHelper;
import io.focuslauncher.phone.main.MainListItemLoader;
import io.focuslauncher.phone.models.AppMenu;
import io.focuslauncher.phone.models.MainListItem;
import io.focuslauncher.phone.service.LoadToolPane;
import io.focuslauncher.phone.utils.PrefSiempo;
import io.focuslauncher.phone.utils.Sorting;
import de.greenrobot.event.EventBus;

public class ToolSelectionActivity extends CoreActivity {

    public static final int TOOL_SELECTION = 100;
    private HashMap<Integer, AppMenu> map;
    private Toolbar toolbar;
    private ArrayList<MainListItem> items = new ArrayList<>();
    private LinearLayoutManager mLayoutManager;
    private RecyclerView recyclerView;
    private ToolsListAdapter mAdapter;
    private long startTime = 0;
    private ArrayList<MainListItem> topItems = new ArrayList<>(12);
    private ArrayList<MainListItem> bottomItems = new ArrayList<>(4);
    private ArrayList<MainListItem> adapterList;
    private List<Long> listOfSortedUpdatedCustomersId = new ArrayList<>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_assignment_list, menu);
        MenuItem menuItem = menu.findItem(R.id.item_save);
//        setTextColorForMenuItem(menuItem, R.color.colorAccent);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (mAdapter != null) {
                    for (MainListItem mainListItem : adapterList) {
                        map.get(mainListItem.getId()).setVisible(mainListItem
                                .isVisable());
                    }

                    PrefSiempo.getInstance(ToolSelectionActivity.this).write(PrefSiempo.TOOLS_SETTING, new Gson().toJson(map));
                    EventBus.getDefault().postSticky(new NotifyBottomView(true));
                    EventBus.getDefault().postSticky(new NotifyToolView(true));
                    finish();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * change text color of Menuitem
     *
     * @param menuItem
     * @param color
     */
    private void setTextColorForMenuItem(MenuItem menuItem, @ColorRes int color) {
        SpannableString spanString = new SpannableString(menuItem.getTitle().toString());
        spanString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, color)), 0, spanString.length(), 0);
        menuItem.setTitle(spanString);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_selection);
        map = CoreApplication.getInstance().getToolsSettings();
//        topItems = (ArrayList<MainListItem>) getIntent().getExtras().getSerializable("TopList");
//        bottomItems = (ArrayList<MainListItem>) getIntent().getExtras().getSerializable("BottomList");

        initView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            PrefSiempo.getInstance(ToolSelectionActivity.this).write(PrefSiempo.TOOLS_SETTING, new Gson().toJson(mAdapter.getMap()));
            EventBus.getDefault().postSticky(new NotifyBottomView(true));
            EventBus.getDefault().postSticky(new NotifyToolView(true));
        }
        new LoadToolPane().execute();
        EventBus.getDefault().postSticky(new NotifySearchRefresh(true));
        FirebaseHelper.getInstance().logScreenUsageTime(this.getClass().getSimpleName(), startTime);
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.select_tools);
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        filterListData();
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ToolsListAdapter(this, adapterList, map);
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private void filterListData() {
        //Copy List
        items = new ArrayList<>();
        new MainListItemLoader().loadItemsDefaultApp(items);
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setVisable(map.get(items.get(i).getId()).isVisible());
        }

        //original list which will be edited
        adapterList = new ArrayList<>();
        new MainListItemLoader().loadItemsDefaultApp(adapterList);
        int size = adapterList.size();
        for (int i = 0; i < size; i++) {
            adapterList.get(i).setVisable(map.get(adapterList.get(i).getId()).isVisible());
        }
        adapterList = Sorting.sortToolAppAssignment(this, adapterList);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TOOL_SELECTION) {
            if (resultCode == RESULT_OK) {
                mAdapter.refreshEvents(adapterList);
            } else if (resultCode == RESULT_CANCELED) {
                mAdapter.changeClickble(true);
                mAdapter.notifyDataSetChanged();
            }
        }
    }


    /**
     * Check items already exists in tool position array list.
     *
     * @param id
     * @return
     */
    public boolean checkItemContains(int id) {

        Log.d("Rajesh", "Un Check::-" + listOfSortedUpdatedCustomersId);
//        for (MainListItem mainListItem : ToolPositioningActivity.sortedList) {
//            if (mainListItem.getId() == id) {
//                return true;
//            }
//        }
        return false;
    }

    /**
     * return first invisible items from list.
     *
     * @return
     */
    public int invisibleItemId() {
        ArrayList<MainListItem> itemsLocal = new ArrayList<>();
        new MainListItemLoader().loadItemsDefaultApp(itemsLocal);
//        for (MainListItem mainListItem : ToolPositioningActivity.sortedList) {
//            if (!mainListItem.isVisable()) {
//                return mainListItem.getId();
//            }
//        }
        return -1;
    }

    public void hideItemInSortedList(int id, boolean isVisible) {
//        ListIterator listIterator = ToolPositioningActivity.sortedList.listIterator();
//        while (listIterator.hasNext()) {
//            MainListItem next = (MainListItem) listIterator.next();
//            if (next.getId() == id) {
//                next.setVisable(isVisible);
//            }
//        }
    }


    public boolean replaceData(int oldId, int newId) {
        ListIterator<Long> iterator = listOfSortedUpdatedCustomersId.listIterator();
        while (iterator.hasNext()) {
            long next = iterator.next();
            if (next == oldId) {
                //Replace element
                iterator.set((long) newId);
            }
        }

//        int idToRemove = -1;
//        MainListItem mainListItemAdd = null;
//        for (int i = 0; i < ToolPositioningActivity.sortedList.size(); i++) {
//            if (ToolPositioningActivity.sortedList.get(i).getId() == oldId) {
//                idToRemove = i;
//            }
//            if (ToolPositioningActivity.sortedList.get(i).getId() == newId) {
//                mainListItemAdd = ToolPositioningActivity.sortedList.get(i);
//            }
//
//
//        }
//
//        if (idToRemove != -1) {
//            ToolPositioningActivity.sortedList.remove(idToRemove);
//        }
//
//        if (mainListItemAdd != null) {
//            ToolPositioningActivity.sortedList.add(mainListItemAdd);
//        }
//        Log.d("Rajesh", "oldId:" + oldId + "   " + "newId:" + newId);
//        Log.d("Rajesh", "Replace::-" + listOfSortedUpdatedCustomersId);
        return false;
    }


    @Override
    public void onBackPressed() {
        map = mAdapter.getMap();
        for (MainListItem mainListItem : items) {
            map.get(mainListItem.getId()).setVisible(mainListItem
                    .isVisable());
        }
        PrefSiempo.getInstance(ToolSelectionActivity.this).write(PrefSiempo.TOOLS_SETTING, new Gson().toJson(map));
        super.onBackPressed();


    }
}
