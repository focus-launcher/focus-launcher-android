package io.focuslauncher.phone.fragments;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.eyeem.chips.ChipsEditText;
import com.eyeem.chips.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import io.focuslauncher.R;
import io.focuslauncher.phone.activities.CoreActivity;
import io.focuslauncher.phone.activities.DashboardActivity;
import io.focuslauncher.phone.activities.JunkfoodFlaggingActivity;
import io.focuslauncher.phone.activities.SettingsActivity;
import io.focuslauncher.phone.adapters.MainListAdapter;
import io.focuslauncher.phone.adapters.PanePagerAdapter;
import io.focuslauncher.phone.adapters.ToolsMenuAdapter;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.customviews.ItemOffsetDecoration;
import io.focuslauncher.phone.customviews.SearchLayout;
import io.focuslauncher.phone.event.HomePress;
import io.focuslauncher.phone.event.NotifyBottomView;
import io.focuslauncher.phone.event.NotifySearchRefresh;
import io.focuslauncher.phone.event.OnBackPressedEvent;
import io.focuslauncher.phone.event.SearchLayoutEvent;
import io.focuslauncher.phone.event.SendSmsEvent;
import io.focuslauncher.phone.helper.FirebaseHelper;
import io.focuslauncher.phone.log.Tracer;
import io.focuslauncher.phone.main.MainFragmentMediator;
import io.focuslauncher.phone.main.MainListAdapterEvent;
import io.focuslauncher.phone.models.MainListItem;
import io.focuslauncher.phone.token.TokenCompleteType;
import io.focuslauncher.phone.token.TokenItem;
import io.focuslauncher.phone.token.TokenItemType;
import io.focuslauncher.phone.token.TokenManager;
import io.focuslauncher.phone.token.TokenParser;
import io.focuslauncher.phone.token.TokenRouter;
import io.focuslauncher.phone.token.TokenUpdateEvent;
import io.focuslauncher.phone.ui.SiempoViewPager;
import io.focuslauncher.phone.utils.PrefSiempo;
import io.focuslauncher.phone.utils.UIUtils;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import me.relex.circleindicator.CircleIndicator;

import static io.focuslauncher.phone.utils.UIUtils.hasUsageStatsPermission;

/**
 * Main class for Tools Pane, Favorites Pane and JunkFood Pane.
 * Ui is changed based on which pane the user is currently on.
 * 1. Tools Pane
 * 2. Favourites Pane
 * 3. Junkfood Pane
 */
public class PaneFragment extends CoreFragment {

