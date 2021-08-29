package io.focuslauncher.phone.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.PopupMenu;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;
import java.util.List;

import io.focuslauncher.R;
import io.focuslauncher.phone.app.BitmapWorkerTask;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.main.MainListAdapterEvent;
import io.focuslauncher.phone.models.MainListItem;
import io.focuslauncher.phone.models.MainListItemType;
import io.focuslauncher.phone.token.TokenItem;
import io.focuslauncher.phone.token.TokenItemType;
import io.focuslauncher.phone.token.TokenManager;
import io.focuslauncher.phone.utils.ColorGenerator;
import io.focuslauncher.phone.utils.DrawableProvider;
import io.focuslauncher.phone.utils.TextDrawable;
import de.greenrobot.event.EventBus;

/**
 * Created by Shahab on 2/16/2017.
 */
public class MainListAdapter extends ArrayAdapter<MainListItem> {

    private static final int HIGHLIGHT_COLOR = 0x999be6ff;
    private final Context context;
    private boolean isHideIconBranding;
    private List<MainListItem> originalData = null;
    private List<MainListItem> filteredData = null;
    private ItemFilter filter = new ItemFilter();
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private DrawableProvider mProvider;
    private TextDrawable.IBuilder mDrawableBuilder;
    private PopupMenu popup;

    public MainListAdapter(Context context, List<MainListItem> items) {
        super(context, 0);
        this.context = context;
        mDrawableBuilder = TextDrawable.builder()
                .round();
        mProvider = new DrawableProvider(context);

        loadData(items);

    }

    public void loadData(List<MainListItem> items) {
        originalData = items;
        filteredData = items;
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public MainListItem getItem(int position) {
        return filteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return MainListItemType.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        int pos = 0;
        try {
            pos = getItem(position).getItemType().ordinal();
        } catch (Exception e) {
            CoreApplication.getInstance().logException(e);
        }
        return pos;

    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        MainListItemType itemViewType = MainListItemType.values()[getItemViewType(position)];
        if (itemViewType != null) {
            switch (itemViewType) {
                case CONTACT:
                    convertView = getContactItemView(position, convertView, parent);
                    break;
                case ACTION:
                case DEFAULT:
                case NUMBERS:
                    convertView = getActionItemView(position, convertView, parent);
                default:
                    break;
            }
        }
        return convertView;
    }

    private View getContactItemView(int position, View view, ViewGroup parent) {
        final ContactViewHolder holder;
        if (view == null) {
            holder = new ContactViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (inflater != null) {
                view = inflater.inflate(R.layout.list_item_contacts, parent, false);
                holder.icon = view.findViewById(R.id.icon);
                holder.txtNumber = view.findViewById(R.id.txtNumber);
                holder.text = view.findViewById(R.id.text);
                view.setTag(holder);
            }


        } else {
            holder = (ContactViewHolder) view.getTag();
        }


        MainListItem item = getItem(position);

        if (item != null) {
            holder.text.setText(item.getContactName());
            if (item.getImageUri() != null && !TextUtils.isEmpty(item.getImageUri())) {

                Glide.with(context).load(Uri.parse(item.getImageUri())).asBitmap().centerCrop().placeholder(R.drawable.placeholder_blank_contact).into(new BitmapImageViewTarget(holder.icon) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        holder.icon.setImageDrawable(circularBitmapDrawable);
                    }
                });
            } else {
                if (!TextUtils.isEmpty(item.getContactName())) {
                    TypedValue typedValue = new TypedValue();
                    Resources.Theme theme = context.getTheme();
                    theme.resolveAttribute(R.attr.icon_color, typedValue, true);
                    int color = typedValue.data;
                    Drawable drawable = mProvider.getRound("" + item
                            .getContactName().charAt(0), color, 24);
                    holder.icon.setImageDrawable(drawable);
                }
            }

            if (item.hasMultipleNumber()) {
                holder.txtNumber.setText(context.getString(R.string.label_multiple_numbers));
            } else {
                holder.txtNumber.setText(item.getNumber().getNumber());
            }
        }

