package de.chhe.wlanvolume;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.Locale;

import de.chhe.wlanvolume.controller.activities.ActivityHelper;
import de.chhe.wlanvolume.controller.activities.MainActivity;
import de.chhe.wlanvolume.model.entity.WifiVolume;
import de.chhe.wlanvolume.model.persistence.DatabaseHelper;

public class WifiConnectionReceiver extends BroadcastReceiver {

    private static final String TAG = WifiConnectionReceiver.class.getSimpleName();

    private static final String EXTRA_UNKNOWN_SSID = "<unknown ssid>";

    public static final String PREFERENCES_NAME             = "WifiConnectionReceiver.Preferences";
    public static final String PREFERENCE_KEY_CONNECTED_TO  = "key.connected.to";
    public static final String PREFERENCE_KEY_CONNECTED     = "key.connected";
    public static final String PREFERENCE_KEY_VOL_OLD       = "key.volume.old";

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (intent != null && intent.hasExtra(WifiManager.EXTRA_NETWORK_INFO)) {

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected() && !networkInfo.getExtraInfo().equals(EXTRA_UNKNOWN_SSID)) {

                String ssid = ActivityHelper.trimSsid(networkInfo.getExtraInfo());

                //start task to check the network and if it's wanted, to change the volume
                new WifiConnectedTask(ssid, context).execute();
                //}
            } else if (!networkInfo.isConnected() && networkInfo.getState() == NetworkInfo.State.DISCONNECTED
                    && networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {

                //restore the old volume, if the user wants it so
                new AsyncTask<Integer, Void, Void>() {
                    @Override
                    protected Void doInBackground(Integer... integers) {
                        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
                        if (prefs.contains(PREFERENCE_KEY_CONNECTED)) {

                            int oldVolume = -1;
                            final String ssid = prefs.getString(PREFERENCE_KEY_CONNECTED_TO, null);

                            //get the old volume that was set before we changed it
                            if (prefs.contains(PREFERENCE_KEY_VOL_OLD)) {
                                oldVolume = prefs.getInt(PREFERENCE_KEY_VOL_OLD, -1);
                            }

                            if (oldVolume != -1 && ssid != null) {
                                WifiVolume wifiVolume = DatabaseHelper.getInstance(context).getWifiVolumeBySsid(ssid);
                                if (wifiVolume != null && wifiVolume.isRestore()) {
                                    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                                    audioManager.setStreamVolume(AudioManager.STREAM_RING, oldVolume, AudioManager.FLAG_VIBRATE);
                                    Log.d(TAG, String.format(Locale.getDefault(), "Disconnected from WiFi network \"%s\". Changed volume from %d to %d.", ssid, wifiVolume.getVolume(), integers[0]));
                                }
                            }

                            //remove values cause we are not connected anymore
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.remove(PREFERENCE_KEY_CONNECTED_TO);
                            editor.remove(PREFERENCE_KEY_CONNECTED);
                            editor.remove(PREFERENCE_KEY_VOL_OLD);
                            editor.commit();
                            Log.d(TAG, String.format(Locale.getDefault(), "Disconnected from WiFi network \"%s\". Removed preferences.", ssid));
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        //remove notification
                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(ActivityHelper.NOTIFICATION_TAG, ActivityHelper.NOTIFICATION_ID);
                    }
                }.execute();
            }
        }
    }


    private static class WifiConnectedTask extends AsyncTask<Void, Void, Integer> {

        private static final int RETURN_CODE_VOLUME_CHANGED           = 0;
        private static final int RETURN_CODE_VOLUME_NOT_CHANGED       = 1;
        private static final int RETURN_CODE_NO_PERMISSION            = 2;
        private final SharedPreferences prefs;

        private String ssid;
        private Context context;
        private WifiVolume wifiVolume;
        private int maxVolume;

        WifiConnectedTask(@NonNull String ssid, @NonNull Context context) {
            this.ssid = ssid;
            this.context = context;
            this.prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            if (ssid != null && (!prefs.contains(PREFERENCE_KEY_CONNECTED) || !prefs.getBoolean(PREFERENCE_KEY_CONNECTED,false))) {

                //save in preferences that the device is connected
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(PREFERENCE_KEY_CONNECTED, true);
                editor.commit();

                //check if the user wants to change the volume, when we are connected to this SSID
                wifiVolume = DatabaseHelper.getInstance(context).getWifiVolumeBySsid(ssid);
                if (wifiVolume != null) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        //check if DnD mode enabled
                        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                        if (notificationManager.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALL
                                && wifiVolume.isEndDnd()) {
                                if (!notificationManager.isNotificationPolicyAccessGranted()) {
                                    return RETURN_CODE_NO_PERMISSION;
                                }
                        }
                    }

                    //get the audio-manager, the max-volume and the current volume
                    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
                    int oldVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);

                    //set the new volume
                    //TODO: check if dnd is enabled
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, wifiVolume.getVolume(), AudioManager.FLAG_VIBRATE);

                    //save the old volume so we can restore it on disconnect
                    editor.putInt(PREFERENCE_KEY_VOL_OLD, oldVolume);
                    editor.putString(PREFERENCE_KEY_CONNECTED_TO, ssid);
                    editor.commit();

                    Log.d(TAG, String.format(Locale.getDefault(), "Connected to WiFi network \"%s\". Changed volume from %d to %d.", ssid, oldVolume, wifiVolume.getVolume()));

                    return RETURN_CODE_VOLUME_CHANGED;
                }
            }
            return RETURN_CODE_VOLUME_NOT_CHANGED;
        }

        @Override
        protected void onPostExecute(Integer returnCode) {
            //show notification if the user wants so
            if (RETURN_CODE_VOLUME_CHANGED == returnCode) {
                ActivityHelper.showNotification(context, wifiVolume, maxVolume);
            }
            if (RETURN_CODE_NO_PERMISSION == returnCode) {
                //create notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                builder.setSmallIcon(R.drawable.ic_volume_up_white_24dp);
                builder.setAutoCancel(true);
                builder.setContentTitle(context.getString(R.string.app_name));
                builder.setContentText(context.getString(R.string.label_no_notify_permission));
                Intent resultIntent = new Intent(context, MainActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addParentStack(MainActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(resultPendingIntent);

                //show notification
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(ActivityHelper.NOTIFICATION_TAG, ActivityHelper.NOTIFICATION_ID, builder.build());
            }
        }
    }
}
