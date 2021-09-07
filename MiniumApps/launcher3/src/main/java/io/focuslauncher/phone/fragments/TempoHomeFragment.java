package io.focuslauncher.phone.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import io.focuslauncher.R;
import io.focuslauncher.phone.activities.CoreActivity;
import io.focuslauncher.phone.activities.UpdateBackgroundActivity;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.event.NotifyBackgroundChange;
import io.focuslauncher.phone.event.NotifyBackgroundToService;
import io.focuslauncher.phone.event.ThemeChangeEvent;
import io.focuslauncher.phone.helper.FirebaseHelper;
import io.focuslauncher.phone.service.ScreenFilterService;
import io.focuslauncher.phone.util.AppUtils;
import io.focuslauncher.phone.utils.PermissionUtil;
import io.focuslauncher.phone.utils.PrefSiempo;
import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_tempo_home)
public class TempoHomeFragment extends CoreFragment {

    @ViewById
    Toolbar toolbar;


    @ViewById
    Switch switchDisableIntentionsControls;

    @ViewById
    RelativeLayout relAllowSpecificApps;

    @ViewById
    Switch switchCustomBackground;

    @ViewById
    RelativeLayout relCustomBackground;

    @ViewById
    Switch switchDarkTheme;

    @ViewById
    Switch switchNotification;


    @ViewById
    Switch switchScreenOverlay;


   /* @ViewById
    Switch switchIconToolsVisibility;

    @ViewById
    Switch switchIconFavoriteVisibility;

    @ViewById
    Switch switchIconJunkFoodVisibility;*/

    @ViewById
    RelativeLayout relDarkTheme;

    @ViewById
    RelativeLayout rel_icon_label;

    private PermissionUtil permissionUtil;

    public TempoHomeFragment() {
        // Required empty public constructor
    }


