package io.focuslauncher.phone.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import io.focuslauncher.R;
import io.focuslauncher.phone.activities.AppAssignmentActivity;
import io.focuslauncher.phone.activities.CoreActivity;
import io.focuslauncher.phone.activities.DashboardActivity;
import io.focuslauncher.phone.activities.ToolPositioningActivity;
import io.focuslauncher.phone.app.BitmapWorkerTask;
import io.focuslauncher.phone.app.Constants;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.helper.ActivityHelper;
import io.focuslauncher.phone.helper.FirebaseHelper;
import io.focuslauncher.phone.models.AppMenu;
import io.focuslauncher.phone.models.MainListItem;
import io.focuslauncher.phone.utils.PrefSiempo;
import io.focuslauncher.phone.utils.UIUtils;

/**
 * Created by Shahab on 2/23/2017.
 */

public class ToolsMenuAdapter extends RecyclerView.Adapter<ToolsMenuAdapter.ViewHolder> {

    private final Context context;
    private List<MainListItem> mainListItemList;
    private boolean isHideIconBranding;
    private HashMap<Integer, AppMenu> map;

    public ToolsMenuAdapter(Context context, boolean isHideIconBranding, boolean isBottomDoc, List<MainListItem> mainListItemList) {
        this.context = context;
        this.mainListItemList = mainListItemList;
        this.isHideIconBranding = isHideIconBranding;
        map = CoreApplication.Companion.getInstance().getToolsSettings();
    }

    @Override
    public ToolsMenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(
                parent.getContext());
        View v =
                inflater.inflate(R.layout.list_item_grid, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        try {
            final MainListItem item = mainListItemList.get(position);
            final AppMenu appMenu = map.get(item.getId());
            if (null != appMenu && appMenu.isVisible()) {
                holder.linearLayout.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(item.getTitle())) {
                    Log.d("Test", "Title is ::" + item.getTitle());
                    holder.text.setText(item.getTitle());
                }
                if (isHideIconBranding) {
                    holder.icon.setVisibility(View.VISIBLE);
                    holder.imgAppIcon.setVisibility(View.GONE);
                    try {
                        holder.icon.setImageResource(item.getDrawable());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    holder.text.setText(item.getTitle());
                } else {
                    Log.d("Test", "hideIcon branding false...");
                    holder.text.setText(CoreApplication.Companion.getInstance().getApplicationNameFromPackageName(appMenu.getApplicationName()));
                    Bitmap bitmap = CoreApplication.Companion.getInstance().getBitmapFromMemCache(appMenu.getApplicationName());
                    if (bitmap != null) {
                        Log.d("Test", "bitmap  null");
                        holder.icon.setVisibility(View.GONE);
                        holder.imgAppIcon.setVisibility(View.VISIBLE);
                        holder.imgAppIcon.setImageBitmap(bitmap);
                    } else {
                        Log.d("Test", "bitmap  not null");
                        if (!appMenu.getApplicationName().equalsIgnoreCase("")) {
                            ApplicationInfo appInfo = null;
                            try {
                                appInfo = context.getPackageManager().getApplicationInfo(appMenu.getApplicationName(), PackageManager.GET_META_DATA);
                                BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(appInfo, context.getPackageManager());
                                CoreApplication.Companion.getInstance().includeTaskPool(bitmapWorkerTask, null);
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        holder.icon.setVisibility(View.VISIBLE);
                        holder.imgAppIcon.setVisibility(View.GONE);
                        holder.icon.setImageResource(item.getDrawable());
                        holder.text.setText(item.getTitle());
                    }
                }
            } else {
                holder.icon.setImageDrawable(null);
                holder.text.setText("");
            }

            holder.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Intent intent = new Intent(context, ToolPositioningActivity.class);
                    intent.putExtra("ID", mainListItemList.get(holder.getAdapterPosition()).getId());
                    context.startActivity(intent);
                    ((CoreActivity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    return true;
                }
            });

            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = item.getId();
                    if (!holder.text.getText().toString().equalsIgnoreCase("")) {
                        if (!appMenu.getApplicationName().equalsIgnoreCase("")) {
                            if (appMenu.getApplicationName().equalsIgnoreCase("Notes")) {
                                new ActivityHelper(context).openNotesApp(false);
                                FirebaseHelper.getInstance().logSiempoMenuUsage(0, item.getTitle(), "Notes");
                            } else {
                                if (UIUtils.isInstalled(context, appMenu.getApplicationName().trim())) {
                                    if (UIUtils.isAppEnabled(context, appMenu.getApplicationName().trim())) {
                                        if (PrefSiempo.getInstance(context).read(PrefSiempo.JUNKFOOD_APPS,
                                                new HashSet<String>()).contains(appMenu.getApplicationName().trim())) {
                                            openAppAssignmentScreen(item);
                                        } else {
//                                If a 3rd party app is already assigned to this tool
                                            FirebaseHelper.getInstance().logSiempoMenuUsage(0, item.getTitle(), CoreApplication.Companion.getInstance().getApplicationNameFromPackageName(appMenu.getApplicationName()));
                                            if (id == 13) {
                                                try {
                                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                                    intent.setPackage(appMenu.getApplicationName().trim());
                                                    context.startActivity(intent);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    new ActivityHelper(context).openAppWithPackageName(appMenu.getApplicationName().trim());
                                                }
                                            } else {
                                                new ActivityHelper(context).openAppWithPackageName(appMenu.getApplicationName().trim());
                                            }
                                        }
                                    } else {
                                        openAppAssignmentScreen(item);
                                    }
                                }
                            }
                        } else {
                            if (CoreApplication.Companion.getInstance()
                                    .getApplicationByCategory(id).size() == 0) {
                                openAppAssignmentScreen(item);
                            } else if (CoreApplication.Companion.getInstance().getApplicationByCategory(id).size() == 1
                                    && !PrefSiempo.getInstance(context).read(PrefSiempo.JUNKFOOD_APPS,
                                    new HashSet<String>()).contains(appMenu.getApplicationName().trim())) {
//                            If a 3 rd party app is already assigned to this tool
                                ResolveInfo resolveInfo = CoreApplication.Companion.getInstance().getApplicationByCategory(id).get(0);
                                if (null != resolveInfo) {
                                    String strPackageName = resolveInfo.activityInfo.packageName;
                                    if (UIUtils.isAppEnabled(context, strPackageName) && strPackageName.equalsIgnoreCase(appMenu.getApplicationName())) {

                                        if (id == 13) {
                                            try {
                                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                                intent.setPackage(strPackageName);
                                                context.startActivity(intent);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                new ActivityHelper(context).openAppWithPackageName(strPackageName);
                                            }
                                        } else {
                                            new ActivityHelper(context).openAppWithPackageName(strPackageName);
                                        }
                                        FirebaseHelper.getInstance().logSiempoMenuUsage(0, item.getTitle(), CoreApplication.Companion.getInstance().getApplicationNameFromPackageName(appMenu.getApplicationName()));
                                    } else {
                                        openAppAssignmentScreen(item);
                                    }
                                } else {
                                    //Notes
                                    if (id == 5) {
                                        openAppAssignmentScreen(item);
                                    }

                                }

                            } else {
                                openAppAssignmentScreen(item);
                            }
                        }
                    }
                }
            });

