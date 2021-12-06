package com.scslab.indoorpositioning;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

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

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


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

            holder.networkName = (TextView)view.findViewById(R.id.txtWifiName);
            holder.RSSI = (TextView)view.findViewById(R.id.WiFiRSSI);
            holder.Frequency = (TextView)view.findViewById(R.id.WiFiFrequency);
            holder.Distance = (TextView)view.findViewById(R.id.WiFiDistance);
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
                        HashMap<String, String> networkCollection = new HashMap<>();

                        networkData.put("macAddress", macAddress);
                        //  String networkDetail[] = new String[2];

                        if(!wifiList.get(position).SSID.isEmpty() & !(wifiList.get(position).SSID == null)){
                            //    networkCollection.put(wifiList.get(position).SSID, String.valueOf(wifiList.get(position).level));
                            //  networkData.put("networkCollection", networkCollection);
                            networkData.put(wifiList.get(position).SSID, String.valueOf(wifiList.get(position).level));




                        }

                        //    networkData.put("GeoPoint", LatLng.getText().toString());

                    }
                    break;
                }
            }
            // Mac_address.setText(macAddress);

        } catch (SocketException e){
            e.printStackTrace();
        }
        //  DocumentReference docRef = db.collection("WBIP").document(networkData.get("macAddress").toString());
        System.out.println("----firebase docRef------");
        System.out.println(networkData);

        //db.collection("WBIP").document(networkData.get("macAddress").toString()).push




        //   holder.RSSI.setText(wifiList.get(position).level);
        //  holder.tvDetails.setText(wifiList.get(position).level);
        System.out.println("wifilist: " + wifiList.get(position).SSID);
        System.out.println(wifiList.get(position).level);



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
