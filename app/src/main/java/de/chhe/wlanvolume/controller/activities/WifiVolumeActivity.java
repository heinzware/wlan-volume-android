package de.chhe.wlanvolume.controller.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import de.chhe.wlanvolume.R;
import de.chhe.wlanvolume.model.entity.WifiVolume;
import de.chhe.wlanvolume.model.persistence.DatabaseHelper;

public class WifiVolumeActivity extends AppCompatActivity {

    private static final String TAG = WifiVolumeActivity.class.getSimpleName();

    private TextView ssidTextView;
    private TextView volumeTextView;
    private SeekBar volumeSeekBar;
    private Switch notifySwitch;
    private EditText commentEditText;

    private MenuItem editItem;
    private MenuItem saveItem;

    private boolean editMode;
    private WifiVolume wifiVolume;
    private int maxVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_volume);

        ssidTextView    = (TextView) findViewById(R.id.ssidTextView);
        volumeTextView  = (TextView) findViewById(R.id.volumeTextView);
        volumeSeekBar   = (SeekBar)  findViewById(R.id.volumeSeekBar);
        notifySwitch    = (Switch)   findViewById(R.id.notificationSwitch);
        commentEditText = (EditText) findViewById(R.id.commentEditText);
        View notifyView = findViewById(R.id.notificationView);

        notifyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editMode) {
                    notifySwitch.setChecked(!notifySwitch.isChecked());
                }
            }
        });

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                volumeTextView.setText(String.format(Locale.getDefault(),"%d/%d", i, maxVolume));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        if(savedInstanceState == null){
            applyBundle(getIntent().getExtras());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wlan_volume, menu);

        editItem = menu.findItem(R.id.action_edit);
        saveItem = menu.findItem(R.id.action_save);

        applyEditMode();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                editMode = true;
                applyEditMode();
                return true;
            case R.id.action_save:
                save();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (editMode) {
            new AlertDialog.Builder(this).setTitle(R.string.label_stop_edit)
                    .setMessage(R.string.label_stop_edit_question)
                    .setPositiveButton(R.string.label_stop_editing, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                            editMode = false;
                            if (wifiVolume != null) {
                                volumeSeekBar.setProgress(wifiVolume.getVolume());
                                applyEditMode();
                            } else {
                                WifiVolumeActivity.super.onBackPressed();
                            }
                        }
                    })
                    .setNegativeButton(R.string.label_keep_editing, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putBoolean(ActivityHelper.INTENT_EXTRA_EDIT_MODE, editMode);
        outState.putInt(ActivityHelper.INTENT_EXTRA_MAX_VOLUME, maxVolume);
        outState.putBoolean(ActivityHelper.INTENT_EXTRA_NOTIFY, notifySwitch.isChecked());
        outState.putString(ActivityHelper.INTENT_EXTRA_COMMENT, commentEditText.getText().toString());
        if (wifiVolume == null) {
            outState.putString(ActivityHelper.INTENT_EXTRA_SSID, ssidTextView.getText().toString());
            outState.putInt(ActivityHelper.INTENT_EXTRA_VOLUME, volumeSeekBar.getProgress());
        } else {
            outState.putParcelable(ActivityHelper.INTENT_EXTRA_WIFI_VOLUME, wifiVolume);
        }
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        applyBundle(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void applyBundle(Bundle bundle){
        if (bundle.containsKey(ActivityHelper.INTENT_EXTRA_EDIT_MODE)) {
            editMode = bundle.getBoolean(ActivityHelper.INTENT_EXTRA_EDIT_MODE, false);
        }
        if (bundle.containsKey(ActivityHelper.INTENT_EXTRA_MAX_VOLUME)) {
            maxVolume = bundle.getInt(ActivityHelper.INTENT_EXTRA_MAX_VOLUME, 0);
            volumeSeekBar.setMax(maxVolume);
        }
        if (bundle.containsKey(ActivityHelper.INTENT_EXTRA_NOTIFY)) {
            notifySwitch.setChecked(bundle.getBoolean(ActivityHelper.INTENT_EXTRA_NOTIFY));
        }
        if (bundle.containsKey(ActivityHelper.INTENT_EXTRA_COMMENT)) {
            commentEditText.setText(bundle.getString(ActivityHelper.INTENT_EXTRA_COMMENT));
        }
        if (bundle.containsKey(ActivityHelper.INTENT_EXTRA_SSID)) {
            ssidTextView.setText(bundle.getString(ActivityHelper.INTENT_EXTRA_SSID));
            setTitle(ssidTextView.getText());
        } else if (bundle.containsKey(ActivityHelper.INTENT_EXTRA_WIFI_VOLUME)) {
            wifiVolume = bundle.getParcelable(ActivityHelper.INTENT_EXTRA_WIFI_VOLUME);
            if (wifiVolume != null) {
                ssidTextView.setText(wifiVolume.getSsid());
                setTitle(wifiVolume.getSsid());
                volumeSeekBar.setProgress(wifiVolume.getVolume());
                notifySwitch.setChecked(wifiVolume.isShowNotification());
                commentEditText.setText(wifiVolume.getComment());
            }
        }
        applyEditMode();
    }

    private void applyEditMode() {
        if (editItem != null) editItem.setVisible(!editMode);
        if (saveItem != null) saveItem.setVisible(editMode);
        if (volumeSeekBar != null) volumeSeekBar.setEnabled(editMode);
        if (notifySwitch != null) notifySwitch.setEnabled(editMode);
        if (commentEditText != null) commentEditText.setEnabled(editMode);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(!editMode);
    }

    private void save(){

        int volume = volumeSeekBar.getProgress();
        boolean showNotification = notifySwitch.isChecked();
        String comment = commentEditText.getText().toString();

        if(wifiVolume == null) {
            wifiVolume = new WifiVolume();
            String ssid = ssidTextView.getText().toString();
            wifiVolume.setSsid(ssid);
        }

        wifiVolume.setVolume(volume);
        wifiVolume.setShowNotification(showNotification);
        wifiVolume.setComment(comment);

        new AsyncTask<Void, Void, Long>(){
            @Override
            protected Long doInBackground(Void... voids) {
                return DatabaseHelper.getInstance(WifiVolumeActivity.this).saveWifiVolume(wifiVolume);
            }

            @Override
            protected void onPostExecute(Long id) {
                if (id == -1L) {
                    Toast.makeText(WifiVolumeActivity.this, R.string.label_save_error, Toast.LENGTH_LONG).show();
                } else {
                    editMode = false;
                    applyEditMode();
                    Toast.makeText(WifiVolumeActivity.this, R.string.label_save_successful, Toast.LENGTH_LONG).show();
                    checkConnected();
                }
            }
        }.execute();
    }

    private void checkConnected() {

        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {

            String ssid = ActivityHelper.trimSsid(wifiInfo.getSSID());

            if (ssid != null && ssid.equals(wifiVolume.getSsid())) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamVolume(AudioManager.STREAM_RING, wifiVolume.getVolume(), AudioManager.FLAG_VIBRATE);

                ActivityHelper.showNotification(this, wifiVolume, maxVolume);
            }
        }

    }
}
