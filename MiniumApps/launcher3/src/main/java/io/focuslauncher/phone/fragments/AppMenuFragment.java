package io.focuslauncher.phone.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import io.focuslauncher.R;
import io.focuslauncher.phone.activities.JunkfoodFlaggingActivity;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.event.NotifyBottomView;
import io.focuslauncher.phone.event.NotifyFavortieView;
import io.focuslauncher.phone.event.NotifyJunkFoodView;
import io.focuslauncher.phone.event.NotifyToolView;
import io.focuslauncher.phone.event.ReduceOverUsageEvent;
import io.focuslauncher.phone.helper.FirebaseHelper;
import io.focuslauncher.phone.utils.PrefSiempo;
import io.focuslauncher.phone.utils.UIUtils;
import de.greenrobot.event.EventBus;

import static com.rvalerio.fgchecker.Utils.hasUsageStatsPermission;


public class AppMenuFragment extends CoreFragment implements View.OnClickListener {


    boolean isFromFlag = false;
    private View view;
    private Toolbar toolbar;
    private RelativeLayout relJunkFoodmize;
    private RelativeLayout relHideIconBranding;
    private RelativeLayout mRelChooseFlagapps;
    private RelativeLayout mRelOverUseScreen;
    private RelativeLayout mRelOverUseFlaggedApp;
    private Switch switchJunkFoodmize, switchHideIcon, switchOveruseFlagged;
    private TextView mTxtReduceOveruseFlaggedDes;
    private Context context;
    private long startTime = 0;
    private int deterTime;
    private String[] deter_after_list;
    private int index;
    private String txtOverUseFlag;
    private Dialog overlayDialogPermission;

    public AppMenuFragment() {
        // Required empty public constructor
    }

    public static AppMenuFragment newInstance(boolean isFromFlag) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("FlagApp", isFromFlag);
        AppMenuFragment appMenuFragment = new AppMenuFragment();
        appMenuFragment.setArguments(bundle);
        return appMenuFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isFromFlag = getArguments().getBoolean("FlagApp");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
        view = inflater.inflate(R.layout.fragment_app_menu, container, false);
        deterTime = PrefSiempo.getInstance(context).read(PrefSiempo.DETER_AFTER, -1);


