package de.chhe.wlanvolume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class VolumeChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null && "android.media.VOLUME_CHANGED_ACTION".equals(intent.getAction())) {

        }

        throw new UnsupportedOperationException("Not yet implemented");
    }
}
