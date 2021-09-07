package io.focuslauncher.phone.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.focuslauncher.R;
import io.focuslauncher.phone.adapters.TempoNotificationSectionAdapter;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.event.AppInstalledEvent;
import io.focuslauncher.phone.helper.FirebaseHelper;
import io.focuslauncher.phone.models.AppListInfo;
import io.focuslauncher.phone.utils.PrefSiempo;
import io.focuslauncher.phone.utils.Sorting;
import de.greenrobot.event.Subscribe;

/**
 * Created by hardik on 22/11/17.
 */

public class NotificationActivity extends CoreActivity {

    long startTime = 0;
    Toolbar toolbar;
    private RecyclerView lst_appList;
    private TextView titleActionBar;
    private ImageView imgBack;

    private List<String> pref_messengerList = new ArrayList<>();
    private Set<String> pref_helpfulRobots = new HashSet<>();
    private Set<String> pref_blockedList = new HashSet<>();

    private List<AppListInfo> messengerList = new ArrayList<>();
    private List<AppListInfo> blockedList = new ArrayList<>();
    private List<AppListInfo> helpfulRobot_List = new ArrayList<>();

    private ArrayList<String> pref_headerSectionList = new ArrayList<>();
    private List<AppListInfo> headerSectionList = new ArrayList<>();

    private List<String> systemAppList = new ArrayList<>();

    private PackageManager packageManager;
    private ProgressBar loading_progress;

    @Override
    protected void onResume() {
        super.onResume();
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseHelper.getInstance().logScreenUsageTime(NotificationActivity.class.getSimpleName(), startTime);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tempo_list_notification);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp);
        toolbar.setTitle(R.string.select_apps_title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        loading_progress = findViewById(R.id.loading_progress);
        loading_progress.setVisibility(View.VISIBLE);
        lst_appList = findViewById(R.id.lst_appList);

        new Thread(new Runnable() {
            @Override
            public void run() {
                bindView();
            }
        }).start();

    }

    public void bindView() {
        // Initialize components
        packageManager = getPackageManager();

        pref_messengerList = new ArrayList<>();
        pref_helpfulRobots = new HashSet<>();
        pref_blockedList = new HashSet<>();

        blockedList = new ArrayList<>();
        messengerList = new ArrayList<>();
        helpfulRobot_List = new ArrayList<>();
        headerSectionList = new ArrayList<>();

        systemAppList = Arrays.asList(getResources().getStringArray(R.array.systemAppList));
        /*
          Load all preference list based on share preference
          Constants.HELPFUL_ROBOTS
          Constants.BLOCKED_APPLIST
         */

        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        List<ResolveInfo> messagingResolveList = getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : messagingResolveList) {
            if (!resolveInfo.activityInfo.packageName.equalsIgnoreCase(getPackageName())) {
                pref_messengerList.add(resolveInfo.activityInfo.packageName);
            }
        }
        String str_helpfulRobots = PrefSiempo.getInstance(this).read(PrefSiempo.HELPFUL_ROBOTS, "");
        if (!TextUtils.isEmpty(str_helpfulRobots)) {
            Type type = new TypeToken<Set<String>>() {
            }.getType();
            pref_helpfulRobots = new Gson().fromJson(str_helpfulRobots, type);
        }
        pref_blockedList = PrefSiempo.getInstance(this).read(PrefSiempo.BLOCKED_APPLIST, new HashSet<String>());


        for (String packageName : CoreApplication.getInstance().getPackagesList()) {
            for (String blockedApp : pref_blockedList) {
                if (!packageName.equalsIgnoreCase(blockedApp)) {
                    pref_helpfulRobots.add(packageName);
                }
            }
        }


        String disableList = new Gson().toJson(pref_helpfulRobots);
        PrefSiempo.getInstance(this).write(PrefSiempo.HELPFUL_ROBOTS, disableList);


