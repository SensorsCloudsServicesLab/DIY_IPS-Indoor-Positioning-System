package com.scslab.indoorpositioning;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    List<ScanResult> wifiList;

    Map<String, Object> networkData = new HashMap<>();

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
        //  holder.tvDetails[0] = wifiList.get(position).SSID;

        DecimalFormat f = new DecimalFormat("0.00");

        // String distance =Double.toString(calculateDistance(Double.parseDouble(RSSI), Double.parseDouble(Frequency)));

        if(!wifiList.get(position).SSID.isEmpty()){
            holder.networkName.setText(wifiList.get(position).SSID);
            holder.RSSI.setText(String.valueOf(wifiList.get(position).level));
            holder.Frequency.setText(String.valueOf(wifiList.get(position).frequency));
            holder.Distance.setText(f.format(calculateDistance(Double.parseDouble(String.valueOf(wifiList.get(position).level)),Double.parseDouble(String.valueOf(wifiList.get(position).frequency)))));
            //   holder.Distance.setText(String.format(Double.toString(f.format(calculateDistance(Double.parseDouble(String.valueOf(wifiList.get(position).level)), Double.parseDouble(String.valueOf(wifiList.get(position).frequency))))));
        }



        System.out.println("-----print SSID and Level-----");
        System.out.println(wifiList.get(position).SSID + "," + wifiList.get(position).level);
        System.out.println(wifiList.get(position).SSID.isEmpty());
        try{
            List<NetworkInterface> networkInterfaceList = Collections.list(NetworkInterface.getNetworkInterfaces());
            String macAddress = "";
            for(NetworkInterface networkInterface: networkInterfaceList){
                if(networkInterface.getName().equalsIgnoreCase("wlan0")){
                    for(int i = 0; i<networkInterface.getHardwareAddress().length;i++){
                        String stringMacByte = Integer.toHexString(networkInterface.getHardwareAddress()[i] & 0xFF);

                        if(stringMacByte.length() == 1){
                            stringMacByte = "0" + stringMacByte;
                        }
                        macAddress = macAddress + stringMacByte.toUpperCase() + ":";

                        networkData.put("macAddress", macAddress);

                        if(!wifiList.get(position).SSID.isEmpty() & !(wifiList.get(position).SSID == null)){
                            networkData.put(wifiList.get(position).SSID, String.valueOf(wifiList.get(position).level));
                        }
                    }
                    break;
                }
            }

        } catch (SocketException e){
            e.printStackTrace();
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