        initView(view);
        Resources res = getResources();
        deter_after_list = res.getStringArray(R.array.deter_after_dialog);
        if (deterTime == -1) {
            switchOveruseFlagged.setChecked(false);
            txtOverUseFlag = String.format(getResources().getString(R.string
                    .reduce_overuse_Flagged_description_setting), "<font " +
                    "color='#42A4FF'>" + deter_after_list[3] + "</font>");
        } else if (deterTime == 0) {
            txtOverUseFlag = String.format(getResources().getString(R.string
                    .reduce_overuse_Flagged_description_setting), "<font " +
                    "color='#42A4FF'>" + deter_after_list[0] + "</font>");
        } else if (deterTime == 1) {
            txtOverUseFlag = String.format(getResources().getString(R.string
                    .reduce_overuse_Flagged_description_setting), "<font " +
                    "color='#42A4FF'>" + deter_after_list[1] + "</font>");
        } else if (deterTime == 2) {
            txtOverUseFlag = String.format(getResources().getString(R.string
                    .reduce_overuse_Flagged_description_setting), "<font " +
                    "color='#42A4FF'>" + deter_after_list[2] + "</font>");
        } else if (deterTime == 5) {
            txtOverUseFlag = String.format(getResources().getString(R.string
                    .reduce_overuse_Flagged_description_setting), "<font " +
                    "color='#42A4FF'>" + deter_after_list[3] + "</font>");
        } else if (deterTime == 10) {
            txtOverUseFlag = String.format(getResources().getString(R.string
                    .reduce_overuse_Flagged_description_setting), "<font " +
                    "color='#42A4FF'>" + deter_after_list[4] + "</font>");
        } else if (deterTime == 15) {
            txtOverUseFlag = String.format(getResources().getString(R.string
                    .reduce_overuse_Flagged_description_setting), "<font " +
                    "color='#42A4FF'>" + deter_after_list[5] + "</font>");
        }
        mTxtReduceOveruseFlaggedDes.setText(Html.fromHtml(txtOverUseFlag));
        return view;
    }


    private void initView(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp);
        toolbar.setTitle(R.string.app_menus);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFromFlag) {
                    getActivity().finish();
                } else {
                    FragmentManager fm = getFragmentManager();
                    fm.popBackStack();
                }
            }
        });

        switchHideIcon = view.findViewById(R.id.switchHideIcon);
        switchHideIcon.setChecked(CoreApplication.getInstance().isHideIconBranding());

        switchJunkFoodmize = view.findViewById(R.id.switchJunkFoodmize);
        switchJunkFoodmize.setChecked(CoreApplication.getInstance().isRandomize());

        switchOveruseFlagged = view.findViewById(R.id.switchOveruseFlagged);
        if (index == -1
                || !hasUsageStatsPermission(getActivity())
                || !hasDrawOverlayPermission()) {
            switchOveruseFlagged.setChecked(false);
        } else {
            switchOveruseFlagged.setChecked(true);
        }

        relJunkFoodmize = view.findViewById(R.id.relJunkFoodmize);
        relJunkFoodmize.setOnClickListener(this);

        relHideIconBranding = view.findViewById(R.id.relHideIconBranding);
        relHideIconBranding.setOnClickListener(this);

        mRelChooseFlagapps = view.findViewById(R.id.relChooseFlagApp);
        mRelChooseFlagapps.setOnClickListener(this);

        mRelOverUseScreen = view.findViewById(R.id.relReduceOveruseScreen);
        mRelOverUseScreen.setOnClickListener(this);

        mRelOverUseFlaggedApp = view.findViewById(R.id.relReduceOveruseFlagged);
        mRelOverUseFlaggedApp.setOnClickListener(this);
        mTxtReduceOveruseFlaggedDes = view.findViewById(R.id.txtReduceOveruseFlaggedDes);
        if (isFromFlag) {
            mRelOverUseFlaggedApp.setBackground(getResources().getDrawable(R
                    .drawable.rounded_card_app_menu));

            int marginInDpLeft = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 50, getResources()
                            .getDisplayMetrics());
            int marginInDpBottom = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 5, getResources()
                            .getDisplayMetrics());
            mRelOverUseFlaggedApp.setPadding(marginInDpLeft, 0, 0, marginInDpBottom);
        } else {
            mRelOverUseFlaggedApp.setBackground(null);
        }

    }

    public boolean hasDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(getActivity());
        } else {
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.relJunkFoodmize:
                if (switchJunkFoodmize.isChecked()) {
                    switchJunkFoodmize.setChecked(false);
                    PrefSiempo.getInstance(context).write(PrefSiempo.IS_RANDOMIZE_JUNKFOOD, false);
                    FirebaseHelper.getInstance().logIntention_IconBranding_Randomize(FirebaseHelper.RANDOMIZED_JUNK_FOOD, 0);
                    CoreApplication.getInstance().setRandomize(false);
                } else {
                    switchJunkFoodmize.setChecked(true);
                    PrefSiempo.getInstance(context).write(PrefSiempo.IS_RANDOMIZE_JUNKFOOD, true);
                    CoreApplication.getInstance().setRandomize(true);
                    FirebaseHelper.getInstance().logIntention_IconBranding_Randomize(FirebaseHelper.RANDOMIZED_JUNK_FOOD, 1);
                }
                EventBus.getDefault().postSticky(new NotifyJunkFoodView(true));
                break;
            case R.id.relHideIconBranding:
                if (switchHideIcon.isChecked()) {
                    showDialogOnHideIconBranding();
                } else {
                    switchHideIcon.setChecked(true);
                    PrefSiempo.getInstance(context).write(PrefSiempo.IS_ICON_BRANDING, true);
                    FirebaseHelper.getInstance().logIntention_IconBranding_Randomize(FirebaseHelper.HIDE_ICON_BRANDING, 1);
                    CoreApplication.getInstance().setHideIconBranding(true);
                    EventBus.getDefault().postSticky(new NotifyJunkFoodView(true));
                    EventBus.getDefault().postSticky(new NotifyFavortieView(true));
                    EventBus.getDefault().postSticky(new NotifyToolView(true));
                    EventBus.getDefault().postSticky(new NotifyBottomView(true));
                }

                break;
            case R.id.relChooseFlagApp:
                Intent junkFoodFlagIntent = new Intent(context, JunkfoodFlaggingActivity.class);
                junkFoodFlagIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                junkFoodFlagIntent.putExtra("FromAppMenu", true);
                startActivity(junkFoodFlagIntent);
                break;
            case R.id.relReduceOveruseFlagged:
                requestUsageStatsPermission();
                break;
            default:
                break;
        }
    }

    void requestUsageStatsPermission() {
        if (!hasUsageStatsPermission(getActivity())) {
            startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), 100);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(context)) {
                    if (null == overlayDialogPermission || !overlayDialogPermission.isShowing())
                        showOverLayForDrawingPermission();
                } else {
                    showDialog();
                }
            } else {
                showDialog();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            if (!UIUtils.hasUsageStatsPermission(getActivity())) {
                Toast.makeText(getActivity(), R.string.msg_control_access, Toast.LENGTH_SHORT).show();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(context)) {
                        if (null == overlayDialogPermission || !overlayDialogPermission.isShowing())
                            showOverLayForDrawingPermission();
                    } else {
                        showDialog();
                    }
                } else {
                    showDialog();
                }
            }
        } else if (requestCode == 1000) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(context)) {
                    Toast.makeText(getActivity(), R.string.msg_draw_over_app, Toast.LENGTH_SHORT).show();
                } else {
                    showDialog();
                }
            } else {
                showDialog();
            }
        }
    }


    private void showDialog() {
        index = -1;
        deterTime = PrefSiempo.getInstance(context).read(PrefSiempo.DETER_AFTER, -1);
        if (deterTime == -1) {
            index = 3;

        } else if (deterTime == 0) {
            index = 0;
        } else if (deterTime == 1) {
            index = 1;
        } else if (deterTime == 2) {
            index = 2;
        } else if (deterTime == 5) {
            index = 3;
        } else if (deterTime == 10) {
            index = 4;
        } else if (deterTime == 15) {
            index = 5;
        }
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Deter after")
                .setCancelable(true)
                .setSingleChoiceItems(deter_after_list, index, new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            switchOveruseFlagged.setChecked(true);
                            dialog.dismiss();
                            PrefSiempo.getInstance(context).write(PrefSiempo.DETER_AFTER, 0);
                            FirebaseHelper.getInstance().logDeterUseEvent(0);
                            txtOverUseFlag = String.format(getResources().getString(R.string
                                    .reduce_overuse_Flagged_description_setting), "<font color='#42A4FF'>" + deter_after_list[which] + "</font>");
                        } else if (which == 1) {
                            switchOveruseFlagged.setChecked(true);
                            dialog.dismiss();
                            PrefSiempo.getInstance(context).write(PrefSiempo
                                    .DETER_AFTER, 1);
                            FirebaseHelper.getInstance().logDeterUseEvent(1);
                            txtOverUseFlag = String.format(getResources().getString(R.string
                                    .reduce_overuse_Flagged_description_setting), "<font color='#42A4FF'>" + deter_after_list[which] + "</font>");
                        } else if (which == 2) {
                            switchOveruseFlagged.setChecked(true);
                            dialog.dismiss();
                            PrefSiempo.getInstance(context).write(PrefSiempo.DETER_AFTER, 2);
                            FirebaseHelper.getInstance().logDeterUseEvent(2);
                            txtOverUseFlag = String.format(getResources().getString(R.string
                                    .reduce_overuse_Flagged_description_setting), "<font color='#42A4FF'>" + deter_after_list[which] + "</font>");
                        } else if (which == 3) {
                            switchOveruseFlagged.setChecked(true);
                            dialog.dismiss();
                            PrefSiempo.getInstance(context).write(PrefSiempo.DETER_AFTER, 5);
                            FirebaseHelper.getInstance().logDeterUseEvent(5);
                            txtOverUseFlag = String.format(getResources().getString(R.string
                                    .reduce_overuse_Flagged_description_setting), "<font color='#42A4FF'>" + deter_after_list[which] + "</font>");
                        } else if (which == 4) {
                            switchOveruseFlagged.setChecked(true);
                            dialog.dismiss();
                            PrefSiempo.getInstance(context).write(PrefSiempo.DETER_AFTER, 10);
                            FirebaseHelper.getInstance().logDeterUseEvent(10);
                            txtOverUseFlag = String.format(getResources().getString(R.string
                                    .reduce_overuse_Flagged_description_setting), "<font color='#42A4FF'>" + deter_after_list[which] + "</font>");
                        } else if (which == 5) {
                            switchOveruseFlagged.setChecked(true);
                            dialog.dismiss();
                            PrefSiempo.getInstance(context).write(PrefSiempo.DETER_AFTER, 15);
                            FirebaseHelper.getInstance().logDeterUseEvent(15);
                            txtOverUseFlag = String.format(getResources().getString(R.string
                                    .reduce_overuse_Flagged_description_setting), "<font color='#42A4FF'>" + deter_after_list[which] + "</font>");
                        } else if (which == 6) {
                            switchOveruseFlagged.setChecked(false);
                            dialog.dismiss();
                            PrefSiempo.getInstance(context).write(PrefSiempo.DETER_AFTER, -1);
                            FirebaseHelper.getInstance().logDeterUseEvent(-1);
                            txtOverUseFlag = String.format(getResources().getString(R.string
                                    .reduce_overuse_Flagged_description_setting), "<font " +
                                    "color='#42A4FF'>" + deter_after_list[3]
                                    + "</font>");
                        }
                        EventBus.getDefault().post(new ReduceOverUsageEvent(true));
                        mTxtReduceOveruseFlaggedDes.setText(Html.fromHtml(txtOverUseFlag));
                        mRelOverUseFlaggedApp.setClickable(true);
                    }
                })
                .create();
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        startTime = System.currentTimeMillis();
        mRelOverUseFlaggedApp.setClickable(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        FirebaseHelper.getInstance().logScreenUsageTime(this.getClass().getSimpleName(), startTime);
    }

    private void showDialogOnHideIconBranding() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.are_you_sure));
        builder.setMessage(R.string.msg_hide_icon_branding);
        builder.setPositiveButton(getString(R.string.yes_unhide), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switchHideIcon.setChecked(false);
                PrefSiempo.getInstance(context).write(PrefSiempo.IS_ICON_BRANDING, false);
                FirebaseHelper.getInstance().logIntention_IconBranding_Randomize(FirebaseHelper.HIDE_ICON_BRANDING, 0);
                CoreApplication.getInstance().setHideIconBranding(false);
                EventBus.getDefault().postSticky(new NotifyJunkFoodView(true));
                EventBus.getDefault().postSticky(new NotifyFavortieView(true));
                EventBus.getDefault().postSticky(new NotifyToolView(true));
                EventBus.getDefault().postSticky(new NotifyBottomView(true));
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.dialog_blue));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.dialog_red));
    }

    private void showOverLayForDrawingPermission() {
        if (null != getActivity()) {
            try {
                getActivity().setRequestedOrientation(ActivityInfo
                        .SCREEN_ORIENTATION_PORTRAIT);
                overlayDialogPermission = new Dialog(getActivity(), 0);
                if (overlayDialogPermission.getWindow() != null)
                    overlayDialogPermission.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                overlayDialogPermission.requestWindowFeature(Window.FEATURE_NO_TITLE);
                overlayDialogPermission.setContentView(R.layout
                        .layout_appland_draw_permission);
                Window window = overlayDialogPermission.getWindow();
                // set "origin" to bottom
                window.setGravity(Gravity.BOTTOM);
                WindowManager.LayoutParams params = window.getAttributes();
                window.setAttributes(params);
                overlayDialogPermission.getWindow().setLayout(WindowManager
                        .LayoutParams.MATCH_PARENT, WindowManager
                        .LayoutParams.WRAP_CONTENT);
                overlayDialogPermission.setCancelable(true);
                overlayDialogPermission.setCanceledOnTouchOutside(true);
                overlayDialogPermission.show();

                final ViewFlipper viewFlipperOverlay = overlayDialogPermission
                        .findViewById(R.id.viewFlipperPermissionDrawOverlay);
                final Button btnEnable = overlayDialogPermission.findViewById
                        (R.id.btnEnable);

                final Button btnGotIt = overlayDialogPermission.findViewById
                        (R.id.btnGotIt);
                final Button btnLater = overlayDialogPermission.findViewById(R.id.btnLater);
                btnLater.setText(getString(R.string.cancel));
                btnLater.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        overlayDialogPermission.dismiss();
                    }
                });

                btnEnable.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewFlipperOverlay.setInAnimation(context, R.anim
                                .in_from_right_email);
                        viewFlipperOverlay.setOutAnimation(context, R.anim
                                .out_to_left_email);
                        viewFlipperOverlay.setDisplayedChild(1);


                    }
                });

                btnGotIt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        overlayDialogPermission.dismiss();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!Settings.canDrawOverlays(context)) {
                                Intent intent = new Intent(Settings
                                        .ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:" +
                                                context.getPackageName()));
                                startActivityForResult(intent, 1000);
                            }
                        }
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
