package de.chhe.wlanvolume.controller.activities;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.chhe.wlanvolume.R;
import de.chhe.wlanvolume.controller.WifiVolumeListAdapter;
import de.chhe.wlanvolume.controller.dialogs.WifiChooserDialog;
import de.chhe.wlanvolume.controller.dialogs.WifiDeleteDialog;
import de.chhe.wlanvolume.model.entity.WifiVolume;
import de.chhe.wlanvolume.model.persistence.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    private static final int ITEM_ID_EDIT   = 1;
    private static final int ITEM_ID_DELETE = 2;

    private WifiVolumeListAdapter listAdapter;

    private WifiManager wifiManager;

    private int maxVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);

        ListView wifiListView = (ListView) findViewById(R.id.wlanListView);
        if (wifiListView != null) {
            listAdapter = new WifiVolumeListAdapter(this, maxVolume);
            wifiListView.setAdapter(listAdapter);
            wifiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    startActivity(ActivityHelper.createWifiVolumeIntent(MainActivity.this, false, maxVolume, (WifiVolume) listAdapter.getItem(i), null));
                }
            });
            registerForContextMenu(wifiListView);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            if (!notificationManager.isNotificationPolicyAccessGranted()) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.label_notification_policy)
                        .setMessage(R.string.label_notification_policy_question)
                        .setPositiveButton(R.string.label_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    Intent notificationIntent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                                    startActivity(notificationIntent);
                                }
                            }
                        })
                        .setNegativeButton(R.string.label_no, ActivityHelper.dialogDismissListener)
                        .show();
            }
        }
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
                //scanWifi();
                addWifi();
                return true;
            case R.id.action_about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.wlanListView) {

            //create header
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            View headerView = getLayoutInflater().inflate(R.layout.context_menu_header, (ViewGroup) findViewById(android.R.id.content), false);
            TextView headerTitle = (TextView)headerView.findViewById(R.id.headerTitle);
            headerTitle.setText(((WifiVolume) listAdapter.getItem(info.position)).getSsid());
            menu.setHeaderView(headerView);

            //add items
            menu.add(Menu.NONE, ITEM_ID_EDIT, 1, R.string.label_edit);
            menu.add(Menu.NONE, ITEM_ID_DELETE, 2, R.string.label_delete);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case ITEM_ID_EDIT:
                startActivity(ActivityHelper.createWifiVolumeIntent(this, true, maxVolume, (WifiVolume)listAdapter.getItem(info.position), null));
                return true;
            case ITEM_ID_DELETE:
                WifiVolume wifiVolume = (WifiVolume)listAdapter.getItem(info.position);
                new WifiDeleteDialog(wifiVolume, this).show();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    public void loadList(){
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

    private void addWifi() {
        if (wifiManager == null) {
            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        }

        if (!wifiManager.isWifiEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.label_wifi_disabled)
                    .setMessage(R.string.label_wifi_disabled_question)
                    .setPositiveButton(R.string.label_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                            wifiManager.setWifiEnabled(true);
                            addWifi();
                        }
                    })
                    .setNegativeButton(R.string.label_no, ActivityHelper.dialogDismissListener)
                    .show();
        } else {

            List<WifiConfiguration> knownNetworksTmp = wifiManager.getConfiguredNetworks();
            List<WifiConfiguration> knownNetworks = new ArrayList<>();
            for (WifiConfiguration wifiConfig : knownNetworksTmp) {
                if (!listAdapter.containsSsid(wifiConfig.SSID)) {
                    knownNetworks.add(wifiConfig);
                }
            }
            new WifiChooserDialog(this, knownNetworks).show();
        }
    }

    public int getMaxVolume(){
        return maxVolume;
    }
}
