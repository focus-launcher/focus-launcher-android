package io.focuslauncher.phone.token;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import io.focuslauncher.BuildConfig;
import io.focuslauncher.R;
import io.focuslauncher.phone.activities.DashboardActivity;
import io.focuslauncher.phone.app.Constants;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.event.SendSmsEvent;
import io.focuslauncher.phone.helper.ActivityHelper;
import io.focuslauncher.phone.log.Tracer;
import io.focuslauncher.phone.models.MainListItem;
import io.focuslauncher.phone.msg.SmsObserver;
import io.focuslauncher.phone.utils.DataUtils;
import io.focuslauncher.phone.utils.PrefSiempo;
import io.focuslauncher.phone.utils.UIUtils;
import de.greenrobot.event.EventBus;


@EBean
public class TokenRouter {

    String SMS_SENT = "SMS_SENT";
    String SMS_DELIVERED = "SMS_DELIVERED";


    void route() {
        EventBus.getDefault().post(new TokenUpdateEvent());
    }

    public void setCurrent(TokenItem tokenItem) {
        TokenManager.getInstance().setCurrent(tokenItem);
        route();
    }

    public void add(TokenItem tokenItem) {
        TokenManager.getInstance().getCurrent().setCompleteType(TokenCompleteType.FULL);
        TokenManager.getInstance().add(tokenItem);
        route();
    }

    public void createNote(Context context) {
        DataUtils.saveNotes(context, TokenManager.getInstance().getCurrent().getTitle());
    }


    public void createContact(Context context) {
        String inputStr = TokenManager.getInstance().getCurrent().getTitle();
        if (BuildConfig.FLAVOR.equalsIgnoreCase(context.getString(R.string.beta)) && inputStr.equalsIgnoreCase(Constants.ALPHA_SETTING)) {
            if (PrefSiempo.getInstance(context).read(PrefSiempo
                    .IS_ALPHA_SETTING_ENABLE, false)) {
                if (PhoneNumberUtils.isGlobalPhoneNumber(inputStr)) {
                    context.startActivity(new Intent(Intent.ACTION_INSERT).setType(ContactsContract.Contacts.CONTENT_TYPE).putExtra(ContactsContract.Intents.Insert.PHONE, inputStr));
                } else {
                    context.startActivity(new Intent(Intent.ACTION_INSERT).setType(ContactsContract.Contacts.CONTENT_TYPE).putExtra(ContactsContract.Intents.Insert.NAME, inputStr));
                }
                TokenManager.getInstance().clear();
            } else {
                PrefSiempo.getInstance(context).write(PrefSiempo
                        .IS_ALPHA_SETTING_ENABLE, true);
                new ActivityHelper(context).openSiempoAlphaSettingsApp();
                TokenManager.getInstance().clear();
            }
        } else {
            if (PhoneNumberUtils.isGlobalPhoneNumber(inputStr)) {
                context.startActivity(new Intent(Intent.ACTION_INSERT).setType(ContactsContract.Contacts.CONTENT_TYPE).putExtra(ContactsContract.Intents.Insert.PHONE, inputStr));
            } else {
                context.startActivity(new Intent(Intent.ACTION_INSERT).setType(ContactsContract.Contacts.CONTENT_TYPE).putExtra(ContactsContract.Intents.Insert.NAME, inputStr));
            }
            TokenManager.getInstance().clear();
        }

    }

    public void contactPicked(MainListItem item) {
        if (item.hasMultipleNumber()) {
            TokenManager.getInstance().setCurrent(new TokenItem(TokenItemType.CONTACT));
            TokenManager.getInstance().getCurrent().setTitle(item.getContactName());
            TokenManager.getInstance().getCurrent().setExtra1(String.valueOf(item.getContactId()));
            TokenManager.getInstance().getCurrent().setCompleteType(TokenCompleteType.HALF);
            route();
        } else {
            TokenManager.getInstance().setCurrent(new TokenItem(TokenItemType.CONTACT));
            TokenManager.getInstance().getCurrent().setTitle(item.getContactName());
            TokenManager.getInstance().getCurrent().setExtra1(String.valueOf(item.getContactId()));
            TokenManager.getInstance().getCurrent().setExtra2(item.getNumber().getNumber());
            TokenManager.getInstance().getCurrent().setCompleteType(TokenCompleteType.FULL);
            contactPickedDone();
        }
    }

    public void contactNumberPicked(MainListItem item) {
        TokenManager.getInstance().getCurrent().setExtra2(item.getTitle());
        TokenManager.getInstance().getCurrent().setCompleteType(TokenCompleteType.FULL);
        contactPickedDone();
    }

    private void contactPickedDone() {
        if (TokenManager.getInstance().hasCompleted(TokenItemType.DATA) && TokenManager.getInstance().hasCompleted(TokenItemType.CONTACT)) {
            TokenManager.getInstance().add(new TokenItem(TokenItemType.END_OP));
        } else if (TokenManager.getInstance().hasCompleted(TokenItemType.CONTACT)) {
            TokenManager.getInstance().add(new TokenItem(TokenItemType.DATA));
        }
        route();
    }

