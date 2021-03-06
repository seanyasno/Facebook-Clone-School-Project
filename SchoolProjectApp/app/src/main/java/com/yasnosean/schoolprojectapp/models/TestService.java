package com.yasnosean.schoolprojectapp.models;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.yasnosean.schoolprojectapp.R;
import com.yasnosean.schoolprojectapp.activities.MainActivity;
import com.yasnosean.schoolprojectapp.activities.StartActivity;

import static com.yasnosean.schoolprojectapp.models.App.CHANNEL_ID;

public class TestService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getExtras().getString("inputExtra");

        Intent notificationIntent = new Intent(TestService.this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(TestService.this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Title service")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        return START_NOT_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