    public static boolean isSearchVisable = false;
    final int MIN_KEYBOARD_HEIGHT_PX = 150;
    public SiempoViewPager pagerPane;
    public View linSearchList;
    PanePagerAdapter mPagerAdapter;
    private LinearLayout linTopDoc;
    private LinearLayout linPane;
    private RelativeLayout linBottomDoc;
    private EditText edtSearchToolsRounded;
    private TextView txtTopDockDate;
    private SearchLayout searchLayout;
    private RelativeLayout relSearchTools;
    private ListView listView;
    private CardView cardViewEdtSearch;
    private View blueLineDivider;
    private TextView txtIntentionLabelJunkPane;
    private TextView txtIntention;
    private Window mWindow;
    private MainFragmentMediator mediator;
    private TokenRouter router;
    private MainListAdapter adapter;
    BroadcastReceiver mKeyBoardReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != intent && null != intent.getAction() && intent.getAction().equals
                    (Utils
                            .KEYBOARD_ACTION)) {
                if (intent.getBooleanExtra(Utils.ACTION, false)) {
                    updateListViewLayout(true);
                } else {
                    updateListViewLayout(false);

                }
            }
        }
    };
    private TokenParser parser;
    private RecyclerView recyclerViewBottomDoc;
    private List<MainListItem> items = new ArrayList<>();
    private ToolsMenuAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ItemOffsetDecoration itemDecoration;
    private ChipsEditText chipsEditText;
    private ImageView imageClear;
    private View rootView;
    private CircleIndicator indicator;
    private Dialog overlayDialog;
    private Dialog overlayDialogPermission;
    private View junkDoc;
    private View searchDoc;
    private View blueLineDividerBottom;
    private int backGroundColor;
    private int statusBarColorJunk;
    private int statusBarColorPane;
    private LinearLayout linMain;
    private boolean firstTimeLoad = true;

    public PaneFragment() {
        // Required empty public constructor
    }

    public static PaneFragment newInstance() {
        return new PaneFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_pane, container, false);
        linMain = rootView.findViewById(R.id.linMain);
        linMain.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                ((CoreActivity)getActivity()).gestureDetector.onTouchEvent(motionEvent);
                return false;
            }
        });
        Log.d("Test", "P1");
        context = (CoreActivity) getActivity();
        getColorOfStatusBar();
        initView(rootView);
        changeColorOfStatusBar();

        mediator = new MainFragmentMediator(PaneFragment.this);
        mediator.loadData();
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.junk_top, typedValue, true);
        backGroundColor = typedValue.resourceId;
        theme.resolveAttribute(R.attr.junk_top, typedValue, true);
        statusBarColorJunk = typedValue.data;
        theme.resolveAttribute(R.attr.status_bar_pane, typedValue, true);
        statusBarColorPane = typedValue.data;
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindView();
    }

    @Override
    public void onPause() {
        super.onPause();
        UIUtils.hideSoftKeyboard(getActivity(), getActivity().getWindow().getDecorView().getWindowToken());
        getActivity().unregisterReceiver(mKeyBoardReceiver);

    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
//        Changing the status bar default value on page change from dashboard
//         to direct Junk Food pane
        if (isVisibleToUser && null != mWindow && pagerPane != null && pagerPane.getCurrentItem() == 0) {
            mWindow.setStatusBarColor(statusBarColorJunk);
        } else {
            if (null != mWindow) {
                mWindow.setStatusBarColor(statusBarColorPane);
            }
        }
        if (!isVisibleToUser && null != imageClear && linSearchList != null &&
                linSearchList.getVisibility() == View.VISIBLE) {
            //Perform click in order to set it when user moves from search
            // pane to DashboardActivity and comes back so as to hide the list
            if (imageClear != null) imageClear.performClick();
            linSearchList.setVisibility(View.GONE);
            if (linPane != null) linPane.setAlpha(1);
        }

        //Added as part of SSA-1332 , when user clicks home button on empty
        // junk food app and swipes back from Intention , empty screen was
        // showing , but now flagging screen will open
        if (isVisibleToUser && pagerPane != null && pagerPane.getCurrentItem() == 0) {
            if (PrefSiempo.getInstance(getActivity()).read(PrefSiempo.JUNKFOOD_APPS, new HashSet<String>()).size() == 0) {
                //Applied for smooth transition
                Intent intent = new Intent(getActivity(), JunkfoodFlaggingActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R
                        .anim.fade_in_junk, R.anim.fade_out_junk);
                DashboardActivity.currentIndexPaneFragment = 0;
            }
        }

        //Add condition if got it is clicked
        if (isVisibleToUser && pagerPane != null && pagerPane.getCurrentItem
                () == 2 && !PrefSiempo.getInstance(context).read(PrefSiempo
                .APPLAND_TOUR_SEEN, false)) {
            showOverLay();
        } else if (!isVisibleToUser && null != overlayDialog && overlayDialog
                .isShowing()) {
            overlayDialog.dismiss();
        } else if (!isVisibleToUser && null != overlayDialogPermission && overlayDialogPermission
                .isShowing()) {
            overlayDialogPermission.dismiss();
        }

//        Added as a part of SSA-1669
        if (isVisibleToUser && firstTimeLoad) {
            if (getView() != null) {
                bindBottomDock();
                firstTimeLoad = false;
            }
        }

        //////////////////////
        if(isVisibleToUser)
        {
         changeStatusBar();
        }
    }


    public void changeStatusBar(){
        String filePath = PrefSiempo.getInstance(context).read(PrefSiempo
                .DEFAULT_BAG, "");
        if (!TextUtils.isEmpty(filePath)) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = context.getTheme();
            theme.resolveAttribute(R.attr.image_alpha, typedValue, true);
            int drawableId = typedValue.resourceId;
            linMain.setBackgroundColor(ContextCompat.getColor(context,
                    drawableId));
            ((DashboardActivity)getActivity()).changeLayoutBackground(ContextCompat.getColor(context,
                    drawableId));
        }
    }
    public void loadView() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediator != null && mediator.getItems() != null) {
                        adapter = new MainListAdapter(getActivity(), mediator.getItems());
                        listView.setAdapter(adapter);
                        adapter.getFilter().filter(TokenManager.getInstance().getCurrent().getTitle());
                    }
                }
            });
        }
    }

    private void changeColorOfStatusBar() {
        if (pagerPane != null && pagerPane.getCurrentItem() == 0 && isVisible
                () && DashboardActivity.currentIndexDashboard == 0) {
            mWindow.setStatusBarColor(statusBarColorJunk);
        } else {
            mWindow.setStatusBarColor(statusBarColorPane);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MainThread)
    public void onEvent(NotifyBottomView notifyBottomView) {
        if (notifyBottomView != null) {
            if (notifyBottomView.isNotify()) {
                items = CoreApplication.Companion.getInstance().getToolBottomItemsList();
                mAdapter = new ToolsMenuAdapter(getActivity(), CoreApplication.Companion.getInstance().isHideIconBranding(), true, items);
                recyclerViewBottomDoc.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
                EventBus.getDefault().removeStickyEvent(notifyBottomView);
            }
        }
    }


    private void initView(View view) {
        linTopDoc = view.findViewById(R.id.linTopDoc);
        junkDoc = view.findViewById(R.id.junk_doc);
        searchDoc = view.findViewById(R.id.search_doc);
        linPane = view.findViewById(R.id.linPane);
        recyclerViewBottomDoc = rootView.findViewById(R.id.recyclerViewBottomDoc);
        edtSearchToolsRounded = view.findViewById(R.id.edtSearchTools);
        try {
            Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/robotocondensedregular.ttf");
            edtSearchToolsRounded.setTypeface(myTypeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
        blueLineDivider = view.findViewById(R.id.blueLineView);
        blueLineDividerBottom = view.findViewById(R.id.blueLineViewBottom);
        cardViewEdtSearch = view.findViewById(R.id.cardViewEdtSearch);
        searchLayout = view.findViewById(R.id.edtSearchListView);
        relSearchTools = view.findViewById(R.id.relSearchTools);
        txtTopDockDate = view.findViewById(R.id.txtTopDockDate);
        txtIntentionLabelJunkPane = junkDoc.findViewById(R.id.txtIntentionLabelJunkPane);
        txtIntention = junkDoc.findViewById(R.id.txtIntention);
        linBottomDoc = view.findViewById(R.id.linBottomDoc);
        linSearchList = view.findViewById(R.id.linSearchList);
        listView = view.findViewById(R.id.listView);
        indicator = view.findViewById(R.id.indicator);
        pagerPane = view.findViewById(R.id.pagerPane);
        pagerPane.setAlpha(1);
        chipsEditText = searchLayout.txtSearchBox;
        imageClear = searchLayout.getBtnClear();
        edtSearchToolsRounded.clearFocus();
        chipsEditText.clearFocus();
    }


    private void bindView() {
        bindViewPager();
        bindBottomDock();
        bindSearchView();
    }

    @Override
    public void onResume() {
        super.onResume();
        String filePath = PrefSiempo.getInstance(context).read(PrefSiempo
                .DEFAULT_BAG, "");
        if (!TextUtils.isEmpty(filePath)) {

            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = context.getTheme();
            theme.resolveAttribute(R.attr.image_alpha, typedValue, true);
            int drawableId = typedValue.resourceId;
            linMain.setBackgroundColor(ContextCompat.getColor(context,
                    drawableId));
            ((DashboardActivity)getActivity()).changeLayoutBackground(ContextCompat.getColor(context,drawableId));
        } else {
            linMain.setBackgroundColor(ContextCompat.getColor(context, R.color
                    .transparent));
            ((DashboardActivity)getActivity()).changeLayoutBackground(ContextCompat.getColor(context, R.color.transparent));
        }
        getActivity().registerReceiver(mKeyBoardReceiver, new IntentFilter(Utils
                .KEYBOARD_ACTION));
        pagerPane.setAlpha(1);
        if (DashboardActivity.currentIndexPaneFragment == 0 && DashboardActivity.isJunkFoodOpen) {
            DashboardActivity.currentIndexPaneFragment = 1;
            DashboardActivity.isJunkFoodOpen = false;
            if (isAdded()) {
                pagerPane.setCurrentItem(DashboardActivity.currentIndexPaneFragment, true);
            }
        }
        if (DashboardActivity.currentIndexDashboard == 1) {
            if (DashboardActivity.currentIndexPaneFragment == 0) {
                Log.d("Firebase", "Junkfood Start");
                DashboardActivity.startTime = System.currentTimeMillis();
            } else if (DashboardActivity.currentIndexPaneFragment == 1) {
                Log.d("Firebase", "Favorite Start");
                DashboardActivity.startTime = System.currentTimeMillis();
            } else if (DashboardActivity.currentIndexPaneFragment == 2) {
                Log.d("Firebase", "Tools Start");
                DashboardActivity.startTime = System.currentTimeMillis();
            }
        }
        setToolsPaneDate();
        if (searchLayout != null && searchLayout.getVisibility() == View.VISIBLE) {
            updateListViewLayout(false);
        }
        try {
            if (PrefSiempo.getInstance(context).read(PrefSiempo
                    .APPLAND_TOUR_SEEN, false) && PrefSiempo.getInstance(context).read(PrefSiempo
                    .IS_AUTOSCROLL, true) && (pagerPane
                    .getCurrentItem() == 0 || pagerPane.getCurrentItem() == 1)) {

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (isAdded()) {
                            pagerPane.setCurrentItem(1);
                        }
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isAdded()) {
                                    pagerPane.setCurrentItem(2);
                                }
                                PrefSiempo.getInstance(context).write(PrefSiempo
                                        .IS_AUTOSCROLL, false);
                                showDialog();

                            }
                        }, 700);
                    }
                }, 800);
                //delay
            }

            changeStatusBar();

            if(mAdapter !=null){
                mAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDialog() {
        getActivity().setRequestedOrientation(ActivityInfo
                .SCREEN_ORIENTATION_PORTRAIT);
        final Dialog overlayDialog = new Dialog(getActivity(), 0);
        if (overlayDialog.getWindow() != null) {
            overlayDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        overlayDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        overlayDialog.setContentView(R.layout.layout_tools_tour);
        Window window = overlayDialog.getWindow();
        // set "origin" to bottom
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams params = window.getAttributes();
        window.setAttributes(params);
        overlayDialog.getWindow().setLayout(WindowManager
                .LayoutParams.MATCH_PARENT, WindowManager
                .LayoutParams.WRAP_CONTENT);

        overlayDialog.setCancelable(false);
        overlayDialog.setCanceledOnTouchOutside(false);
        overlayDialog.show();

        final Button btnNext = overlayDialog.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlayDialog.dismiss();
            }
        });
     }

    private void bindBottomDock() {

        mLayoutManager = new GridLayoutManager(getActivity(), 4);
        if (null != recyclerViewBottomDoc) {
            recyclerViewBottomDoc.setLayoutManager(mLayoutManager);
            if (itemDecoration != null) {
                recyclerViewBottomDoc.removeItemDecoration(itemDecoration);
            }
            itemDecoration = new ItemOffsetDecoration(context, R.dimen.dp_10);
            recyclerViewBottomDoc.addItemDecoration(itemDecoration);
            items = CoreApplication.Companion.getInstance().getToolBottomItemsList();
            mAdapter = new ToolsMenuAdapter(getActivity(), CoreApplication.Companion.getInstance().isHideIconBranding(), true, items);
            recyclerViewBottomDoc.setAdapter(mAdapter);
        }
    }

    private void getColorOfStatusBar() {
        mWindow = getActivity().getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        if (null != mWindow) {
            mWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            mWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        }
    }

    public MainListAdapter getAdapter() {
        return adapter;
    }

    private void bindViewPager() {
        try {
            mPagerAdapter = new PanePagerAdapter(getChildFragmentManager());
            if (null != pagerPane) {
                pagerPane.setAdapter(mPagerAdapter);
                indicator.setViewPager(pagerPane);
                pagerPane.setOffscreenPageLimit(3);
                if (DashboardActivity.isJunkFoodOpen) {
                    DashboardActivity.currentIndexPaneFragment = 1;
                    DashboardActivity.isJunkFoodOpen = false;
                }
                if (DashboardActivity.currentIndexPaneFragment == -1) {
                    DashboardActivity.currentIndexPaneFragment = 2;
                    DashboardActivity.startTime = System.currentTimeMillis();
                }
                //Code for Page change
                if (isAdded()) {
                    pagerPane.setCurrentItem(DashboardActivity.currentIndexPaneFragment);
                    pagerPane.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                        @Override
                        public void onPageScrolled(int i, float v, int i1) {
                            edtSearchToolsRounded.clearFocus();
                            chipsEditText.clearFocus();
                        }


                        @Override
                        public void onPageSelected(int i) {

                            if (i == 0) {
                                /* Junkfood Pane */
                                if (PrefSiempo.getInstance(getActivity()).read(PrefSiempo.JUNKFOOD_APPS, new HashSet<String>()).size() == 0) {
                                    //Applied for smooth transition
                                    pagerPane.setAlpha(0);
                                    Intent intent = new Intent(getActivity(), JunkfoodFlaggingActivity.class);
                                    startActivity(intent);
                                    getActivity().overridePendingTransition(R
                                            .anim.fade_in_junk, R.anim.fade_out_junk);
                                } else {
                                    if (PrefSiempo.getInstance(context).read(PrefSiempo
                                            .APPLAND_TOUR_SEEN, false)) {
                                        //Show overlay for draw over other apps permission


//                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                            if (!Settings.canDrawOverlays(context) &&
//                                                    PrefSiempo.getInstance(context).read
//                                                            (PrefSiempo.DETER_AFTER,
//                                                                    -1) != -1) {
//                                                if (null == overlayDialogPermission || !overlayDialogPermission.isShowing())
//                                                    showOverLayForDrawingPermission();
//                                            }
//                                        }
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            if (!hasUsageStatsPermission(getActivity())) {
                                                if (!Settings.canDrawOverlays(context) &&
                                                        PrefSiempo.getInstance(context).read
                                                                (PrefSiempo.DETER_AFTER,
                                                                        -1) != -1) {
                                                    if (null == overlayDialogPermission || !overlayDialogPermission.isShowing())

                                                        showOverLayForDrawingPermission();
                                                }
                                            }
                                        }

                                    }


                                }
                                UIUtils.hideSoftKeyboard(getActivity(), getActivity().getWindow().getDecorView().getWindowToken());

                                if (linSearchList.getVisibility() == View.VISIBLE) {
                                    linSearchList.setVisibility(View.GONE);
                                    linPane.setAlpha(1);
                                }
                                if (linPane.getVisibility() == View.GONE)
                                    linPane.setVisibility(View.VISIBLE);
                                if (linBottomDoc.getVisibility() == View.GONE)
                                    linBottomDoc.setVisibility(View.VISIBLE);
                                if (searchLayout.getVisibility() == View.VISIBLE)
                                    searchLayout.setVisibility(View.GONE);
                                if (cardViewEdtSearch.getVisibility() == View.VISIBLE)
                                    cardViewEdtSearch.setVisibility(View.GONE);
                                if (relSearchTools.getVisibility() == View.GONE)
                                    relSearchTools.setVisibility(View.VISIBLE);
                                isSearchVisable = false;
                                imageClear.setVisibility(View.VISIBLE);
                                if (searchLayout != null && chipsEditText != null && chipsEditText.getText().toString().length() > 0) {
                                    if (linSearchList.getVisibility() == View.VISIBLE)
                                        searchLayout.txtSearchBox.setText("");
                                }

                                junkFoodAppPane();
                                blueLineDivider.setVisibility(View.GONE);
                                mWindow.setStatusBarColor(statusBarColorJunk);
                                linTopDoc.setElevation(20);
                                linTopDoc.setBackgroundColor(getResources().getColor(backGroundColor));
                                searchDoc.setVisibility(View.GONE);
                                junkDoc.setVisibility(View.VISIBLE);

                            } else {
                                /* Tools and Favourite Pane */
                                linTopDoc.setElevation(0);
                                blueLineDivider.setVisibility(View.VISIBLE);
                                TypedValue typedValue = new TypedValue();
                                Resources.Theme theme = context.getTheme();
                                theme.resolveAttribute(R.attr.top_doc, typedValue, true);
                                int drawableId = typedValue.resourceId;
                                linTopDoc.setBackgroundColor(getResources().getColor(R
                                        .color.transparent));
                                linTopDoc.setBackground(getResources().getDrawable(drawableId));
                                txtTopDockDate.setVisibility(View.VISIBLE);
                                edtSearchToolsRounded.setVisibility(View.VISIBLE);
                                txtIntention.setVisibility(View.GONE);
                                txtIntentionLabelJunkPane.setVisibility(View.GONE);
                                searchDoc.setVisibility(View.VISIBLE);
                                junkDoc.setVisibility(View.GONE);
                                // finally change the color
                                mWindow.setStatusBarColor(statusBarColorPane);
                            }

                            //Indicator to be set here so that when coming from another
                            // application, the sliding dots retain the shape as previous
                            indicator.setViewPager(pagerPane);

                            if (DashboardActivity.currentIndexPaneFragment == 0 && i == 1) {
                                Log.d("Firebase ", "JunkFood End");
                                Log.d("Firebase ", "Favorite Start");
                                FirebaseHelper.getInstance().logScreenUsageTime(JunkFoodPaneFragment.class.getSimpleName(), DashboardActivity.startTime);
                                DashboardActivity.startTime = System.currentTimeMillis();
                            } else if (DashboardActivity.currentIndexPaneFragment == 1 && i == 2) {
                                Log.d("Firebase ", "Favorite End");
                                Log.d("Firebase ", "Tools Start");
                                FirebaseHelper.getInstance().logScreenUsageTime(FavoritePaneFragment.class.getSimpleName(), DashboardActivity.startTime);
                                DashboardActivity.startTime = System.currentTimeMillis();
                            } else if (DashboardActivity.currentIndexPaneFragment == 2 && i == 1) {
                                Log.d("Firebase ", "Tools End");
                                Log.d("Firebase ", "Favorite Start");
                                FirebaseHelper.getInstance().logScreenUsageTime(ToolsPaneFragment.class.getSimpleName(), DashboardActivity.startTime);
                                DashboardActivity.startTime = System.currentTimeMillis();
                            } else if (DashboardActivity.currentIndexPaneFragment == 1 && i == 0) {
                                Log.d("Firebase ", "Favorite End");
                                Log.d("Firebase ", "JunkFood Start");
                                FirebaseHelper.getInstance().logScreenUsageTime(FavoritePaneFragment.class.getSimpleName(), DashboardActivity.startTime);
                                DashboardActivity.startTime = System.currentTimeMillis();
                            }
                            DashboardActivity.currentIndexPaneFragment = i;
                            //Make the junk food pane visible
                        }

                        @Override
                        public void onPageScrollStateChanged(int i) {
                        }
                    });

                    if (DashboardActivity.currentIndexPaneFragment == 0) {
                        junkFoodAppPane();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void junkFoodAppPane() {
        txtTopDockDate.setVisibility(View.GONE);
        searchLayout.setVisibility(View.GONE);
        edtSearchToolsRounded.setVisibility(View.GONE);

        txtIntention.setVisibility(View.VISIBLE);
        txtIntentionLabelJunkPane.setVisibility(View.VISIBLE);

        String strIntention = PrefSiempo.getInstance(getActivity()).read
                (PrefSiempo.DEFAULT_INTENTION, "");

        //If Intentions are enabled and intention field is not empty then show
        //it in Junk food Top dock else not
        if (!TextUtils.isEmpty(strIntention) && !PrefSiempo.getInstance
                (context).read(PrefSiempo
                .IS_INTENTION_ENABLE, false)) {
            txtIntentionLabelJunkPane.setText(getString(R.string
                    .you_ve_flag));
            txtIntention.setText(strIntention);
            txtIntention.setVisibility(View.VISIBLE);
            txtIntentionLabelJunkPane.setVisibility(View.VISIBLE);

        } else {
            txtIntention.setText("You flagged these apps to use them less.");
            txtIntentionLabelJunkPane.setVisibility(View.INVISIBLE);
        }


    }

    /**
     * Set Date for Tools Pane
     */
    private void setToolsPaneDate() {
        Calendar c = Calendar.getInstance();
        DateFormat df = getDateInstanceWithoutYears(Locale.getDefault());
        if (getActivity() != null && txtTopDockDate != null) {
            txtTopDockDate.setText(df.format(c.getTime()));
            txtTopDockDate.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (null != imageClear && imageClear.getVisibility() == View
                            .VISIBLE && searchLayout.getVisibility() == View.VISIBLE) {
                        imageClear.performClick();
                    }
                    return true;
                }
            });
        }

    }

    public DateFormat getDateInstanceWithoutYears(Locale locale) {


        SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateInstance
                (DateFormat.FULL, locale);
        try {
            sdf.applyPattern(sdf.toPattern().replaceAll(
                    "([^\\p{Alpha}']|('[\\p{Alpha}]+'))*y+([^\\p{Alpha}']|('[\\p{Alpha}]+'))*",
                    ""));
        } catch (Exception e) {
            Tracer.d("Exception  :: " + e.toString());
        }

        return sdf;
    }

    public TokenManager getManager() {
        return TokenManager.getInstance();
    }

    public void setCurrentPage(int viewPagerPage) {
        if (isAdded()) {
            pagerPane.setCurrentItem(viewPagerPage);
        }

    }

    public void hidePaneAndBottomView(final Context context) {
        linPane.setVisibility(View.GONE);
        linBottomDoc.setVisibility(View.GONE);
        blueLineDivider.setVisibility(View.GONE);
        blueLineDividerBottom.setVisibility(View.GONE);
        searchListVisible(context);
    }

    public void showPaneAndBottomView(final Context context) {
        linSearchList.setVisibility(View.GONE);
        linPane.setVisibility(View.VISIBLE);
        blueLineDivider.setVisibility(View.VISIBLE);
        blueLineDividerBottom.setVisibility(View.VISIBLE);
        linBottomDoc.setVisibility(View.VISIBLE);
        isSearchVisable = false;

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.top_doc, typedValue, true);
        int color = typedValue.resourceId;
        linTopDoc.setBackgroundResource(color);
        FirebaseHelper.getInstance().logScreenUsageTime(FirebaseHelper.SEARCH_PANE, DashboardActivity.startTime);
        DashboardActivity.startTime = System.currentTimeMillis();
    }

    private void showViews(boolean wantToShow, ArrayList<View> viewArrayList) {
        for (View view : viewArrayList) {
            if (wantToShow) {
                if (view.getVisibility() == View.GONE) {
                    view.setVisibility(View.VISIBLE);
                }
            } else {
                if (view.getVisibility() == View.VISIBLE) {
                    view.setVisibility(View.GONE);
                }
            }
        }
    }

    public void searchListVisible(Context context) {
        linSearchList.setVisibility(View.VISIBLE);

        linSearchList.setBackgroundColor(getResources().getColor(backGroundColor));
        isSearchVisable = true;
        imageClear.setVisibility(View.VISIBLE);
        if (DashboardActivity.currentIndexDashboard == 0) {
            if (DashboardActivity.currentIndexPaneFragment == 1) {
                FirebaseHelper.getInstance().logScreenUsageTime("FavoritePaneFragment", DashboardActivity.startTime);
            } else if (DashboardActivity.currentIndexPaneFragment == 2) {
                FirebaseHelper.getInstance().logScreenUsageTime("ToolsPaneFragment", DashboardActivity.startTime);
            }
        }
        DashboardActivity.startTime = System.currentTimeMillis();

    }

    private void logSearchViewShow() {
        DashboardActivity.startTime = System.currentTimeMillis();
    }

    private void logSearchViewEnd() {
        if (DashboardActivity.currentIndexDashboard == 0) {
            if (DashboardActivity.currentIndexPaneFragment == 1) {
                Log.d("Firebase", "Favorite Start");
                FirebaseHelper.getInstance().logScreenUsageTime("SearchPaneFragment", DashboardActivity.startTime);
                DashboardActivity.startTime = System.currentTimeMillis();
            } else if (DashboardActivity.currentIndexPaneFragment == 2) {
                Log.d("Firebase", "Tools Start");
                FirebaseHelper.getInstance().logScreenUsageTime("SearchPaneFragment", DashboardActivity.startTime);
                DashboardActivity.startTime = System.currentTimeMillis();
            }
        }
    }


    @Subscribe
    public void onBackPressedEvent(OnBackPressedEvent onBackPressedEvent) {
        if (onBackPressedEvent.isBackPressed()) {
            if (linSearchList.getVisibility() == View.VISIBLE) {
                UIUtils.hideSoftKeyboard(getActivity(), getActivity().getWindow().getDecorView().getWindowToken());
                blueLineDivider.setVisibility(View.VISIBLE);
                searchLayout.setVisibility(View.GONE);
                cardViewEdtSearch.setVisibility(View.GONE);
                relSearchTools.setVisibility(View.VISIBLE);
                linSearchList.setVisibility(View.GONE);
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = context.getTheme();
                theme.resolveAttribute(R.attr.top_doc, typedValue, true);
                int color = typedValue.resourceId;
                linTopDoc.setBackgroundResource(color);
                linBottomDoc.setVisibility(View.VISIBLE);
                linPane.setVisibility(View.VISIBLE);
                isSearchVisable = false;
                linPane.setAlpha(1);
                imageClear.setVisibility(View.VISIBLE);
                //SSA-1458 to clear searchLayout.
                if (searchLayout != null && chipsEditText != null && chipsEditText.getText().toString().length() > 0) {
                    searchLayout.txtSearchBox.setText("");
                }
            }
        }
    }

    @Subscribe
    public void filterDataMainAdapter(MainListAdapterEvent event) {
        try {

            List<MainListItem> mainListItems = event.getData();
            if (null != mainListItems && mainListItems.size() > 0 && mainListItems
                    .size() <= 3) {
                listView.setOnTouchListener(new OnSwipeTouchListener(context,
                        listView));

            } else {
                listView.setOnTouchListener(null);
                Log.d("Rajesh",""+adapter.getCount());
            }


        } catch (Exception e) {
            CoreApplication.Companion.getInstance().logException(e);
            Tracer.e(e, e.getMessage());
        }
    }

    @Subscribe
    public void searchLayoutEvent(final SearchLayoutEvent event) {
        try {
            if (getActivity() != null) {

                if (event.getString().equalsIgnoreCase("") && searchLayout
                        .getVisibility() == View.GONE) {

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //changeToken(event.getString());
                            parser.parse(event.getString());
                            if (adapter != null) {
                                adapter.getFilter().filter(TokenManager.getInstance().getCurrent().getTitle());
                            }
                        }
                    }, 40);

                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            changeToken(event.getString());
                            parser.parse(event.getString());


                            if (adapter != null && !TextUtils.isEmpty
                                    (TokenManager.getInstance().getCurrent().getTitle().trim())) {
                                adapter.getFilter().filter(TokenManager.getInstance().getCurrent().getTitle());
                            }

                            //Cancelling the result of previous async task
                            // for empty token in case of Edit Text string
                            // not being empty
                            if (!event.getString().equalsIgnoreCase("")) {
                                mediator.cancelAsync();
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            CoreApplication.Companion.getInstance().logException(e);
            Tracer.e(e, e.getMessage());
        }
    }

    public void changeToken(String str) {
        if (str != null && str.length() > 0) {
            List<TokenItem> itemList = TokenManager.getInstance().getItems();
            for (TokenItem item : itemList) {
                if (item.getItemType() == TokenItemType.CONTACT && item.getExtra1() != null &&
                        item.getExtra2() != null &&
                        !str.trim().contains(item
                                .getTitle().trim()) && item.getCompleteType() == TokenCompleteType.FULL) {
                    int index = itemList.indexOf(item);
                    if (index == 1) {
                        if (item.getCompleteType() == TokenCompleteType.FULL) {
                            itemList.remove(itemList.size() - 1);
                            item.setExtra1("");
                            item.setExtra2("");
                            item.setTitle("@");
                            item.setCompleteType(TokenCompleteType.DEFAULT);
                            router.setCurrent(item);
                        }
                    } else if (index == 0) {
                        itemList.remove(item);
                        router.setCurrent(itemList.get(itemList.size() - 1));
                    }
                } else if (item.getItemType() == TokenItemType.DATA && item
                        .getTitle() != null && !str
                        .trim().contains(item
                                .getTitle()
                                .trim()) && item.getExtra1() == null && item.getExtra2() == null) {
                    int index = itemList.indexOf(item);
                    if (item.getCompleteType() == TokenCompleteType.FULL && index == 0) {
                        itemList.remove(item);
                        itemList.remove(itemList.size() - 1);
                        router.setCurrent(itemList.get(itemList.size() - 1));
                        itemList.add(new TokenItem(TokenItemType.DATA));
                    }
                }
            }
        }
    }

    @Subscribe
    public void sendSmsEvent(SendSmsEvent event) {
        if (event.isClearList()) {
            mediator.resetData();
            imageClear.performClick();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MainThread)
    public void homePress(HomePress event) {
        if (event != null) {
            if (isAdded()) {
                pagerPane.setCurrentItem(event.getCurrentIndexPaneFragment(), true);
                isSearchVisable = false;
                if (searchLayout != null && chipsEditText != null) {
                    searchLayout.txtSearchBox.setText("");
                    if(mediator!=null && !mediator.getRunningStatus()){
                        mediator.resetData();
                    }
                }
                TokenManager.getInstance().clear();
//                if (edtSearchToolsRounded != null) {
//                    edtSearchToolsRounded.performClick();
//                }
//                EventBus.getDefault().post(new TokenUpdateEvent());
//                EventBus.getDefault().post(new SearchLayoutEvent(""));

            }
            EventBus.getDefault().removeStickyEvent(event);
        }
    }

    @Subscribe
    public void tokenManagerEvent(TokenUpdateEvent event) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final TokenItem current = TokenManager.getInstance().getCurrent();
                    if (current != null) {
                        if (current.getItemType() == TokenItemType.END_OP) {
                            mediator.defaultData();
                        } else if (current.getItemType() == TokenItemType.CONTACT) {
                            if (current.getCompleteType() == TokenCompleteType.HALF) {
                                mediator.contactNumberPicker(Integer.parseInt(current.getExtra1()));
                            } else {
                                mediator.contactPicker();
                            }
                        } else if (current.getItemType() == TokenItemType.DATA) {
                            if (TokenManager.getInstance().get(0).getItemType() == TokenItemType.DATA) {

                                //If the task of async is running, then no
                                // need to run its another instance. This
                                // will happen in case of list item click
                                // where already the data is being reset and
                                // after it the empty token ("") is being
                                // called. Hence in order to prevent this,
                                // empty token will be called only in case if
                                // no previous async task of similar type is
                                // running.
                                if (!mediator.getRunningStatus()) {
                                    mediator.resetData();
                                }
                                if (adapter != null)
                                    adapter.getFilter().filter(current.getTitle());


                            } else {
                                if (current.getTitle().trim().isEmpty()) {
                                    if (adapter != null) {
                                        mediator.loadDefaultData();
                                        adapter.getFilter().filter("^");
                                    }
                                } else {
                                    if (adapter != null) {
                                        mediator.loadDefaultData();
                                        adapter.getFilter().filter(current.getTitle());
                                    }
                                }

                            }
                        }

                    }
                }
            });
        } catch (Exception e) {
            CoreApplication.Companion.getInstance().logException(e);
            Tracer.e(e, e.getMessage());
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MainThread)
    public void onEvent(NotifySearchRefresh notifySearchRefresh) {
        if (notifySearchRefresh != null && notifySearchRefresh.isNotify()) {
            mediator = new MainFragmentMediator(PaneFragment.this);
            mediator.loadData();

            if (adapter != null) {
                adapter.getFilter().filter("");
            }
            EventBus.getDefault().removeStickyEvent(notifySearchRefresh);
        }

    }

    /**
     * Method to show overlay for default launcher setting
     */
    private void showOverLay() {
        if (null != getActivity()) {
            try {
                getActivity().setRequestedOrientation(ActivityInfo
                        .SCREEN_ORIENTATION_PORTRAIT);
                overlayDialog = new Dialog(getActivity(), 0);
                if (overlayDialog.getWindow() != null) {
                    overlayDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                }
                overlayDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                overlayDialog.setContentView(R.layout.layout_appland_tour);
                Window window = overlayDialog.getWindow();
                // set "origin" to bottom
                window.setGravity(Gravity.BOTTOM);
                WindowManager.LayoutParams params = window.getAttributes();
                window.setAttributes(params);
                overlayDialog.getWindow().setLayout(WindowManager
                        .LayoutParams.MATCH_PARENT, WindowManager
                        .LayoutParams.WRAP_CONTENT);

                overlayDialog.setCancelable(false);
                overlayDialog.setCanceledOnTouchOutside(false);
                overlayDialog.show();

                final ViewFlipper viewFlipper = overlayDialog.findViewById(R.id.viewFlipperTour);
                final Button btnNext = overlayDialog.findViewById(R.id.btnNext);
                final TextView txtNext = overlayDialog.findViewById(R.id.txtNext);
                final TextView txtToolsMessage = overlayDialog.findViewById(R.id.txtToolsMessage);
                final TextView txtToolsTitle = overlayDialog.findViewById(R.id
                        .txtToolsTitle);
                if (viewFlipper.getDisplayedChild() == 0) {
                    String sourceString = "From this <b>Tools Screen</b>, you " +
                            "can launch your most helpful apps. Assign your preferred app to each tool. Add, change, or rearrange tools as desired.";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        txtToolsMessage.setText(Html.fromHtml(sourceString, Html
                                .FROM_HTML_MODE_COMPACT));
                    } else {
                        txtToolsMessage.setText(Html.fromHtml(sourceString));
                    }

                }

                btnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (viewFlipper.getDisplayedChild()) {
                            case 0:

                                viewFlipper.setInAnimation(context, R.anim
                                        .in_from_left_tour);
                                viewFlipper.setOutAnimation(context, R.anim
                                        .out_to_right_tour);
                                viewFlipper.setDisplayedChild(1);
                                txtToolsTitle.setText(R.string.frequently_used_title);
                                txtNext.setText("2 of 3");
                                if (isAdded()) {
                                    pagerPane.setCurrentItem(1);
                                }
                                break;
                            case 1:
                                viewFlipper.setInAnimation(context, R.anim
                                        .in_from_left_tour);
                                viewFlipper.setOutAnimation(context, R.anim
                                        .out_to_right_tour);
                                viewFlipper.setDisplayedChild(2);
                                btnNext.setText(R.string.gotit);
                                txtToolsTitle.setText(R.string.flagged_app_title);
                                txtNext.setText("3 of 3");
                                if (isAdded()) {
                                    pagerPane.setCurrentItem(0);
                                }
                                break;
                            case 2:
                                PrefSiempo.getInstance(context).write(PrefSiempo
                                        .APPLAND_TOUR_SEEN, true);
                                //Start Flagging activity
                                if (pagerPane != null && pagerPane.getCurrentItem() == 0) {
                                    //Applied for smooth transition
                                    Intent intent = new Intent(getActivity(), JunkfoodFlaggingActivity.class);
                                    startActivity(intent);
                                    getActivity().overridePendingTransition(R
                                            .anim.in_from_left_tour, R.anim
                                            .out_to_right_tour);
                                    DashboardActivity.currentIndexPaneFragment = 0;
                                }
                                overlayDialog.dismiss();

                                break;

                        }


                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void showOverLayForDrawingPermission() {
        if (null != getActivity()) {
            try {
                getActivity().setRequestedOrientation(ActivityInfo
                        .SCREEN_ORIENTATION_PORTRAIT);
                overlayDialogPermission = new Dialog(getActivity(), 0);
                if (overlayDialogPermission.getWindow() != null) {
                    overlayDialogPermission.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                }
                overlayDialogPermission.requestWindowFeature(Window.FEATURE_NO_TITLE);
                overlayDialogPermission.setContentView(R.layout
                        .layout_appland_draw_permission);
                Window window = overlayDialogPermission.getWindow();
                // set "origin" to bottom
                window.setGravity(Gravity.BOTTOM);
                WindowManager.LayoutParams params = window.getAttributes();
                window.setAttributes(params);
                overlayDialogPermission.getWindow().setLayout(WindowManager
                        .LayoutParams.MATCH_PARENT, WindowManager
                        .LayoutParams.WRAP_CONTENT);
                overlayDialogPermission.setCancelable(true);
                overlayDialogPermission.setCanceledOnTouchOutside(true);
                overlayDialogPermission.show();

                final ViewFlipper viewFlipperOverlay = overlayDialogPermission
                        .findViewById(R.id.viewFlipperPermissionDrawOverlay);
                final Button btnEnable = overlayDialogPermission.findViewById
                        (R.id.btnEnable);
                final Button btnLater = overlayDialogPermission.findViewById
                        (R.id.btnLater);

                final Button btnGotIt = overlayDialogPermission.findViewById
                        (R.id.btnGotIt);
                btnLater.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), SettingsActivity.class);
                        intent.putExtra("FlagApp", true);
                        startActivity(intent);
                    }
                });

                btnEnable.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewFlipperOverlay.setInAnimation(context, R.anim
                                .in_from_right_email);
                        viewFlipperOverlay.setOutAnimation(context, R.anim
                                .out_to_left_email);
                        viewFlipperOverlay.setDisplayedChild(1);


                    }
                });
                btnGotIt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        overlayDialogPermission.dismiss();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!Settings.canDrawOverlays(context)) {
                                Intent intent = new Intent(Settings
                                        .ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:" +
                                                context.getPackageName()));
                                startActivityForResult(intent, 1000);
                            }
                        }


                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void bindSearchView() {
//        //Circular Edit Text
        router = new TokenRouter();
        parser = new TokenParser(router, context, mediator);
        loadView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (router != null && searchLayout != null && searchLayout.txtSearchBox != null) {
                    mediator.listItemClicked(router, position, searchLayout.txtSearchBox.getStrText());
                }
            }
        });
        edtSearchToolsRounded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchLayout.getVisibility() == View.GONE) {
                    hidePaneAndBottomView(context);
                    listView.setAdapter(adapter);
                    imageClear.setVisibility(View.VISIBLE);
                    blueLineDivider.setVisibility(View.GONE);
                    searchLayout.setVisibility(View.VISIBLE);
                    searchLayout.txtSearchBox.requestFocus();
                    cardViewEdtSearch.setVisibility(View.VISIBLE);
                    relSearchTools.setVisibility(View.GONE);
                    UIUtils.showKeyboard(chipsEditText);
                    updateListViewLayout(true);
                    if (adapter != null) {
                        adapter.getFilter().filter("");
                    }

                } else {
                    UIUtils.hideSoftKeyboard(getActivity(), getActivity().getWindow().getDecorView().getWindowToken());
                    showPaneAndBottomView(context);
                    blueLineDivider.setVisibility(View.VISIBLE);
                    searchLayout.setVisibility(View.GONE);
                    cardViewEdtSearch.setVisibility(View.GONE);
                    relSearchTools.setVisibility(View.VISIBLE);
                    imageClear.setVisibility(View.GONE);

                }
            }
        });

        imageClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtSearchToolsRounded != null) {
                    edtSearchToolsRounded.performClick();
                }
                if (searchLayout != null && chipsEditText != null && chipsEditText.getText().toString().length() > 0) {
                    searchLayout.txtSearchBox.setText("");
                }

            }
        });

    }

    private void updateListViewLayout(boolean b) {
        try {
            int val;
            if (b) {
                val = Math.min(adapter.getCount() * 54, 54 * 4);
                if (val != 0) val += 8;
                // extra padding when there is something in listView
                listView.getLayoutParams().height = UIUtils.dpToPx(getActivity(), val);
                listView.requestLayout();
            } else {
                listView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                listView.requestLayout();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(context)) {
                    Toast.makeText(context, R.string.success_msg, Toast
                            .LENGTH_SHORT).show();
                    if (overlayDialogPermission != null && overlayDialogPermission.isShowing())
                        overlayDialogPermission.dismiss();
                }
            }
        }
    }

    private class OnSwipeTouchListener implements View.OnTouchListener {

        ListView list;
        private GestureDetector gestureDetector;
        private Context context;

        OnSwipeTouchListener(Context ctx, ListView list) {
            gestureDetector = new GestureDetector(ctx, new GestureListener());
            context = ctx;
            this.list = list;
        }

        public OnSwipeTouchListener() {
            super();
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            //Added as a part of SSA-1475, in case if GestureDetector is not
            // initialised and null, it will be assigned and then its event
            // will be captured
            if (null != gestureDetector) {
                return gestureDetector.onTouchEvent(event);
            } else {
                gestureDetector = new GestureDetector(context, new GestureListener
                        ());
                return gestureDetector.onTouchEvent(event);
            }
        }

        void onSwipeRight(int pos) {
            //Do what you want after swiping left to right
            if (pagerPane != null) {
                if (isAdded()) {
                    pagerPane.setCurrentItem(0);
                }
            }

        }

        void onSwipeLeft(int pos) {

            //Do what you want after swiping right to left
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            private int getPostion(MotionEvent e1) {
                return list.pointToPosition((int) e1.getX(), (int) e1.getY());
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2,
                                   float velocityX, float velocityY) {
                try {
                    float distanceX = e2.getX() - e1.getX();
                    float distanceY = e2.getY() - e1.getY();
                    if (Math.abs(distanceX) > Math.abs(distanceY)
                            && Math.abs(distanceX) > SWIPE_THRESHOLD
                            && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (distanceX > 0)
                            onSwipeRight(getPostion(e1));
                        else
                            onSwipeLeft(getPostion(e1));
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

        }
    }

}
