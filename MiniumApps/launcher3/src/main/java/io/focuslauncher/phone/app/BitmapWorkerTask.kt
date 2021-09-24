package io.focuslauncher.phone.app

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.AsyncTask
import io.focuslauncher.phone.utils.PackageUtil

/**
 * Created by rajeshjadi on 23/2/18.
 */
class BitmapWorkerTask(
    private val appInfo: ApplicationInfo,
    private val packageManager: PackageManager
) : AsyncTask<Any?, Void?, Void?>() {

    override fun doInBackground(vararg params: Any?): Void? {
        val drawable = appInfo.loadIcon(packageManager)
        val bitmap = PackageUtil.drawableToBitmap(drawable)
        CoreApplication.instance?.addBitmapToMemoryCache(appInfo.packageName, bitmap)
        return null
    }
}
