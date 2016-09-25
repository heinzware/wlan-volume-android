package de.chhe.wlanvolume;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import de.chhe.wlanvolume.model.entity.WlanVolume;



public class WlanVolumeListAdapter extends ArrayAdapter<WlanVolume> {

    public WlanVolumeListAdapter(@NonNull Context context) {
        super(context, 0, new ArrayList<WlanVolume>());
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        WlanVolume wlanVolume = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.wlan_volume_list_item,parent);
        }

        TextView ssidTextView   = (TextView)convertView.findViewById(R.id.SsidTextView);
        TextView volumeTextView = (TextView)convertView.findViewById(R.id.volumeTextView);

        if(wlanVolume != null) {
            int maxVolume = 0;
            ssidTextView.setText(wlanVolume.getSsid());
            volumeTextView.setText(String.format(Locale.getDefault(), "%d/%d", wlanVolume.getVolume(), maxVolume));
        }


        return convertView;
    }
}
