package com.example.ajay.cs125_final_app;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

public class ItemAlertService extends Service {
    private final String CHANNELNAME = "QuicklistAlertService";
    private final String CHANNELDESC = "QuicklistAlertService";

    public ItemAlertService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(this, "henlo", Toast.LENGTH_SHORT).show();

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNELNAME)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Quicklist")
                .setContentText("Quicklist is monitoring your location")
                .setContentIntent(pendingIntent)
                .setTicker("ticker text").build();

        startForeground(1337, notification);
        NotificationManagerCompat.from(this).notify(1337, notification);

        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNELNAME, CHANNELNAME, importance);
            channel.setDescription(CHANNELDESC);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
