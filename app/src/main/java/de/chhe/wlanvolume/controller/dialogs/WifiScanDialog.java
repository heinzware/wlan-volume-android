package de.chhe.wlanvolume.controller.dialogs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import de.chhe.wlanvolume.R;
import de.chhe.wlanvolume.controller.activities.MainActivity;


public class WifiScanDialog extends AlertDialog.Builder {

    public WifiScanDialog(final MainActivity mainActivity) {
        super(mainActivity, R.style.AppTheme_Dialog);
        setTitle(R.string.label_scanning_wifi);
        setContentView(mainActivity);
        setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                mainActivity.unregisterWifiScanReceiver();
                dialog.dismiss();
            }
        });
        setCancelable(false);
    }

    private void setContentView(MainActivity mainActivity) {
        View dialogView = LayoutInflater.from(mainActivity).inflate(R.layout.dialog_wifi_scan, null);
        ImageView imageView = (ImageView) dialogView.findViewById(R.id.imageView);
        imageView.setBackgroundResource(R.drawable.wifi_scan_animation);
        AnimationDrawable animation = (AnimationDrawable) imageView.getBackground();
        animation.start();
        setView(dialogView);
    }

}
