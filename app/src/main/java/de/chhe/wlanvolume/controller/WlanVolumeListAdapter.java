package de.chhe.wlanvolume.controller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import de.chhe.wlanvolume.R;
import de.chhe.wlanvolume.model.entity.WlanVolume;



public class WlanVolumeListAdapter extends BaseAdapter implements ListAdapter {

    private ArrayList<WlanVolume> list = new ArrayList<>();
    private Context context;
    private int maxVolume;

    public WlanVolumeListAdapter(@NonNull Context context, int maxVolume) {
        this.context = context;
        this.maxVolume = maxVolume;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        WlanVolume wlanVolume = list.get(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.wlan_volume_list_item, parent, false);
        }

        TextView ssidTextView   = (TextView)convertView.findViewById(R.id.SsidTextView);
        TextView volumeTextView = (TextView)convertView.findViewById(R.id.volumeTextView);

        if(wlanVolume != null) {
            ssidTextView.setText(wlanVolume.getSsid());
            volumeTextView.setText(String.format(Locale.getDefault(), "%d/%d", wlanVolume.getVolume(), maxVolume));
        }

        return convertView;
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return list.get(i).getId();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    public void setList(ArrayList<WlanVolume> list) {
        this.list = list;
    }
}
