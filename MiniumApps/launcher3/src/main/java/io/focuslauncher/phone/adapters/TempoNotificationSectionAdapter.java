package io.focuslauncher.phone.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.focuslauncher.R;
import io.focuslauncher.phone.adapters.viewholder.NoticationFooterViewHolder;
import io.focuslauncher.phone.adapters.viewholder.TempoNotificationHeaderViewHolder;
import io.focuslauncher.phone.adapters.viewholder.TempoNotificationItemViewHolder;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.log.Tracer;
import io.focuslauncher.phone.models.AppListInfo;
import io.focuslauncher.phone.utils.PrefSiempo;
import io.focuslauncher.phone.utils.Sorting;

/**
 * Below adapter is use to Display the section wise below apps
 * 1. Humand Direct Messaging
 * 2. Helpful Robots
 * 3. All Other Apps
 */
public class TempoNotificationSectionAdapter extends SectionedRecyclerViewAdapter<TempoNotificationHeaderViewHolder,
        TempoNotificationItemViewHolder,
        NoticationFooterViewHolder> {

    protected Context context = null;
    private ArrayList<String> pref_helpfulRobots = new ArrayList<>();
    private Set<String> pref_blockedList = new HashSet<>();
    private ArrayList<String> pref_headerSectionList = new ArrayList<>();
    private SharedPreferences launcherPrefs;
    private AlertDialog alertDialog;
    private PopupMenu popup;
    private List<String> pref_messengerList = new ArrayList<>();
    private PackageManager packageManager;
    private List<AppListInfo> helpfulRobot_List, blockedList, messengerList;
    private List<AppListInfo> headerList;


    public TempoNotificationSectionAdapter(Context context, List<AppListInfo> helpfulRobot_List, List<AppListInfo> messengerList, List<AppListInfo> blockedList, List<AppListInfo> headerList) {
        this.context = context;
        this.helpfulRobot_List = helpfulRobot_List;
        this.blockedList = blockedList;
        this.headerList = headerList;
        this.messengerList = messengerList;
        this.packageManager = context.getPackageManager();

        String pref_helpfulRobots = PrefSiempo.getInstance(context).read(PrefSiempo.HELPFUL_ROBOTS,
                "");
        if (!TextUtils.isEmpty(pref_helpfulRobots)) {
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            this.pref_helpfulRobots = new Gson().fromJson(pref_helpfulRobots, type);
        }


        pref_blockedList = PrefSiempo.getInstance(context).read(PrefSiempo.BLOCKED_APPLIST, new HashSet<String>());


        String headerAppList = PrefSiempo.getInstance(context).read(PrefSiempo.HEADER_APPLIST, "");
        if (!TextUtils.isEmpty(headerAppList)) {
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            pref_headerSectionList = new Gson().fromJson(headerAppList, type);
        }

        pref_messengerList = new ArrayList<>();
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        List<ResolveInfo> messagingResolveList = context.getPackageManager()
                .queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : messagingResolveList) {
            if (!resolveInfo.activityInfo.packageName.equalsIgnoreCase(context.getPackageName())) {
                pref_messengerList.add(resolveInfo.activityInfo.packageName);
            }
        }
    }

    public List<AppListInfo> getHelpfulRobot_List() {
        return helpfulRobot_List;
    }

    public List<AppListInfo> getBlockedList() {
        return blockedList;
    }

    public List<AppListInfo> getMessengerList() {
        return messengerList;
    }

    @Override
    protected int getItemCountForSection(int section) {
        int size = 0;
        if (headerList.get(section).headerName.equals("All other apps")) {
            size = blockedList.size();
        }
        if (headerList.get(section).headerName.equals("Human direct messaging")) {
            size = messengerList.size();
        }
        if (headerList.get(section).headerName.equals("Helpful robots")) {
            size = helpfulRobot_List.size();
        }
        return size;
    }

    @Override
    protected int getSectionCount() {
        return headerList.size();
    }

    @Override
    protected boolean hasFooterInSection(int section) {
        return false;
    }

    protected LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(context);
    }

    @Override
    protected TempoNotificationHeaderViewHolder onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {
        View view = getLayoutInflater().inflate(R.layout.tempo_section_header_layout, parent, false);
        return new TempoNotificationHeaderViewHolder(view);
    }

    @Override
    protected NoticationFooterViewHolder onCreateSectionFooterViewHolder(ViewGroup parent, int viewType) {
        View view = getLayoutInflater().inflate(R.layout.view_count_footer, parent, false);
        return new NoticationFooterViewHolder(view);
    }

    @Override
    protected TempoNotificationItemViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = getLayoutInflater().inflate(R.layout.tempo_installed_app_list_row, parent, false);
        return new TempoNotificationItemViewHolder(view, context);
    }

    @Override
    protected void onBindSectionHeaderViewHolder(final TempoNotificationHeaderViewHolder holder, final int section) {

        holder.render(headerList.get(section).headerName);

        if (headerList.get(section).headerName.equalsIgnoreCase("All other apps")) {
            holder.getHeaderToggle().setText("(BLOCKED)");
        } else {
            holder.getHeaderToggle().setText("(OK)");
        }
    }

    @Override
    protected void onBindSectionFooterViewHolder(NoticationFooterViewHolder holder, int section) {
    }


    @Override
    protected void onBindItemViewHolder(final TempoNotificationItemViewHolder holder, final int section, final int position) {

        holder.displayToggle();

        if (headerList.get(section).headerName.equals("All other apps")) {

            final AppListInfo otherAppsItems = blockedList.get(position);
            holder.enableViews();
            String appName = CoreApplication.getInstance().getListApplicationName().get(otherAppsItems.packageName);
            holder.render(appName);


            if (!TextUtils.isEmpty(otherAppsItems.errorMessage)) {
                holder.render(otherAppsItems.errorMessage);
                holder.disableViews();
            }
            holder.displayImage(otherAppsItems.packageName, packageManager, otherAppsItems.errorMessage);
            holder.getLinearList().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (TextUtils.isEmpty(otherAppsItems.errorMessage)) {
                        if (popup != null) {
                            popup.dismiss();
                        }
                        popup = new PopupMenu(context, v, Gravity.END);
                        popup.getMenuInflater().inflate(R.menu.tempo_notification_popup, popup.getMenu());
                        MenuItem menuItem = popup.getMenu().findItem(R.id.block);
                        menuItem.setTitle(context.getResources().getString(R.string.allow_interrupt_msg));

                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                    holder.addToBlockList(otherAppsItems.packageName, true, pref_blockedList, context);
                                    blockedList.remove(otherAppsItems);
                                    if (pref_messengerList.contains(otherAppsItems.packageName)) {
                                        messengerList.add(otherAppsItems);

                                        int disableCount = PrefSiempo.getInstance(context).read
                                                (PrefSiempo.MESSENGER_DISABLE_COUNT, 0);
                                        PrefSiempo.getInstance(context).write
                                                (PrefSiempo
                                                        .MESSENGER_DISABLE_COUNT, disableCount - 1);
                                    } else {
                                        helpfulRobot_List.add(otherAppsItems);

                                        int disableCount = PrefSiempo.getInstance(context).read
                                                (PrefSiempo
                                                        .APP_DISABLE_COUNT, 0);

                                        PrefSiempo.getInstance(context).write
                                                (PrefSiempo
                                                        .MESSENGER_DISABLE_COUNT, disableCount - 1);
                                    }

                                    changeHeaderNotification(section, pref_headerSectionList, context);
                                return true;

                            }
                        });

                        popup.show();
                    }
                }
            });


        }


        if (headerList.get(section).headerName.equals("Human direct messaging")) {


            final AppListInfo messengerAppsItem = messengerList.get(position);
            String appName = CoreApplication.getInstance().getListApplicationName().get(messengerAppsItem
                    .packageName);
            holder.render(appName);
            holder.enableViews();
            if (!TextUtils.isEmpty(messengerAppsItem.errorMessage)) {
                holder.render(messengerAppsItem.errorMessage);
                holder.disableViews();
            }
            holder.displayImage(messengerAppsItem.packageName, packageManager, messengerAppsItem.errorMessage);

            holder.getLinearList().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (TextUtils.isEmpty(messengerAppsItem.errorMessage)) {


                        if (popup != null) {
                            popup.dismiss();
                        }
                        popup = new PopupMenu(context, v, Gravity.END);
                        popup.getMenuInflater().inflate(R.menu.tempo_notification_popup, popup.getMenu());

                        MenuItem menuItem = popup.getMenu().findItem(R.id.block);
                        menuItem.setTitle(context.getResources().getString(R.string.stop_interrupt_msg));


                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                holder.addToBlockList(messengerAppsItem.packageName, false, pref_blockedList, context);
                                messengerList.remove(messengerAppsItem);
                                blockedList.add(messengerAppsItem);
                                int disableCount = PrefSiempo.getInstance
                                        (context).read(PrefSiempo
                                        .MESSENGER_DISABLE_COUNT, 0);
                                PrefSiempo.getInstance(context).write
                                        (PrefSiempo
                                                        .MESSENGER_DISABLE_COUNT,
                                                disableCount + 1);
                                changeHeaderNotification(section, pref_headerSectionList, context);
                                return true;
                            }
                        });

                        popup.show();
                    }
                }
            });


        }


        if (headerList.get(section).headerName.equals("Helpful robots")) {
            final AppListInfo appListItem = helpfulRobot_List.get(position);
            holder.enableViews();
            String appName = CoreApplication.getInstance().getListApplicationName().get(appListItem.packageName);
            holder.render(appName);

            holder.displayImage(appListItem.packageName, packageManager, appListItem.errorMessage);
            if (!TextUtils.isEmpty(appListItem.errorMessage)) {
                holder.render(appListItem.errorMessage);
                holder.disableViews();
            }


            holder.getLinearList().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (TextUtils.isEmpty(appListItem.errorMessage)) {

                        if (popup != null) {
                            popup.dismiss();
                        }
                        popup = new PopupMenu(context, v, Gravity.END);

                        popup.getMenuInflater().inflate(R.menu.tempo_notification_popup, popup.getMenu());
                        MenuItem menuItem = popup.getMenu().findItem(R.id.block);
                        menuItem.setTitle(context.getResources().getString(R.string.stop_interrupt_msg));

                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                holder.addToBlockList(appListItem.packageName, false, pref_blockedList, context);
                                helpfulRobot_List.remove(appListItem);
                                blockedList.add(appListItem);
                                int disableCount = PrefSiempo.getInstance(context).read
                                        (PrefSiempo
                                                .APP_DISABLE_COUNT, 0);
                                PrefSiempo.getInstance(context).write(
                                        PrefSiempo
                                                .APP_DISABLE_COUNT, disableCount + 1);
                                changeHeaderNotification(section, pref_headerSectionList, context);
                                return true;
                            }
                        });

                        popup.show();

                    }


                }
            });


        }


    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    /**
     * This method is used to remove apps from Open Category to Blocked List - true
     * Or to move Blocked app to normal open app - false
     *
     * @param position
     * @param disableHeaderApps
     * @param context
     */
    private void changeHeaderNotification(int position, ArrayList<String> disableHeaderApps, Context context) {

        AppListInfo headerAppList = headerList.get(position);


        try {
            if (messengerList.size() > 1) {
                for (int i = 0; i < messengerList.size(); i++)
                    if (!TextUtils.isEmpty(messengerList.get(i).errorMessage)) {
                        messengerList.remove(messengerList.get(i));
                    }
            }

            if (helpfulRobot_List.size() > 1) {
                for (int i = 0; i < helpfulRobot_List.size(); i++)
                    if (!TextUtils.isEmpty(helpfulRobot_List.get(i).errorMessage)) {
                        helpfulRobot_List.remove(helpfulRobot_List.get(i));
                    }
            }

            if (blockedList.size() > 1) {
                for (int i = 0; i < blockedList.size(); i++)
                    if (!TextUtils.isEmpty(blockedList.get(i).errorMessage)) {
                        blockedList.remove(blockedList.get(i));
                    }
            }
        } catch (Exception e) {
            Tracer.i("Exception in remove error message");
        }


        if (messengerList.size() > 0) {
            Collections.sort(messengerList, new Comparator<AppListInfo>() {
                @Override
                public int compare(AppListInfo o1, AppListInfo o2) {
                    return o1.packageName.compareToIgnoreCase(o2.packageName);
                }
            });
        }

        if (helpfulRobot_List.size() > 0) {
            Collections.sort(helpfulRobot_List, new Comparator<AppListInfo>() {
                @Override
                public int compare(AppListInfo o1, AppListInfo o2) {
                    return o1.packageName.compareToIgnoreCase(o2.packageName);
                }
            });
        }

        if (blockedList.size() > 0) {

            Collections.sort(blockedList, new Comparator<AppListInfo>() {
                @Override
                public int compare(AppListInfo o1, AppListInfo o2) {
                    return o1.packageName.compareToIgnoreCase(o2.packageName);
                }
            });
        }


        if (messengerList.size() == 0) {
            AppListInfo d = new AppListInfo();
            d.errorMessage = context.getResources().getString(R.string.msg_no_apps);
            messengerList.add(d);
        }

        if (helpfulRobot_List.size() == 0) {
            AppListInfo d = new AppListInfo();
            d.errorMessage = context.getResources().getString(R.string.msg_no_apps);
            helpfulRobot_List.add(d);
        }

        if (blockedList.size() == 0) {
            AppListInfo d = new AppListInfo();
            d.errorMessage = context.getResources().getString(R.string.msg_no_apps);
            blockedList.add(d);
        }
        if (helpfulRobot_List.size() > 0) {
            helpfulRobot_List = Sorting.sortApplication(helpfulRobot_List);
        }
        if (messengerList.size() > 0) {
            messengerList = Sorting.sortApplication(messengerList);
        }
        if (blockedList.size() > 0) {
            blockedList = Sorting.sortApplication(blockedList);
        }
        notifyDataSetChanged();
    }


}
