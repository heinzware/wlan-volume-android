package de.chhe.wlanvolume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;


import de.chhe.wlanvolume.model.entity.WlanVolume;
import de.chhe.wlanvolume.model.persistence.DatabaseHelper;

public class WifiConnectionReceiver extends BroadcastReceiver {

    private static final String TAG = WifiConnectionReceiver.class.getSimpleName();

    private static final String EXTRA_UNKNOWN_SSID = "<unknown ssid>";

    private static final String PREFERENCES_NAME         = "WifiConnectionReceiver.Preferences";
    private static final String PREFERENCE_KEY_CONNECTED = "key.connected";

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (intent != null) {

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected() && !networkInfo.getExtraInfo().equals(EXTRA_UNKNOWN_SSID)) {

                SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
                String ssid = networkInfo.getExtraInfo();

                if (!prefs.contains(PREFERENCE_KEY_CONNECTED) || !ssid.equals(prefs.getString(PREFERENCE_KEY_CONNECTED,EXTRA_UNKNOWN_SSID))) {

                    //save in preferences that the device is connected to the given SSID
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(PREFERENCE_KEY_CONNECTED, ssid);
                    editor.commit();

                    //start task to check the network and if it's wanted, to change the volume
                    new WifiConnectedTask(ssid, context).execute();
                }
            } else {
                SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
                if(prefs.contains(PREFERENCE_KEY_CONNECTED)){
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove(PREFERENCE_KEY_CONNECTED);
                    editor.commit();
                }
            }
        }
    }

    private static class WifiConnectedTask extends AsyncTask<Void, Void, Integer> {

        private static final int RETURN_CODE_CONNECTED      = 0;
        private static final int RETURN_CODE_NOT_CONNECTED  = 1;

        private String ssid;
        private Context context;

        WifiConnectedTask(@NonNull String ssid, @NonNull Context context) {
            this.ssid = ssid;
            this.context = context;
        }

        @Override
        protected Integer doInBackground(Void... voids) {

            //check if the user wants to change the volume, when we are connected to this SSID
            WlanVolume wlanVolume = DatabaseHelper.getInstance(context).getWlanVolumeBySsid(ssid);
            if (wlanVolume != null) {
                Log.d(TAG, "The user wants to change the volume for the network " + ssid + " to " + wlanVolume.getVolume());
            }
            return null;
        }
    }
}
