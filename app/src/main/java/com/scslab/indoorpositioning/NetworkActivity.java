package com.scslab.indoorpositioning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;


    TextView NetworkSSID;
    TextView NetworkRSSI;
    TextView NetworkFrequency;
    TextView Distance;
    EditText editTextCoordinate;
    Map<String, Object> networkData = new HashMap<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DatabaseReference mDatabase;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        NetworkSSID = findViewById(R.id.NetworkSSID);
        NetworkRSSI = findViewById(R.id.NetworkRSSI);
        NetworkFrequency = findViewById(R.id.NetworkFrequency);
        Distance = findViewById(R.id.distance);
        editTextCoordinate = findViewById(R.id.editTextCoordinate);

        Intent intent = this.getIntent();

        sharedPreferences = getSharedPreferences("coordinate_save", Context.MODE_PRIVATE);


        //  mDatabase = FirebaseDatabase.getInstance().getReference();


        if(intent != null){
            String SSID = intent.getStringExtra("SSID");
            String RSSI = intent.getStringExtra("RSSI");
            //  int Frequency = intent.getIntExtra("Frequency");
            String Frequency = intent.getStringExtra("Frequency");
            // String Geolocation = intent.getStringExtra("Geolocation");

            // Toast.makeText(NetworkActivity.this, Geolocation, Toast.LENGTH_SHORT).show();

            // String MacAddress = intent.getStringExtra("macAddress");


            NetworkSSID.setText(SSID);
            NetworkRSSI.setText(RSSI);
            NetworkFrequency.setText(Frequency);

            //Toast.makeText(NetworkActivity.this, MacAddress, Toast.LENGTH_SHORT).show();
            DecimalFormat f = new DecimalFormat("0.00");

            String distance =Double.toString(calculateDistance(Double.parseDouble(RSSI), Double.parseDouble(Frequency)));


            Distance.setText(f.format(calculateDistance(Double.parseDouble(RSSI), Double.parseDouble(Frequency))));

            //      mDatabase.getDatabase();

//            mDatabase.child("users").child(SSID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DataSnapshot> task) {
//                    if (!task.isSuccessful()) {
//                        Log.e("firebase", "Error getting data", task.getException());
//                    }
//                    else {
//                        Log.d("firebase", String.valueOf(task.getResult().getValue()));
//                    }
//                }
//            });

            // System.out.println(db);


            getMacAddress();

//            updateValue();




        }
    }

    /*
     */
    public void testSharedPreference(View view){
        Intent intent = this.getIntent();


        String saveData = editTextCoordinate.getText().toString() + "," + Distance.getText() ;


        if(TextUtils.isEmpty(editTextCoordinate.getText())){
            Toast.makeText(this, "please type coordinate", Toast.LENGTH_SHORT).show();
            return;
        }
        String SSID = intent.getStringExtra("SSID");

        updateValue(SSID, saveData);
    }
    public void readDataToScreen(View view){

        Intent intent = this.getIntent();
        String SSID = intent.getStringExtra("SSID");

        String data = getData(SSID);
        Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
    }

    private void updateValue(String key, String data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, data);
        editor.commit();
        Toast.makeText(this, data, Toast.LENGTH_SHORT).show();

        //   Toast.makeText(NetworkActivity.this, networkData.get("macAddress").toString(), Toast.LENGTH_SHORT).show();

    }

    private String getData(String key){
        return sharedPreferences.getString(key, "");
    }

    public double calculateDistance(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }

    private void getMacAddress() {
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
                        //  newIntent.putExtra("macAddress", networkData.get("macAddress").toString());

                        //    networkData.put("GeoPoint", LatLng.getText().toString());

                    }
                    break;
                }
            }
            //   Mac_address.setText(macAddress);

        } catch (SocketException e){
            e.printStackTrace();
        }
    }
}
