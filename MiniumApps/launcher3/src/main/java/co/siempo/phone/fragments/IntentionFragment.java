package co.siempo.phone.fragments;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import co.siempo.phone.R;
import co.siempo.phone.activities.DashboardActivity;
import co.siempo.phone.activities.EnableTempoActivity;
import co.siempo.phone.activities.HelpActivity;
import co.siempo.phone.activities.IntentionEditActivity;
import co.siempo.phone.activities.JunkfoodFlaggingActivity;
import co.siempo.phone.activities.SettingsActivity_;
import co.siempo.phone.activities.UpdateBackgroundActivity;
import co.siempo.phone.dialog.DialogTempoSetting;
import co.siempo.phone.helper.ActivityHelper;
import co.siempo.phone.service.StatusBarService;
import co.siempo.phone.util.AppUtils;
import co.siempo.phone.utils.PackageUtil;
import co.siempo.phone.utils.PermissionUtil;
import co.siempo.phone.utils.PrefSiempo;
import co.siempo.phone.utils.UIUtils;


public class IntentionFragment extends CoreFragment implements View.OnClickListener {

    Context context;
    TextView txtIntention, txtHint;
    private View view;
    private ImageView imgTempo;
    private ImageView imgOverFlow, imgPullTab;
    private CardView cardView;
    private PopupWindow mPopupWindow;
    private RelativeLayout relRootLayout;
    private Window mWindow;
    private int defaultStatusBarColor;
    private PermissionUtil permissionUtil;
    private DialogTempoSetting dialogTempo;
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;

    public IntentionFragment() {
        // Required empty public constructor
    }

    public static IntentionFragment newInstance() {
        return new IntentionFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_intention, container, false);
        mWindow = getActivity().getWindow();
        context = getActivity();
        permissionUtil = new PermissionUtil(context);
        Intent myService = new Intent(getActivity(), StatusBarService.class);
        getActivity().startService(myService);
        initView(view);


