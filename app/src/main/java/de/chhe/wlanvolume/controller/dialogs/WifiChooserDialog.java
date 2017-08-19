package de.chhe.wlanvolume.controller.dialogs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;

import java.util.ArrayList;
import java.util.List;

import de.chhe.wlanvolume.R;
import de.chhe.wlanvolume.controller.activities.ActivityHelper;
import de.chhe.wlanvolume.controller.activities.MainActivity;


public class WifiChooserDialog extends AlertDialog.Builder {

    public WifiChooserDialog(final MainActivity mainActivity, final List<WifiConfiguration> knownNetworks) {
        super(mainActivity, R.style.AppTheme_Dialog);

        if (knownNetworks == null || knownNetworks.size() == 0) {
            setTitle(R.string.label_wifi_results);
            setMessage(R.string.label_wifi_no_result);
            setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });
        } else {
            setTitle(R.string.label_choose_wifi);
            setCancelable(false);
            final CharSequence[] ssids = getSsids(knownNetworks);
            setSingleChoiceItems(ssids, 0, null);
            setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                    int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    mainActivity.startActivity(ActivityHelper.createWifiVolumeIntent(mainActivity, true, mainActivity.getMaxVolume(), null, ssids[position].toString()));
                }
            });
            setNegativeButton(R.string.label_cancel, ActivityHelper.dialogDismissListener);
        }
    }

    private CharSequence[] getSsids(List<WifiConfiguration> knownNetworks) {
        List<CharSequence> ssidList = new ArrayList<>();
        for (WifiConfiguration config : knownNetworks) {
            if (!ssidList.contains(config.SSID)) {
                ssidList.add(config.SSID);
            }
        }
        return ssidList.toArray(new CharSequence[ssidList.size()]);
    }

}
