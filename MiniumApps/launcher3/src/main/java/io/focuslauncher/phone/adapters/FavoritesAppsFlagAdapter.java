package io.focuslauncher.phone.adapters;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.focuslauncher.R;

/**
 * Created by hardik on 16/8/17.
 */

public class FavoritesAppsFlagAdapter extends ArrayAdapter<ResolveInfo> {
    private final Context context;
    boolean isFlagList;
    private List<ResolveInfo> resolveInfoList = null;

    public FavoritesAppsFlagAdapter(Context context, List<ResolveInfo> resolveInfoList, boolean isFlagList) {
        super(context, 0);
        this.context = context;
        this.resolveInfoList = resolveInfoList;
        this.isFlagList = isFlagList;
    }

    @Override
    public int getCount() {
        return resolveInfoList.size();
    }

    @Nullable
    @Override
    public ResolveInfo getItem(int position) {
        return resolveInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            if (!isFlagList) {
                convertView = inflater.inflate(R.layout.list_item_junkfoodflag, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.list_item_junkfoodflag_red, parent, false);
            }
            holder.txtAppName = convertView.findViewById(R.id.txtAppName);
            holder.imgAppIcon = convertView.findViewById(R.id.imgAppIcon);
            holder.imgChevron = convertView.findViewById(R.id.imgChevron);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ResolveInfo resolveInfo = resolveInfoList.get(position);
        try {
            holder.txtAppName.setText(resolveInfo.loadLabel(context.getPackageManager()));
            holder.imgAppIcon.setImageDrawable(resolveInfo.loadIcon(context.getPackageManager()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }


    private static class ViewHolder {
        ImageView imgChevron, imgAppIcon;
        TextView txtAppName;
    }

}