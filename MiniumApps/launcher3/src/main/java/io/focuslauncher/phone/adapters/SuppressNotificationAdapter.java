/*
 * Copyright (C) 2015 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.focuslauncher.phone.adapters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Telephony;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.List;

import io.focuslauncher.R;
import io.focuslauncher.phone.app.BitmapWorkerTask;
import io.focuslauncher.phone.app.Constants;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.db.SingleItemDelete;
import io.focuslauncher.phone.interfaces.ItemTouchHelperAdapter;
import io.focuslauncher.phone.interfaces.ItemTouchHelperViewHolder;
import io.focuslauncher.phone.models.DeleteItem;
import io.focuslauncher.phone.models.Notification;
import io.focuslauncher.phone.models.NotificationContactModel;
import io.focuslauncher.phone.utils.NotificationUtility;
import io.focuslauncher.phone.utils.UIUtils;


public class SuppressNotificationAdapter extends RecyclerView.Adapter<SuppressNotificationAdapter.ItemViewHolder>
        implements ItemTouchHelperAdapter {

    private Context mContext;
    private List<Notification> notificationList;
    //= new ArrayList<>();

    private String defSMSApp;

    public SuppressNotificationAdapter(Context context, List<Notification> notificationList) {
        mContext = context;
        this.notificationList = notificationList;
        Log.d("Test", "notificationList" + notificationList.size());
        defSMSApp = Telephony.Sms.getDefaultSmsPackage(mContext);
    }


    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_card, parent, false);

        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        if (notification.getNotificationType() == NotificationUtility.NOTIFICATION_TYPE_EVENT) {
//            Drawable drawable = CoreApplication.Companion.getInstance().getApplicationIconFromPackageName(notification.getPackageName());
            holder.imgAppIcon.setBackground(null);
            holder.imgAppIcon.setImageBitmap(null);

            Bitmap bitmap = CoreApplication.Companion.getInstance().getBitmapFromMemCache(notification.getPackageName());
            if (bitmap != null) {
                holder.imgAppIcon.setImageBitmap(bitmap);
            } else {
                ApplicationInfo appInfo = null;
                try {
                    appInfo = mContext.getPackageManager().getApplicationInfo(notification.getPackageName(), PackageManager.GET_META_DATA);
                    BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(appInfo, mContext.getPackageManager());
                    CoreApplication.Companion.getInstance().includeTaskPool(bitmapWorkerTask, null);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                Drawable drawable = CoreApplication.Companion.getInstance().getApplicationIconFromPackageName(notification.getPackageName());
                holder.imgAppIcon.setImageDrawable(drawable);
            }

            holder.txtAppName.setText(CoreApplication.Companion.getInstance().getApplicationNameFromPackageName(notification.getPackageName()));
            if (notification.getStrTitle() == null || notification.getStrTitle().equalsIgnoreCase("")) {
                holder.txtUserName.setText("");
                holder.txtUserName.setVisibility(View.GONE);
                holder.txtMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            } else {
                holder.txtUserName.setText(notification.getStrTitle());
                holder.txtUserName.setVisibility(View.VISIBLE);
                holder.txtMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            }

            holder.txtMessage.setText(notification.get_text());
            holder.txtTime.setText(notification.get_time());
            holder.imgUserImage.setVisibility(View.GONE);
            if (notification.getPackageName().equalsIgnoreCase(Constants.FACEBOOK_MESSENGER_PACKAGE)
                    || notification.getPackageName().equalsIgnoreCase(Constants.WHATSAPP_PACKAGE)
                    || notification.getPackageName().equalsIgnoreCase(Constants.FACEBOOK_LITE_PACKAGE)
                    || notification.getPackageName().equalsIgnoreCase(Constants.FACEBOOK_PACKAGE)
                    || notification.getPackageName().equalsIgnoreCase(Constants.GOOGLE_HANGOUTS_PACKAGES)) {
                holder.imgUserImage.setVisibility(View.VISIBLE);
                if (notification.getUser_icon() != null) {
                    holder.imgUserImage.setImageBitmap(UIUtils.convertBytetoBitmap(notification.getUser_icon()));
                } else {
                    holder.imgUserImage.setBackground(null);
                    holder.imgUserImage.setImageBitmap(null);
                    holder.imgUserImage.setImageResource(R.drawable.ic_person_black_24dp);
                }
            }

        } else {
            NotificationContactModel notificationContactModel = notification.getNotificationContactModel();
            if (null != notificationContactModel) {
                holder.txtUserName.setText(notificationContactModel.getName());
            }
            if (notification.get_text().equalsIgnoreCase(mContext.getString(R.string.missed_call))) {
                holder.imgAppIcon.setBackground(null);
                holder.imgAppIcon.setImageDrawable(mContext.getResources().getDrawable(android.R.drawable.sym_call_missed, null));
                holder.txtAppName.setText(R.string.phone);
            } else {
                holder.imgAppIcon.setBackground(null);
                String strAppName = CoreApplication.Companion.getInstance().getListApplicationName().get(defSMSApp);
                if (strAppName == null) {
                    strAppName = CoreApplication.Companion.getInstance().getApplicationNameFromPackageName(defSMSApp);
                }
                holder.txtAppName.setText(strAppName);
                Bitmap bitmap = CoreApplication.Companion.getInstance().getBitmapFromMemCache(defSMSApp);
                if (bitmap != null) {
                    holder.imgAppIcon.setImageBitmap(bitmap);
                } else {
                    ApplicationInfo appInfo = null;
                    try {
                        appInfo = mContext.getPackageManager().getApplicationInfo(defSMSApp, PackageManager.GET_META_DATA);
                        BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(appInfo, mContext.getPackageManager());
                        CoreApplication.Companion.getInstance().includeTaskPool(bitmapWorkerTask, null);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    Drawable drawable = CoreApplication.Companion.getInstance().getApplicationIconFromPackageName(defSMSApp);
                    holder.imgAppIcon.setImageDrawable(drawable);
                }
            }
            holder.txtMessage.setText(notification.get_text());
            holder.txtTime.setText(notification.get_time());
            try {
                if (notificationContactModel != null) {
                    if (notificationContactModel.getImage() != null && !notificationContactModel.getImage().equals("")) {
                        Glide.with(mContext)
                                .load(Uri.parse(notificationContactModel.getImage()))
                                .placeholder(R.drawable.ic_person_black_24dp)
                                .into(holder.imgUserImage);
                    } else {
                        holder.imgUserImage.setBackground(null);
                        holder.imgUserImage.setImageBitmap(null);
                        holder.imgUserImage.setImageResource(R.drawable.ic_person_black_24dp);
                    }
                }

            } catch (Exception e) {
                CoreApplication.Companion.getInstance().logException(e);
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onItemDismiss(int position) {
        //++Tarun following code will delete this item form database
        try {
            if (notificationList != null && notificationList.get(position) != null) {
                DeleteItem deleteItem = new DeleteItem(new SingleItemDelete());
                deleteItem.executeDelete(notificationList.get(position));
                notificationList.remove(position);
                notifyItemRemoved(position);

            }
        } catch (Exception e) {
            CoreApplication.Companion.getInstance().logException(e);
            e.printStackTrace();
        }

    }


    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(notificationList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    /**
     * Simple example of a view holder that implements {@link ItemTouchHelperViewHolder} and has a
     * "handle" view that initiates a drag event when touched.
     */
    static class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {
        ImageView imgAppIcon, imgUserImage;
        TextView txtAppName, txtTime, txtUserName, txtMessage;

        ItemViewHolder(View view) {
            super(view);
            imgAppIcon = view.findViewById(R.id.imgAppIcon);
            imgUserImage = view.findViewById(R.id.imgUserImage);
            txtAppName = view.findViewById(R.id.txtAppName);
            txtTime = view.findViewById(R.id.txtTime);
            txtUserName = view.findViewById(R.id.txtUserName);
            txtMessage = view.findViewById(R.id.txtMessage);

        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }
}
