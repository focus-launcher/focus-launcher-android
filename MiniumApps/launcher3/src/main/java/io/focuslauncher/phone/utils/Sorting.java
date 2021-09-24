package io.focuslauncher.phone.utils;

import android.content.Context;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.db.TableNotificationSms;
import io.focuslauncher.phone.models.AppListInfo;
import io.focuslauncher.phone.models.CustomNotification;
import io.focuslauncher.phone.models.MainListItem;

import static java.util.Collections.sort;

/**
 * Created by rajeshjadi on 9/2/18.
 */

public class Sorting {
    /**
     * Sort the application alphabetically.
     *
     * @param context
     * @param list
     * @return
     */
    public synchronized static List<ResolveInfo> sortAppAssignment(final Context context, List<ResolveInfo> list) {
        sort(list, new Comparator<ResolveInfo>() {
            @Override
            public int compare(final ResolveInfo object1, final ResolveInfo object2) {
                if (object1 != null && object2 != null) {
                    return object1.loadLabel(context
                            .getPackageManager()).toString().toLowerCase().compareTo
                            (object2.loadLabel(context.getPackageManager())
                                    .toString().toLowerCase());
                } else {
                    return 1;
                }
            }
        });
        return list;
    }

    /**
     * Sort the application alphabetically.
     *
     * @param list
     * @return
     */
    public synchronized static ArrayList<String> sortJunkAppAssignment(ArrayList<String> list) {
        sort(list, new Comparator<String>() {
            @Override
            public int compare(final String object1, final String object2) {
                String strObj1;
                String strObj2;
                if (CoreApplication.Companion.getInstance().getListApplicationName().get(object1) != null) {
                    strObj1 = CoreApplication.Companion.getInstance().getListApplicationName().get(object1);
                } else {
                    strObj1 = CoreApplication.Companion.getInstance().getApplicationNameFromPackageName(object1);
                }

                if (CoreApplication.Companion.getInstance().getListApplicationName().get(object2) != null) {
                    strObj2 = CoreApplication.Companion.getInstance().getListApplicationName().get(object2);
                } else {
                    strObj2 = CoreApplication.Companion.getInstance().getApplicationNameFromPackageName(object2);
                }

                return (strObj1 != null ? strObj1.toLowerCase() : "").compareTo((strObj2 != null ? strObj2.toLowerCase() : ""));
            }
        });
        return list;
    }


    public synchronized static ArrayList<MainListItem> SortApplications(final ArrayList<MainListItem> appList) {
        sort(appList, new Comparator<MainListItem>() {
            @Override
            public int compare(final MainListItem object1, final MainListItem object2) {
                return object1.getTitle().toLowerCase().compareTo(object2.getTitle().toLowerCase());
            }
        });
        return appList;
    }


    public synchronized static ArrayList<MainListItem> sortToolAppAssignment(final Context context, ArrayList<MainListItem> list) {
        Collections.sort(list, new Comparator<MainListItem>() {
            @Override
            public int compare(final MainListItem object1, final MainListItem object2) {
                return object1.getTitle().toLowerCase().compareTo(object2.getTitle().toLowerCase());
            }
        });
        return list;
    }

    public synchronized static List<AppListInfo> sortApplication(List<AppListInfo> list) {
        Collections.sort(list, new Comparator<AppListInfo>() {
            @Override
            public int compare(final AppListInfo object1, final AppListInfo object2) {
                String strObj1;
                String strObj2;

                if (CoreApplication.Companion.getInstance().getListApplicationName().get(object1.packageName) != null) {
                    strObj1 = CoreApplication.Companion.getInstance().getListApplicationName().get(object1.packageName);
                } else {
                    strObj1 = CoreApplication.Companion.getInstance().getApplicationNameFromPackageName(object1.packageName);
                }

                if (CoreApplication.Companion.getInstance().getListApplicationName().get(object2.packageName) != null) {
                    strObj2 = CoreApplication.Companion.getInstance().getListApplicationName().get(object2.packageName);
                } else {
                    strObj2 = CoreApplication.Companion.getInstance().getApplicationNameFromPackageName(object2.packageName);
                }

                return (strObj1 != null ? strObj1.toLowerCase() : "").compareTo((strObj2 != null ? strObj2.toLowerCase() : ""));
            }
        });
        return list;
    }


    public synchronized static ArrayList<TableNotificationSms> sortNotificationByDate(final ArrayList<TableNotificationSms> appList) {
        sort(appList, new Comparator<TableNotificationSms>() {
            @Override
            public int compare(final TableNotificationSms object1, final TableNotificationSms object2) {
                return object2.get_date().compareTo(object1.get_date());
            }
        });
        return appList;
    }

    public synchronized static ArrayList<CustomNotification> sortNotificationByDate1(final ArrayList<CustomNotification> appList) {
        sort(appList, new Comparator<CustomNotification>() {
            @Override
            public int compare(final CustomNotification object1, final CustomNotification object2) {
                return object2.getDate().compareTo(object1.getDate());
            }
        });
        return appList;
    }
}
