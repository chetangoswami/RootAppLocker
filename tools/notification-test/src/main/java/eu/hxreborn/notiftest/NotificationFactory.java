package eu.hxreborn.notiftest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public final class NotificationFactory {
    public static final int ID_ALPHA = 1001;
    public static final int ID_BETA = 1002;

    public static final String EXTRA_DESTINATION = "destination";
    public static final String EXTRA_POSTED_AT = "posted_at_ms";
    public static final String EXTRA_REQUEST_ID = "request_id";

    public static final String TAG_ALPHA = "alpha";
    public static final String TAG_BETA = "beta";

    private NotificationFactory() {
    }

    public static void postAlpha(Context ctx) {
        post(ctx, ID_ALPHA, TAG_ALPHA, TargetAlphaActivity.class,
            "Open Target Alpha", "Tap to land on TargetAlphaActivity");
    }

    public static void postBeta(Context ctx) {
        post(ctx, ID_BETA, TAG_BETA, TargetBetaActivity.class,
            "Open Target Beta", "Tap to land on TargetBetaActivity");
    }

    private static void post(
        Context ctx,
        int notifId,
        String destinationTag,
        Class<?> targetActivity,
        String title,
        String text
    ) {
        long now = System.currentTimeMillis();
        Intent intent = new Intent(ctx, targetActivity)
            .setAction(Intent.ACTION_VIEW)
            .putExtra(EXTRA_DESTINATION, destinationTag)
            .putExtra(EXTRA_POSTED_AT, now)
            .putExtra(EXTRA_REQUEST_ID, Long.toHexString(now));

        PendingIntent pi = PendingIntent.getActivity(
            ctx,
            notifId,
            intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification n = new Notification.Builder(ctx, TestApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build();

        ctx.getSystemService(NotificationManager.class).notify(notifId, n);
    }
}
