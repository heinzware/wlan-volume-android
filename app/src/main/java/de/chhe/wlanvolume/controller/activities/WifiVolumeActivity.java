package de.chhe.wlanvolume.controller.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import de.chhe.wlanvolume.WifiConnectionReceiver;
import de.chhe.wlanvolume.model.entity.WifiVolume;
import de.chhe.wlanvolume.model.persistence.DatabaseHelper;

public class WifiVolumeActivity extends AppCompatActivity {

    private static final String TAG = WifiVolumeActivity.class.getSimpleName();

    private TextView ssidTextView;
    private TextView volumeTextView;
    private SeekBar volumeSeekBar;
    private Switch restoreSwitch;
    private Switch notifySwitch;
    private Switch endDndSwitch;
    private EditText commentEditText;


    private MenuItem editItem;
    private MenuItem saveItem;

    private boolean editMode;
    private WifiVolume wifiVolume;
    private int maxVolume;
    private Drawable standardThumb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_volume);

        ssidTextView    = (TextView) findViewById(R.id.ssidTextView);
        volumeTextView  = (TextView) findViewById(R.id.volumeTextView);
        volumeSeekBar   = (SeekBar)  findViewById(R.id.volumeSeekBar);
        restoreSwitch   = (Switch)   findViewById(R.id.restoreSwitch);
        notifySwitch    = (Switch)   findViewById(R.id.notificationSwitch);
        commentEditText = (EditText) findViewById(R.id.commentEditText);
        View notifyView = findViewById(R.id.notificationView);
        View restoreView= findViewById(R.id.restoreView);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editMode) {
                    if (view.getId() == R.id.notificationView) notifySwitch.setChecked(!notifySwitch.isChecked());
                    if (view.getId() == R.id.restoreView) restoreSwitch.setChecked(!restoreSwitch.isChecked());
                    if (view.getId() == R.id.endDndView) endDndSwitch.setChecked(!endDndSwitch.isChecked());
                }
            }
        };

        notifyView.setOnClickListener(listener);
        restoreView.setOnClickListener(listener);

        restoreSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
        notifySwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
        //volumeSeekBar.getThumb().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            endDndSwitch = (Switch) findViewById(R.id.endDndSwitch);
            endDndSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
            View endDndView = findViewById(R.id.endDndView);
            endDndView.setOnClickListener(listener);
        }

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
                    .setNegativeButton(R.string.label_keep_editing, ActivityHelper.dialogDismissListener)
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
        outState.putBoolean(ActivityHelper.INTENT_EXTRA_RESTORE, restoreSwitch.isChecked());
        outState.putString(ActivityHelper.INTENT_EXTRA_SSID, ssidTextView.getText().toString());
        outState.putInt(ActivityHelper.INTENT_EXTRA_VOLUME, volumeSeekBar.getProgress());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            outState.putBoolean(ActivityHelper.INTENT_EXTRA_END_DND, endDndSwitch.isChecked());
        }
        if (wifiVolume != null) {
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
        if (bundle.containsKey(ActivityHelper.INTENT_EXTRA_WIFI_VOLUME)) {
            wifiVolume = bundle.getParcelable(ActivityHelper.INTENT_EXTRA_WIFI_VOLUME);
            if (wifiVolume != null) {
                notifySwitch.setChecked(wifiVolume.isShowNotification());
                ssidTextView.setText(wifiVolume.getSsid());
                setTitle(wifiVolume.getSsid());
                commentEditText.setText(wifiVolume.getComment());
                restoreSwitch.setChecked(wifiVolume.isRestore());
                volumeSeekBar.setProgress(wifiVolume.getVolume());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    endDndSwitch.setChecked(wifiVolume.isEndDnd());
                }
            }
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
        }
        if (bundle.containsKey(ActivityHelper.INTENT_EXTRA_RESTORE)) {
            restoreSwitch.setChecked(bundle.getBoolean(ActivityHelper.INTENT_EXTRA_RESTORE));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && bundle.containsKey(ActivityHelper.INTENT_EXTRA_END_DND)) {
            endDndSwitch.setChecked(bundle.getBoolean(ActivityHelper.INTENT_EXTRA_END_DND));
        }
        applyEditMode();
    }

    private void applyEditMode() {
        if (editItem != null) editItem.setVisible(!editMode);
        if (saveItem != null) saveItem.setVisible(editMode);
        if (volumeSeekBar != null) {
            if (editMode) {
                if(standardThumb != null) {
                    volumeSeekBar.setThumb(standardThumb);
                }
            } else {
                standardThumb = volumeSeekBar.getThumb();
                volumeSeekBar.setThumb(ContextCompat.getDrawable(this, R.drawable.scrubber_control_on_mtrl_alpha));
                volumeSeekBar.getThumb().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.MULTIPLY);

            }
            volumeSeekBar.setEnabled(editMode);
        }
        if (restoreSwitch != null) restoreSwitch.setEnabled(editMode);
        if (notifySwitch != null) notifySwitch.setEnabled(editMode);
        if (commentEditText != null) commentEditText.setEnabled(editMode);
        if (endDndSwitch != null) endDndSwitch.setEnabled(editMode);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(!editMode);
    }

    private void save(){

        int volume = volumeSeekBar.getProgress();
        boolean showNotification = notifySwitch.isChecked();
        String comment = commentEditText.getText().toString();
        boolean restore = restoreSwitch.isChecked();

        if(wifiVolume == null) {
            wifiVolume = new WifiVolume();
            String ssid = ssidTextView.getText().toString();
            wifiVolume.setSsid(ssid);
        }

        wifiVolume.setVolume(volume);
        wifiVolume.setShowNotification(showNotification);
        wifiVolume.setComment(comment);
        wifiVolume.setRestore(restore);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            wifiVolume.setEndDnd(endDndSwitch.isChecked());
        }

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
                    Log.d(TAG, String.format(Locale.getDefault(), "Saved settings for WiFi network \"%s\".", wifiVolume.getSsid()));
                    editMode = false;
                    wifiVolume.setId(id);
                    applyEditMode();
                    Toast.makeText(WifiVolumeActivity.this, R.string.label_save_successful, Toast.LENGTH_LONG).show();
                    checkConnected();
                }
            }
        }.execute();
    }

    private void checkConnected() {

        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {

            String ssid = ActivityHelper.trimSsid(wifiInfo.getSSID());

            if (ssid != null && ssid.equals(wifiVolume.getSsid())) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                int old = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                audioManager.setStreamVolume(AudioManager.STREAM_RING, wifiVolume.getVolume(), AudioManager.FLAG_VIBRATE);

                Log.d(TAG, String.format(Locale.getDefault(), "Connected to WiFi network \"%s\", changed volume from %d to %d.", wifiVolume.getSsid(), old, wifiVolume.getVolume()));

                SharedPreferences prefs = getSharedPreferences(WifiConnectionReceiver.PREFERENCES_NAME, Context.MODE_PRIVATE);

                if (!prefs.contains(WifiConnectionReceiver.PREFERENCE_KEY_CONNECTED_TO)) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(WifiConnectionReceiver.PREFERENCE_KEY_CONNECTED_TO, ssid);
                    editor.putInt(WifiConnectionReceiver.PREFERENCE_KEY_VOL_OLD, old);
                    editor.commit();
                    Log.d(TAG, String.format(Locale.getDefault(), "Connected to WiFi network \"%s\", saved preferences.", wifiVolume.getSsid()));

                }
                ActivityHelper.showNotification(this, wifiVolume, maxVolume);
            }
        }
    }
}
