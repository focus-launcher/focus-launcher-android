package io.focuslauncher.phone.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import io.focuslauncher.phone.log.Tracer;

/**
 * http://stackoverflow.com/questions/3667022/checking-if-an-android-application-is-running-in-the-background/
 * <p>
 * Added by Shahab on 4/1/2016.
 */
public class LifecycleHandler implements Application.ActivityLifecycleCallbacks {
    private static int sResumed;
    private static int sPaused;
    private static int sStarted;
    private static int sStopped;
    private final String TRACE_TAG = "LifecycleHandler";

    public static boolean isApplicationVisible() {
        return sStarted > sStopped;
    }

    public static boolean isApplicationInForeground() {
        return sResumed > sPaused;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        sResumed++;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        sPaused++;
        Tracer.v("Application in background: " + (sResumed > sPaused));
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        sStarted++;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        sStopped++;
        Tracer.v("Application in background: " + (sStarted > sStopped));
    }
}