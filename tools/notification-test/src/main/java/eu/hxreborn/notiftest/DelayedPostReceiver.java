package eu.hxreborn.notiftest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Map;
import java.util.function.Consumer;

public class DelayedPostReceiver extends BroadcastReceiver {
    public static final String EXTRA_TAG = "tag";

    private static final Map<String, Consumer<Context>> POSTERS = Map.of(
        NotificationFactory.TAG_ALPHA, NotificationFactory::postAlpha,
        NotificationFactory.TAG_BETA, NotificationFactory::postBeta
    );

    @Override
    public void onReceive(Context context, Intent intent) {
        String tag = intent.getStringExtra(EXTRA_TAG);
        Log.i("NotifTest", "DelayedPostReceiver fired tag=" + tag);
        Consumer<Context> poster = POSTERS.get(tag);
        if (poster != null) poster.accept(context);
    }
}
