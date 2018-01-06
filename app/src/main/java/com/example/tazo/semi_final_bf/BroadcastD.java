package com.example.tazo.semi_final_bf;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Created by Tazo on 2017-04-26.
 */

public class BroadcastD extends BroadcastReceiver {
    String INTENT_ACTION = Intent.ACTION_BOOT_COMPLETED;


    private static String getDevice(){
        try{
            return (String)Build.class.getField("SERIAL").get(null);
        }catch (Exception e){
            return null;
        }
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,new Intent(context,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.min).setTicker("HETT").setWhen(System.currentTimeMillis())
                .setContentTitle("ALARM").setContentText(getDevice())
                .setDefaults(Notification.DEFAULT_SOUND)
                .setVibrate(new long[]{500, 1000, 500, 1000, 500, 1000, 500, 1000, 500, 1000})
                .setContentIntent(pendingIntent).setAutoCancel(true);

        notificationManager.notify(1,builder.build());
    }
}