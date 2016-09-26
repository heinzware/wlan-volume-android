package de.chhe.wlanvolume.controller.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import de.chhe.wlanvolume.R;
import de.chhe.wlanvolume.controller.dialogs.WifiChooserDialog;
import de.chhe.wlanvolume.controller.WlanVolumeListAdapter;
import de.chhe.wlanvolume.controller.dialogs.WifiScanDialog;
import de.chhe.wlanvolume.model.entity.WlanVolume;
import de.chhe.wlanvolume.model.persistence.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    private WlanVolumeListAdapter listAdapter;

    private WifiManager wifiManager;
    private BroadcastReceiver wifiScanReceiver;
    private AlertDialog wifiScanDialog;

    private int maxVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);

        ListView wlanListView = (ListView)findViewById(R.id.wlanListView);
        if (wlanListView != null) {
            listAdapter = new WlanVolumeListAdapter(this, maxVolume);
            wlanListView.setAdapter(listAdapter);
            wlanListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent wifiVolumeIntent = new Intent(MainActivity.this, WlanVolumeActivity.class);
                    wifiVolumeIntent.putExtra(ActivityHelper.INTENT_EXTRA_EDIT_MODE, false);
                    wifiVolumeIntent.putExtra(ActivityHelper.INTENT_EXTRA_WLAN_VOLUME, (WlanVolume)listAdapter.getItem(i));
                    wifiVolumeIntent.putExtra(ActivityHelper.INTENT_EXTRA_MAX_VOLUME, maxVolume);
                    startActivity(wifiVolumeIntent);
                }
            });
        }

        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showScanResults();
            }
        };
    }

    @Override
    protected void onResume() {
        loadList();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_add:
                scanWifi();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadList(){
        if (listAdapter != null) {
            new AsyncTask<Void, Void, ArrayList<WlanVolume>>(){

                @Override
                protected ArrayList<WlanVolume> doInBackground(Void... voids) {
                    return DatabaseHelper.getInstance(MainActivity.this).getAllWlanVolumes();
                }

                @Override
                protected void onPostExecute(ArrayList<WlanVolume> wlanVolumes) {
                    listAdapter.setList(wlanVolumes);
                    listAdapter.notifyDataSetChanged();
                }

            }.execute();
        }
    }

    private void scanWifi() {

        if (wifiManager == null) {
            wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        }

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), R.string.label_enabling_wifi, Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiScanDialog = new WifiScanDialog(this).show();
        wifiManager.startScan();


    }

    private void showScanResults() {

        wifiScanDialog.dismiss();
        unregisterWifiScanReceiver();
        WifiChooserDialog wifiChooserDialog = new WifiChooserDialog(this, wifiManager.getScanResults());
        wifiChooserDialog.show();

    }

    public void unregisterWifiScanReceiver(){
        unregisterReceiver(wifiScanReceiver);
    }

    public int getMaxVolume(){
        return maxVolume;
    }
}