        return view;
    }

    private View getActionItemView(int position, View view, ViewGroup parent) {
        ActionViewHolder holder;
        isHideIconBranding = CoreApplication.getInstance()
                .isHideIconBranding();

        if (view == null) {
            holder = new ActionViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (inflater != null) {
                view = inflater.inflate(R.layout.list_item, parent, false);
                holder.icon = view.findViewById(R.id.icon);
                holder.text = view.findViewById(R.id.text);
                holder.imgChevron = view.findViewById(R.id.imgChevron);
                holder.linList = view.findViewById(R.id.linList);

                view.setTag(holder);
            }

        } else {
            holder = (ActionViewHolder) view.getTag();
        }

        view.setTag(holder);

        ImageView imgChevron = view.findViewById(R.id.imgChevron);


        final MainListItem item = getItem(position);


        if (item != null) {

            String titleApp = item
                    .getTitle();
            if (item.getId() == -1) {
                final String packageName = item.getPackageName();
                if (!TextUtils.isEmpty(packageName)) {
                    if (isHideIconBranding) {
                        String upperCaseTitle;
                        if (TextUtils.isEmpty(titleApp)) {
                            upperCaseTitle = "";
                        } else {
                            upperCaseTitle = String.valueOf(titleApp.toUpperCase().charAt(0));
                        }

                        TypedValue typedValue = new TypedValue();
                        Resources.Theme theme = context.getTheme();
                        theme.resolveAttribute(R.attr.icon_color, typedValue, true);
                        int color = typedValue.data;
                        Drawable drawable = mProvider.getRound(upperCaseTitle,
                                color, 24);
                        holder.icon.setImageDrawable(drawable);
                    } else {
                        Bitmap bitmap = CoreApplication.getInstance().getBitmapFromMemCache(packageName);
                        if (bitmap != null) {
                            holder.icon.setImageBitmap(bitmap);
                        } else {
                            try {
                                ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                                BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(appInfo, context.getPackageManager());
                                CoreApplication.getInstance().includeTaskPool(bitmapWorkerTask, null);
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                            Drawable drawable = CoreApplication.getInstance().getApplicationIconFromPackageName(packageName);
                            holder.icon.setImageDrawable(drawable);
                        }
                    }
                }
                holder.text.setText(titleApp);
                holder.imgChevron.setVisibility(View.VISIBLE);
            } else {
                if (item.getDrawable() != 0) {

                    holder.icon.setImageResource(item.getDrawable());
                }
                holder.text.setText(titleApp);
                holder.imgChevron.setVisibility(View.GONE);
            }

            MainListItemType itemType = item.getItemType();
            // Call item in Tools has id=13 , while as a default type has id=4
            if ((null != itemType) && (itemType == MainListItemType.DEFAULT)) {

                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = context.getTheme();
                theme.resolveAttribute(R.attr.default_item_color, typedValue, true);
                int itemColor = typedValue.data;
                holder.text.setTextColor(itemColor);
                theme.resolveAttribute(R.attr.default_item_bg, typedValue, true);
                int color = typedValue.data;
                holder.linList.setBackgroundColor(color);


            } else {
                holder.linList.setBackgroundColor(context.getResources().getColor(R
                        .color.transparent));
            }

        }

        imgChevron.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup = new PopupMenu(context, v, Gravity.END);
                popup.getMenuInflater().inflate(R.menu.tempo_notification_popup, popup.getMenu());
                MenuItem menuItem = popup.getMenu().findItem(R.id.block);
                menuItem.setTitle(context.getResources().getString(R.string.info_or_uninstall));

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = null;
                        if (item != null) {
                            uri = Uri.fromParts("package", item
                                    .getPackageName(), null);
                        }
                        intent.setData(uri);
                        context.startActivity(intent);
                        return true;
                    }
                });
                popup.show();

            }
        });

        return view;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return filter;
    }

    private boolean checkDuplicate(List<MainListItem> buildData, String str) {
        if (buildData != null) {
            for (MainListItem mainListItem : buildData) {
                if (mainListItem.getTitle().equalsIgnoreCase(str)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkToolsDuplicate(List<MainListItem> buildData, String str) {
        if (buildData != null) {
            for (MainListItem mainListItem : buildData) {
                if (mainListItem.getTitle().equalsIgnoreCase(str) && TextUtils.isEmpty(mainListItem.getPackageName().toLowerCase().trim())) {
                    return false;
                }
            }
        }
        return true;
    }

    private String phoneNumberString(String str) {
        return str.replaceAll("\\+", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll(context.getString(R.string.phone_replace_regex), "");
    }

    private static class ActionViewHolder {
        ImageView icon;
        TextView text;
        ImageView imgChevron;
        LinearLayout linList;
    }


    private static class ContactViewHolder {
        ImageView icon;
        TextView text;
        TextView txtNumber;
    }

    /**
     * Filter class for filtering Search Pane list in tools
     */
    private class ItemFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String searchString = constraint.toString().toLowerCase().trim();


            FilterResults ret = new FilterResults();


            int count = originalData.size();
            List<MainListItem> buildData = new ArrayList<>();
            boolean isValidNumber = false;
            if (!searchString.isEmpty()) {

                for (int i = 0; i < count; i++) {
                    String filterableString;
                    String[] splits;


                    switch (originalData.get(i).getItemType()) {
                        case CONTACT:
                            if (searchString.startsWith("@")) {
                                if (searchString.equals("@")) {
                                    isValidNumber = true;
                                    buildData.add(originalData.get(i));
                                } else {
                                    /*
                                      A blank space was added with searchString2. After using trim the search problem is resolved
                                     */
                                    String searchString2 = searchString.replaceAll("@", "").trim();
                                    MainListItem item = originalData.get(i);
                                    filterableString = item.getContactName();
                                    boolean isAdded = false;
                                    if (filterableString.toLowerCase().contains(searchString2)) {
                                        isValidNumber = true;
                                        buildData.add(originalData.get(i));
                                        isAdded = true;
                                    }

                                    if (!isAdded) {
                                        searchString2 = phoneNumberString(searchString);
                                        List<MainListItem.ContactNumber> numbers = item.getNumbers();
                                        for (MainListItem.ContactNumber number : numbers) {
                                            String phoneNum = phoneNumberString(number.getNumber());
                                            if (phoneNum.contains(searchString2)) {
                                                isValidNumber = true;
                                                buildData.add(originalData.get(i));
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            break;
                        case ACTION:
                            filterableString = originalData.get(i).getTitle();
                            if (!TextUtils.isEmpty(filterableString)) {


                                if (!TextUtils.isEmpty(originalData.get
                                        (i).getPackageName())) {
                                    if (filterableString.toLowerCase().contains(searchString.toLowerCase().trim())) {
                                        buildData.add(originalData.get(i));
                                        break;
                                    }
                                } else {
                                    if (originalData.get(i).getTitle().toLowerCase().contains(searchString.toLowerCase())) {
                                        if (checkToolsDuplicate(buildData, searchString.toLowerCase().toLowerCase())) {
                                            buildData.add(originalData.get(i));
                                        }
                                    }
                                }
                            }
                            break;
                        case NUMBERS:
                            if (PhoneNumberUtils.isGlobalPhoneNumber(searchString)) {
                                isValidNumber = true;
                                TokenManager.getInstance().getCurrent().setExtra2(searchString);
                                buildData.add(originalData.get(i));
                            }
                            break;
                        case DEFAULT:
                            TokenItem current = TokenManager.getInstance().getCurrent();
                            if ((!searchString.equalsIgnoreCase("@") || !(searchString.length() > 1 && searchString.startsWith("@") && TokenManager.getInstance().hasCompleted(TokenItemType.CONTACT))) && checkDuplicate(buildData, originalData.get(i).getTitle().toLowerCase().toLowerCase())) {
                                if (searchString.length() > 0 && searchString.startsWith("@") && !TokenManager.getInstance().hasCompleted(TokenItemType.CONTACT) && isValidNumber) {
                                } else {
                                    if (originalData.get(i).getTitle().toLowerCase().equalsIgnoreCase(context.getResources().getString(R.string.title_sendAsSMS)) &&
                                            !isValidNumber && searchString.startsWith("@")) {

                                    } else if (originalData.get(i).getTitle()
                                            .toLowerCase().equalsIgnoreCase
                                                    (context.getResources
                                                            ().getString
                                                            (R.string.title_saveNote)
                                                    ) && searchString.equalsIgnoreCase("^") && current.getItemType() == TokenItemType.DATA) {

                                    } else if (originalData.get(i).getTitle()
                                            .toLowerCase().equalsIgnoreCase
                                                    (context.getResources
                                                            ().getString
                                                            (R.string
                                                                    .title_saveNote)) && TokenManager.getInstance().hasCompleted(TokenItemType.CONTACT)) {


                                    } else if (searchString.equalsIgnoreCase("@") || (searchString.startsWith("@") && isValidNumber)) {

                                    } else {

                                        buildData.add(originalData.get(i));
                                    }
                                }

                            }
                            break;

                    }
                }
            } else {
                for (MainListItem menuMainListItem : originalData) {
                    if (!TextUtils.isEmpty(menuMainListItem.getTitle()) && menuMainListItem.getItemType() != MainListItemType.DEFAULT && menuMainListItem.getItemType() != MainListItemType.CONTACT) {
                        buildData.add(menuMainListItem);
                    }
                }
            }
            ret.values = buildData;
            ret.count = buildData.size();
            return ret;
        }


        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.values != null) {
                filteredData = (List<MainListItem>) results.values;
            } else {
                filteredData = new ArrayList<>(originalData);
            }
            EventBus.getDefault().post(new MainListAdapterEvent(filteredData));
            notifyDataSetChanged();
        }
    }
}