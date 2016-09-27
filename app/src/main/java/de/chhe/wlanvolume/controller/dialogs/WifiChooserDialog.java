package de.chhe.wlanvolume.controller.dialogs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.List;

import de.chhe.wlanvolume.R;
import de.chhe.wlanvolume.controller.activities.ActivityHelper;
import de.chhe.wlanvolume.controller.activities.MainActivity;
import de.chhe.wlanvolume.controller.activities.WifiVolumeActivity;


public class WifiChooserDialog extends AlertDialog.Builder {

    private List<ScanResult> scanResults;

    public WifiChooserDialog(final MainActivity mainActivity, final List<ScanResult> scanResults) {
        super(mainActivity, R.style.AppTheme_Dialog);

        if (scanResults == null || scanResults.size() == 0) {
            setTitle(R.string.label_wifi_results);
            setMessage(R.string.label_wifi_no_result);
            setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });
        } else {
            this.scanResults = scanResults;
            setTitle(R.string.label_choose_wifi);
            setCancelable(false);
            setSingleChoiceItems(getSsids(), 0, null);
            setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                    int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                    Intent wifiVolumeIntent = new Intent(mainActivity, WifiVolumeActivity.class);
                    wifiVolumeIntent.putExtra(ActivityHelper.INTENT_EXTRA_SSID, scanResults.get(selectedPosition).SSID);
                    wifiVolumeIntent.putExtra(ActivityHelper.INTENT_EXTRA_EDIT_MODE, true);
                    wifiVolumeIntent.putExtra(ActivityHelper.INTENT_EXTRA_MAX_VOLUME, mainActivity.getMaxVolume());
                    mainActivity.startActivity(wifiVolumeIntent);
                }
            });
            setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });
        }
    }

    private CharSequence[] getSsids() {
        List<CharSequence> ssidList = new ArrayList<>();
        List<ScanResult> remove = new ArrayList<>();
        for (ScanResult scanResult : scanResults) {
            if (!ssidList.contains(scanResult.SSID)) {
                ssidList.add(scanResult.SSID);
            } else {
                remove.add(scanResult);
            }
        }
        scanResults.removeAll(remove);
        return ssidList.toArray(new CharSequence[ssidList.size()]);
    }

}