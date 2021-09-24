package io.focuslauncher.phone.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.focuslauncher.BuildConfig;
import io.focuslauncher.R;
import io.focuslauncher.phone.activities.DashboardActivity;
import io.focuslauncher.phone.adapters.MainListAdapter;
import io.focuslauncher.phone.app.Constants;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.event.SendSmsEvent;
import io.focuslauncher.phone.fragments.PaneFragment;
import io.focuslauncher.phone.helper.ActivityHelper;
import io.focuslauncher.phone.helper.FirebaseHelper;
import io.focuslauncher.phone.log.Tracer;
import io.focuslauncher.phone.models.MainListItem;
import io.focuslauncher.phone.models.MainListItemType;
import io.focuslauncher.phone.token.TokenItemType;
import io.focuslauncher.phone.token.TokenManager;
import io.focuslauncher.phone.token.TokenRouter;
import io.focuslauncher.phone.util.ContactSmsPermissionHelper;
import io.focuslauncher.phone.utils.CategoryUtils;
import io.focuslauncher.phone.utils.ContactsLoader;
import io.focuslauncher.phone.utils.PackageUtil;
import io.focuslauncher.phone.utils.PermissionUtil;
import io.focuslauncher.phone.utils.PrefSiempo;
import io.focuslauncher.phone.utils.UIUtils;
import de.greenrobot.event.EventBus;


/**
 * Created by shahab on 2/16/17.
 */

public class MainFragmentMediator {

    private SharedPreferences launcher3Prefs;
    private Context context;
    private PaneFragment fragment;
    private List<MainListItem> items;

    private List<MainListItem> contactItems;

    private resetData resetData;
    private PermissionUtil permissionUtil;

    public MainFragmentMediator(PaneFragment paneFragment) {
        this.fragment = paneFragment;
        context = this.fragment.getActivity();
        permissionUtil = new PermissionUtil(context);
    }