            boolean isEnable = PrefSiempo.getInstance(context).read(PrefSiempo.DEFAULT_ICON_TOOLS_TEXT_VISIBILITY_ENABLE, false);
            if(isEnable)
            {
                holder.txtLayout.setVisibility(View.GONE);
            }else
            {
                holder.txtLayout.setVisibility(View.VISIBLE);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     * if the user has multiple apps that are installed and relevant to this tool (e.g. tool is browser, and Chrome and Firefox are installed)
     * navigate to the tool-app assignment screen
     * if the user has one one app that is installed and relevant to this tool (e.g. tool is browser, and only Chrome is installed)
     * if that 3rd party app IS currently flagged by this user as junkfood
     * navigate to the tool-app assignment screen
     *
     * @param item
     */
    private void openAppAssignmentScreen(MainListItem item) {
        FirebaseHelper.getInstance().logSiempoMenuUsage(0, item.getTitle(), "");
        Intent intent = new Intent(context, AppAssignmentActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(Constants.INTENT_MAINLISTITEM, item);
        intent.putExtra("class_name", DashboardActivity.class.getSimpleName());
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return mainListItemList.size();

    }

    public void setMainListItemList(List<MainListItem> mainListItemList, boolean isBottomDoc, boolean isHideIconBranding) {
        this.mainListItemList = mainListItemList;
        this.isHideIconBranding = isHideIconBranding;
        map = CoreApplication.Companion.getInstance().getToolsSettings();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View layout;
        // each data item is just a string in this case
        ImageView icon, imgView;
        ImageView imgAppIcon;
        TextView text;
        TextView textDefaultApp;
        RelativeLayout relMenu;
        private LinearLayout linearLayout;
        LinearLayout txtLayout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            linearLayout = v.findViewById(R.id.linearList);
            relMenu = v.findViewById(R.id.relMenu);
            text = v.findViewById(R.id.text);
            textDefaultApp = v.findViewById(R.id.textDefaultApp);
            icon = v.findViewById(R.id.icon);
            imgView = v.findViewById(R.id.imgView);
            imgAppIcon = v.findViewById(R.id.imgAppIcon);
            txtLayout = v.findViewById(R.id.tools_txtLayout);
        }
    }
}
