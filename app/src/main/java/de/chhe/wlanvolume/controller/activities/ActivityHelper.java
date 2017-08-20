package de.chhe.wlanvolume.controller.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

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
    static final String INTENT_EXTRA_RESTORE     = "intent.extra.restore";
    static final String INTENT_EXTRA_END_DND     = "intent.extra.end.dnd";

    public static final String NOTIFICATION_TAG = "WifiConnectionReceiver.Notification.Tag";
    public static final int NOTIFICATION_ID     = 42;

    public static DialogInterface.OnClickListener dialogDismissListener= new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int i) {
            dialog.dismiss();
        }
    };

    /**
     * Static method to show a notification when the ringer volume was changed.
     * <p>
     * <b>Note:</b> The method checks if a notification should be shown using the <b>restore</b> field of the {@link WifiVolume} parameter.
     *
     * @param context Context used to show the notification.
     * @param wifiVolume {@link WifiVolume} object specifying the WiFi network and the new ringer volume.
     * @param maxVolume Maximum ringer volume of the device.
     */
    public static void showNotification(@NonNull Context context, @NonNull WifiVolume wifiVolume, int maxVolume) {
        if (wifiVolume.isShowNotification()) {

            //create notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setContentTitle(String.format(Locale.getDefault(), context.getString(R.string.label_connected_to), wifiVolume.getSsid()));
            builder.setContentText(String.format(Locale.getDefault(), context.getString(R.string.label_changed_to), wifiVolume.getVolume(), maxVolume));

            //select icon by volume
            float relativeVol = (float)wifiVolume.getVolume()/(float)maxVolume;
            if (relativeVol == 0.0f) {
                builder.setSmallIcon(R.drawable.ic_volume_mute_white_24dp);
            } else if (relativeVol < .5f) {
                builder.setSmallIcon(R.drawable.ic_volume_down_white_24dp);
            } else {
                builder.setSmallIcon(R.drawable.ic_volume_up_white_24dp);
            }

            //show notification
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, builder.build());
        }
    }

    /**
     * Static method to remove quotation marks surrounding a SSID.
     *
     * @param ssid SSID that might be surrounded by quotation marks
     * @return SSID without quotation marks
     */
    @Nullable
    public static String trimSsid(@Nullable String ssid) {
        if (ssid != null && ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }

    /**
     * Static method to create an {@link Intent} to start a {@link WifiVolumeActivity} with the given extras.
     * <p>
     * <b>Note</b>: Either the  {@link WifiVolume} parameter or the SSID parameter must be nonnull.
     *
     * @param context Context used to start the {@link WifiVolumeActivity}.
     * @param editMode Extra, specifying if the user can edit the values in the opened {@link WifiVolumeActivity}.
     * @param maxVolume Extra, specifying the maximum ringer volume of the device.
     * @param wifiVolume The {@link WifiVolume} object that should be displayed.
     * @param ssid The SSID that should be displayed.
     * @return Intent that can be used to start a {@link WifiVolumeActivity} with the given extras.
     */
    @NonNull
    public static Intent createWifiVolumeIntent(@NonNull Context context, boolean editMode, int maxVolume, WifiVolume wifiVolume, String ssid) {
        Intent intent = new Intent(context, WifiVolumeActivity.class);
        if (ssid != null)       intent.putExtra(INTENT_EXTRA_SSID,          ssid);
        if (wifiVolume != null) intent.putExtra(INTENT_EXTRA_WIFI_VOLUME,   wifiVolume);
        intent.putExtra(INTENT_EXTRA_EDIT_MODE, editMode);
        intent.putExtra(INTENT_EXTRA_MAX_VOLUME, maxVolume);
        return intent;
    }
}
