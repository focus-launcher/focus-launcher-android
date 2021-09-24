package io.focuslauncher.phone.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import io.focuslauncher.R;
import io.focuslauncher.phone.adapters.FavoriteFlaggingAdapter;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.event.AppInstalledEvent;
import io.focuslauncher.phone.helper.FirebaseHelper;
import io.focuslauncher.phone.models.AppListInfo;
import io.focuslauncher.phone.service.LoadFavoritePane;
import io.focuslauncher.phone.service.LoadJunkFoodPane;
import io.focuslauncher.phone.utils.PackageUtil;
import io.focuslauncher.phone.utils.PrefSiempo;
import io.focuslauncher.phone.utils.Sorting;
import io.focuslauncher.phone.utils.UIUtils;
import de.greenrobot.event.Subscribe;

public class FavoritesSelectionActivity extends CoreActivity implements AdapterView.OnItemClickListener {

    public Set<String> list = new HashSet<>();
    public Set<String> adapterList = new HashSet<>();
    //Junk list removal will be needed here as we need to remove the
    //junk-flagged app from other app list which cn be marked as favorite
    Set<String> junkFoodList = new HashSet<>();
    FavoriteFlaggingAdapter junkfoodFlaggingAdapter;
    int firstPosition;
    List<String> installedPackageList;
    private Toolbar toolbar;
    private ListView listAllApps;
    private PopupMenu popup;
    private boolean isLoadFirstTime = true;
    private List<AppListInfo> favoriteList = new ArrayList<>();
    private List<AppListInfo> unfavoriteList = new ArrayList<>();
    private ArrayList<AppListInfo> bindingList = new ArrayList<>();
    private long startTime = 0;
    private boolean isClickOnView;
    private CardView cardView;
    private ImageView imgClear;
    private EditText edtSearch;

    @Subscribe
    public void appInstalledEvent(AppInstalledEvent event) {
        if (!isFinishing() && event.isAppInstalledSuccessfully()) {
            loadApps();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_selection);
        initView();
        list = PrefSiempo.getInstance(this).read(PrefSiempo.FAVORITE_APPS, new HashSet<String>());
        adapterList = new HashSet<>();
        junkFoodList = PrefSiempo.getInstance(this).read(PrefSiempo.JUNKFOOD_APPS, new HashSet<String>());
        list.removeAll(junkFoodList);
        adapterList.addAll(list);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        if (bindingList != null && bindingList.get(position) != null
//                && !bindingList.get(position).packageName.equalsIgnoreCase("")) {
//            showPopUp(view, bindingList.get(position).isFlagApp, position);
//        }
    }

    /**
     * Initialize the view.
     */
    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_flagging_screen);
        setSupportActionBar(toolbar);
        listAllApps = findViewById(R.id.lst_OtherApps);

        cardView = findViewById(R.id.cardView);
        imgClear = findViewById(R.id.imgClear);
        edtSearch = findViewById(R.id.edtSearch);
        listAllApps.setOnItemClickListener(FavoritesSelectionActivity.this);
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (junkfoodFlaggingAdapter != null) {
                    junkfoodFlaggingAdapter.getFilter().filter(s.toString());
                }
                if (s.toString().length() > 0) {
                    imgClear.setVisibility(View.VISIBLE);
                } else {
                    imgClear.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        imgClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtSearch.setText("");
            }
        });
    }


    /**
     * load system apps and filter the application for junkfood and normal.
     */
    private void loadApps() {
        List<String> installedPackageListLocal = CoreApplication.Companion.getInstance().getPackagesList();
        List<String> appList = new ArrayList<>(installedPackageListLocal);
        installedPackageList = appList;
        bindData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_assignment_list, menu);
        MenuItem menuItem = menu.findItem(R.id.item_save);
