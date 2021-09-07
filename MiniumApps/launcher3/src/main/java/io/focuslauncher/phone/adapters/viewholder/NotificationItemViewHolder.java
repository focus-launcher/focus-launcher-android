package io.focuslauncher.phone.adapters.viewholder;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.focuslauncher.R;
import io.focuslauncher.phone.utils.PrefSiempo;


public class NotificationItemViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.txt_app_name)
    TextView txt_app_name;
    @BindView(R.id.switch_appNotification)
    Switch switch_appNotification;


    @BindView(R.id.imv_appicon)
    ImageView imv_appicon;


    public NotificationItemViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void render(String text) {
        txt_app_name.setText(text);
    }

    public void displayToggle() {
        switch_appNotification.setVisibility(View.VISIBLE);
    }

    public void setData(boolean ischecked) {
        switch_appNotification.setChecked(ischecked);
    }

    public Switch getToggle() {
        return switch_appNotification;
    }


    public void changeNotification(ApplicationInfo applicationInfo, boolean ischecked, ArrayList<String> disableNotificationApps, Context context) {
        if (ischecked && disableNotificationApps.contains(applicationInfo.packageName)) {
            disableNotificationApps.remove(applicationInfo.packageName);
        }
        if (!ischecked && !disableNotificationApps.contains(applicationInfo.packageName)) {
            disableNotificationApps.add(applicationInfo.packageName);
        }
        String disableList = new Gson().toJson(disableNotificationApps);
        PrefSiempo.getInstance(context).write(PrefSiempo.HELPFUL_ROBOTS, disableList);
//        launcherPrefs.edit().putString(Constants.HELPFUL_ROBOTS, disableList).commit();
        switch_appNotification.setChecked(ischecked);
    }


}
