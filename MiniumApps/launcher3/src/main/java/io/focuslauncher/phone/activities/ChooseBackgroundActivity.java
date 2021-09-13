package io.focuslauncher.phone.activities;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import io.focuslauncher.R;
import io.focuslauncher.phone.adapters.BackgroundItemAdapter;
import io.focuslauncher.phone.models.ImageItem;
import io.focuslauncher.phone.utils.PermissionUtil;
import io.focuslauncher.phone.utils.PrefSiempo;

public class ChooseBackgroundActivity extends CoreActivity {
    ArrayList<ImageItem> internalItemList;
    File folderSiempoImage;
    boolean openSubFolder = false;
    BackgroundItemAdapter backgroundItemAdapter;
    PermissionUtil permissionUtil;
    ProgressBar loading_progress;
    private Toolbar toolbar;
    private GridView mImageGridview;
    private ArrayList<ImageItem> mainItemList;
    private int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_background);


        initView();

        createSiempoFolder();

        checkPermissionAndBind();

    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.choose_background);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mImageGridview = findViewById(R.id.ImageGridview);
        loading_progress = findViewById(R.id.loading_progress);
    }

    private void createSiempoFolder() {
        try {
            folderSiempoImage = new File(Environment.getExternalStorageDirectory() +
                    "/Siempo images");
            if (!folderSiempoImage.exists()) {
                folderSiempoImage.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindGridView() {
        mainItemList = new ArrayList<>();
        final String strDefault = PrefSiempo.getInstance(ChooseBackgroundActivity.this).read(PrefSiempo
                .DEFAULT_BAG, "");
        new AsyncTask<String, String, ArrayList<ImageItem>>() {
            ArrayList<ImageItem> local;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                local = new ArrayList<>();
                loading_progress.setVisibility(View.VISIBLE);
            }

            @Override
            protected ArrayList<ImageItem> doInBackground(String... strings) {
                if (!strDefault.equalsIgnoreCase("")) {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(strDefault);
                    local.add(new ImageItem(getString(R.string.current_background), list,
                            true));
                }

                if (getAllImagePaths() != null && getAllImagePaths().size() > 0) {
                    local.add(new ImageItem(getString(R.string.my_images), getAllImagePaths(),
                            true));
                }

                if (folderSiempoImage.exists()) {
                    ArrayList<String> list = new ArrayList<>();
                    for (File imagePath : folderSiempoImage.listFiles()) {
                        if (imagePath.toString().endsWith(".jpg") || imagePath.toString().endsWith(".JPG")
                                || imagePath.toString().endsWith(".jpeg") || imagePath.toString().endsWith(".JPEG")
                                || imagePath.toString().endsWith(".png") || imagePath.toString().endsWith(".PNG")
                                ) {
                            list.add(imagePath.toString());
                        }
                    }
                    if (list.size() > 0) {
                        String first_image = list.get(0);
                        local.add(new ImageItem(getString(R.string.siempo_images), list,
                                true));
                    }

                }
                return local;
            }

            @Override
            protected void onPostExecute(ArrayList<ImageItem> local) {
                super.onPostExecute(local);
                loading_progress.setVisibility(View.GONE);
                mainItemList = local;
                backgroundItemAdapter = new BackgroundItemAdapter(ChooseBackgroundActivity
                        .this, mainItemList);
                mImageGridview.setAdapter(backgroundItemAdapter);
            }
        }.execute();


        mImageGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ImageItem item = (ImageItem) parent.getItemAtPosition(position);
                String mItemText = item.name;
                if (mItemText.equalsIgnoreCase(getString(R.string.current_background))) {
                    finish();
                } else if (mItemText.equalsIgnoreCase(getString(R.string.my_images))) {
                    openSubFolder = true;
                    internalItemList = new ArrayList<>();
                    for (String imageItem : mainItemList.get(position).getDrawableId()) {
                        ArrayList<String> stringArrayList = new ArrayList<>();
                        stringArrayList.add(imageItem);
                        ImageItem imageItem1 = new ImageItem("", stringArrayList, true);
                        internalItemList.add(imageItem1);
                    }
                    toolbar.setTitle(getString(R.string.my_images));
                    backgroundItemAdapter = new BackgroundItemAdapter(ChooseBackgroundActivity
                            .this, internalItemList);
                    mImageGridview.setAdapter(backgroundItemAdapter);

                } else if (mItemText.equalsIgnoreCase(getString(R.string.siempo_images))) {
                    openSubFolder = true;
                    internalItemList = new ArrayList<>();
                    for (String imageItem : mainItemList.get(position).getDrawableId()) {
                        ArrayList<String> stringArrayList = new ArrayList<>();
                        stringArrayList.add(imageItem);
                        ImageItem imageItem1 = new ImageItem("", stringArrayList, true);
                        internalItemList.add(imageItem1);

                    }
                    toolbar.setTitle(getString(R.string.siempo_images));
                    backgroundItemAdapter = new BackgroundItemAdapter(ChooseBackgroundActivity
                            .this, internalItemList);
                    mImageGridview.setAdapter(backgroundItemAdapter);
                } else {
                    if (internalItemList != null && internalItemList.size() > 0) {
                        Intent mUpdateBackgroundIntent = new Intent(ChooseBackgroundActivity.this,
                                UpdateBackgroundActivity
                                        .class);
                        mUpdateBackgroundIntent.putExtra("imageUri", internalItemList.get(position)
                                .getDrawableId().get(0));
                        startActivityForResult(mUpdateBackgroundIntent, PICK_IMAGE_REQUEST);
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (openSubFolder) {
            openSubFolder = false;
            toolbar.setTitle(getString(R.string.choose_background));
            backgroundItemAdapter = new BackgroundItemAdapter(ChooseBackgroundActivity
                    .this, mainItemList);
            mImageGridview.setAdapter(backgroundItemAdapter);
        } else {
            Log.e("Finishing Activity", "Finish called");
            finish();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            openSubFolder = false;
            toolbar.setTitle(getString(R.string.choose_background));
            String strDefault = PrefSiempo.getInstance(ChooseBackgroundActivity.this).read(PrefSiempo
                    .DEFAULT_BAG, "");

            if (!strDefault.equalsIgnoreCase("")) {
                ArrayList<String> list = new ArrayList<>();
                list.add(strDefault);
                int id = checkItem();
                if (id == -1) {
                    mainItemList.add(0, new ImageItem(getString(R.string.current_background), list,
                            true));
                } else {
                    mainItemList.set(id, new ImageItem(getString(R.string.current_background), list,
                            true));

                }
            }
            backgroundItemAdapter = new BackgroundItemAdapter(ChooseBackgroundActivity
                    .this, mainItemList);
            mImageGridview.setAdapter(backgroundItemAdapter);
        }
    }


    int checkItem() {
        if (mainItemList != null && mainItemList.size() > 0) {
            for (int i = 0; i < mainItemList.size(); i++) {
                if (mainItemList.get(i).getName().equalsIgnoreCase(getString(R.string
                        .current_background))) {
                    return i;
                }
            }
        }

        return -1;
    }

    private void checkPermissionAndBind() {
        permissionUtil = new PermissionUtil(this);
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !permissionUtil.hasGiven
                (PermissionUtil.WRITE_EXTERNAL_STORAGE_PERMISSION))) {
            try {
                TedPermission.with(this)
                        .setPermissionListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                // CoreApplication.getInstance().downloadSiempoImages();
                                bindGridView();
                            }

                            @Override
                            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                                Log.e("OnBackPress", "onBackPress Called");
                                finish();
                            }
                        })
                        .setDeniedMessage(R.string.msg_permission_denied)
                        .setPermissions(new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest
                                        .permission
                                        .READ_EXTERNAL_STORAGE,})
                        .check();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // CoreApplication.getInstance().downloadSiempoImages();
            bindGridView();
        }
    }


    public ArrayList<String> getAllImagePaths() {
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA};
        Cursor cursor = null;
        final SortedSet<String> dirList = new TreeSet<>();
        final ArrayList<File> resultIAV = new ArrayList<>();
        final ArrayList<String> resultIAV1 = new ArrayList<>();
        String[] directories = null;
        if (imageUri != null) {
            cursor = getContentResolver().query(imageUri, projection, null, null, null);
        }

        if ((cursor != null) && (cursor.moveToFirst())) {
            do {
                String tempDir = cursor.getString(0);
                tempDir = tempDir.substring(0, tempDir.lastIndexOf("/"));
                try {
                    if (!tempDir.contains("Siempo images")) {
                        dirList.add(tempDir);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            while (cursor.moveToNext());
            directories = new String[dirList.size()];

            dirList.toArray(directories);
            cursor.close();
        }
        final String[] finalDirectories = directories;
        for (int i = 0; i < dirList.size(); i++) {
            File imageDir = null;
            if (finalDirectories != null) {
                imageDir = new File(finalDirectories[i]);
            }
            File[] imageList = new File[0];
            if (imageDir != null) {
                imageList = imageDir.listFiles();
            }
            if (imageList == null)
                continue;
            for (File imagePath : imageList) {
                try {

                    if (imagePath.getName().contains(".jpg") || imagePath.getName().contains(".JPG")
                            || imagePath.getName().contains(".jpeg") || imagePath.getName().contains(".JPEG")
                            || imagePath.getName().contains(".png") || imagePath.getName().contains(".PNG")
                            ) {
                        String path = imagePath.getAbsolutePath();
                        resultIAV.add(imagePath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (resultIAV.size() > 0) {
            try {
                Collections.sort(resultIAV, new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        //return Long.compare(f2.lastModified(), f1.lastModified());
                        return Long.valueOf(f2.lastModified()).compareTo(Long.valueOf(f1.lastModified()));
                    }
                });

                for (File file : resultIAV) {
                    String path = file.getAbsolutePath();
                    resultIAV1.add(path);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return resultIAV1;
    }

}

