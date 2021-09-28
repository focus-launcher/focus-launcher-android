package io.focuslauncher.phone.adapters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.PopupMenu;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.focuslauncher.R;
import io.focuslauncher.phone.activities.JunkfoodFlaggingActivity;
import io.focuslauncher.phone.app.BitmapWorkerTask;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.models.AppListInfo;
import io.focuslauncher.phone.utils.Sorting;
import io.focuslauncher.phone.utils.UIUtils;

/**
 * Created by rajeshjadi on 26/2/18.
 */

public class JunkfoodFlaggingAdapter extends BaseAdapter implements Filterable {

    public final JunkfoodFlaggingActivity context;
    private final int foreground;
    private String searchReference = "";
    private ArrayList<AppListInfo> mData;
    private LayoutInflater mInflater;
    private List<AppListInfo> filterList;
    private ItemFilter mFilter = new ItemFilter();
    private PopupMenu popup;

    public JunkfoodFlaggingAdapter(JunkfoodFlaggingActivity context, ArrayList<AppListInfo> mData) {
        this.context = context;
        this.mData = mData;
        filterList = mData;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.flag_foreground, typedValue, true);
        foreground = typedValue.resourceId;
    }

    public int getCount() {
        return filterList.size();
    }

    public void setData(ArrayList<AppListInfo> bind) {
        mData = bind;
        filterList = bind;
        notifyDataSetChanged();
    }

    public AppListInfo getItem(int position) {
        return filterList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.list_item_junkfoodflag, parent, false);
            holder.txtAppName = convertView.findViewById(R.id.txtAppName);
            holder.imgAppIcon = convertView.findViewById(R.id.imgAppIcon);
            holder.imgChevron = convertView.findViewById(R.id.imgChevron);
            holder.linTop = convertView.findViewById(R.id.linTop);
            holder.txtNoAppsMessage = convertView.findViewById(R.id.txtNoAppsMessage);
            holder.txtHeader = convertView.findViewById(R.id.txtHeader);
            holder.linearList = convertView.findViewById(R.id.linearList);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

