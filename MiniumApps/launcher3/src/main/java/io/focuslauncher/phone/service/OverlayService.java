package io.focuslauncher.phone.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import io.focuslauncher.phone.app.Constants;
import io.focuslauncher.phone.utils.NotificationUtils;
import io.focuslauncher.R;

/**
 * Created by roma on 23/4/18.
 */

public class OverlayService extends Service {

    private WindowManager wm;
    private View bottomView;
    private Handler handler;
    private int delay;
    private int heightWindow;
    private int maxHeightWindow;
    private int variableMaxHeightPortrait;
    private int variableMaxHeightLandscape;

    private int heightWindowLandscape;
    private int maxHeightWindowLandscape;
    private View topView;
    private Runnable runnableViewBottom;
    private int minusculeHeight;
    private int minusculeHeightLandscape;
    private WindowManager.LayoutParams paramsBottom;
    private WindowManager.LayoutParams paramsTop;

    private NotificationUtils notificationUtils;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            notificationUtils = new NotificationUtils(this);
            notificationUtils.createChannels();
            startForeground(Constants.STATUSBAR_SERVICE_ID, notificationUtils.getForeGroundNotification());
            bottomView = ((LayoutInflater) getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE)).inflate(R.layout
                    .gray_scale_layout, null);
            wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            paramsBottom = new WindowManager.LayoutParams();
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            heightWindow = (size.y - (getNavigationBarHeight()
                    + getStatusBarHeight())) * 6 / 9;

            heightWindowLandscape = (size.x) * 6 / 9;
            maxHeightWindowLandscape = heightWindowLandscape;

            maxHeightWindow = heightWindow;
            variableMaxHeightPortrait = heightWindow;
            variableMaxHeightLandscape = heightWindowLandscape;
            minusculeHeight = heightWindow / 9;
            minusculeHeightLandscape = heightWindowLandscape / 9;
            paramsBottom.width = ViewGroup.LayoutParams.MATCH_PARENT;
            paramsBottom.height = minusculeHeight;
            paramsBottom.gravity = Gravity.BOTTOM;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                paramsBottom.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                paramsBottom.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }

            paramsBottom.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
            ;

            paramsBottom.format = PixelFormat.TRANSLUCENT;


            topView = ((LayoutInflater) getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE)).inflate(R.layout
                    .gray_scale_layout_reverse, null);
            paramsTop = new WindowManager
                    .LayoutParams();
            paramsTop.width = ViewGroup.LayoutParams.MATCH_PARENT;
            paramsTop.height = 0;
            paramsTop.gravity = Gravity.TOP;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                paramsTop.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                paramsTop.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }

            paramsTop.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

            paramsTop.format = PixelFormat.TRANSLUCENT;

            if (null != wm) {
                wm.addView(bottomView, paramsBottom);
                wm.addView(topView, paramsTop);
            }
            handler = new Handler();
