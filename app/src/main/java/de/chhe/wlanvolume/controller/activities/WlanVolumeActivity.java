package de.chhe.wlanvolume.controller.activities;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import de.chhe.wlanvolume.R;
import de.chhe.wlanvolume.model.entity.WlanVolume;
import de.chhe.wlanvolume.model.persistence.DatabaseHelper;

public class WlanVolumeActivity extends AppCompatActivity {

    private static final String TAG = WlanVolumeActivity.class.getSimpleName();

    private TextView ssidTextView;
    private TextView volumeTextView;
    private SeekBar volumeSeekBar;

    private MenuItem editItem;
    private MenuItem saveItem;

    private boolean editMode;
    private WlanVolume wlanVolume;
    private int maxVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wlan_volume);

        ssidTextView    = (TextView)findViewById(R.id.ssidTextView);
        volumeTextView  = (TextView)findViewById(R.id.volumeTextView);
        volumeSeekBar   = (SeekBar)findViewById(R.id.volumeSeekBar);

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
            applyIntent();
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

    private void applyIntent(){
        if (getIntent().hasExtra(ActivityHelper.INTENT_EXTRA_EDIT_MODE)) {
            editMode = getIntent().getBooleanExtra(ActivityHelper.INTENT_EXTRA_EDIT_MODE, false);
        }
        if (getIntent().hasExtra(ActivityHelper.INTENT_EXTRA_MAX_VOLUME)) {
            maxVolume = getIntent().getIntExtra(ActivityHelper.INTENT_EXTRA_MAX_VOLUME, 0);
            volumeSeekBar.setMax(maxVolume);
        }
        if (getIntent().hasExtra(ActivityHelper.INTENT_EXTRA_SSID)) {
            ssidTextView.setText(getIntent().getStringExtra(ActivityHelper.INTENT_EXTRA_SSID));
            setTitle(ssidTextView.getText());
        } else if (getIntent().hasExtra(ActivityHelper.INTENT_EXTRA_WLAN_VOLUME)) {
            wlanVolume = getIntent().getParcelableExtra(ActivityHelper.INTENT_EXTRA_WLAN_VOLUME);
            ssidTextView.setText(wlanVolume.getSsid());
            setTitle(wlanVolume.getSsid());
            volumeSeekBar.setProgress(wlanVolume.getVolume());
        }
        applyEditMode();
    }

    private void applyEditMode() {
        if (editItem != null) {
            editItem.setVisible(!editMode);
        }
        if (saveItem != null) {
            saveItem.setVisible(editMode);
        }
        if (volumeSeekBar != null) {
            volumeSeekBar.setEnabled(editMode);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!editMode);
        }
    }

    private void save(){

        int volume = volumeSeekBar.getProgress();

        if(wlanVolume == null) {
            wlanVolume = new WlanVolume();
            String ssid = ssidTextView.getText().toString();
            wlanVolume.setSsid(ssid);
        }

        wlanVolume.setVolume(volume);

        new AsyncTask<Void, Void, Long>(){
            @Override
            protected Long doInBackground(Void... voids) {
                return DatabaseHelper.getInstance(WlanVolumeActivity.this).saveWlanVolume(wlanVolume);
            }

            @Override
            protected void onPostExecute(Long id) {
                if (id == -1L) {
                    Toast.makeText(WlanVolumeActivity.this, R.string.label_save_error, Toast.LENGTH_LONG).show();
                } else {
                    editMode = false;
                    applyEditMode();
                    Toast.makeText(WlanVolumeActivity.this, R.string.label_save_successful, Toast.LENGTH_LONG).show();
                }
            }
        }.execute();

    }
}
