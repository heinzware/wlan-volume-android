package de.chhe.wlanvolume;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import de.chhe.wlanvolume.controller.activities.ActivityHelper;
import de.chhe.wlanvolume.model.entity.WifiVolume;
import de.chhe.wlanvolume.model.persistence.DatabaseHelper;

public class WifiConnectionReceiver extends BroadcastReceiver {

    private static final String TAG = WifiConnectionReceiver.class.getSimpleName();

    private static final String EXTRA_UNKNOWN_SSID = "<unknown ssid>";

    private static final String PREFERENCES_NAME         = "WifiConnectionReceiver.Preferences";
    private static final String PREFERENCE_KEY_CONNECTED = "key.connected";

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (intent != null && intent.hasExtra(WifiManager.EXTRA_NETWORK_INFO)) {

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

            if (networkInfo.isConnected() && !networkInfo.getExtraInfo().equals(EXTRA_UNKNOWN_SSID)) {

                String ssid = ActivityHelper.trimSsid(networkInfo.getExtraInfo());

                if (!prefs.contains(PREFERENCE_KEY_CONNECTED) || !ssid.equals(prefs.getString(PREFERENCE_KEY_CONNECTED,EXTRA_UNKNOWN_SSID))) {

                    //save in preferences that the device is connected to the given SSID
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(PREFERENCE_KEY_CONNECTED, ssid);
                    editor.commit();

                    //start task to check the network and if it's wanted, to change the volume
                    new WifiConnectedTask(ssid, context).execute();
                }
            } else {

                if (prefs.contains(PREFERENCE_KEY_CONNECTED)) {

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove(PREFERENCE_KEY_CONNECTED);
                    editor.commit();

                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(ActivityHelper.NOTIFICATION_TAG, ActivityHelper.NOTIFICATION_ID);
                }
            }
        }
    }

    private static class WifiConnectedTask extends AsyncTask<Void, Void, Integer> {

        private static final int RETURN_CODE_VOLUME_CHANGED           = 0;
        private static final int RETURN_CODE_VOLUME_NOT_CHANGED       = 1;

        private String ssid;
        private Context context;
        private WifiVolume wifiVolume;
        private int maxVolume;

        WifiConnectedTask(@NonNull String ssid, @NonNull Context context) {
            this.ssid = ssid;
            this.context = context;
        }

        @Override
        protected Integer doInBackground(Void... voids) {

            //check if the user wants to change the volume, when we are connected to this SSID
            wifiVolume = DatabaseHelper.getInstance(context).getWifiVolumeBySsid(ssid);
            if (wifiVolume != null) {
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
                audioManager.setStreamVolume(AudioManager.STREAM_RING, wifiVolume.getVolume(), AudioManager.FLAG_VIBRATE);
                return RETURN_CODE_VOLUME_CHANGED;
            }
            return RETURN_CODE_VOLUME_NOT_CHANGED;
        }

        @Override
        protected void onPostExecute(Integer returnCode) {

            if (RETURN_CODE_VOLUME_CHANGED == returnCode) {

                ActivityHelper.showNotification(context, wifiVolume, maxVolume);
            }
        }
    }
}
