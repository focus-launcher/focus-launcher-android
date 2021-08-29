package io.focuslauncher.phone.app;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import io.focuslauncher.phone.utils.PackageUtil;

/**
 * Created by rajeshjadi on 23/2/18.
 */

public class BitmapWorkerTask extends AsyncTask<Object, Void, Void> {
    // Decode image in background.
    private ApplicationInfo appInfo;
    private PackageManager packageManager;

    public BitmapWorkerTask(ApplicationInfo appInfo, PackageManager packageManager) {
        this.appInfo = appInfo;
        this.packageManager = packageManager;
    }

    @Override
    protected Void doInBackground(Object... params) {
        Drawable drawable = appInfo.loadIcon(packageManager);
        Bitmap bitmap = PackageUtil.drawableToBitmap(drawable);
        CoreApplication.getInstance().addBitmapToMemoryCache(appInfo.packageName, bitmap);
        return null;
    }
}