    public synchronized void loadData() {
        new resetData(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public synchronized void resetData() {
        new resetData(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public synchronized void cancelAsync() {
        resetData.cancel(true);
    }

    public synchronized boolean getRunningStatus() {
        return null != resetData && resetData.getStatus() == AsyncTask.Status.RUNNING;

    }

    private synchronized void loadActions() {
        new MainListItemLoader().loadItems(items, fragment);
    }

    public void loadContacts() {

        if (permissionUtil.hasGiven(PermissionUtil
                .CONTACT_PERMISSION)) {
            try {
                List<MainListItem> localList = null;
                if (fragment != null && fragment.getManager() != null && fragment.getManager().hasCompleted(TokenItemType.CONTACT)) {
                    return;
                }
                if (fragment != null && contactItems.size() == 0) {

                    localList = new ContactsLoader().loadContacts(fragment.getActivity());
                    contactItems = localList;

                }
                if (localList != null) {
                    items.addAll(localList);
                }
            } catch (Exception e) {
                CoreApplication.Companion.getInstance().logException(e);
                Tracer.e(e, e.getMessage());
            }
        }

    }

    private void loadDefaults() {
        try {
            if (fragment != null) {

                if (fragment.getManager().hasCompleted(TokenItemType.CONTACT) && fragment.getManager().has(TokenItemType.DATA) && !fragment.getManager().get(TokenItemType.DATA).getTitle().isEmpty()) {
                    //items.add(new MainListItem(1, fragment.getString(R.string.title_sendAsSMS), R.drawable.ic_messages_tool, MainListItemType.DEFAULT));
                    items.add(new MainListItem(3, fragment.getString(R.string.title_swipe), R.drawable.ic_default_swipe, MainListItemType.DEFAULT, CategoryUtils.EMPTY));
                } else if (fragment.getManager().hasCompleted(TokenItemType.CONTACT)) {
                    //items.add(new MainListItem(1, fragment.getString(R.string.title_sendAsSMS), R.drawable.ic_messages_tool, MainListItemType.DEFAULT));
                    items.add(new MainListItem(3, fragment.getString(R.string.title_swipe), R.drawable.ic_default_swipe, MainListItemType.DEFAULT, CategoryUtils.EMPTY));
                } else if (fragment.getManager().hasCompleted(TokenItemType.DATA)) {
                    //items.add(new MainListItem(1, fragment.getString(R.string.title_sendAsSMS), R.drawable.ic_messages_tool,MainListItemType.DEFAULT));
                    items.add(new MainListItem(2, fragment.getString(R
                            .string.title_saveNote), R.drawable.ic_notes_tool, MainListItemType.DEFAULT,CategoryUtils.EMPTY));
                    items.add(new MainListItem(3, fragment.getString(R.string.title_swipe), R.drawable.ic_default_swipe, MainListItemType.DEFAULT,CategoryUtils.EMPTY));
                } else {
                    //items.add(new MainListItem(1, fragment.getString(R.string.title_sendAsSMS), R.drawable.ic_messages_tool, MainListItemType.DEFAULT));
                    items.add(new MainListItem(2, fragment.getString(R.string.title_saveNote), R.drawable.ic_notes_tool, MainListItemType.DEFAULT,CategoryUtils.EMPTY));
                    items.add(new MainListItem(3, fragment.getString(R.string.title_swipe), R.drawable.ic_default_swipe, MainListItemType.DEFAULT,CategoryUtils.EMPTY));
                }
            }
        } catch (Exception e) {
            CoreApplication.Companion.getInstance().logException(e);
            Tracer.e(e, e.getMessage());
        }

    }

    public List<MainListItem> getItems() {
        return items;
    }

    private MainListAdapter getAdapter() {
        return fragment.getAdapter();
    }

    public void listItemClicked(final TokenRouter router, int position, final String data) {
        MainListItemType type;
        if (getAdapter() != null) {
            type = getAdapter().getItem(position).getItemType();
            if (type != null)
                switch (type) {
                    case CONTACT:
                        if (router != null) {
                            try {
                                router.contactPicked(getAdapter().getItem(position));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            FirebaseHelper.getInstance().logIFAction(FirebaseHelper.ACTION_CONTACT_PICK, "", data);
                        }
                        break;
                    case ACTION:
                        if (getAdapter() != null && TextUtils.isEmpty(getAdapter().getItem(position).getPackageName())) {
                            SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateInstance
                                    (DateFormat.FULL, Locale
                                            .getDefault());
                            PackageUtil.addRecentItemList(getAdapter().getItem(position), context);
                            position = getAdapter().getItem(position).getId();
                            new MainListItemLoader().listItemClicked(position);
                            EventBus.getDefault().post(new SendSmsEvent(true));
                        } else {
                            if (fragment != null) {

                                DashboardActivity.isTextLenghGreater = "";
                                UIUtils.hideSoftKeyboard(fragment.getActivity(), fragment.getActivity().getWindow().getDecorView().getWindowToken());
                                boolean status = new ActivityHelper(fragment.getActivity()).openAppWithPackageName(getAdapter().getItem(position).getPackageName());
                                FirebaseHelper.getInstance().logIFAction(FirebaseHelper.ACTION_APPLICATION_PICK, getAdapter().getItem(position).getPackageName(), "");
                                if (status) {
                                    PackageUtil.addRecentItemList(getAdapter().getItem(position), context);
                                    EventBus.getDefault().post(new SendSmsEvent(true));
                                }
                            }
                        }
                        break;
                    case DEFAULT:
                        position = getAdapter().getItem(position).getId();
                        switch (position) {
                            case 1:
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    ContactSmsPermissionHelper
                                            contactSmsPermissionHelper = new
                                            ContactSmsPermissionHelper(router,
                                            context, this, false, data);
                                    contactSmsPermissionHelper.checkForContactAndSMSPermission();
                                } else {
                                    loadContacts();
                                    if (router != null && fragment != null) {
                                        router.sendText(fragment.getActivity());
                                        FirebaseHelper.getInstance().logIFAction(FirebaseHelper
                                                .ACTION_SMS, "", data);
                                    }
                                }

                                break;
                            //Notes
                            case 2:
                                if (router != null && fragment != null) {
                                    String inputStr = TokenManager.getInstance().getCurrent().getTitle();
                                    if (BuildConfig.FLAVOR.equalsIgnoreCase(context.getString(R.string.beta))
                                            && inputStr.equalsIgnoreCase(Constants.ALPHA_SETTING)) {
                                        if (PrefSiempo.getInstance(context).read(PrefSiempo
                                                .IS_ALPHA_SETTING_ENABLE, false)) {
                                            router.createNote(fragment.getActivity());
                                            FirebaseHelper.getInstance().logIFAction(FirebaseHelper.ACTION_SAVE_NOTE, "", data);
                                            new ActivityHelper(context).openNotesApp(true);
                                            EventBus.getDefault().post(new SendSmsEvent(true));
                                        } else {
                                            PrefSiempo.getInstance(context).write(PrefSiempo
                                                    .IS_ALPHA_SETTING_ENABLE, true);
                                            new ActivityHelper(context).openSiempoAlphaSettingsApp();
                                            TokenManager.getInstance().clear();
                                        }
                                    } else {
                                        router.createNote(fragment.getActivity());
                                        FirebaseHelper.getInstance().logIFAction(FirebaseHelper.ACTION_SAVE_NOTE, "", data);
                                        new ActivityHelper(context).openNotesApp(true);
                                        EventBus.getDefault().post(new SendSmsEvent(true));
                                    }
                                }
                                break;
                            //Write code for Junk Food Pane on this code
                            case 3:
                                if (router != null && fragment != null) {
                                    EventBus.getDefault().post(new SendSmsEvent(true));
                                    fragment.setCurrentPage(0);
                                }
                                break;
                            case 4:
                                if (router != null && fragment != null) {
                                    router.call(fragment.getActivity());
                                    DashboardActivity.isTextLenghGreater = "";
                                    FirebaseHelper.getInstance().logIFAction(FirebaseHelper.ACTION_CALL, "", data);
                                    EventBus.getDefault().post(new SendSmsEvent(true, "", ""));
                                }
                                break;
                            default:
                                UIUtils.alert(fragment.getActivity(), fragment.getString(R.string.msg_not_yet_implemented));
                                break;
                        }
                        break;
                    case NUMBERS:
                        if (getAdapter().getItem(position).getTitle().trim().equalsIgnoreCase("call") && getAdapter().getItem(position).getId() == 4) {
                            try {
                                fragment.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + TokenManager.getInstance().getCurrent().getExtra2())));
                                DashboardActivity.isTextLenghGreater = "";
                                FirebaseHelper.getInstance().logIFAction(FirebaseHelper.ACTION_CALL, "", data);
                                EventBus.getDefault().post(new SendSmsEvent(true, "", ""));
                            } catch (Exception e) {
                                CoreApplication.Companion.getInstance().logException(e);
                                e.printStackTrace();
                            }
                        } else {
                            if (router != null) {
                                router.contactNumberPicked(getAdapter().getItem(position));
                                FirebaseHelper.getInstance().logIFAction(FirebaseHelper.ACTION_CONTACT_PICK, "", data);
                            }
                        }
                        break;
                    default:
                        break;
                }
        }
    }

    public void loadDefaultData() {
        List<MainListItem> defaultItems = new ArrayList<>();
        items = new ArrayList<>();
        contactItems = new ArrayList<>();
        loadActions();

        loadContacts();
        loadDefaults();
        items = PackageUtil.getListWithMostRecentData(items, context);
        for (MainListItem cItems : items) {
            if (cItems.getItemType() == MainListItemType.DEFAULT) {
                defaultItems.add(cItems);
            }
        }
        try {
            if (getAdapter() != null) {
                getAdapter().loadData(defaultItems);
                getAdapter().notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void contactPicker() {
        items = new ArrayList<>();
        List<MainListItem> newList = new ArrayList<>();
        contactItems = new ArrayList<>();

        // loadActions();
        loadContacts();
        loadDefaults();
        items = PackageUtil.getListWithMostRecentData(items, context);

        for (MainListItem cItems : items) {
            if (cItems.getItemType() == MainListItemType.CONTACT || cItems.getItemType() == MainListItemType.DEFAULT) {
                newList.add(cItems);
            }
        }

        contactItems = newList;
        try {
            if (getAdapter() != null) {
                getAdapter().loadData(newList);
                getAdapter().getFilter().filter("@");
                getAdapter().notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void contactNumberPicker(int selectedContactId) {

        items = new ArrayList<>();

        if (contactItems != null) {
            for (MainListItem item : contactItems) {
                if (item != null && item.getContactId() == selectedContactId) {
                    for (MainListItem.ContactNumber number : item.getNumbers()) {
                        items.add(new MainListItem(selectedContactId, number.getNumber(), R.drawable.icon_call, MainListItemType.NUMBERS,CategoryUtils.EMPTY));
                    }
                }
            }
            try {
                if (getAdapter() != null) {
                    getAdapter().loadData(items);
                    getAdapter().notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public void defaultData() {
        List<MainListItem> defaultItems = new ArrayList<>();
        items = new ArrayList<>();
        contactItems = new ArrayList<>();
        loadActions();
        loadContacts();
        loadDefaults();
        items = PackageUtil.getListWithMostRecentData(items, context);
        for (MainListItem cItems : items) {
            if (cItems.getItemType() == MainListItemType.DEFAULT && !cItems.getTitle().equalsIgnoreCase(context.getResources
                    ().getString
                    (R.string.title_saveNote))) {
                defaultItems.add(cItems);
            }
        }
        try {
            if (getAdapter() != null) {
                getAdapter().loadData(defaultItems);
                getAdapter().notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public class resetData extends AsyncTask<String, String, List<MainListItem>> {

        Context context;

        public resetData(Context context) {
            this.context = context;
            items = new ArrayList<>();
            contactItems = new ArrayList<>();
            resetData = this;
        }

        @Override
        protected List<MainListItem> doInBackground(String... strings) {
            try {

                if (getAdapter() != null) {
                    getAdapter().setNotifyOnChange(false);
                }
                loadActions();
                loadContacts();
                loadDefaults();
                items = PackageUtil.getListWithMostRecentData(items, context);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return items;
        }

        @Override
        protected void onPostExecute(List<MainListItem> s) {
            super.onPostExecute(s);

            try {
                if (getAdapter() != null) {
                    getAdapter().setNotifyOnChange(true);
                    getAdapter().loadData(items);
                    getAdapter().getFilter().filter("");
                    getAdapter().notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

}