//        holder.linTop.setTag(filterList.get(position));
        try {
            final AppListInfo resolveInfo = filterList.get(position);
            Log.d("Tetsing ", resolveInfo.toString());
            if (resolveInfo.isShowHeader && resolveInfo.isShowTitle) {
                holder.txtHeader.setVisibility(View.VISIBLE);
                holder.txtNoAppsMessage.setVisibility(View.VISIBLE);
                holder.linTop.setVisibility(View.GONE);
                if (resolveInfo.isFlagApp) {
                    holder.txtHeader.setBackgroundColor(ContextCompat.getColor(context, R.color.flageapp_header));
//                    holder.txtHeader.setText(context.getString(R.string.flag_app));
                    holder.txtHeader.setText(context.getString(R.string.flag_app));

                    holder.txtNoAppsMessage.setBackground(new
                            ColorDrawable(ContextCompat.getColor(context,
                            foreground)));
                    holder.txtNoAppsMessage.setText(searchReference.equalsIgnoreCase("") ? context.getString(R.string.tap_apps_below_to_move_them_into_this_section) : context.getString(R
                            .string.no_apps));

                } else {
                    holder.txtHeader.setBackgroundColor(ContextCompat.getColor(context, R.color.unflageapp_header));
                    holder.txtHeader.setText(context.getString(R.string.all_other_installed_apps));
                    int color = R.color.transparent;

                    holder.txtNoAppsMessage.setBackground(new ColorDrawable
                            (ContextCompat.getColor(context, color)));
                    holder.txtNoAppsMessage.setText(searchReference.equalsIgnoreCase("") ? context.getString(R.string.tap_apps_above_to_move_them_into_this_section) : context.getString(R
                            .string.no_apps));
                }
            } else if (resolveInfo.isShowHeader && !resolveInfo.isShowTitle) {
                holder.txtHeader.setVisibility(View.VISIBLE);
                holder.linTop.setVisibility(View.GONE);
                if (resolveInfo.isFlagApp) {
                    holder.txtHeader.setBackgroundColor(ContextCompat.getColor(context, R.color.flageapp_header));
                    holder.txtHeader.setText(context.getString(R.string.flag_app_cell));
                } else {
                    holder.txtHeader.setBackgroundColor(ContextCompat.getColor(context, R.color.unflageapp_header));
                    holder.txtHeader.setText(context.getString(R.string.all_other_installed_apps));
                }
                holder.txtNoAppsMessage.setVisibility(View.GONE);

            } else {
                holder.linTop.setVisibility(View.VISIBLE);
                holder.txtNoAppsMessage.setVisibility(View.GONE);
                holder.txtHeader.setVisibility(View.GONE);
                try {
                    //Done as a part of SSA-1454, in order to change the app name
                    // based on user selected language
                    holder.txtAppName.setText(CoreApplication.getInstance()
                            .getApplicationNameFromPackageName(resolveInfo.packageName));
                    Bitmap bitmap = CoreApplication.getInstance().getBitmapFromMemCache(resolveInfo.packageName);
                    if (bitmap != null) {
                        holder.imgAppIcon.setImageBitmap(bitmap);
                    } else {
                        ApplicationInfo appInfo = null;
                        try {
                            appInfo = context.getPackageManager().getApplicationInfo(resolveInfo.packageName, PackageManager.GET_META_DATA);
                            BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(appInfo, context.getPackageManager());
                            CoreApplication.getInstance().includeTaskPool(bitmapWorkerTask, null);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        Drawable drawable = CoreApplication.getInstance().getApplicationIconFromPackageName(resolveInfo.packageName);
                        holder.imgAppIcon.setImageDrawable(drawable);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (resolveInfo.isFlagApp) {
                    holder.imgChevron.setImageResource(R.drawable.ic_down_arrow_red);
                    holder.linTop.setBackground(new ColorDrawable(ContextCompat
                            .getColor(context, foreground)));

                } else {
                    holder.imgChevron.setImageResource(R.drawable.ic_down_arrow);
                    int color = R.color.transparent;
                    holder.linTop.setBackground(new ColorDrawable(ContextCompat.getColor(context, color)));

                }
            }
            holder.linearList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!resolveInfo.packageName.equalsIgnoreCase("")) {
                        UIUtils.hideSoftKeyboard(context, context.getWindow().getDecorView().getWindowToken());
                        context.showPopUp(v, resolveInfo.packageName, resolveInfo.isFlagApp);
                    }
                }
            });

            holder.txtHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIUtils.hideSoftKeyboard(context, context.getWindow().getDecorView().getWindowToken());
                }
            });
            holder.txtNoAppsMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIUtils.hideSoftKeyboard(context, context.getWindow().getDecorView().getWindowToken());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            CoreApplication.getInstance().logException(e);
        }
        return convertView;
    }


    public Filter getFilter() {
        return mFilter;
    }


    public static class ViewHolder {
        ImageView imgChevron, imgAppIcon;
        TextView txtNoAppsMessage;
        TextView txtAppName, txtHeader;
        LinearLayout linTop, linearList;
    }

    private class ItemFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String searchString = constraint.toString().toLowerCase().trim();
            searchReference = searchString;
            FilterResults ret = new FilterResults();

            int count = mData.size();
            final ArrayList<AppListInfo> nlist = new ArrayList<>();


            String filterableString;
            List<AppListInfo> bindingList = new ArrayList<>();
            if (!searchString.isEmpty()) {
                for (int i = 0; i < count; i++) {
                    filterableString = mData.get(i).applicationName;
                    if (filterableString.toLowerCase().contains(searchString)) {
                        nlist.add(mData.get(i));
                    }
                }

                List<AppListInfo> flagAppList = new ArrayList<>();
                List<AppListInfo> unflageAppList = new ArrayList<>();
                for (AppListInfo resolveInfo : nlist) {
                    if (!resolveInfo.packageName.equalsIgnoreCase(context.getPackageName())) {
                        String applicationname = CoreApplication.getInstance()
                                .getListApplicationName().get(resolveInfo.packageName);
                        if (!TextUtils.isEmpty(applicationname)) {
                            if (context.getAdapterlist().contains(resolveInfo.packageName)) {
                                flagAppList.add(new AppListInfo(resolveInfo.packageName, applicationname, false, false, true));
                            } else {
                                unflageAppList.add(new AppListInfo(resolveInfo.packageName, applicationname, false, false, false));
                            }
                        }
                    }
                }
                //Code for removing the junk app from Favorite Sorted Menu and
                //Favorite List

                if (flagAppList.size() == 0) {
                    flagAppList.add(new AppListInfo("", "", true, true, true));
                } else {
                    flagAppList.add(0, new AppListInfo("", "", true, false, true));
                }

                flagAppList = Sorting.sortApplication(flagAppList);
                bindingList.addAll(flagAppList);
                if (unflageAppList.size() == 0) {
                    unflageAppList.add(new AppListInfo("", "", true, true, false));
                } else {
                    unflageAppList.add(0, new AppListInfo("", "", true, false, false));
                }
                unflageAppList = Sorting.sortApplication(unflageAppList);
                bindingList.addAll(unflageAppList);
            } else {
                bindingList.addAll(mData);
            }
            ret.values = bindingList;
            ret.count = bindingList.size();
            return ret;
        }


        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.values != null) {
                filterList = (List<AppListInfo>) results.values;
            } else {
                filterList = new ArrayList<>(mData);
            }
            notifyDataSetChanged();
        }
    }


}
