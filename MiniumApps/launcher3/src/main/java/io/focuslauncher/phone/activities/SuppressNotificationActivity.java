package io.focuslauncher.phone.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.focuslauncher.R;
import io.focuslauncher.phone.adapters.SuppressNotificationAdapter;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.db.DBUtility;
import io.focuslauncher.phone.db.MultipleItemDelete;
import io.focuslauncher.phone.db.TableNotificationSms;
import io.focuslauncher.phone.db.TableNotificationSmsDao;
import io.focuslauncher.phone.helper.FirebaseHelper;
import io.focuslauncher.phone.models.CustomNotification;
import io.focuslauncher.phone.models.DeleteItem;
import io.focuslauncher.phone.models.Notification;
import io.focuslauncher.phone.models.NotificationContactModel;
import io.focuslauncher.phone.utils.NotificationUtility;
import io.focuslauncher.phone.utils.PrefSiempo;
import io.focuslauncher.phone.utils.Sorting;

public class SuppressNotificationActivity extends CoreActivity {


    public static final String TAG = SuppressNotificationActivity.class.getName();
    Context context;
    List<Notification> notificationList = new ArrayList<>();
    List<Notification> suggetionList = new ArrayList<>();
    ArrayList<String> disableNotificationApps = new ArrayList<>();
    long startTime = 0;
    private RecyclerView recyclerView;
    private TextView txtClearAll, emptyView;
    private TableNotificationSmsDao smsDao;
    private SuppressNotificationAdapter adapter;
    private EditText edt_search;