//            handlerTop = new Handler();
            //milliseconds
            delay = 1000;

            //Code for timer to increase the height of cover
            final CountDownTimer countDownTimer = new CountDownTimer(10000,
                    1000) {

                public void onTick(long millisUntilFinished) {
                    //here you can have your logic to set text to edittext
                    try {

                        if (wm.getDefaultDisplay().getRotation() == Surface
                                .ROTATION_0 || wm.getDefaultDisplay().getRotation() == Surface
                                .ROTATION_90) {


                            if (bottomView.getWindowToken() != null && (bottomView
                                    .getLayoutParams().height != 0)) {
                                if (paramsBottom.height <
                                        variableMaxHeightPortrait) {
                                    //Increase height of overlay
                                    if (paramsBottom.height % 2 == 0) {
                                        paramsBottom.height = paramsBottom.height + minusculeHeight;
                                    } else {
                                        paramsBottom.height = paramsBottom.height +
                                                (minusculeHeight / 2);
                                    }

                                    if (paramsBottom.height <= variableMaxHeightPortrait) {
                                        bottomView.setLayoutParams(new ViewGroup.LayoutParams(paramsBottom));
                                        wm.updateViewLayout(bottomView, paramsBottom);
                                    }
                                }

                            }

                            if (topView.getWindowToken() != null && topView
                                    .getLayoutParams().height != 0) {
                                if (paramsTop.height <
                                        variableMaxHeightPortrait) {
                                    //Increase height of overlay
                                    if (paramsTop.height % 2 == 0) {
                                        paramsTop.height = paramsTop.height + minusculeHeight;
                                    } else {
                                        paramsTop.height = paramsTop.height +
                                                (minusculeHeight / 2);
                                    }

                                    if (paramsTop.height <= variableMaxHeightPortrait) {
                                        topView.setLayoutParams(new ViewGroup.LayoutParams
                                                (paramsTop));
                                        wm.updateViewLayout(topView, paramsTop);
                                    }
                                }

                            }
                        } else {
                            if (bottomView.getWindowToken() != null && (bottomView
                                    .getLayoutParams().height != 0)) {
                                if (paramsBottom.height <
                                        variableMaxHeightLandscape) {
                                    //Increase height of overlay
                                    if (paramsBottom.height % 2 == 0) {
                                        paramsBottom.height = paramsBottom
                                                .height + minusculeHeightLandscape;
                                    } else {
                                        paramsBottom.height = paramsBottom.height +
                                                (minusculeHeightLandscape / 2);
                                    }

                                    if (paramsBottom.height <= variableMaxHeightLandscape) {
                                        bottomView.setLayoutParams(new ViewGroup.LayoutParams(paramsBottom));
                                        wm.updateViewLayout(bottomView, paramsBottom);
                                    }
                                }

                            }

                            if (topView.getWindowToken() != null && topView
                                    .getLayoutParams().height != 0) {
                                if (paramsTop.height <
                                        variableMaxHeightLandscape) {
                                    //Increase height of overlay
                                    if (paramsTop.height % 2 == 0) {
                                        paramsTop.height = paramsTop.height + minusculeHeightLandscape;
                                    } else {
                                        paramsTop.height = paramsTop.height +
                                                (minusculeHeightLandscape / 2);
                                    }

                                    if (paramsTop.height <= variableMaxHeightLandscape) {
                                        topView.setLayoutParams(new ViewGroup.LayoutParams
                                                (paramsTop));
                                        wm.updateViewLayout(topView, paramsTop);
                                    }
                                }

                            }
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                public void onFinish() {
                    //done
                    Log.d("", "");
                }

            };

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    countDownTimer.start();
                }
            }, 2000);


            topView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (paramsBottom.height != 0) {
                        paramsTop.height = topView.getHeight() +
                                bottomView.getHeight();
                        topView.setLayoutParams(new ViewGroup.LayoutParams
                                (paramsTop));
                        wm.updateViewLayout(topView, paramsTop);
                        paramsBottom.height = 0;
                        if (bottomView.getWindowToken() != null) {
                            wm.removeView(bottomView);
                        }
                        variableMaxHeightPortrait = heightWindow;
                        variableMaxHeightLandscape = heightWindowLandscape;
                    } else {
                        if (paramsTop.height != minusculeHeight) {
                            paramsTop.height = topView.getHeight() / 2;
                            topView.setLayoutParams(new ViewGroup.LayoutParams
                                    (paramsTop));
                            wm.updateViewLayout(topView, paramsTop);
                            paramsBottom.height = paramsTop.height;
                            bottomView.setLayoutParams(new ViewGroup.LayoutParams
                                    (paramsBottom));
                            if (bottomView.getWindowToken() != null) {
                                wm.updateViewLayout(bottomView, paramsBottom);
                            } else {
                                wm.addView(bottomView, paramsBottom);
                            }
//                        handler.postDelayed(runnableViewBottom, delay);
                            variableMaxHeightPortrait = heightWindow / 2;
                            variableMaxHeightLandscape = heightWindowLandscape / 2;
                        } else if (paramsTop.height == minusculeHeight) {
                            //code for shifting bottom view to top
                            paramsTop.height = 0;
                            topView.setLayoutParams(new ViewGroup.LayoutParams
                                    (paramsTop));
                            wm.updateViewLayout(topView, paramsTop);
                            paramsBottom.height = minusculeHeight;
                            bottomView.setLayoutParams(new ViewGroup.LayoutParams
                                    (paramsBottom));
                            if (bottomView.getWindowToken() != null) {
                                wm.updateViewLayout(bottomView, paramsBottom);
                            } else {
                                wm.addView(bottomView, paramsBottom);
                            }

                        }
                    }

                }
            });

            bottomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (paramsTop.height != 0) {

                        paramsBottom.height = topView.getHeight() +
                                bottomView.getHeight();
                        bottomView.setLayoutParams(new ViewGroup.LayoutParams
                                (paramsBottom));
                        wm.updateViewLayout(bottomView, paramsBottom);
                        paramsTop.height = 0;
                        if (topView.getWindowToken() != null) {
                            wm.removeView(topView);
                        }
                        variableMaxHeightPortrait = heightWindow;
                        variableMaxHeightLandscape = heightWindowLandscape;

                    } else {

                        if (paramsBottom.height != minusculeHeight) {
                            paramsBottom.height = bottomView.getHeight() / 2;
                            bottomView.setLayoutParams(new ViewGroup.LayoutParams
                                    (paramsBottom));
                            wm.updateViewLayout(bottomView, paramsBottom);
                            paramsTop.height = paramsBottom.height;
                            topView.setLayoutParams(new ViewGroup.LayoutParams
                                    (paramsTop));
                            if (topView.getWindowToken() != null) {
                                wm.updateViewLayout(topView, paramsTop);
                            } else {
                                wm.addView(topView, paramsTop);
                            }
                            variableMaxHeightPortrait = heightWindow / 2;
                            variableMaxHeightLandscape = heightWindowLandscape / 2;
                        } else if (paramsBottom.height == minusculeHeight) {
                            //code for shifting bottom view to top
                            paramsBottom.height = 0;
                            bottomView.setLayoutParams(new ViewGroup.LayoutParams
                                    (paramsBottom));
                            wm.updateViewLayout(bottomView, paramsBottom);
                            paramsTop.height = minusculeHeight;
                            topView.setLayoutParams(new ViewGroup.LayoutParams
                                    (paramsTop));
                            if (topView.getWindowToken() != null) {
                                wm.updateViewLayout(topView, paramsTop);
                            } else {
                                wm.addView(topView, paramsTop);
                            }

                        }

                    }
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Call method of resizing
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            if (topView.getWindowToken() != null && bottomView.getWindowToken()
                    == null && topView.getHeight() != 0) {
                paramsTop.height = maxHeightWindow;
                topView.setLayoutParams(new ViewGroup.LayoutParams
                        (paramsTop));
                wm.updateViewLayout(topView, paramsTop);

            } else if (topView.getWindowToken() == null && bottomView
                    .getWindowToken()
                    != null && bottomView.getHeight() != 0) {
                paramsBottom.height = maxHeightWindow;
                bottomView.setLayoutParams(new ViewGroup.LayoutParams
                        (paramsBottom));
                wm.updateViewLayout(bottomView, paramsBottom);
            } else if (topView.getWindowToken() != null && bottomView
                    .getWindowToken()
                    != null && topView.getHeight() != 0 && bottomView.getHeight
                    () != 0) {
                paramsTop.height = maxHeightWindow / 2;
                topView.setLayoutParams(new ViewGroup.LayoutParams
                        (paramsTop));
                wm.updateViewLayout(topView, paramsTop);
                paramsBottom.height = maxHeightWindow / 2;
                bottomView.setLayoutParams(new ViewGroup.LayoutParams
                        (paramsBottom));
                wm.updateViewLayout(bottomView, paramsBottom);
            }


        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {


            if (topView.getWindowToken() != null && (bottomView.getWindowToken()
                    == null || bottomView.getHeight() == 0) && (topView
                    .getHeight()
                    != 0 && !(topView.getHeight() < maxHeightWindow))) {
                paramsTop.height = maxHeightWindowLandscape;
                topView.setLayoutParams(new ViewGroup.LayoutParams
                        (paramsTop));
                wm.updateViewLayout(topView, paramsTop);

            } else if ((topView.getWindowToken() == null || topView.getHeight
                    () == 0) &&
                    bottomView
                            .getWindowToken()
                            != null && (bottomView.getHeight() != 0)) {
                paramsBottom.height = maxHeightWindowLandscape;
                bottomView.setLayoutParams(new ViewGroup.LayoutParams
                        (paramsBottom));
                wm.updateViewLayout(bottomView, paramsBottom);
            } else if (topView.getWindowToken() != null && bottomView
                    .getWindowToken()
                    != null && topView.getHeight() != 0 && bottomView.getHeight
                    () != 0) {
                paramsTop.height = maxHeightWindowLandscape / 2;
                topView.setLayoutParams(new ViewGroup.LayoutParams
                        (paramsTop));
                wm.updateViewLayout(topView, paramsTop);
                paramsBottom.height = maxHeightWindowLandscape / 2;
                bottomView.setLayoutParams(new ViewGroup.LayoutParams
                        (paramsBottom));
                wm.updateViewLayout(bottomView, paramsBottom);
            }


        }


    }

    public int getNavigationBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        removeView();
    }

    private void removeView() {
        try {
            if (wm != null) {
                if (topView.getWindowToken() != null) {
                    wm.removeView(topView);
                }
                if (bottomView.getWindowToken() != null) {
                    wm.removeView(bottomView);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
