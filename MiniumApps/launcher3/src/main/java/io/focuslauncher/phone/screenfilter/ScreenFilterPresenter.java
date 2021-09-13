package io.focuslauncher.phone.screenfilter;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

import io.focuslauncher.Receivers.OrientationChangeReceiver;

import io.focuslauncher.R;
import io.focuslauncher.phone.service.ScreenFilterService;
import io.focuslauncher.phone.utils.NotificationUtils;

import static io.focuslauncher.phone.utils.NotificationUtils.ANDROID_CHANNEL_ID;

public class ScreenFilterPresenter implements OrientationChangeReceiver.OnOrientationChangeListener,
        SettingsModel.OnSettingsChangedListener {
    private static final String TAG = "ScreenFilterPresenter";
    private static final boolean DEBUG = true;

    public static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_CODE_ACTION_SETTINGS = 1000;
    private static final int REQUEST_CODE_ACTION_STOP = 2000;
    private static final int REQUEST_CODE_ACTION_PAUSE_OR_RESUME = 3000;

    private static final int FADE_DURATION_MS = 1000;

    private ScreenFilterView mView;
    private SettingsModel mSettingsModel;
    private ServiceLifeCycleController mServiceController;
    private Context mContext;
    private WindowViewManager mWindowViewManager;
    private ScreenManager mScreenManager;
    public Notification.Builder mNotificationBuilder;
    private FilterCommandFactory mFilterCommandFactory;
    private FilterCommandParser mFilterCommandParser;
    private NotificationUtils mNotificationUtils;

    private boolean mShuttingDown = false;
    private boolean mScreenFilterOpen = false;

    private ValueAnimator mColorAnimator;
    private ValueAnimator mDimAnimator;

    private final State mOnState = new OnState();
    private final State mOffState = new OffState();
    private final State mPauseState = new PauseState();
    private State mCurrentState = mOffState;

    public ScreenFilterPresenter(ScreenFilterView view,
                                 SettingsModel model,
                                 ServiceLifeCycleController serviceController,
                                 Context context,
                                 WindowViewManager windowViewManager,
                                 ScreenManager screenManager,
                                 android.app.Notification.Builder notificationBuilder,
                                 FilterCommandFactory filterCommandFactory,
                                 FilterCommandParser filterCommandParser) {
        mView = view;
        mSettingsModel = model;
        mServiceController = serviceController;
        mContext = context;
        mWindowViewManager = windowViewManager;
        mScreenManager = screenManager;
        mNotificationBuilder = notificationBuilder;
        mFilterCommandFactory = filterCommandFactory;
        mFilterCommandParser = filterCommandParser;
        mNotificationUtils = new NotificationUtils(context);
    }

    private void refreshForegroundNotification() {
        Context context = mView.getContext();

        String title = context.getString(R.string.app_name);
        //int color = context.getResources().getColor(R.color.color_primary);
        Intent offCommand = mFilterCommandFactory.createCommand(ScreenFilterService.COMMAND_OFF);

        int smallIconResId;
        String contentText;
        int pauseOrResumeDrawableResId;
        Intent pauseOrResumeCommand;

        if (isPaused()) {
            Log.d(TAG, "Creating notification while in pause state");
//            smallIconResId = R.drawable.ic_shades_off_white;
//            contentText = context.getString(R.string.paused);
//            pauseOrResumeDrawableResId = R.drawable.ic_play;
            pauseOrResumeCommand = mFilterCommandFactory.createCommand(ScreenFilterService.COMMAND_ON);
        } else {
            Log.d(TAG, "Creating notification while NOT in pause state");
//            smallIconResId = R.drawable.ic_shades_on_white;
//            contentText = context.getString(R.string.running);
//            pauseOrResumeDrawableResId = R.drawable.ic_pause;
            pauseOrResumeCommand = mFilterCommandFactory.createCommand(ScreenFilterService.COMMAND_PAUSE);
        }

//        Intent shadesActivityIntent = new Intent(context, ShadesActivity.class);
//        shadesActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//
//        PendingIntent stopPI = PendingIntent.getService(context,
//                REQUEST_CODE_ACTION_STOP, offCommand, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        PendingIntent pauseOrResumePI = PendingIntent.getService(context, REQUEST_CODE_ACTION_PAUSE_OR_RESUME,
//                pauseOrResumeCommand, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        PendingIntent settingsPI = PendingIntent.getActivity(context, REQUEST_CODE_ACTION_SETTINGS,
//                shadesActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder newBuilder = new NotificationCompat.Builder(mContext, ANDROID_CHANNEL_ID)
                .setSmallIcon(R.drawable.siempo_notification_icon)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("Focus launcher with screen covers is running!!!")
                .setPriority(NotificationManagerCompat.IMPORTANCE_LOW)
                .setCategory(Context.NOTIFICATION_SERVICE)
                .setColor(0xFF91a7ff)
                .setAutoCancel(true);
        Notification newNotification = newBuilder.build();

        try {
            mNotificationUtils.createChannels();
            mServiceController.startForeground(NOTIFICATION_ID, newNotification);
        }catch (Throwable e) {
            Log.e("Notifications", "Couldn't start ScreenFilterService foreground", e);
        }
    }

    public void onScreenFilterCommand(Intent command) {
        int commandFlag = mFilterCommandParser.parseCommandFlag(command);

        if (mShuttingDown) {
            Log.i(TAG, "In the process of shutting down; ignoring command: " + commandFlag);
            return;
        }

        if (DEBUG) Log.i(TAG, String.format("Handling command: %d in current state: %s",
                commandFlag, mCurrentState));

        mCurrentState.onScreenFilterCommand(commandFlag);
    }

    //region OnSettingsChangedListener
    @Override
    public void onShadesPowerStateChanged(boolean powerState) {/* do nothing */}

    @Override
    public void onShadesPauseStateChanged(boolean pauseState) {/* do nothing */}

    @Override
    public void onShadesDimLevelChanged(int dimLevel) {
        if (!isPaused()) {
            cancelRunningAnimator(mDimAnimator);

            mView.setFilterDimLevel(dimLevel);
        }
    }

    @Override
    public void onShadesColorChanged(int color) {
        if (!isPaused()) {
            animateShadesColor(color);
        }
    }

    private void animateShadesColor(int toColor) {
        cancelRunningAnimator(mColorAnimator);

        int fromColor = mView.getFilterRgbColor();

        mColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
        mColorAnimator.setDuration(FADE_DURATION_MS);
        mColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mView.setFilterRgbColor((Integer) valueAnimator.getAnimatedValue());
            }
        });

        mColorAnimator.start();
    }

    private void animateDimLevel(int toDimLevel, Animator.AnimatorListener listener) {
        cancelRunningAnimator(mDimAnimator);

        int fromDimLevel = mView.getFilterDimLevel();

        mDimAnimator = ValueAnimator.ofInt(fromDimLevel, toDimLevel);
        mDimAnimator.setDuration(FADE_DURATION_MS);
        mDimAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mView.setFilterDimLevel((Integer) valueAnimator.getAnimatedValue());
            }
        });

        if (listener != null) {
            mDimAnimator.addListener(listener);
        }

        mDimAnimator.start();
    }

    private boolean isOff() {
        return mCurrentState == mOffState;
    }

    private boolean isPaused() {
        return mCurrentState == mPauseState;
    }

    private void cancelRunningAnimator(Animator animator) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }
    //endregion

    //region OnOrientationChangeListener
    public void onPortraitOrientation() {
        reLayoutScreenFilter();
    }

    public void onLandscapeOrientation() {
        reLayoutScreenFilter();
    }
    //endregion

    private WindowManager.LayoutParams createFilterLayoutParams() {

        WindowManager.LayoutParams wlp;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            wlp = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    mScreenManager.getScreenHeight(),
                    0,
                    -mScreenManager.getStatusBarHeightPx(),
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                            WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    PixelFormat.TRANSLUCENT);
        } else {
            wlp = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    mScreenManager.getScreenHeight(),
                    0,
                    -mScreenManager.getStatusBarHeightPx(),
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                            WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    PixelFormat.TRANSLUCENT);
        }

        wlp.gravity = Gravity.TOP | Gravity.START;
        return wlp;
    }

    private void openScreenFilter() {
        if (!mScreenFilterOpen) {
            // Display the transparent filter
            mWindowViewManager.openWindow(mView, createFilterLayoutParams());
            mScreenFilterOpen = true;
        }
    }

    private void reLayoutScreenFilter() {
        if (!mScreenFilterOpen) {
            return;
        }
        mWindowViewManager.reLayoutWindow(mView, createFilterLayoutParams());
    }

    private void closeScreenFilter() {
        if (!mScreenFilterOpen) {
            return;
        }

        // Close the window once the fade-out animation is complete
        mWindowViewManager.closeWindow(mView);
        mScreenFilterOpen = false;
    }

    private void moveToState(State newState) {
        if (DEBUG) Log.i(TAG, String.format("Transitioning state from %s to %s", mCurrentState, newState));

        mCurrentState = newState;

        mSettingsModel.setShadesPowerState(!isOff());
        mSettingsModel.setShadesPauseState(isPaused());
    }

    private abstract class State {
        protected abstract void onScreenFilterCommand(int commandFlag);

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    private class OnState extends State {
        @Override
        protected void onScreenFilterCommand(int commandFlag) {
            switch (commandFlag) {
                case ScreenFilterService.COMMAND_PAUSE:
                    moveToState(mPauseState);
                    refreshForegroundNotification();

                    animateDimLevel(ScreenFilterView.MIN_DIM, null);

                    break;

                case ScreenFilterService.COMMAND_OFF:
                    mShuttingDown = true;

                    moveToState(mOffState);
                    mServiceController.stopForeground(true);

                    animateDimLevel(ScreenFilterView.MIN_DIM, new AbstractAnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            closeScreenFilter();

                            mServiceController.stop();
                        }
                    });
                    break;
            }
        }
    }

    private class PauseState extends State {
        @Override
        protected void onScreenFilterCommand(int commandFlag) {
            switch (commandFlag) {
                case ScreenFilterService.COMMAND_ON:
                    moveToState(mOnState);
                    refreshForegroundNotification();

                    animateDimLevel(mSettingsModel.getShadesDimLevel(), null);

                    break;

                case ScreenFilterService.COMMAND_OFF:
                    moveToState(mOffState);
                    mServiceController.stopForeground(true);

                    closeScreenFilter();

                    break;
            }
        }
    }

    private class OffState extends State {
        @Override
        protected void onScreenFilterCommand(int commandFlag) {
            switch (commandFlag) {
                case ScreenFilterService.COMMAND_ON:
                    moveToState(mOnState);
                    refreshForegroundNotification();

                    int fromDim = ScreenFilterView.MIN_DIM;
                    int toDim = mSettingsModel.getShadesDimLevel();
                    int color = mSettingsModel.getShadesColor();

                    mView.setFilterDimLevel(fromDim);
                    mView.setFilterRgbColor(color);

                    openScreenFilter();

                    animateDimLevel(toDim, null);

                    break;

                case ScreenFilterService.COMMAND_PAUSE:
                    moveToState(mPauseState);
                    refreshForegroundNotification();

                    mView.setFilterDimLevel(ScreenFilterView.MIN_DIM);
                    mView.setFilterRgbColor(mSettingsModel.getShadesColor());

                    openScreenFilter();

                    break;

                case ScreenFilterService.COMMAND_OFF:
                    mSettingsModel.setShadesPowerState(false);

                    break;
            }
        }
    }
}

