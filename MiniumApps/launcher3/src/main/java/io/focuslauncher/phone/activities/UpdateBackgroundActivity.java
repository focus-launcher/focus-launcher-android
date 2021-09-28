package io.focuslauncher.phone.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import io.focuslauncher.R;
import io.focuslauncher.phone.event.NotifyBackgroundChange;
import io.focuslauncher.phone.event.NotifyBackgroundToService;
import io.focuslauncher.phone.utils.PermissionUtil;
import io.focuslauncher.phone.utils.PrefSiempo;
import de.greenrobot.event.EventBus;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class UpdateBackgroundActivity extends CoreActivity {

    String strImage;
    private Toolbar toolbar;
    private PermissionUtil permissionUtil;
    private ImageView imageView;
    private CircularProgressDrawable circularProgressDrawable;

    private PhotoViewAttacher mAttacher;
    private PhotoView photoView;
    private RelativeLayout hintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_background);
        permissionUtil = new PermissionUtil(this);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.update_background);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setSupportActionBar(toolbar);
        Intent imageIntent = getIntent();
        imageView = findViewById(R.id.imageView);

        circularProgressDrawable = new CircularProgressDrawable(this);
        circularProgressDrawable.setStrokeWidth(8f);
        circularProgressDrawable.setCenterRadius(80f);
        circularProgressDrawable.setColorSchemeColors(Color.parseColor("#448AFF"));
        circularProgressDrawable.start();

        photoView = findViewById(R.id.ivFullScreen);

        mAttacher = new PhotoViewAttacher(photoView);
        hintLayout = findViewById(R.id.hintLayout);

        boolean isVisible = PrefSiempo.getInstance(this).read(PrefSiempo.IS_ASK_HINT, false);

        if (!isVisible) {
            hintLayout.setVisibility(View.VISIBLE);
            hintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PrefSiempo.getInstance(UpdateBackgroundActivity.this).write(PrefSiempo.IS_ASK_HINT, true);
                    hintLayout.setVisibility(View.GONE);
                }
            });
        } else {
            hintLayout.setVisibility(View.GONE);
        }

        if (imageIntent.getExtras() != null && imageIntent.hasExtra("imageUri")) {
            strImage = imageIntent.getExtras().getString("imageUri");
            checkPermissionAndDisplay(this, strImage);
        }
    }

    private void checkPermissionAndDisplay(final Context context, final String strImage) {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !permissionUtil.hasGiven
                (PermissionUtil.WRITE_EXTERNAL_STORAGE_PERMISSION))) {
            try {
                TedPermission.with(context)
                        .setPermissionListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                displayImageAsPhoto(strImage);
                            }

                            @Override
                            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                                startActivity(new Intent(UpdateBackgroundActivity.this,
                                        SettingsActivity.class).addFlags(Intent
                                        .FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                finish();
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
            displayImageAsPhoto(strImage);
        }
    }

    private void displayImageAsPhoto(String strImage) {
        /*Glide.with(this)
                .load(Uri.fromFile(new File(strImage)))
                .placeholder(circularProgressDrawable)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageView);*/

        Glide.with(getApplicationContext())
                .load(Uri.fromFile(new File(strImage)))
                .placeholder(circularProgressDrawable)
                .listener(new RequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        mAttacher.update();
                        return false;
                    }
                }).into(photoView);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED, new Intent());
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.update_email_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.tick);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                setWall();

                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void setWall() {
        photoView.buildDrawingCache();
        Bitmap bitmap = photoView.getDrawingCache();
        String path = new File(getExternalFilesDir(null), "/" + getString(R.string.app_name) + "_" + System.currentTimeMillis() + ".png").getAbsolutePath();
        OutputStream out = null;
        File file = new File(path);
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            strImage = file.getPath();
            Log.e("strImage ", "new strImage " + strImage);
            PrefSiempo.getInstance(UpdateBackgroundActivity.this).write(PrefSiempo
                    .DEFAULT_BAG, strImage);
            PrefSiempo.getInstance(UpdateBackgroundActivity.this).write(PrefSiempo.DEFAULT_BAG_ENABLE, true);
            setResult(Activity.RESULT_OK, new Intent());
            EventBus.getDefault().postSticky(new NotifyBackgroundChange(true));
            EventBus.getDefault().post(new NotifyBackgroundToService(true));
            finish();
        }
    }
}
