package io.focuslauncher.phone.adapters.viewholder;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import io.focuslauncher.R;
import io.focuslauncher.phone.activities.AppAssignmentActivity;
import io.focuslauncher.phone.activities.DashboardActivity;
import io.focuslauncher.phone.activities.JunkfoodFlaggingActivity;
import io.focuslauncher.phone.app.BitmapWorkerTask;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.helper.ActivityHelper;
import io.focuslauncher.phone.models.AppMenu;
import io.focuslauncher.phone.service.LoadToolPane;
import io.focuslauncher.phone.utils.DrawableProvider;
import io.focuslauncher.phone.utils.PrefSiempo;


public class AppAssignmentAdapter extends RecyclerView.Adapter<AppAssignmentAdapter.ViewHolder>
        implements Filterable {
    private final AppAssignmentActivity context;
    private List<ResolveInfo> filterList;
    private List<ResolveInfo> resolveInfoList;
    private HashMap<Integer, AppMenu> map;
    private DrawableProvider mProvider;
    private int id;
    private String class_name;
    private ItemFilter mFilter = new ItemFilter();

    public AppAssignmentAdapter(AppAssignmentActivity context, int id, List<ResolveInfo> resolveInfoList, String class_name) {
        this.context = context;
        this.resolveInfoList = resolveInfoList;
        this.id = id;
        filterList = resolveInfoList;
        map = CoreApplication.getInstance().getToolsSettings();
        mProvider = new DrawableProvider(context);
        this.class_name = class_name;
    }

    public void setdata(ArrayList<ResolveInfo> appListAll) {
        resolveInfoList.clear();
        filterList.clear();
        resolveInfoList = appListAll;
        filterList = resolveInfoList;
        notifyDataSetChanged();
    }

    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public AppAssignmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(
                parent.getContext());
        View v =
                inflater.inflate(R.layout.list_item_app_assignment, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ResolveInfo item = filterList.get(position);
        if (id == 5 && item == null) {
            holder.txtAppName.setText(context.getString(R.string.label_note));
            holder.btnHideApps.setVisibility(View.GONE);
            holder.imgIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_menu_notes));
        } else if (item != null) {
            holder.txtAppName.setText(item.loadLabel(context.getPackageManager()));
            String packageName = item.activityInfo.packageName;
            if (PrefSiempo.getInstance(context).read(PrefSiempo.JUNKFOOD_APPS, new HashSet<String>()).contains(packageName)) {
                holder.btnHideApps.setVisibility(View.VISIBLE);
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = context.getTheme();
                theme.resolveAttribute(R.attr.icon_color, typedValue, true);
                int color = typedValue.data;
                Drawable drawable = mProvider.getRound("" + item.loadLabel
                        (context.getPackageManager()).charAt(0), color, 30);
                holder.imgIcon.setImageDrawable(drawable);
                holder.btnHideApps.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, JunkfoodFlaggingActivity.class);
                        context.startActivity(intent);
                    }
                });
            } else {
                Bitmap bitmap = CoreApplication.getInstance().getBitmapFromMemCache(item.activityInfo.packageName);
                if (bitmap != null) {
                    holder.imgIcon.setImageBitmap(bitmap);
                } else {
                    ApplicationInfo appInfo = null;
                    try {
                        appInfo = context.getPackageManager().getApplicationInfo(item.activityInfo.packageName, PackageManager.GET_META_DATA);
                        BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(appInfo, context.getPackageManager());
                        CoreApplication.getInstance().includeTaskPool(bitmapWorkerTask, null);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    Drawable drawable = CoreApplication.getInstance().getApplicationIconFromPackageName(item.activityInfo.packageName);
                    holder.imgIcon.setImageDrawable(drawable);
                }
                holder.btnHideApps.setVisibility(View.GONE);
//                holder.txtAppName.setTextColor(ContextCompat.getColor(context, R.color.app_assignment_normal));

            }
        }
        holder.linearList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.btnHideApps.getVisibility() != View.VISIBLE) {
                    HashMap<Integer, AppMenu> map = CoreApplication.getInstance().getToolsSettings();
                    boolean isSameApp = false;
                    if (id == 5 && item == null) {
                        if (map.get(id).getApplicationName().equalsIgnoreCase("Notes")) {
                            isSameApp = true;
                        } else {
                            isSameApp = false;
                            map.get(id).setApplicationName(context.getString(R.string.notes));
                        }
                    } else {
                        if (null != item) {
                            map.get(id).setVisible(true);
                            if (map.get(id).getApplicationName().equalsIgnoreCase(item.activityInfo.packageName)) {
                                isSameApp = true;
                            } else {
                                isSameApp = false;
                                map.get(id).setApplicationName(item.activityInfo.packageName);
                            }
                        }
                    }

                    String hashMapToolSettings = new Gson().toJson(map);
                    PrefSiempo.getInstance(context).write(PrefSiempo.TOOLS_SETTING, hashMapToolSettings);

                    new LoadToolPane().execute();
                    if (class_name.equalsIgnoreCase(DashboardActivity.CLASS_NAME)) {
                        if (id == 5 && item == null) {
                            new ActivityHelper(context).openNotesApp(false);
                            context.finish();

                        } else {
                            if (item != null) {
                                new ActivityHelper(context).openAppWithPackageName(item.activityInfo
                                        .packageName);
                            }
                            context.finish();
                        }
                    } else {
                        Intent returnIntent = new Intent();
                        context.setResult(isSameApp ? Activity.RESULT_CANCELED : Activity.RESULT_OK, returnIntent);
                        context.finish();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return filterList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        ImageView imgIcon;
        TextView txtAppName;
        Button btnHideApps;
        RelativeLayout linearList;

        public ViewHolder(View v) {
            super(v);
            imgIcon = v.findViewById(R.id.imgIcon);
            txtAppName = v.findViewById(R.id.txtAppName);
            btnHideApps = v.findViewById(R.id.btnHideApps);
            linearList = v.findViewById(R.id.linearList);
        }
    }

    private class ItemFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String searchString = constraint.toString().toLowerCase().trim();
            FilterResults ret = new FilterResults();

            int count = resolveInfoList.size();
            List<ResolveInfo> templist = new ArrayList<>();

            String filterableString;

            if (!searchString.isEmpty()) {
                try {
                    for (int i = 0; i < count; i++) {
                        if (id == 5 && resolveInfoList.get(i) == null) {
                            filterableString = context.getString(R.string.label_note);
                        } else {
                            filterableString = CoreApplication.getInstance().getListApplicationName()
                                    .get(resolveInfoList.get(i)
                                            .activityInfo
                                            .packageName);
                        }
                        if (filterableString == null) {
                            filterableString = CoreApplication.getInstance()
                                    .getApplicationNameFromPackageName(resolveInfoList.get(i)
                                            .activityInfo.packageName);
                        }
                        if (filterableString != null) {
                            if (filterableString.toLowerCase().contains(searchString.toLowerCase())) {
                                templist.add(resolveInfoList.get(i));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                templist = resolveInfoList;
            }
            ret.values = templist;
            ret.count = templist.size();
            return ret;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.values != null) {
                filterList = (ArrayList<ResolveInfo>) results.values;
            } else {
                filterList = new ArrayList<>(resolveInfoList);
            }
            if (filterList != null && filterList.size() > 0) {
                context.hideOrShowMessage(true);
            } else {
                context.hideOrShowMessage(false);
            }
            notifyDataSetChanged();
        }
    }
}