//        launcherPrefs.edit().putString(Constants.HELPFUL_ROBOTS, disableList).commit();

        String str_Header_AppList = PrefSiempo.getInstance(this).read(PrefSiempo.HEADER_APPLIST, "");
        if (!TextUtils.isEmpty(str_Header_AppList)) {
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            pref_headerSectionList = new Gson().fromJson(str_Header_AppList, type);
        }

        loadAndDisplayAppList();
    }

    @Subscribe
    public void appInstalledEvent(AppInstalledEvent event) {
        if (!isFinishing() && event.isAppInstalledSuccessfully()) {
            bindView();
        }
    }


    /**
     * Prepare List for display based on preference list
     */
    public void loadAndDisplayAppList() {

        for (int i = 0; i < CoreApplication.getInstance().getPackagesList().size(); i++) {
            if (pref_blockedList.contains(CoreApplication.getInstance().getPackagesList().get(i))) {
                AppListInfo d = new AppListInfo();
                d.packageName = CoreApplication.getInstance().getPackagesList().get(i);
                d.ischecked = !pref_blockedList.contains(d.packageName);
                blockedList.add(d);
            } else if (pref_messengerList.contains(CoreApplication.getInstance().getPackagesList().get(i))) {
                AppListInfo d = new AppListInfo();
                d.packageName = CoreApplication.getInstance().getPackagesList().get(i);
                d.ischecked = !pref_helpfulRobots.contains(d.packageName);
                messengerList.add(d);
            } else {
                AppListInfo d = new AppListInfo();
                d.packageName = CoreApplication.getInstance().getPackagesList().get(i);
                if (!TextUtils.isEmpty(d.packageName) && !systemAppList.contains(d.packageName)) {
                    d.ischecked = !pref_helpfulRobots.contains(d.packageName);
                    helpfulRobot_List.add(d);
                }
            }
        }


        if (messengerList.size() >= 0) {
            AppListInfo d1 = new AppListInfo();
            d1.headerName = "Human direct messaging";
            d1.ischecked = !pref_headerSectionList.contains("Human direct messaging");
            headerSectionList.add(d1);
        }

        if (helpfulRobot_List.size() >= 0) {
            AppListInfo d2 = new AppListInfo();
            d2.headerName = "Helpful robots";

            d2.ischecked = !pref_headerSectionList.contains("Helpful robots");
            headerSectionList.add(d2);
        }

        if (pref_blockedList.size() >= 0) {
            AppListInfo d3 = new AppListInfo();
            d3.headerName = "All other apps";

            d3.ischecked = !pref_headerSectionList.contains("All other apps");
            headerSectionList.add(d3);
        }

        checkAppListEmpty(this, helpfulRobot_List, messengerList, blockedList);


        if (helpfulRobot_List.size() > 0) {
            helpfulRobot_List = Sorting.sortApplication(helpfulRobot_List);
        }
        if (messengerList.size() > 0) {
            messengerList = Sorting.sortApplication(messengerList);
        }
        if (blockedList.size() > 0) {
            blockedList = Sorting.sortApplication(blockedList);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setData();
            }
        });

    }

    private void setData() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(NotificationActivity.this);
        lst_appList.setLayoutManager(linearLayoutManager);
        lst_appList.setHasFixedSize(true);
        TempoNotificationSectionAdapter adapter = new TempoNotificationSectionAdapter(NotificationActivity.this, helpfulRobot_List, messengerList, blockedList, headerSectionList);
        lst_appList.setAdapter(adapter);
        loading_progress.setVisibility(View.GONE);
    }

    public void checkAppListEmpty(Context context, List<AppListInfo> appList, List<AppListInfo> messengerList, List<AppListInfo> blockedAppList) {
        if (messengerList.size() > 1) {
            for (int i = 0; i < messengerList.size(); i++)
                if (!TextUtils.isEmpty(messengerList.get(i).errorMessage)) {
                    messengerList.remove(messengerList.get(i));
                }
        }

        if (appList.size() > 1) {
            for (int i = 0; i < appList.size(); i++)
                if (!TextUtils.isEmpty(appList.get(i).errorMessage)) {
                    appList.remove(appList.get(i));
                }
        }

        if (blockedAppList.size() > 1) {
            for (int i = 0; i < blockedAppList.size(); i++)
                if (!TextUtils.isEmpty(blockedAppList.get(i).errorMessage)) {
                    blockedAppList.remove(blockedAppList.get(i));
                }
        }

        if (messengerList.size() == 0) {
            AppListInfo d = new AppListInfo();
            d.errorMessage = context.getResources().getString(R.string.msg_no_apps);
            messengerList.add(d);
        }

        if (appList.size() == 0) {
            AppListInfo d = new AppListInfo();
            d.errorMessage = context.getResources().getString(R.string.msg_no_apps);
            appList.add(d);
        }


        if (blockedAppList.size() == 0) {
            AppListInfo d = new AppListInfo();
            d.errorMessage = context.getResources().getString(R.string.msg_no_apps);
            blockedAppList.add(d);
        }

    }
}