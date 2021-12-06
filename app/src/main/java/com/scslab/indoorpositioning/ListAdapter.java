package com.scslab.indoorpositioning;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

public class ListAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    List<ScanResult> wifiList;

    public ListAdapter(Context context, List<ScanResult> wifiList) {
        this.context = context;
        this.wifiList = wifiList;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        View view = convertView;

        if(view == null){
            view = inflater.inflate(R.layout.list_item, null);
            holder = new Holder();

            holder.networkName = view.findViewById(R.id.txtWifiName);
            holder.RSSI = view.findViewById(R.id.WiFiRSSI);
            holder.Frequency = view.findViewById(R.id.WiFiFrequency);
            holder.Distance = view.findViewById(R.id.WiFiDistance);
            view.setTag(holder);

        } else {
            holder = (Holder)view.getTag();
        }

        DecimalFormat f = new DecimalFormat("0.00");

        if(!wifiList.get(position).SSID.isEmpty()){
            holder.networkName.setText(wifiList.get(position).SSID);
            holder.RSSI.setText(String.valueOf(wifiList.get(position).level));
            holder.Frequency.setText(String.valueOf(wifiList.get(position).frequency));
            holder.Distance.setText(f.format(calculateDistance(Double.parseDouble(String.valueOf(wifiList.get(position).level)),Double.parseDouble(String.valueOf(wifiList.get(position).frequency)))));
        }

        return view;
    }

    class Holder{
        TextView networkName;
        TextView RSSI;
        TextView Frequency;
        TextView Distance;
    }

    public double calculateDistance(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }
}
