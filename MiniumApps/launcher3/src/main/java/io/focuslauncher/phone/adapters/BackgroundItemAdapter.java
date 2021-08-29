package io.focuslauncher.phone.adapters;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.widget.CircularProgressDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.focuslauncher.R;
import io.focuslauncher.phone.models.ImageItem;


public final class BackgroundItemAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private List<ImageItem> mItems = new ArrayList<ImageItem>();
    private Context context;

    public BackgroundItemAdapter(Context context, List<ImageItem> mItems) {
        this.context = context;
        this.mItems = mItems;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public ImageItem getItem(int i) {
        return mItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = new ViewHolder();

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.choose_background_grid_item, viewGroup, false);
            viewHolder.mImage = (ImageView) convertView.findViewById(R.id.gridImage);
            viewHolder.imageText = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ImageItem item = getItem(i);
        if (item.getName().equalsIgnoreCase(context.getString(R.string.current_background))) {
            viewHolder.imageText.setCompoundDrawables(null, null, null, null);
        }
        if (item.getName().equalsIgnoreCase("")) {
            viewHolder.imageText.setVisibility(View.GONE);
        } else {
            viewHolder.imageText.setVisibility(View.VISIBLE);
            viewHolder.imageText.setText(item.name);
        }
        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setVisibility(View.VISIBLE);

        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(8f);
        circularProgressDrawable.setCenterRadius(50f);
        circularProgressDrawable.setColorSchemeColors(Color.parseColor("#448AFF"));
        circularProgressDrawable.start();

        Glide.with(context)
                .load(Uri.fromFile(new File(item.getDrawableId().get(0))))
                .placeholder(circularProgressDrawable)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(viewHolder.mImage);

        return convertView;
    }

    public static class ViewHolder {
        ImageView mImage;
        TextView imageText;
    }


}