    public void sendText(Context context) {
        try {
            if (TokenManager.getInstance().hasCompleted(TokenItemType.CONTACT) && TokenManager.getInstance().has(TokenItemType.DATA)) {
                String strNumber = TokenManager.getInstance().get(TokenItemType.CONTACT).getExtra2();
                String strTitle = TokenManager.getInstance().get(TokenItemType.CONTACT).getTitle();
                String strMessage = TokenManager.getInstance().get(TokenItemType.DATA).getTitle();
                Log.e("Message string", strMessage);
                if (!strMessage.trim().equalsIgnoreCase("")) {
                    if ((!DashboardActivity.isTextLenghGreater.contains(strTitle))) {
                        UIUtils.toast(context, "Please choose a contact.");

                        List<TokenItem> itemList = TokenManager.getInstance().getItems();
                        if (itemList.size() == 3) {
                            itemList.remove(TokenManager.getInstance().get(TokenItemType.END_OP));
                            //itemList.remove(TokenManager.getInstance().get(TokenItemType
                            // .CONTACT));
                            TokenManager.getInstance().get(TokenItemType.CONTACT).setTitle("@");
                            TokenManager.getInstance().get(TokenItemType.CONTACT).setExtra1("");
                            TokenManager.getInstance().get(TokenItemType.CONTACT).setExtra2("");
                            TokenManager.getInstance().get(TokenItemType.CONTACT).setCompleteType
                                    (TokenCompleteType.HALF);
                        }
                        if (itemList.size() == 2) {
                            itemList.remove(TokenManager.getInstance().get(TokenItemType.CONTACT));
                            String newMsg = DashboardActivity.isTextLenghGreater;
                            if (newMsg.endsWith(" ")) {
                                newMsg = newMsg.substring(0, newMsg.length() - 1);
                            }
                            TokenManager.getInstance().get(TokenItemType.DATA).setTitle(newMsg);
                            if (TokenManager.getInstance().get(TokenItemType.DATA).getCompleteType
                                    () != TokenCompleteType.FULL) {
                                TokenManager.getInstance().get(TokenItemType.DATA).setCompleteType(TokenCompleteType.FULL);
                            }
                            add(new TokenItem(TokenItemType.CONTACT));
                        }
                    } else if (!DashboardActivity.isTextLenghGreater.contains(strMessage)) {
                        UIUtils.toast(context, "Please enter message.");
                        List<TokenItem> itemList = TokenManager.getInstance().getItems();
                        itemList.remove(TokenManager.getInstance().get(TokenItemType.DATA));
                        if (itemList.contains(TokenManager.getInstance().get(TokenItemType.END_OP))) {
                            itemList.remove(TokenManager.getInstance().get(TokenItemType.END_OP));
                        }
                        itemList.add(new TokenItem(TokenItemType.DATA));
                        setCurrent(itemList.get(itemList.size() - 1));
                    } else {
                        new SmsObserver(context, strNumber, strMessage).start();
                        SmsManager smsManager = SmsManager.getDefault();

                        //Added as a part of SSA-1479, in case of long text
                        // messages, splitting the text and sending the message
                        // in multi-part
                        PendingIntent sentPendingIntent = PendingIntent
                                .getBroadcast(context, 0, new Intent(SMS_SENT),
                                        0);
                        PendingIntent deliveredPendingIntent = PendingIntent
                                .getBroadcast(context, 0, new Intent
                                        (SMS_DELIVERED), 0);
                        ArrayList<String> smsBodyParts = smsManager
                                .divideMessage(strMessage);
                        ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
                        ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<PendingIntent>();

                        for (int i = 0; i < smsBodyParts.size(); i++) {
                            sentPendingIntents.add(sentPendingIntent);
                            deliveredPendingIntents.add(deliveredPendingIntent);
                        }

                        smsManager.sendMultipartTextMessage(strNumber, null,
                                smsBodyParts, sentPendingIntents,
                                deliveredPendingIntents);


                        Toast.makeText(context, "Sending Message...", Toast.LENGTH_LONG).show();
                        String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(context);


                        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + Uri.encode(strNumber)));
                        if (defaultSmsPackageName != null) // Can be null in case that there is no default, then the user would be able to choose any app that supports this intent.
                        {
                            intent.setPackage(defaultSmsPackageName);
                        }
                        context.startActivity(intent);
                        EventBus.getDefault().post(new SendSmsEvent(true));
                    }
                } else {
                    UIUtils.toast(context, "Please enter message.");
                }
            } else if (!TokenManager.getInstance().has(TokenItemType.CONTACT)) {
                TokenManager.getInstance().getCurrent().setCompleteType(TokenCompleteType.FULL);
                TokenManager.getInstance().add(new TokenItem(TokenItemType.CONTACT));
                route();
            }
        } catch (Exception e) {
            CoreApplication.getInstance().logException(e);
            Tracer.e(e, e.getMessage());
        }
    }

    public void call(Activity activity) {

        /*if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }*/
        try {
            activity.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + TokenManager.getInstance().get(TokenItemType.CONTACT).getExtra2())));
        } catch (Exception e) {
            CoreApplication.getInstance().logException(e);
            e.printStackTrace();
            UIUtils.alert(activity, activity.getString(R.string.app_not_found));
        }
    }
}