    @Override
    protected void onResume() {
        super.onResume();
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseHelper.getInstance().logScreenUsageTime(this.getClass().getSimpleName(), startTime);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_siempo_supress_notification);
        initView();
    }

    public void initView() {
        context = SuppressNotificationActivity.this;
        txtClearAll = findViewById(R.id.txtClearAll);
        emptyView = findViewById(R.id.emptyView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setNestedScrollingEnabled(false);
        notificationList = new ArrayList<>();
        suggetionList = new ArrayList<>();
        edt_search = findViewById(R.id.edt_search);
        try {
            Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/robotocondensedregular.ttf");
            edt_search.setTypeface(myTypeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
        edt_search.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                suggetionList.clear();
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                suggetionList.clear();
                for (int i = 0; i < notificationList.size(); i++) {
                    if (!TextUtils.isEmpty(notificationList.get(i).getPackageName())) {
                        if (CoreApplication.Companion.getInstance().getApplicationNameFromPackageName(notificationList.get(i).getPackageName()).toLowerCase().startsWith(s.toString().toLowerCase())) {
                            suggetionList.add(notificationList.get(i));
                        }
                        if (suggetionList.size() == 0) {
                            txtClearAll.setVisibility(View.GONE);
                        } else {
                            txtClearAll.setVisibility(View.VISIBLE);
                        }
                    }
                    adapter = new SuppressNotificationAdapter(context, suggetionList);
                    recyclerView.setAdapter(adapter);

                }

            }
        });

        adapter = new SuppressNotificationAdapter(context, notificationList);
        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        smsDao = DBUtility.getNotificationDao();
        loadData();


        txtClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteItem deleteItem = new DeleteItem(new MultipleItemDelete());
                deleteItem.deleteAll();
                notificationList.clear();
                adapter = new SuppressNotificationAdapter(context, notificationList);
                recyclerView.setAdapter(adapter);
                edt_search.setVisibility(View.GONE);
                txtClearAll.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        });

    }


    private void loadData() {

        String disable_AppList = PrefSiempo.getInstance(context).read
                (PrefSiempo.HELPFUL_ROBOTS, "");
        if (!TextUtils.isEmpty(disable_AppList)) {
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            disableNotificationApps = new Gson().fromJson(disable_AppList, type);
        }


        List<TableNotificationSms> SMSItems = smsDao.queryBuilder().orderDesc(TableNotificationSmsDao.Properties.Notification_date).build().list();

        Log.d(TAG, "Store Notifications size :: " + SMSItems.size());
        setUpNotifications(SMSItems);
    }


    private void setUpNotifications(List<TableNotificationSms> items) {
        notificationList.clear();
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Enable permission from Settings to access Contact details.", Toast.LENGTH_SHORT).show();
        }

        Set<String> packageList = new HashSet<>();
        if (items.size() > 0) {
            for (TableNotificationSms sms : items) {
                packageList.add(sms.getPackageName());
            }
        }

        ArrayList<CustomNotification> customNotifications = new ArrayList<>();
        for (String string : packageList) {
            CustomNotification customNotification = new CustomNotification();
            ArrayList<TableNotificationSms> tableNotificationSms = new ArrayList<>();
            for (TableNotificationSms sms : items) {
                if (sms.getPackageName().equalsIgnoreCase(string)) {
                    tableNotificationSms.add(sms);
                }
            }
            Sorting.sortNotificationByDate(tableNotificationSms);
            customNotification.setDate(tableNotificationSms.get(0).get_date());
            customNotification.setNotificationSms(tableNotificationSms);
            customNotifications.add(customNotification);
        }
        Sorting.sortNotificationByDate1(customNotifications);
        items = new ArrayList<>();
        for (CustomNotification customNotification : customNotifications) {
            items.addAll(customNotification.getNotificationSms());
        }

        for (int i = 0; i < items.size(); i++) {
            DateFormat sdf = new SimpleDateFormat(getTimeFormat(context));
            String time = sdf.format(items.get(i).get_date());
            if (items.get(i).getNotification_type() == NotificationUtility.NOTIFICATION_TYPE_EVENT) {
                Notification notification = new Notification();
                notification.setId(items.get(i).getId());
                notification.setNotitification_date(items.get(i).getNotification_date());
                notification.setNotificationType(items.get(i).getNotification_type());
                notification.setApp_icon(items.get(i).getApp_icon());
                notification.setUser_icon(items.get(i).getUser_icon());
                notification.setPackageName(items.get(i).getPackageName());
                notification.set_time(time);
                notification.setStrTitle(items.get(i).get_contact_title());
                notification.set_text(items.get(i).get_message());
                notificationList.add(notification);
            } else {
                Notification n = new Notification(gettingNameAndImageFromPhoneNumber(items.get(i).get_contact_title()), items.get(i).getId(), items.get(i).get_contact_title(), items.get(i).get_message(), time, false, items.get(i).getNotification_type(), items.get(i).getPackageName());
                notificationList.add(n);
            }
            adapter = new SuppressNotificationAdapter(context, notificationList);
            recyclerView.setAdapter(adapter);


        }

        if (notificationList.size() == 0) {
            edt_search.setVisibility(View.GONE);
            txtClearAll.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            edt_search.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            txtClearAll.setVisibility(View.VISIBLE);
        }


    }

    public String getTimeFormat(Context context) {
        String format;
        boolean is24hourformat = android.text.format.DateFormat.is24HourFormat(context);

        if (is24hourformat) {
            format = "HH:mm";
        } else {
            format = "hh:mm a";
        }
        return format;
    }


    private NotificationContactModel gettingNameAndImageFromPhoneNumber(String number) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            Cursor cursor = context.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI}, null, null, null);

            String contactName, imageUrl = "";
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    imageUrl = cursor
                            .getString(cursor
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                    cursor.close();

                } else {
                    contactName = number;
                }
            } catch (Exception e) {
                contactName = "";
                imageUrl = "";
                e.printStackTrace();
            }


            NotificationContactModel notificationContactModel = new NotificationContactModel();
            notificationContactModel.setName(contactName);
            notificationContactModel.setImage(imageUrl);
            return notificationContactModel;
        }
        return null;
    }


}