//        setTextColorForMenuItem(menuItem, R.color.colorAccent);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                junkFoodList.removeAll(adapterList);
                String jsonListOfSortedFavorites = PrefSiempo.getInstance(FavoritesSelectionActivity.this).read(PrefSiempo.FAVORITE_SORTED_MENU, "");
                //convert onNoteListChangedJSON array into a List<Long>
                Gson gson1 = new Gson();
                List<String> listOfSortFavoritesApps = gson1.fromJson(jsonListOfSortedFavorites, new TypeToken<List<String>>() {
                }.getType());

                for (ListIterator<String> it =
                     listOfSortFavoritesApps.listIterator(); it.hasNext
                        (); ) {
                    String packageName = it.next();
                    if (!adapterList.contains(packageName)) {
                        //Used List Iterator to set empty
                        // value for package name retaining
                        // the positions of elements
                        it.set("");
                    }
                }

                Gson gson2 = new Gson();
                String jsonListOfFavoriteApps = gson2.toJson(listOfSortFavoritesApps);
                PrefSiempo.getInstance(FavoritesSelectionActivity.this).write(PrefSiempo.FAVORITE_SORTED_MENU, jsonListOfFavoriteApps);


                PrefSiempo.getInstance(FavoritesSelectionActivity.this).write
                        (PrefSiempo.FAVORITE_APPS, adapterList);
                PrefSiempo.getInstance(FavoritesSelectionActivity.this).write(PrefSiempo.JUNKFOOD_APPS, junkFoodList);
                new LoadJunkFoodPane(PrefSiempo.getInstance(FavoritesSelectionActivity.this)).execute();
                new LoadFavoritePane(PrefSiempo.getInstance(FavoritesSelectionActivity.this)).execute();
                finish();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * change text color of menuitem
     *
     * @param menuItem
     * @param color
     */
    private void setTextColorForMenuItem(MenuItem menuItem, @ColorRes int color) {
        SpannableString spanString = new SpannableString(menuItem.getTitle().toString());
        spanString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, color)), 0, spanString.length(), 0);
        menuItem.setTitle(spanString);
    }

    /**
     * bind the list view of flag app and all apps.
     */
    private void bindData() {
        try {
            favoriteList = new ArrayList<>();
            unfavoriteList = new ArrayList<>();
            bindingList = new ArrayList<>();

            for (String resolveInfo : installedPackageList) {
                if (resolveInfo != null && !resolveInfo.equalsIgnoreCase(getPackageName())) {
                    boolean isEnable = UIUtils.isAppInstalledAndEnabled(this, resolveInfo);
                    if (isEnable) {
                        String applicationname = CoreApplication.Companion.getInstance()
                                .getListApplicationName().get(resolveInfo);
                        if (!TextUtils.isEmpty(applicationname)) {
                            if (adapterList.contains(resolveInfo)) {
                                favoriteList.add(new AppListInfo(resolveInfo, applicationname,
                                        false, false, true));
                            } else {
                                if (null != junkFoodList && !junkFoodList
                                        .contains(resolveInfo)) {
                                    unfavoriteList.add(new AppListInfo(resolveInfo,
                                            applicationname, false, false, false));
                                }
                            }
                        }
                    }
                }
            }
            setToolBarText(favoriteList.size());
            if (favoriteList.size() == 0) {
                favoriteList.add(new AppListInfo("", "", true, true, true));
            } else {
                favoriteList.add(0, new AppListInfo("", "", true, false, true));
            }
            favoriteList = Sorting.sortApplication(favoriteList);
            bindingList.addAll(favoriteList);

            if (unfavoriteList.size() == 0) {
                unfavoriteList.add(new AppListInfo("", "", true, true, false));
            } else {
                unfavoriteList.add(0, new AppListInfo("", "", true, false, false));
            }
            unfavoriteList = Sorting.sortApplication(unfavoriteList);
            bindingList.addAll(unfavoriteList);
            junkfoodFlaggingAdapter = new FavoriteFlaggingAdapter(this, bindingList);
            listAllApps.setAdapter(junkfoodFlaggingAdapter);
            listAllApps.setOnItemClickListener(this);
            junkfoodFlaggingAdapter.getFilter().filter(edtSearch.getText().toString());
            listAllApps.setSelection(firstPosition);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * show pop dialog on List item click for flag/un-flag and application information.
     */
    public void showPopUp(View view, final String packagename, final boolean isFlagApp) {

        if (popup != null) {
            popup.dismiss();
        }
        popup = new PopupMenu(FavoritesSelectionActivity.this, view, Gravity.END);
        popup.getMenuInflater().inflate(R.menu.junkfood_popup, popup.getMenu());
        MenuItem menuItem = popup.getMenu().findItem(R.id.item_Unflag);
        if (isFlagApp) {
            if (favoriteList
                    .size() == 2) {
                menuItem.setVisible(false);
            } else {
                menuItem.setVisible(true);
            }
        } else {
            if (favoriteList != null && (favoriteList.size() < 13)) {
                menuItem.setVisible(true);
            } else {
                menuItem.setVisible(false);
            }
        }
        menuItem.setTitle(isFlagApp ? getString(R.string.favorite_menu_unselect) : getString(R.string.favorite_menu_select));
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.item_Unflag) {
                    try {
                        popup.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (adapterList.contains(packagename)) {
                                    adapterList.remove(packagename);
                                    isLoadFirstTime = false;
                                    //setToolBarText(favoriteList.size());
                                } else {

                                    if (favoriteList != null && favoriteList.size() < 13) {
                                        adapterList.add(packagename);
                                        isLoadFirstTime = false;
                                    }
                                    // setToolBarText(favoriteList.size());
                                }
                                firstPosition = listAllApps.getFirstVisiblePosition();
                                new FilterApps(true, FavoritesSelectionActivity.this).execute();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if (item.getItemId() == R.id.item_Info) {
                    try {
                        PackageUtil.appSettings(FavoritesSelectionActivity.this, packagename);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

        popup.show();
        popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                popup = null;
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        startTime = System.currentTimeMillis();
        loadApps();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseHelper.getInstance().logScreenUsageTime(this.getClass().getSimpleName(), startTime);
    }

    public void setToolBarText(int count) {
        int remainapps = 12 - count;
        toolbar.setTitle("Select up to " + remainapps + " more apps");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (popup != null) {
                popup.dismiss();
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    class FilterApps extends AsyncTask<String, String, ArrayList<AppListInfo>> {
        boolean isNotify;
        int size;
        private WeakReference<FavoritesSelectionActivity> activityReference;

        FilterApps(boolean isNotify, FavoritesSelectionActivity context) {
            activityReference = new WeakReference<>(context);
            this.isNotify = isNotify;
            listAllApps.setOnItemClickListener(null);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isClickOnView = false;
            favoriteList = new ArrayList<>();
            unfavoriteList = new ArrayList<>();
            bindingList = new ArrayList<>();
        }

        @Override
        protected ArrayList<AppListInfo> doInBackground(String... strings) {
            try {
                for (String resolveInfo : installedPackageList) {
                    if (!resolveInfo.equalsIgnoreCase(getPackageName())) {
                        boolean isEnable = UIUtils.isAppInstalledAndEnabled(FavoritesSelectionActivity.this, resolveInfo);
                        if (isEnable) {
                            String applicationname = CoreApplication.Companion.getInstance()
                                    .getListApplicationName().get(resolveInfo);
                            if (!TextUtils.isEmpty(applicationname)) {
                                if (adapterList.contains(resolveInfo)) {
                                    favoriteList.add(new AppListInfo(resolveInfo, applicationname,
                                            false, false,
                                            true));
                                } else {
                                    if (null != junkFoodList && !junkFoodList
                                            .contains(resolveInfo)) {
                                        unfavoriteList.add(new AppListInfo(resolveInfo,
                                                applicationname, false,
                                                false, false));
                                    }
                                }
                            }
                        }
                    }
                }
                size = favoriteList.size();

                if (favoriteList.size() == 0) {
                    favoriteList.add(new AppListInfo("", "", true, true, true));
                } else {
                    favoriteList.add(0, new AppListInfo("", "", true, false, true));
                }
                favoriteList = Sorting.sortApplication(favoriteList);
                bindingList.addAll(favoriteList);
               /* if(bindingList.size()==0){
                    bindingList.add(new AppListInfo(Constants.SETTINGS_APP_PACKAGE,"Settings",
                            true,false,false));
                }*/

                if (unfavoriteList.size() == 0) {
                    unfavoriteList.add(new AppListInfo("", "", true, true, false));
                } else {
                    unfavoriteList.add(0, new AppListInfo("", "", true, false, false));
                }
                unfavoriteList = Sorting.sortApplication(unfavoriteList);
                bindingList.addAll(unfavoriteList);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bindingList;
        }

        @Override
        protected void onPostExecute(ArrayList<AppListInfo> s) {
            super.onPostExecute(s);
            try {
                FavoritesSelectionActivity activity = activityReference.get();
                if (activity == null || activity.isFinishing()) return;

                if (toolbar != null) {
                    setToolBarText(size);
                    junkfoodFlaggingAdapter = new FavoriteFlaggingAdapter(FavoritesSelectionActivity.this, bindingList);
                    listAllApps.setAdapter(junkfoodFlaggingAdapter);
                    listAllApps.setOnItemClickListener(FavoritesSelectionActivity.this);
                    if (isNotify) {
                        junkfoodFlaggingAdapter.notifyDataSetChanged();
                        edtSearch.setText("");
                        listAllApps.setSelection(firstPosition);
                    }
                }
                isClickOnView = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}

