package com.example.ajay.cs125_final_app;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;

import com.example.lib.Item;
import com.example.lib.ItemList;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;


public class ItemAlertService extends Service {
    private final String CHANNELNAME = "QuicklistAlertService";
    private final String CHANNELDESC = "QuicklistAlertService";

    private List<ItemList> lists;
    private FusedLocationProviderClient client;

    public ItemAlertService() {
    }

    private void loadLists() {
        SharedPreferences prefs = this.getSharedPreferences(this.getPackageName(),
                this.getApplicationContext().MODE_PRIVATE);
        String json = prefs.getString(ListManager.SHARED_PREFERENCES_KEY, new Gson().toJson(new ArrayList()));
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(json).getAsJsonArray();

        lists = new ArrayList<>();

        for (int i = 0; i < array.size(); i++)
            lists.add(gson.fromJson(array.get(i), ItemList.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        client = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        client.requestLocationUpdates(new LocationRequest(), new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                loadLists();
                Location loc = locationResult.getLastLocation();
                for (ItemList list : lists) {
                    for (Item item : list.getItems()) {
                        if (!item.hasLocation() || item.isCompleted()) continue;
                        float[] results = new float[1];
                        Location.distanceBetween(loc.getLatitude(), loc.getLongitude(),
                                item.getLatitude(), item.getLongitude(), results);
                        float dist = results[0];
                        if (dist < 30) {
                            Intent notificationIntent = new Intent(ItemAlertService.this, MainActivity.class);

                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                                    notificationIntent, 0);

                            Notification notification = new NotificationCompat.Builder(ItemAlertService.this, CHANNELNAME)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle("Quicklist")
                                    .setContentText(String.format("Location alert: %s", item.getName()))
                                    .setContentIntent(pendingIntent)
                                    .build();
                            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(1338, notification);
                        }
                    }
                }
            }
        }, null);
    }

    @Override
    public IBinder onBind(Intent intent) {
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
