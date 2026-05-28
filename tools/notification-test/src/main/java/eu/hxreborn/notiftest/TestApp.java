package eu.hxreborn.notiftest;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

public class TestApp extends Application {
    public static final String CHANNEL_ID = "notiftest.default";

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(getString(R.string.channel_description));
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }
}
