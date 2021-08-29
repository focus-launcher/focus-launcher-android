package io.focuslauncher.phone.util;

import android.Manifest;
import android.content.Context;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

import io.focuslauncher.R;
import io.focuslauncher.phone.helper.FirebaseHelper;
import io.focuslauncher.phone.main.MainFragmentMediator;
import io.focuslauncher.phone.token.TokenItem;
import io.focuslauncher.phone.token.TokenItemType;
import io.focuslauncher.phone.token.TokenRouter;
import io.focuslauncher.phone.utils.PermissionUtil;
import io.focuslauncher.phone.utils.UIUtils;

/**
 * Created by roma on 16/4/18.
 */

public class ContactSmsPermissionHelper {


    private boolean isFromTokenParser;
    private TokenRouter router;
    private Context context;
    private PermissionUtil permissionUtil;
    private MainFragmentMediator mediator;
    private String messageData;
    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            if (isFromTokenParser) {
                router.setCurrent(new TokenItem(TokenItemType.CONTACT));
            } else {
                mediator.loadContacts();
                if (router != null) {
                    router.sendText(context);
                    FirebaseHelper.getInstance()
                            .logIFAction
                                    (FirebaseHelper
                                            .ACTION_SMS, "", messageData);
                }
            }

        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            UIUtils.toast(context, "Permission denied");
            //If needed to call the permission again on deny, uncomment the
            // below code
            //askForPermission(Constants.PERMISSIONS);
        }
    };


    public ContactSmsPermissionHelper(TokenRouter router, Context context,
                                      MainFragmentMediator mediator, boolean
                                              isFromTokenParser, String data) {
        this.router = router;
        this.context = context;
        this.mediator = mediator;
        this.isFromTokenParser = isFromTokenParser;
        this.messageData = data;

        permissionUtil = new PermissionUtil(context);

    }

    public void checkForContactAndSMSPermission() {

        if (permissionUtil.hasGiven(PermissionUtil.CONTACT_PERMISSION)
                /*&& permissionUtil.hasGiven(PermissionUtil.SEND_SMS_PERMISSION)*/
                ) {
            if (isFromTokenParser) {
                router.setCurrent(new TokenItem(TokenItemType.CONTACT));
            } else {
                mediator.loadContacts();
                if (router != null) {
                    router.sendText(context);
                    FirebaseHelper.getInstance().logIFAction(FirebaseHelper
                            .ACTION_SMS, "", messageData);
                }


            }

        } else {

            if (!permissionUtil.hasGiven(PermissionUtil.CONTACT_PERMISSION)
                    /*&& !permissionUtil.hasGiven(PermissionUtil.SEND_SMS_PERMISSION)*/
                    ) {
                try {
                    TedPermission.with(context)
                            .setPermissionListener(permissionlistener)
                            .setDeniedMessage(R.string.msg_permission_denied)
                            .setPermissions(new String[]{
                                    Manifest.permission.READ_CONTACTS,
                                    Manifest.permission.WRITE_CONTACTS
                                    /*Manifest.permission.RECEIVE_SMS,
                                    Manifest.permission.SEND_SMS,
                                    Manifest.permission.READ_SMS*/})
                            .check();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else if (!permissionUtil.hasGiven(PermissionUtil.CONTACT_PERMISSION)
                    /*&& permissionUtil.hasGiven(PermissionUtil.SEND_SMS_PERMISSION)*/
                    ) {


                try {
                    TedPermission.with(context)
                            .setPermissionListener(permissionlistener)
                            .setDeniedMessage(R.string.msg_permission_denied)
                            .setPermissions(new String[]{
                                    Manifest.permission.READ_CONTACTS,
                                    Manifest.permission.WRITE_CONTACTS})
                            .check();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }/* else if (!permissionUtil.hasGiven(PermissionUtil.SEND_SMS_PERMISSION)
                    *//*&& permissionUtil.hasGiven(PermissionUtil.CONTACT_PERMISSION)*//*
                    ) {

                try {
                    TedPermission.with(context)
                            .setPermissionListener(permissionlistener)
                            .setDeniedMessage(R.string.msg_permission_denied)
                            .setPermissions(new String[]{
                                    Manifest.permission.RECEIVE_SMS,
                                    Manifest.permission.SEND_SMS,
                                    Manifest.permission.READ_SMS})
                            .check();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }*/
        }
    }
}
