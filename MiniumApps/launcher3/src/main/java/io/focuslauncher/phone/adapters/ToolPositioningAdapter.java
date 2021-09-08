package io.focuslauncher.phone.adapters;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;

import android.util.TypedValue;
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
import io.focuslauncher.phone.app.BitmapWorkerTask;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.interfaces.ItemTouchHelperAdapter;
import io.focuslauncher.phone.interfaces.ItemTouchHelperViewHolder;
import io.focuslauncher.phone.interfaces.OnToolItemListChangedListener;
import io.focuslauncher.phone.main.OnStartDragListener;
import io.focuslauncher.phone.models.AppMenu;
import io.focuslauncher.phone.models.MainListItem;

/**
 * Created by rajeshjadi on 14/2/18.
 */

public class ToolPositioningAdapter extends RecyclerView.Adapter<ToolPositioningAdapter.ItemViewHolder> implements ItemTouchHelperAdapter {
    private final Activity context;
    private final HashMap<Integer, AppMenu> map;
    private boolean isHideIconBranding = true;
    private ArrayList<MainListItem> arrayList;
    private OnStartDragListener mDragStartListener;
    private OnToolItemListChangedListener mListChangedListener;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ToolPositioningAdapter(Activity context, ArrayList<MainListItem> arrayList, OnStartDragListener dragListener,
                                  OnToolItemListChangedListener listChangedListener, boolean isHideIconBranding) {
        this.context = context;
        this.arrayList = arrayList;
        mDragStartListener = dragListener;
        mListChangedListener = listChangedListener;
        this.isHideIconBranding = isHideIconBranding;
        map = CoreApplication.getInstance().getToolsSettings();
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
                mListChangedListener.onToolItemListChanged(arrayList,toPosition);
                notifyItemMoved(fromPosition, toPosition);
            }
        } catch (Exception e) {
            CoreApplication.getInstance().logException(e);
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
        v = inflater.inflate(R.layout.list_item_grid, parent, false);
//             set the view's size, margins, paddings and layout parameters
        return new ItemViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        final MainListItem item = arrayList.get(position);
        final AppMenu appMenu = map.get(item.getId());
        if (null != appMenu && appMenu.isVisible()) {
            holder.linearLayout.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(item.getTitle())) {
                holder.text.setText(item.getTitle());
            }
            if (isHideIconBranding) {
                holder.icon.setVisibility(View.VISIBLE);
                holder.imgAppIcon.setVisibility(View.GONE);
                holder.icon.setImageResource(item.getDrawable());
                holder.text.setText(item.getTitle());
            } else {
                holder.text.setText(CoreApplication.getInstance().getApplicationNameFromPackageName(appMenu.getApplicationName()));
                Bitmap bitmap = CoreApplication.getInstance().getBitmapFromMemCache(appMenu.getApplicationName());
                if (bitmap != null) {
                    holder.icon.setVisibility(View.GONE);
                    holder.imgAppIcon.setVisibility(View.VISIBLE);
                    holder.imgAppIcon.setImageBitmap(bitmap);
                } else {
                    ApplicationInfo appInfo = null;
                    try {
                        appInfo = context.getPackageManager().getApplicationInfo(appMenu.getApplicationName(), PackageManager.GET_META_DATA);
                        BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(appInfo, context.getPackageManager());
                        CoreApplication.getInstance().includeTaskPool(bitmapWorkerTask, null);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    holder.icon.setVisibility(View.VISIBLE);
                    holder.imgAppIcon.setVisibility(View.GONE);
                    holder.icon.setImageResource(item.getDrawable());
                    holder.text.setText(item.getTitle());
                }
            }
        } else {
            holder.linearLayout.setVisibility(View.INVISIBLE);
        }


        if (position + 4 >= arrayList.size()) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = context.getTheme();
            theme.resolveAttribute(R.attr.bottom_doc, typedValue, true);
            int drawable = typedValue.resourceId;

            holder.relMenu.setBackgroundResource(drawable);

        } else {

            holder.relMenu.setBackgroundColor(context.getResources().getColor
                    (R.color.transparent));
        }
        holder.linearLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (holder.linearLayout.getVisibility() == View.VISIBLE) {
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

    class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {
        public View layout;
        // each data item is just a string in this case
        ImageView icon, imgView, temp;
        ImageView imgAppIcon;
        TextView text, textDefaultApp;
        RelativeLayout relMenu;
        private LinearLayout linearLayout;

        ItemViewHolder(View v) {
            super(v);
            layout = v;
            linearLayout = v.findViewById(R.id.linearList);
            relMenu = v.findViewById(R.id.relMenu);
            text = v.findViewById(R.id.text);
            textDefaultApp = v.findViewById(R.id.textDefaultApp);
            icon = v.findViewById(R.id.icon);
            imgView = v.findViewById(R.id.imgView);
            temp = v.findViewById(R.id.temp);
            imgAppIcon = v.findViewById(R.id.imgAppIcon);
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
                CoreApplication.getInstance().logException(e);
            }

        }
    }

}