    @AfterViews
    void afterViews() {
        // Download siempo images
        if (permissionUtil == null) {
            permissionUtil = new PermissionUtil(getActivity());
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp);
        toolbar.setTitle(R.string.homescreen);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
            }
        });
        switchDisableIntentionsControls.setChecked(PrefSiempo.getInstance(getActivity()).read(PrefSiempo
                .IS_INTENTION_ENABLE, false));
        switchDisableIntentionsControls.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefSiempo.getInstance(getActivity()).write(PrefSiempo
                        .IS_INTENTION_ENABLE, isChecked);
                FirebaseHelper.getInstance().logIntention_IconBranding_Randomize(FirebaseHelper.INTENTIONS, isChecked ? 1 : 0);

            }
        });

        if (PrefSiempo.getInstance(getActivity()).read(PrefSiempo
                .IS_DARK_THEME, false)) {
            switchDarkTheme.setChecked(true);
        } else {
            switchDarkTheme.setChecked(false);
        }

        switchDarkTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefSiempo.getInstance(getActivity()).write(PrefSiempo
                        .IS_DARK_THEME, isChecked);
                EventBus.getDefault().postSticky(new ThemeChangeEvent(true));
                android.os.Handler handler = new android.os.Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (null != getActivity()) {
                            getActivity().finish();
                        }
                    }
                }, 60);
            }
        });

        switchCustomBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strImage = PrefSiempo.getInstance(getActivity()).read(PrefSiempo.DEFAULT_BAG, "");
                boolean isEnable = PrefSiempo.getInstance(getActivity()).read(PrefSiempo.DEFAULT_BAG_ENABLE, false);
                if (isEnable
                        && !TextUtils.isEmpty(strImage)) {
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_BAG_ENABLE, false);
                    EventBus.getDefault().post(new NotifyBackgroundToService(false));
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_BAG, "");
                    EventBus.getDefault().postSticky(new NotifyBackgroundChange(true));
                    switchCustomBackground.setChecked(false);
                } else if (!isEnable && TextUtils.isEmpty(strImage)) {
                    //    startActivity(new Intent(context, ChooseBackgroundActivity.class));
                    switchCustomBackground.setChecked(false);
                    checkPermissionsForBackground();

                } else if (!isEnable && !TextUtils.isEmpty(strImage)) {
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_BAG_ENABLE, true);
                    EventBus.getDefault().postSticky(new NotifyBackgroundChange(true));
                    switchCustomBackground.setChecked(true);
                }

            }
        });

        /*switchIconToolsVisibility.setChecked(PrefSiempo.getInstance(getActivity()).read(PrefSiempo.DEFAULT_ICON_TOOLS_TEXT_VISIBILITY_ENABLE, false));
        switchIconToolsVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch sb = (Switch) v;
                if (sb.isChecked()) {
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_ICON_TOOLS_TEXT_VISIBILITY_ENABLE, true);
                } else  {
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_ICON_TOOLS_TEXT_VISIBILITY_ENABLE, false);
                }
            }
        });

        switchIconFavoriteVisibility.setChecked(PrefSiempo.getInstance(getActivity()).read(PrefSiempo.DEFAULT_ICON_FAVORITE_TEXT_VISIBILITY_ENABLE, false));
        switchIconFavoriteVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch sb = (Switch) v;
                if (sb.isChecked()) {
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_ICON_FAVORITE_TEXT_VISIBILITY_ENABLE, true);
                } else  {
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_ICON_FAVORITE_TEXT_VISIBILITY_ENABLE, false);
                }
            }
        });

        switchIconJunkFoodVisibility.setChecked(PrefSiempo.getInstance(getActivity()).read(PrefSiempo.DEFAULT_ICON_JUNKFOOD_TEXT_VISIBILITY_ENABLE, false));
        switchIconJunkFoodVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch sb = (Switch) v;
                if (sb.isChecked()) {
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_ICON_JUNKFOOD_TEXT_VISIBILITY_ENABLE, true);
                } else  {
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_ICON_JUNKFOOD_TEXT_VISIBILITY_ENABLE, false);
                }
            }
        });*/


        rel_icon_label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CoreActivity) getActivity()).loadChildFragment(IconLabelsFragment_.builder().build(), R.id.tempoView);
            }
        });

        final View decorView = getActivity().getWindow().getDecorView();
        final int uiOptions = decorView.getSystemUiVisibility();
        final int[] newUiOptions = {uiOptions};

        switchNotification.setChecked(PrefSiempo.getInstance(getActivity()).read(PrefSiempo.DEFAULT_NOTIFICATION_ENABLE, false));
        switchNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch sb = (Switch) v;
                if (sb.isChecked()) {
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_NOTIFICATION_ENABLE, true);
                } else {
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_NOTIFICATION_ENABLE, false);
                }
                AppUtils.notificationBarManaged(getActivity(), null);
            }
        });

        switchScreenOverlay.setChecked(PrefSiempo.getInstance(getActivity()).read(PrefSiempo.DEFAULT_SCREEN_OVERLAY, false));
        switchScreenOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Switch sb = (Switch) view;
                if (sb.isChecked()) {
                    checkPermissionsForSystemWindow();

                } else {
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_SCREEN_OVERLAY, false);
                    Intent command = new Intent(getActivity(), ScreenFilterService.class);
                    command.putExtra(ScreenFilterService.BUNDLE_KEY_COMMAND, 1);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        getActivity().startForegroundService(command);
                    else
                        getActivity().startService(command);
                }

            }
        });

    }

    private void checkPermissionsForSystemWindow() {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !permissionUtil.hasGiven
                (PermissionUtil.SYSTEM_WINDOW_ALERT))) {
            try {
                TedPermission.with(getActivity())
                        .setPermissionListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_SCREEN_OVERLAY, true);
                                Intent command = new Intent(getActivity(), ScreenFilterService.class);
                                command.putExtra(ScreenFilterService.BUNDLE_KEY_COMMAND, 0);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                    getActivity().startForegroundService(command);
                                else
                                    getActivity().startService(command);
                            }

                            @Override
                            public void onPermissionDenied(ArrayList<String> deniedPermissions) {

                            }
                        })
                        .setDeniedMessage(R.string.msg_permission_denied)
                        .setPermissions(new String[]{
                                Manifest.permission.SYSTEM_ALERT_WINDOW})
                        .check();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_SCREEN_OVERLAY, true);
            Intent command = new Intent(getActivity(), ScreenFilterService.class);
            command.putExtra(ScreenFilterService.BUNDLE_KEY_COMMAND, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                getActivity().startForegroundService(command);
            else
                getActivity().startService(command);
        }
    }


    private void notificationVisibility() {

        if (PrefSiempo.getInstance(getActivity()).read(PrefSiempo.DEFAULT_NOTIFICATION_ENABLE, false)) {
            View decorView = getActivity().getWindow().getDecorView();
            decorView.setFitsSystemWindows(false);
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getActivity().getWindow().getDecorView();
            decorView.setFitsSystemWindows(true);
        }
    }

    @Click
    void relAllowSpecificApps() {
        switchDisableIntentionsControls.performClick();
    }

    @Click
    void relCustomBackground() {
        checkPermissionsForBackground();
    }

    private void checkPermissionsForBackground() {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !permissionUtil.hasGiven
                (PermissionUtil.WRITE_EXTERNAL_STORAGE_PERMISSION))) {
            try {
                TedPermission.with(getActivity())
                        .setPermissionListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                CoreApplication.getInstance().downloadSiempoImages();
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");
                                startActivityForResult(intent, 10);
                            }

                            @Override
                            public void onPermissionDenied(ArrayList<String> deniedPermissions) {

                            }
                        })
                        .setDeniedMessage(R.string.msg_permission_denied)
                        .setPermissions(new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest
                                        .permission
                                        .READ_EXTERNAL_STORAGE})
                        .check();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            CoreApplication.getInstance().downloadSiempoImages();
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 10);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String strImage = PrefSiempo.getInstance(getActivity()).read(PrefSiempo.DEFAULT_BAG, "");
        boolean isEnable = PrefSiempo.getInstance(getActivity()).read(PrefSiempo.DEFAULT_BAG_ENABLE, false);
        boolean isPermission = permissionUtil.hasGiven(PermissionUtil
                .WRITE_EXTERNAL_STORAGE_PERMISSION);
        if (isEnable
                && !TextUtils.isEmpty(strImage) && isPermission) {
            switchCustomBackground.setChecked(true);
        } else if (!isEnable && TextUtils.isEmpty(strImage) && !isPermission) {
            switchCustomBackground.setChecked(false);
            PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_BAG_ENABLE, false);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Intention screen Wallpaper selection
        if (requestCode == 10 || requestCode == 7) {
            try {
                switch (requestCode) {
                    case 10:
                        if (resultCode == Activity.RESULT_OK) {
                            Uri uri = data.getData();

                            if (uri != null && !TextUtils.isEmpty(uri.toString())) {
// For what reason this thing is here?
//                                if (uri.toString().contains("com.google.android.apps.photos.contentprovider")) {
//                                    return;
//                                }

                                if (uri.toString().contains("/storage")) {
                                    String[] storagepath = uri.toString().split("/storage");
                                    if (storagepath.length > 1) {
                                        String filePath = "/storage" + storagepath[1];
                                        Intent mUpdateBackgroundIntent = new Intent(getActivity(), UpdateBackgroundActivity.class);
                                        mUpdateBackgroundIntent.putExtra("imageUri", filePath);
                                        startActivityForResult(mUpdateBackgroundIntent, 3);
                                    }
                                } else {
                                    String id = uri.getLastPathSegment();

                                    if (!TextUtils.isEmpty(id) && uri != null) {

                                        try {
                                            InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
                                            File file = new File(getActivity().getCacheDir(), id);
                                            writeFile(inputStream, file);
                                            String filePath = file.getAbsolutePath();

                                            if (filePath.contains("raw:")) {
                                                String[] downloadPath = filePath.split("raw:");
                                                if (downloadPath.length > 1) {
                                                    filePath = downloadPath[1];
                                                }
                                            }

                                            Intent mUpdateBackgroundIntent = new Intent(getActivity(), UpdateBackgroundActivity.class);
                                            mUpdateBackgroundIntent.putExtra("imageUri", filePath);
                                            startActivityForResult(mUpdateBackgroundIntent, 3);

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }

                        }
                        break;
                    case 7:
                        if (resultCode == Activity.RESULT_OK) {
                            Uri selectedImage = data.getData();
                            String[] filePathColumn = {MediaStore.Images.Media.DATA};

                            Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            cursor.moveToFirst();
                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            String picturePath = cursor.getString(columnIndex);
                            cursor.close();
                            Intent mUpdateBackgroundIntent = new Intent(getActivity(),
                                    UpdateBackgroundActivity
                                            .class);
                            mUpdateBackgroundIntent.putExtra("imageUri", picturePath);
                            startActivityForResult(mUpdateBackgroundIntent, 3);
                        }
                        break;
                }
            } catch (Exception e) {
                Log.e("ERROR", "FOCUS_LAUNCHER_ERROR");
                e.printStackTrace();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    void writeFile(InputStream in, File file) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Click
    void relDarkTheme() {
        switchDarkTheme.performClick();
    }
}