        return view;
    }


    public void hideView() {
        if (PrefSiempo.getInstance(getActivity()).read(PrefSiempo.TOGGLE_LEFTMENU, 0) >= 3) {
            if (imgPullTab != null) imgPullTab.setVisibility(View.GONE);
        } else {
            if (imgPullTab != null) imgPullTab.setVisibility(View.VISIBLE);
        }

        if (mWindow != null) {
            //mWindow.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //mWindow.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }



//        boolean isEnable = PrefSiempo.getInstance(getActivity()).read(PrefSiempo
//                .DEFAULT_BAG_ENABLE, false);
//        if(isEnable){
//            if (mWindow != null) {
//                //mWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//                // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
//                //mWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//                TypedValue typedValue = new TypedValue();
//                Resources.Theme theme = context.getTheme();
//                theme.resolveAttribute(R.attr.transparent, typedValue, true);
//                int transparentcolor= typedValue.data;
//                // finally change the color
//                //mWindow.setStatusBarColor(transparentcolor);
//                //mWindow.setNavigationBarColor(transparentcolor);
//            }
//        }

    }

    private void initView(View view) {
        relRootLayout = view.findViewById(R.id.relRootLayout);
        imgTempo = view.findViewById(R.id.imgTempo);
        imgTempo.setOnClickListener(this);
        imgPullTab = view.findViewById(R.id.imgPullTab);
        imgPullTab.setOnClickListener(this);
        imgOverFlow = view.findViewById(R.id.imgOverFlow);
        imgOverFlow.setOnClickListener(this);
        txtIntention = view.findViewById(R.id.txtIntention);
        txtHint = view.findViewById(R.id.txtHint);
        cardView = view.findViewById(R.id.cardView);
        cardView.setOnClickListener(this);

        hideView();
    }


    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            hideView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DashboardActivity.currentIndexDashboard == 1) {
            hideView();
        }
        if (dialogTempo != null && dialogTempo.isShowing()) {
            if (!permissionUtil.hasGiven(PermissionUtil.NOTIFICATION_ACCESS)
                    /*|| !permissionUtil.hasGiven(PermissionUtil.CALL_PHONE_PERMISSION)*/
                    || !PackageUtil.isSiempoLauncher(context)) {
                dialogTempo.dismiss();
            }
        }
        if (PrefSiempo.getInstance(getActivity()).read(PrefSiempo.IS_INTENTION_ENABLE, false)) {
            cardView.setVisibility(View.GONE);
        } else {
            cardView.setVisibility(View.VISIBLE);
        }
        if (PrefSiempo.getInstance(getActivity()).read(PrefSiempo.DEFAULT_INTENTION, "").equalsIgnoreCase("")) {
            txtHint.setVisibility(View.INVISIBLE);
            txtIntention.setText(getString(R.string.what_s_your_intention));
            txtIntention.setTextColor(ContextCompat.getColor(getActivity(), R.color.hint_white));
        } else {
            txtHint.setVisibility(View.VISIBLE);
            txtHint.setText(getString(R.string.your_intention));
            txtIntention.setText(PrefSiempo.getInstance(getActivity()).read(PrefSiempo.DEFAULT_INTENTION, ""));
            txtIntention.setTextColor(ContextCompat.getColor(getActivity(), R.color.settings_title_black));
        }
        if (getActivity() != null) {
            if (PrefSiempo.getInstance(getActivity()).read(PrefSiempo.IS_APP_INSTALLED_FIRSTTIME_SHOW_TOOLTIP, true)) {
                if (!UIUtils.isMyLauncherDefault(getActivity())) {
                    android.os.Handler handler = new android.os.Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                new ActivityHelper(getActivity()).handleDefaultLauncher(getActivity());
                                PrefSiempo.getInstance(getActivity()).write(PrefSiempo.IS_APP_INSTALLED_FIRSTTIME_SHOW_TOOLTIP, false);
                            } else {
                                new ActivityHelper(getActivity()).handleDefaultLauncher(getActivity());
                                PrefSiempo.getInstance(getActivity()).write(PrefSiempo.IS_APP_INSTALLED_FIRSTTIME_SHOW_TOOLTIP, false);
                            }

                        }
                    }, 500);
                }
            }
        }

    }


    private void setWindowFlag(final int bits, boolean on) {
        Window win = getActivity().getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgTempo:
                if (null != getActivity()) {
                    if (permissionUtil.hasGiven(PermissionUtil.NOTIFICATION_ACCESS)
                            /*&& permissionUtil.hasGiven(PermissionUtil.CALL_PHONE_PERMISSION)*/
                            && PackageUtil.isSiempoLauncher(context) &&
                            UIUtils.hasUsageStatsPermission(context)) {

                        TypedValue typedValue = new TypedValue();
                        Resources.Theme theme = context.getTheme();
                        theme.resolveAttribute(R.attr.dialog_style, typedValue, true);
                        int dialogStyle = typedValue.resourceId;
                        dialogTempo = new DialogTempoSetting(getActivity(), dialogStyle);
                        if (dialogTempo.getWindow() != null)
                            dialogTempo.getWindow().setGravity(Gravity.TOP);
                        if (dialogTempo != null && !dialogTempo.isShowing()) {
                            dialogTempo.show();
                            imgTempo.setClickable(false);
                            if (dialogTempo != null) {
                                dialogTempo.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        imgTempo.setClickable(true);
                                    }
                                });
                            }

                        }
                    } else {
                        Intent intent = new Intent(context, EnableTempoActivity.class);
                        startActivityForResult(intent, 100);
                    }

                }
                break;
            case R.id.imgPullTab:
                ObjectAnimator animY = ObjectAnimator.ofFloat(relRootLayout, "translationX", 100f, 0f);
                animY.setDuration(700);//1sec
                animY.setInterpolator(new BounceInterpolator());
                animY.setRepeatCount(0);
                animY.start();

                break;
            case R.id.imgOverFlow:
                showOverflowDialog();
                break;
            case R.id.cardView:
