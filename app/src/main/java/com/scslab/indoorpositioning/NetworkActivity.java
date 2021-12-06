package com.scslab.indoorpositioning;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;

public class NetworkActivity extends AppCompatActivity {

    TextView NetworkSSID;
    TextView NetworkRSSI;
    TextView NetworkFrequency;
    TextView Distance;
    EditText editTextCoordinate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        NetworkSSID = findViewById(R.id.NetworkSSID);
        NetworkRSSI = findViewById(R.id.NetworkRSSI);
        NetworkFrequency = findViewById(R.id.NetworkFrequency);
        Distance = findViewById(R.id.distance);
        editTextCoordinate = findViewById(R.id.editTextCoordinate);

        Intent intent = this.getIntent();

        if(intent != null){
            String SSID = intent.getStringExtra("SSID");
            String RSSI = intent.getStringExtra("RSSI");
            String Frequency = intent.getStringExtra("Frequency");

            NetworkSSID.setText(SSID);
            NetworkRSSI.setText(RSSI);
            NetworkFrequency.setText(Frequency);

            DecimalFormat f = new DecimalFormat("0.00");
            double distance = calculateDistance(Double.parseDouble(RSSI), Double.parseDouble(Frequency));
            Distance.setText(f.format(distance));

        }
    }

    public double calculateDistance(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }
}
