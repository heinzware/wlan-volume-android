package de.chhe.wlanvolume.controller.dialogs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.Locale;

import de.chhe.wlanvolume.R;
import de.chhe.wlanvolume.controller.activities.ActivityHelper;
import de.chhe.wlanvolume.controller.activities.MainActivity;
import de.chhe.wlanvolume.model.entity.WifiVolume;
import de.chhe.wlanvolume.model.persistence.DatabaseHelper;

public class WifiDeleteDialog extends AlertDialog.Builder {

    public WifiDeleteDialog(final WifiVolume wifiVolume, final MainActivity activity) {
        super(activity, R.style.AppTheme_Dialog);
        setTitle(R.string.label_delete);
        setMessage(String.format(Locale.getDefault(), activity.getString(R.string.label_delete_question), wifiVolume.getSsid()));
        setCancelable(false);
        setPositiveButton(R.string.label_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                new AsyncTask<Void, Void, Boolean>(){
                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        return DatabaseHelper.getInstance(activity).deleteWifiVolume(wifiVolume);
                    }
                    @Override
                    protected void onPostExecute(Boolean success) {
                        int msg = success ? R.string.label_delete_success : R.string.label_delete_error;
                        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
                        if (success) {
                            activity.loadList();
                        }
                    }
                }.execute();
            }
        });
        setNegativeButton(R.string.label_no, ActivityHelper.dialogDismissListener);
    }
}
