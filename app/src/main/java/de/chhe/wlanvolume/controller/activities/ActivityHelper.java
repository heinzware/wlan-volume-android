package de.chhe.wlanvolume.controller.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import java.util.Locale;

import de.chhe.wlanvolume.R;
import de.chhe.wlanvolume.model.entity.WifiVolume;

public class ActivityHelper {

    static final String INTENT_EXTRA_SSID        = "intent.extra.ssid";
    static final String INTENT_EXTRA_WIFI_VOLUME = "intent.extra.wifi.volume";
    static final String INTENT_EXTRA_EDIT_MODE   = "intent.extra.edit.mode";
    static final String INTENT_EXTRA_MAX_VOLUME  = "intent.extra.max.volume";
    static final String INTENT_EXTRA_VOLUME      = "intent.extra.volume";
    static final String INTENT_EXTRA_NOTIFY      = "intent.extra.notify";
    static final String INTENT_EXTRA_COMMENT     = "intent.extra.comment";

    public static final String NOTIFICATION_TAG = "WifiConnectionReceiver.Notification.Tag";
    public static final int NOTIFICATION_ID     = 42;

    public static void showNotification(Context context, WifiVolume wifiVolume, int maxVolume) {
        if (wifiVolume.isShowNotification()) {
            Notification.Builder builder = new Notification.Builder(context)
                    .setContentTitle(String.format(Locale.getDefault(), context.getString(R.string.label_connected_to), wifiVolume.getSsid()))
                    .setContentText(String.format(Locale.getDefault(), context.getString(R.string.label_changed_to), wifiVolume.getVolume(), maxVolume));

            float relativeVol = (float)wifiVolume.getVolume()/(float)maxVolume;
            if (relativeVol == 0.0f) {
                builder.setSmallIcon(R.drawable.ic_volume_mute_white_24dp);
            } else if (relativeVol < .5f) {
                builder.setSmallIcon(R.drawable.ic_volume_down_white_24dp);
            } else {
                builder.setSmallIcon(R.drawable.ic_volume_up_white_24dp);
            }

            //notification.flags |= Notification.FLAG_NO_CLEAR;

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, builder.build());
        }
    }

    public static String trimSsid(String ssid) {
        if (ssid != null && ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }

    public static Intent createWifiVolumeIntent(Context context, boolean editMode, int maxVolume, WifiVolume wifiVolume, String ssid) {
        Intent intent = new Intent(context, WifiVolumeActivity.class);
        if (ssid != null)       intent.putExtra(INTENT_EXTRA_SSID,          ssid);
        if (wifiVolume != null) intent.putExtra(INTENT_EXTRA_WIFI_VOLUME,   wifiVolume);
        intent.putExtra(INTENT_EXTRA_EDIT_MODE, editMode);
        intent.putExtra(INTENT_EXTRA_MAX_VOLUME, maxVolume);
        return intent;
    }
}
