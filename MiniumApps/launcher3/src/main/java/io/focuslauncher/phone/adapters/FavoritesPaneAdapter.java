package io.focuslauncher.phone.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.focuslauncher.R;
import io.focuslauncher.phone.activities.CoreActivity;
import io.focuslauncher.phone.activities.FavoriteAppsPositionActivity;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.helper.ActivityHelper;
import io.focuslauncher.phone.helper.FirebaseHelper;
import io.focuslauncher.phone.models.AppMenu;
import io.focuslauncher.phone.models.MainListItem;
import io.focuslauncher.phone.utils.DrawableProvider;
import io.focuslauncher.phone.utils.PrefSiempo;

/**
 * Created by Shahab on 2/23/2017.
 */

public class FavoritesPaneAdapter extends RecyclerView.Adapter<FavoritesPaneAdapter.ViewHolder> {

    private final Context context;
    private List<MainListItem> mainListItemList;
    private boolean isBottomDoc = false, isHideIconBranding;
    private HashMap<Integer, AppMenu> map;
    private DrawableProvider mProvider;

    public FavoritesPaneAdapter(Context context, boolean isHideIconBranding, boolean isBottomDoc, List<MainListItem> mainListItemList) {
        this.context = context;
        this.mainListItemList = mainListItemList;
        this.isHideIconBranding = isHideIconBranding;
        this.isBottomDoc = isBottomDoc;
        mProvider = new DrawableProvider(context);
        map = CoreApplication.Companion.getInstance().getToolsSettings();
    }

    @Override
    public FavoritesPaneAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(
                parent.getContext());
        View v =
                inflater.inflate(R.layout.list_application_item_grid, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final MainListItem item = mainListItemList.get(position);
        holder.linearLayout.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(item.getTitle())) {

            //Done as a part of SSA-1454, in order to change the app name
            // based on user selected language, and in case of package nme
            // not available showing the default item name
            if (!TextUtils.isEmpty(item.getPackageName())) {
                String applicationName = CoreApplication.Companion.getInstance()
                        .getApplicationNameFromPackageName(item.getPackageName());
                holder.text.setText(applicationName);
            } else {
                holder.text.setText(item.getTitle());
            }
        }
        if (!TextUtils.isEmpty(item.getPackageName())) {
            if (isHideIconBranding) {
                holder.imgAppIcon.setVisibility(View.GONE);
                holder.txtAppTextImage.setVisibility(View.VISIBLE);
                holder.imgUnderLine.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(item.getTitle())) {
                } else {
                    String fontPath = "fonts/robotocondensedregular.ttf";
                    holder.txtAppTextImage.setText("" + item
                            .getTitle().toUpperCase().charAt(0));

                    // Loading Font Face
                    Typeface tf = Typeface.createFromAsset(context.getAssets(), fontPath);
                    // Applying font
                    holder.txtAppTextImage.setTypeface(tf);
                }

            } else {
                holder.imgAppIcon.setVisibility(View.VISIBLE);
                holder.txtAppTextImage.setVisibility(View.GONE);
                holder.imgUnderLine.setVisibility(View.GONE);
                Drawable drawable = CoreApplication.Companion.getInstance().getApplicationIconFromPackageName(item.getPackageName());
                if (drawable != null) {
                    holder.imgAppIcon.setImageDrawable(drawable);
                } else {
                    holder.linearLayout.setVisibility(View.INVISIBLE);
                }
            }


        } else {
            holder.imgAppIcon.setImageDrawable(null);
            holder.text.setText("");
            holder.txtAppTextImage.setText("");
            holder.imgUnderLine.setVisibility(View.GONE);
        }

        holder.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(context, FavoriteAppsPositionActivity.class);
                context.startActivity(intent);
                ((CoreActivity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            }
        });

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = item.getId();
                if (holder.linearLayout.getVisibility() == View.VISIBLE) {
                    if (!TextUtils.isEmpty(item.getPackageName())) {
                        FirebaseHelper.getInstance().logSiempoMenuUsage(1, "", CoreApplication.Companion.getInstance().getApplicationNameFromPackageName(item.getPackageName().trim()));
                        new ActivityHelper(context).openAppWithPackageName(item.getPackageName().trim());
                    }
                }
            }
        });

        boolean isEnable = PrefSiempo.getInstance(context).read(PrefSiempo.DEFAULT_ICON_FAVORITE_TEXT_VISIBILITY_ENABLE, false);
        if(isEnable)
        {
            holder.txtLayout.setVisibility(View.GONE);
        }else
        {
            holder.txtLayout.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public int getItemCount() {
        return mainListItemList.size();
    }

    public Drawable getAppIconByPackageName(String ApkTempPackageName, Context context) {

        Drawable drawable;

        try {
            drawable = context.getPackageManager().getApplicationIcon(ApkTempPackageName);

        } catch (PackageManager.NameNotFoundException e) {

            e.printStackTrace();

            drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher);
        }
        return drawable;
    }

    public void setMainListItemList(ArrayList<MainListItem> mainListItemList, boolean hideIconBranding) {
        this.mainListItemList = mainListItemList;
        this.isHideIconBranding = hideIconBranding;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View layout;
        // each data item is just a string in this case
        ImageView imgView, imgAppIcon;
        View imgUnderLine;
        TextView text, txtAppTextImage;
        TextView textDefaultApp;
        RelativeLayout relMenu;
        private LinearLayout linearLayout;
        LinearLayout txtLayout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            linearLayout = v.findViewById(R.id.linearList);
            imgUnderLine = v.findViewById(R.id.imgUnderLine);
            relMenu = v.findViewById(R.id.relMenu);
            text = v.findViewById(R.id.text);
            textDefaultApp = v.findViewById(R.id.textDefaultApp);
            txtAppTextImage = v.findViewById(R.id.txtAppTextImage);
            imgAppIcon = v.findViewById(R.id.imgAppIcon);
            imgView = v.findViewById(R.id.imgView);
            txtLayout = v.findViewById(R.id.favorite_txtLayout);
        }
    }
}