//                EventBus.getDefault().post(new ReduceOverUsageEvent(true));
                if (null != getActivity()) {
                    Intent intent = new Intent(getActivity(), IntentionEditActivity.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
                break;
            default:
                break;
        }
    }

    private void showOverflowDialog() {
        if (getActivity() != null && imgOverFlow != null) {
            //popupMenu();
            final ViewGroup root = (ViewGroup) getActivity().getWindow().getDecorView().getRootView();
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // Inflate the custom layout/view
            View customView;
            if (inflater != null) {
                customView = inflater.inflate(R.layout.home_popup, null);

                mPopupWindow = new PopupWindow(
                        customView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                // Set an elevation value for popup window
                // Call requires API level 21
                if (Build.VERSION.SDK_INT >= 21) {
                    mPopupWindow.setElevation(5.0f);
                }

                LinearLayout linHelp = customView.findViewById(R.id.linHelp);
                LinearLayout linSettings = customView.findViewById(R.id.linSettings);
                LinearLayout linTempo = customView.findViewById(R.id.linTempo);
                LinearLayout linWallpaper = customView.findViewById(R.id.linWallpaper);
                LinearLayout linDistractingApp = customView.findViewById(R.id.linDistractingApp);

                linTempo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (getActivity() != null) {
                            UIUtils.clearDim(root);
                            mPopupWindow.dismiss();
                            if (permissionUtil.hasGiven(PermissionUtil.NOTIFICATION_ACCESS)
                                    /*&& permissionUtil.hasGiven(PermissionUtil.CALL_PHONE_PERMISSION)*/
                                    && PackageUtil.isSiempoLauncher(context)  &&
                                    UIUtils.hasUsageStatsPermission(context)) {
                                TypedValue typedValue = new TypedValue();
                                Resources.Theme theme = context.getTheme();
                                theme.resolveAttribute(R.attr.dialog_style, typedValue, true);
                                int dialogStyle = typedValue.resourceId;
                                DialogTempoSetting dialogTempo = new
                                        DialogTempoSetting(getActivity(), dialogStyle);
                                if (dialogTempo.getWindow() != null)
                                    dialogTempo.getWindow().setGravity(Gravity.TOP);
                                dialogTempo.show();
                            } else {
                                Intent intent = new Intent(context, EnableTempoActivity.class);
                                startActivityForResult(intent, 100);
                            }
                        }
                    }
                });
                linSettings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Code for opening Tempo Settings
                        if (getActivity() != null) {
                            Intent intent = new Intent(getActivity(), SettingsActivity_.class);
                            startActivity(intent);
                            UIUtils.clearDim(root);
                            mPopupWindow.dismiss();
                        }
                    }
                });
                linHelp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (getActivity() != null) {
                            UIUtils.clearDim(root);
                            mPopupWindow.dismiss();
                            Intent intent = new Intent(getActivity(), HelpActivity.class);
                            startActivity(intent);
                        }
                    }
                });

                linWallpaper.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (getActivity() != null) {
                            UIUtils.clearDim(root);
                            mPopupWindow.dismiss();
                            showWallPaperSelection();
                        }
                    }
                });

                linDistractingApp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (getActivity() != null) {
                            UIUtils.clearDim(root);
                            mPopupWindow.dismiss();
                            Intent junkFoodFlagIntent = new Intent(getActivity(), JunkfoodFlaggingActivity.class);
                            startActivity(junkFoodFlagIntent);
                        }
                    }
                });

                mPopupWindow.setOutsideTouchable(true);
                mPopupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                mPopupWindow.showAsDropDown(imgOverFlow, 0, (int) -imgOverFlow.getX() - 10);
                UIUtils.applyDim(root, 0.7f);
                if (null != getActivity()) {
                    UIUtils.hideSoftKeyboard(getActivity(), getActivity().getWindow().getDecorView().getWindowToken());
                }
                mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        UIUtils.clearDim(root);

                    }
                });
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser &&  getActivity() != null)
        {
            AppUtils.notificationBarManaged(getActivity(), null);
            ((DashboardActivity)getActivity()).changeLayoutBackground(-1);
            AppUtils.statusbarColor0(getActivity(), 1);
        }
    }

    public void showWallPaperSelection(){
        final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(getActivity());
        View sheetView = getActivity().getLayoutInflater().inflate(R.layout.shortcuts_wallpaper, null);
        mBottomSheetDialog.setContentView(sheetView);

        ImageView folder = sheetView.findViewById(R.id.shortcut_icon_folder);
        folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 10);

            }
        });

        ImageView gallary = sheetView.findViewById(R.id.shortcut_icon_gallary);
        gallary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, 7);

            }
        });


        ImageView browser = sheetView.findViewById(R.id.shortcut_icon_globe);
        browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                Log.e("permissionCheck","permissionCheck "+permissionCheck);
                if(permissionCheck == -1){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                }else
                {
                    openBrowser();
                }
            }
        });

        mBottomSheetDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        boolean canUseExternalStorage = false;

        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                canUseExternalStorage = true;
            }
            if (!canUseExternalStorage) {
                Toast.makeText(getActivity(), "Cannot use this feature without requested permission", Toast.LENGTH_SHORT).show();
            } else {
                openBrowser();
            }
        }
    }

    private String isDuplicatePath = "";
    private void openBrowser()
    {
        isDuplicatePath = "";
        setMediaObserver();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("https://www.google.com/"));
        startActivity(i);
    }


    private void setMediaObserver() {
        getActivity().getContentResolver().registerContentObserver(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true,
                new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        Log.d("your_tag","External Media has been changed3");
                        super.onChange(selfChange);

                        Long timestamp = readLastDateFromMediaStore(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        String mime = readLastMIMEFromMediaStore(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        // comapare with your stored last value and do what you need to do

                        String path = "blank";
                        if(mime.toLowerCase().contains("image"))
                        {
                            path = readLastPathFromMediaStore(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        }

                        if(!path.equalsIgnoreCase("blank"))
                        {
                            if(!isDuplicatePath.equalsIgnoreCase(path))
                            {
                                isDuplicatePath = path;

                                Intent mUpdateBackgroundIntent = new Intent(getActivity(),UpdateBackgroundActivity.class);
                                mUpdateBackgroundIntent.putExtra("imageUri", path);
                                startActivityForResult(mUpdateBackgroundIntent, 3);
                            }

                            Log.e("lastImage","lastImage timestemp3 "+timestamp+" mime "+mime+" path = "+path);
                        }

                    }
                }
        );
    }


    private Long readLastDateFromMediaStore(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, "date_added DESC");

        Long dateAdded =-1l;
        if (cursor.moveToNext()) {
            dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED));
        }
        cursor.close();
        return dateAdded;
    }

    private String readLastMIMEFromMediaStore(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, "date_added DESC");

        String mime = "";
        if (cursor.moveToNext()) {
            mime= cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE));
        }
        cursor.close();
        return mime;
    }

    private String readLastPathFromMediaStore(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, "date_added DESC");

        String displayName = "";
        String[] column = { MediaStore.Images.Media.DATA };

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            displayName= cursor.getString(columnIndex);
        }
        cursor.close();
        return displayName;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 100:
                if(resultCode == Activity.RESULT_OK){
                    imgTempo.performClick();
                }
                break;
            case 10:
                if(resultCode==Activity.RESULT_OK){
                    Uri uri=data.getData();

                    if(uri !=null && !TextUtils.isEmpty(uri.toString())){

                        if(uri.toString().contains("com.google.android.apps.photos.contentprovider")){
                            return;
                        }

                        if(uri.toString().contains("/storage")){
                            String[] storagepath=uri.toString().split("/storage");
                            if(storagepath.length>1){
                                String filePath="/storage"+storagepath[1];
                                Intent mUpdateBackgroundIntent = new Intent(getActivity(),UpdateBackgroundActivity.class);
                                mUpdateBackgroundIntent.putExtra("imageUri", filePath);
                                startActivityForResult(mUpdateBackgroundIntent, 3);
                            }
                        }
                        else{
                            String id = DocumentsContract.getDocumentId(uri);

                            if(!TextUtils.isEmpty(id) && uri!=null){

                                try {
                                    InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
                                    File file = new File(getActivity().getCacheDir().getAbsolutePath()+"/"+id);
                                    writeFile(inputStream, file);
                                    String filePath = file.getAbsolutePath();

                                    if(filePath.contains("raw:")){
                                        String[] downloadPath=filePath.split("raw:");
                                        if(downloadPath.length>1){
                                            filePath =downloadPath[1];
                                        }
                                    }

                                    Intent mUpdateBackgroundIntent = new Intent(getActivity(),UpdateBackgroundActivity.class);
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
                if(resultCode == Activity.RESULT_OK){
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };

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

    }

    void writeFile(InputStream in, File file) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if ( out != null ) {
                    out.close();
                }
                in.close();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }

}
