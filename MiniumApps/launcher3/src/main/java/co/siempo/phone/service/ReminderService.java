package co.siempo.phone.service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import co.siempo.phone.R;
import co.siempo.phone.activities.ChooseBackgroundActivity;

/**
What the heck does this do?
 */
public class ReminderService extends IntentService {
    private static  int NOTIFICATION_ID = 1;

    public ReminderService(){
        super("ReminderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        //int notificationId = 1;
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        /////
        //Intent intent = new Intent(this, SplashActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
        //       PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        /////
        NotificationCompat.BigTextStyle bigStyle =
                new NotificationCompat.BigTextStyle();
        bigStyle.setBigContentTitle(intent.getStringExtra("title"));
        bigStyle.bigText(intent.getStringExtra("body"));

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(intent.getStringExtra("title"))
                .setContentText(intent.getStringExtra("body"))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setStyle(bigStyle);


        //  .setContentIntent(pendingIntent);
        if(intent.getStringExtra("type").equals("1")){
            Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("http://wefunder.com/siempo"));
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    NOTIFICATION_ID,
                    intent1,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pendingIntent);

        }
        if(intent.getStringExtra("type").equals("0")) {
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    NOTIFICATION_ID,
                    new Intent(this, ChooseBackgroundActivity.class),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pendingIntent);

        }
        if (NOTIFICATION_ID > 1073741824) {
            NOTIFICATION_ID = 0;
        }


        notificationManager.notify(NOTIFICATION_ID++, mBuilder.build());



    }

}
