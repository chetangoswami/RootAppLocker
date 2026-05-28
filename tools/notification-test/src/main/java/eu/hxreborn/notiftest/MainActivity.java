package eu.hxreborn.notiftest;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.Map;

public class MainActivity extends Activity {
    private static final long DELAY_MS = 10_000L;
    private static final int REQ_NOTIF_PERM = 1;

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        status = findViewById(R.id.status);

        Map<Integer, Runnable> actions = Map.of(
            R.id.btn_request_permission, this::requestNotifPermission,
            R.id.btn_post_alpha_now, () -> post("alpha", () -> NotificationFactory.postAlpha(this)),
            R.id.btn_post_beta_now, () -> post("beta", () -> NotificationFactory.postBeta(this)),
            R.id.btn_post_alpha_delayed, () -> scheduleDelayed(NotificationFactory.TAG_ALPHA),
            R.id.btn_post_beta_delayed, () -> scheduleDelayed(NotificationFactory.TAG_BETA),
            R.id.btn_open_alpha_direct, () -> openDirect(TargetAlphaActivity.class, "alpha-direct"),
            R.id.btn_open_beta_direct, () -> openDirect(TargetBetaActivity.class, "beta-direct")
        );
        actions.forEach((id, run) -> findViewById(id).setOnClickListener(v -> run.run()));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.i("NotifTest", "MainActivity onNewIntent intent=" + intent);
    }

    private void requestNotifPermission() {
        boolean already = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED;
        if (already) {
            setStatus("POST_NOTIFICATIONS granted");
            return;
        }
        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF_PERM);
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (code != REQ_NOTIF_PERM) return;
        boolean granted = results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED;
        setStatus("POST_NOTIFICATIONS " + (granted ? "granted" : "denied"));
    }

    private void post(String tag, Runnable poster) {
        poster.run();
        setStatus("posted " + tag);
    }

    private void openDirect(Class<?> target, String tag) {
        startActivity(new Intent(this, target).putExtra(NotificationFactory.EXTRA_DESTINATION, tag));
    }

    private void scheduleDelayed(String tag) {
        PendingIntent pi = PendingIntent.getBroadcast(
            this,
            tag.hashCode(),
            new Intent(this, DelayedPostReceiver.class)
                .putExtra(DelayedPostReceiver.EXTRA_TAG, tag),
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        getSystemService(AlarmManager.class).set(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + DELAY_MS,
            pi
        );
        setStatus("scheduled " + tag + " +" + (DELAY_MS / 1000) + "s");
    }

    private void setStatus(String msg) {
        status.setText(msg);
        Log.i("NotifTest", msg);
    }
}
