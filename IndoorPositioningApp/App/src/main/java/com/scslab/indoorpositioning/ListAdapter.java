package com.scslab.indoorpositioning;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ListAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    List<ScanResult> wifiList;

    public ListAdapter(Context context, List<ScanResult> wifiList) {
        this.context = context;
        this.wifiList = new ArrayList<>();
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (ScanResult scanResult : wifiList) {
            if (scanResult.SSID.contains("SCSLAB_AP")) {
                this.wifiList.add(scanResult);
            }
        }

        this.wifiList.sort(Comparator.comparing(a -> a.SSID));

        for (ScanResult scanResult : wifiList) {
            if (!scanResult.SSID.contains("SCSLAB_AP")) {
                this.wifiList.add(scanResult);
            }
        }
    }

    @Override
    public int getCount() {
        return wifiList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null) {
            view = inflater.inflate(R.layout.list_item, null);
        }

        TextView networkNameTV = view.findViewById(R.id.txtWifiName);
        TextView RSSITV = view.findViewById(R.id.WiFiRSSI);
        TextView frequencyTV = view.findViewById(R.id.WiFiFrequency);

        if(!wifiList.get(position).SSID.isEmpty()){
            networkNameTV.setText(wifiList.get(position).SSID);
            RSSITV.setText(String.valueOf(wifiList.get(position).level));
            frequencyTV.setText(String.valueOf(wifiList.get(position).frequency));
        }

        return view;
    }
}
