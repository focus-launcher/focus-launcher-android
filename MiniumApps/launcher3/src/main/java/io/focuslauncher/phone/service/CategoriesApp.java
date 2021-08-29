package io.focuslauncher.phone.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.models.CategoryAppList;
import io.focuslauncher.phone.utils.CategoryUtils;
import io.focuslauncher.phone.utils.NetworkUtil;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CategoriesApp extends IntentService {

    public ArrayList<CategoryUtils> appList = new ArrayList<>();
    Context context = this;

    public CategoriesApp() {
        super("CategoriesApp");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
     context=this;
        if(NetworkUtil.isOnline(context)){
            getCategoryApps();
        }
    }

    public void getCategoryApps(){
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(NetworkUtil.maximumPoolSize);
        Executor threadPoolExecutor = new ThreadPoolExecutor(NetworkUtil.corePoolSize, NetworkUtil.maximumPoolSize, NetworkUtil.keepAliveTime, TimeUnit.SECONDS, workQueue);
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> installedPackageList = context.getPackageManager().queryIntentActivities(mainIntent, 0);
        new FetchCategoryTask(installedPackageList, getPackageName()).executeOnExecutor(threadPoolExecutor);
    }


    private static class FetchCategoryTask extends AsyncTask<Void, Void, Void> {

        private List<ResolveInfo> installedPackageList;
        private String packageName;

        public FetchCategoryTask(List<ResolveInfo> installedPackageList, String packageName) {
            this.installedPackageList = installedPackageList;
            this.packageName = packageName;
        }

        @Override
        protected Void doInBackground(Void... errors) {

            List<CategoryAppList> categoryAppList= CoreApplication.getInstance().categoryAppList;
            String category;
            for (ResolveInfo resolveInfo : installedPackageList) {
                if(resolveInfo.activityInfo.packageName!=null && !resolveInfo.activityInfo.packageName.equalsIgnoreCase(packageName)){
                    String query_url = "https://play.google.com/store/apps/details?id="+resolveInfo.activityInfo.packageName;  //GOOGLE_URL + packageInfo.packageName;
                    category = getCategory(query_url);
                    if(!TextUtils.isEmpty(category) && !category.equalsIgnoreCase("error")){
                        CategoryAppList categoryInstance= new CategoryAppList();
                        categoryInstance.setCategoryName(category);
                        categoryInstance.setPackageName(resolveInfo.activityInfo.packageName);
                        categoryAppList.add(categoryInstance);
                    }
                }
            }
            return null;
        }

        String getCategory(String query_url) {

            try {
                Document doc = Jsoup.connect(query_url).get();
                Elements link = doc.select("a[class=\"hrTbp R8zArc\"]");
                return link.last().text();
            } catch (Exception e) {
                Log.e("DOc", e.toString());
            }
            return "";
        }

    }


}
