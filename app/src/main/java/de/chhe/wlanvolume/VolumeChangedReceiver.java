package de.chhe.wlanvolume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BroadcastReceiver to receive Intents when the volume of the device is changed by the user.
 * <p>
 * <b>Note:</b> The action <i>android.media.VOLUME_CHANGED_ACTION</i> is undocumented behavior, so
 * it is not guaranteed that every device sends it.
 *
 */
public class VolumeChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null && "android.media.VOLUME_CHANGED_ACTION".equals(intent.getAction())) {

        }
    }
}
