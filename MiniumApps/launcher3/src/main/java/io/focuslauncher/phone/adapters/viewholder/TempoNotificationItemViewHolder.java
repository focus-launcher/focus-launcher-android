package io.focuslauncher.phone.adapters.viewholder;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.focuslauncher.R;
import io.focuslauncher.phone.app.BitmapWorkerTask;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.helper.FirebaseHelper;
import io.focuslauncher.phone.utils.PrefSiempo;


public class TempoNotificationItemViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.txt_app_name)
    TextView txt_app_name;
    @BindView(R.id.img_block_unblock)
    ImageView img_block_unblock;


    @BindView(R.id.imv_appicon)
    ImageView imv_appicon;

    @BindView(R.id.linearList)
    LinearLayout linearList;
    Context context;

    public TempoNotificationItemViewHolder(View itemView, Context context) {
        super(itemView);

        img_block_unblock = itemView.findViewById(R.id.img_block_unblock);
        txt_app_name = itemView.findViewById(R.id.txt_app_name);
        imv_appicon = itemView.findViewById(R.id.imv_appicon);
        linearList = itemView.findViewById(R.id.linearList);
        this.context = context;
        ButterKnife.bind(this, itemView);
    }

    public void render(String text) {
        txt_app_name.setText(text);
    }

    public void displayToggle() {
        img_block_unblock.setVisibility(View.VISIBLE);
    }


    public ImageView getToggle() {
        return img_block_unblock;
    }

    public LinearLayout getLinearList() {
        return linearList;
    }


    public void displayImage(String packageName, PackageManager packageManager, String errormessage) {
        if (TextUtils.isEmpty(errormessage)) {

            Bitmap bitmap = CoreApplication.getInstance().getBitmapFromMemCache(packageName);
            if (bitmap != null) {
                imv_appicon.setImageBitmap(bitmap);
            } else {

                ApplicationInfo appInfo = null;
                try {
                    appInfo = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                    BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(appInfo, context.getPackageManager());
                    CoreApplication.getInstance().includeTaskPool(bitmapWorkerTask, null);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                Drawable drawable = CoreApplication.getInstance().getApplicationIconFromPackageName(packageName);
                if (drawable != null) {
                    imv_appicon.setImageDrawable(drawable);
                } else {
                    imv_appicon.setImageBitmap(null);
                }
            }
        } else {
            imv_appicon.setImageBitmap(null);
        }
    }

    public void disableViews() {
        imv_appicon.setVisibility(View.INVISIBLE);
        img_block_unblock.setVisibility(View.INVISIBLE);
//        txt_app_name.setTextColor(Color.parseColor("#777777"));
        txt_app_name.setTextSize(12);

    }

    public void enableViews() {
        txt_app_name.setTextSize(16);
//        txt_app_name.setTextColor(Color.parseColor("#000000"));
        img_block_unblock.setVisibility(View.VISIBLE);
        imv_appicon.setVisibility(View.VISIBLE);
    }

    public void changeNotification(ApplicationInfo applicationInfo, boolean ischecked, ArrayList<String> disableNotificationApps, Context context) {
        if (ischecked && disableNotificationApps.contains(applicationInfo.packageName)) {
            disableNotificationApps.remove(applicationInfo.packageName);
        }
        if (!ischecked && !disableNotificationApps.contains(applicationInfo.packageName)) {
            disableNotificationApps.add(applicationInfo.packageName);
        }
        String disableList = new Gson().toJson(disableNotificationApps);
        PrefSiempo.getInstance(context).write(PrefSiempo.HELPFUL_ROBOTS,
                disableList);
//        launcherPrefs.edit().putString(Constants.HELPFUL_ROBOTS, disableList).commit();
    }


    public void addToBlockList(String applicationInfo, boolean ischecked, Set<String> blockedApps, Context context) {

        if (ischecked && blockedApps.contains(applicationInfo)) {
            blockedApps.remove(applicationInfo);
            FirebaseHelper.getInstance().logBlockUnblockApplication(applicationInfo, 0);
        }
        if (!ischecked && !blockedApps.contains(applicationInfo)) {
            blockedApps.add(applicationInfo);
            FirebaseHelper.getInstance().logBlockUnblockApplication(applicationInfo, 1);
        }
        PrefSiempo.getInstance(context).write(PrefSiempo.BLOCKED_APPLIST, blockedApps);


    }


}
