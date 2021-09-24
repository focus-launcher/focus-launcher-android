package io.focuslauncher.phone.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import io.focuslauncher.R;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.interfaces.ItemTouchHelperAdapter;
import io.focuslauncher.phone.interfaces.ItemTouchHelperViewHolder;
import io.focuslauncher.phone.interfaces.OnFavoriteItemListChangedListener;
import io.focuslauncher.phone.main.OnStartDragListener;
import io.focuslauncher.phone.models.AppMenu;
import io.focuslauncher.phone.models.MainListItem;

/**
 * Created by rajeshjadi on 14/2/18.
 */

public class FavoritePositioningAdapter extends RecyclerView.Adapter<FavoritePositioningAdapter.ItemViewHolder> implements ItemTouchHelperAdapter {
    private final Activity context;
    private final HashMap<Integer, AppMenu> map;
    private ArrayList<MainListItem> arrayList;
    private OnStartDragListener mDragStartListener;
    private boolean isHideIconBranding;
    private OnFavoriteItemListChangedListener mListChangedListener;

    // Provide a suitable constructor (depends on the kind of dataset)
    public FavoritePositioningAdapter(Activity context, boolean isHideIconBranding, ArrayList<MainListItem> arrayList, OnStartDragListener dragListener,
                                      OnFavoriteItemListChangedListener listChangedListener) {
        this.context = context;
        this.arrayList = arrayList;
        this.isHideIconBranding = isHideIconBranding;
        mDragStartListener = dragListener;
        mListChangedListener = listChangedListener;
        map = CoreApplication.Companion.getInstance().getToolsSettings();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        try {
            if (arrayList != null && arrayList.size() > 0) {
                if (fromPosition < toPosition) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        Collections.swap(arrayList, i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(arrayList, i, i - 1);
                    }
                }
                mListChangedListener.onFavoriteItemListChanged(arrayList);
                notifyItemMoved(fromPosition, toPosition);
            }
        } catch (Exception e) {
            CoreApplication.Companion.getInstance().logException(e);
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }


    // Create new views (invoked by the layout manager)
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(
                parent.getContext());
        View v;
        v = inflater.inflate(R.layout.list_application_item_grid, parent, false);
//             set the view's size, margins, paddings and layout parameters
        return new ItemViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        final MainListItem item = arrayList.get(position);
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
                if (!TextUtils.isEmpty(item.getTitle())) {
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
                Drawable drawable = getAppIconByPackageName(item.getPackageName(), context);
                if (drawable != null) {
                    holder.imgAppIcon.setImageDrawable(drawable);
                } else {
                    holder.linearLayout.setVisibility(View.INVISIBLE);
                }
            }


        } else {
            holder.linearLayout.setVisibility(View.INVISIBLE);
        }


        holder.linearLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!TextUtils.isEmpty(item.getPackageName())) {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        mDragStartListener.onStartDrag(holder);
                    }
                }
                return false;
            }
        });


    }

    @Override
    public int getItemCount() {
        return arrayList.size();
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

    class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {
        public View layout;
        // each data item is just a string in this case
        ImageView imgView, imgAppIcon;
        View imgUnderLine;
        TextView text, txtAppTextImage;
        TextView textDefaultApp;
        RelativeLayout relMenu;
        private LinearLayout linearLayout;

        ItemViewHolder(View v) {
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
        }

        @Override
        public void onItemSelected() {
        }

        @Override
        public void onItemClear() {
            try {
                notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
                CoreApplication.Companion.getInstance().logException(e);
            }

        }
    }

}
