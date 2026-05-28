package eu.hxreborn.notiftest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class TargetAlphaActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_alpha);
        render(getIntent(), "onCreate");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        render(intent, "onNewIntent");
    }

    private void render(Intent intent, String origin) {
        String dump = IntentDump.format(intent);
        Log.i("NotifTest", "TargetAlpha " + origin + " " + dump.replace('\n', ' '));
        ((TextView) findViewById(R.id.intent_dump)).setText(dump);
    }
}
