package com.scslab.indoorpositioning;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView networkListView;
    private Button checkLocationButton;
    private Button startFingerprintingButton;

    WifiManager wifiManager;
    ListAdapter listAdapter;

    List<ScanResult> wifiList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Find all views
        this.networkListView = findViewById(R.id.networkListView);
        this.checkLocationButton = findViewById(R.id.check_location_button);
        this.startFingerprintingButton = findViewById(R.id.fingerprint_button);

        //Initialisations
        initNetwork();
        initUI();

        getNetworkLocation();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            getNetworkLocation();
        }
    }

    private void initUI() {
        checkLocationButton.setOnClickListener(v -> this.getNetworkLocation());
        startFingerprintingButton.setOnClickListener(v -> {
            Intent myIntent = new Intent(getApplicationContext(), FingerprintActivity.class);
            startActivity(myIntent);
        });
        networkListView.setOnItemClickListener((parent, view, position, id) -> {

        });
    }

    private void initNetwork() {
        this.wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private void getNetworkLocation() {
        wifiManager.startScan();
        wifiList = wifiManager.getScanResults();
        listAdapter = new ListAdapter(getApplicationContext(), wifiList);
        networkListView.setAdapter(listAdapter);
    }
}