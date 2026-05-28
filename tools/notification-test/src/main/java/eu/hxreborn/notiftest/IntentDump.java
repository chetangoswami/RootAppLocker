package eu.hxreborn.notiftest;

import android.content.Intent;
import android.os.Bundle;

import java.util.StringJoiner;

public final class IntentDump {
    private IntentDump() {
    }

    public static String format(Intent intent) {
        StringJoiner out = new StringJoiner("\n");
        out.add("action=" + intent.getAction());
        out.add("data=" + intent.getData());

        Bundle extras = intent.getExtras();
        if (extras == null || extras.isEmpty()) {
            out.add("(no extras)");
            return out.toString();
        }
        extras.keySet().forEach(k -> out.add(k + "=" + extras.get(k)));
        return out.toString();
    }
}
