package de.chhe.wlanvolume.controller.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.chhe.wlanvolume.R;
import de.chhe.wlanvolume.controller.dialogs.WifiChooserDialog;
import de.chhe.wlanvolume.controller.WifiVolumeListAdapter;
import de.chhe.wlanvolume.controller.dialogs.WifiScanDialog;
import de.chhe.wlanvolume.model.entity.WifiVolume;
import de.chhe.wlanvolume.model.persistence.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 0;
    private static final int ITEM_ID_EDIT   = 1;
    private static final int ITEM_ID_DELETE = 2;

    private WifiVolumeListAdapter listAdapter;

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

        ListView wifiListView = (ListView)findViewById(R.id.wlanListView);
        if (wifiListView != null) {
            listAdapter = new WifiVolumeListAdapter(this, maxVolume);
            wifiListView.setAdapter(listAdapter);
            wifiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent wifiVolumeIntent = new Intent(MainActivity.this, WifiVolumeActivity.class);
                    wifiVolumeIntent.putExtra(ActivityHelper.INTENT_EXTRA_EDIT_MODE, false);
                    wifiVolumeIntent.putExtra(ActivityHelper.INTENT_EXTRA_WLAN_VOLUME, (WifiVolume)listAdapter.getItem(i));
                    wifiVolumeIntent.putExtra(ActivityHelper.INTENT_EXTRA_MAX_VOLUME, maxVolume);
                    startActivity(wifiVolumeIntent);
                }
            });
            registerForContextMenu(wifiListView);
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.wlanListView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            View headerView = ((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.context_menu_header, null);
            TextView headerTitle = (TextView)headerView.findViewById(R.id.headerTitle);
            headerTitle.setText(((WifiVolume) listAdapter.getItem(info.position)).getSsid());
            menu.setHeaderView(headerView);
            menu.add(Menu.NONE, ITEM_ID_EDIT, 1, R.string.label_edit);
            menu.add(Menu.NONE, ITEM_ID_DELETE, 2, R.string.label_delete);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case ITEM_ID_EDIT:
                Intent wifiVolumeIntent = new Intent(this, WifiVolumeActivity.class);
                wifiVolumeIntent.putExtra(ActivityHelper.INTENT_EXTRA_EDIT_MODE, true);
                wifiVolumeIntent.putExtra(ActivityHelper.INTENT_EXTRA_WLAN_VOLUME, maxVolume);
                wifiVolumeIntent.putExtra(ActivityHelper.INTENT_EXTRA_WLAN_VOLUME, (WifiVolume)listAdapter.getItem(info.position));
                startActivity(wifiVolumeIntent);
                return true;
            case ITEM_ID_DELETE:
                final WifiVolume wifiVolume = (WifiVolume)listAdapter.getItem(info.position);
                new AlertDialog.Builder(this)
                        .setTitle(R.string.label_delete)
                        .setMessage(String.format(Locale.getDefault(), getString(R.string.label_delete_question), wifiVolume.getSsid()))
                        .setCancelable(false)
                        .setPositiveButton(R.string.label_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        new AsyncTask<Void, Void, Boolean>(){
                                            @Override
                                            protected Boolean doInBackground(Void... voids) {
                                                return DatabaseHelper.getInstance(MainActivity.this).deleteWifiVolume(wifiVolume);
                                            }
                                            @Override
                                            protected void onPostExecute(Boolean success) {
                                                int msg = success ? R.string.label_delete_success : R.string.label_delete_error;
                                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                                                if (success) {
                                                    loadList();
                                                }
                                            }
                                        }.execute();
                                    }
                                })
                        .setNegativeButton(R.string.label_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                            }
                        }).show();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            scanWifi();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void loadList(){
        if (listAdapter != null) {
            new AsyncTask<Void, Void, ArrayList<WifiVolume>>(){

                @Override
                protected ArrayList<WifiVolume> doInBackground(Void... voids) {
                    return DatabaseHelper.getInstance(MainActivity.this).getAllWifiVolumes();
                }

                @Override
                protected void onPostExecute(ArrayList<WifiVolume> wifiVolumes) {
                    listAdapter.setList(wifiVolumes);
                    listAdapter.notifyDataSetChanged();
                }

            }.execute();
        }
    }

    private void scanWifi() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_ACCESS_COARSE_LOCATION);
                return;
            }

            LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
            if (locationManager.getProviders(true).size() == 0) {
                new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                        .setTitle(R.string.label_location_title)
                        .setMessage(R.string.label_location_message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.label_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.label_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return;
            }
        }

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
        List<ScanResult> scanResultsTmp = wifiManager.getScanResults();
        List<ScanResult> scanResults = new ArrayList<>();
        for (ScanResult scanResult : scanResultsTmp) {
            if (!listAdapter.containsSsid(scanResult.SSID)) {
                scanResults.add(scanResult);
            }
        }
        WifiChooserDialog wifiChooserDialog = new WifiChooserDialog(this, scanResults);
        wifiChooserDialog.show();
    }

    public void unregisterWifiScanReceiver(){
        unregisterReceiver(wifiScanReceiver);
    }

    public int getMaxVolume(){
        return maxVolume;
    }
}
