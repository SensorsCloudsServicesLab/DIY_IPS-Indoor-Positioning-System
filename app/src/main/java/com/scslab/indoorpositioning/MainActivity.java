package com.scslab.indoorpositioning;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView latLongText;
    private ListView networkListView;
    private Button checkLocationButton;

    boolean isPermissionGranted;
    GoogleMap mGoogleMap;

    WifiManager wifiManager;
    ListAdapter listAdapter;

    List<ScanResult> wifiList;

    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Find all views
        this.latLongText = findViewById(R.id.Latlng);
        this.networkListView = findViewById(R.id.networkListView);
        this.checkLocationButton = findViewById(R.id.click);

        //Initialisations
        checkPermissions();

        initMap();
        initNetwork();
        initUI();

        getGPSLocation();
        getNetworkLocation();

        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            getNetworkLocation();
            getGPSLocation();
        }
    }

    private void initUI() {
        checkLocationButton.setOnClickListener(v -> this.getNetworkLocation());
        networkListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent newIntent = new Intent(MainActivity.this, NetworkActivity.class);
            newIntent.putExtra("SSID", wifiList.get(position).SSID);
            newIntent.putExtra("RSSI", String.valueOf(wifiList.get(position).level));
            newIntent.putExtra("Frequency", String.valueOf(wifiList.get(position).frequency));
            startActivity(newIntent);
        });
    }

    private void initNetwork() {
        this.wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private void getNetworkLocation() {
        wifiManager.startScan();
        wifiList = wifiManager.getScanResults();
        listAdapter = new ListAdapter(getApplicationContext(), wifiList);
        networkListView.setAdapter(listAdapter);
    }

    private void initMap() {
        fusedLocationProviderClient = new FusedLocationProviderClient(this);

        if(isPermissionGranted){
            SupportMapFragment supportMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
            supportMapFragment.getMapAsync(this::onMapReady);
        }
    }
    @SuppressLint("MissingPermission")
    private void getGPSLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Location location = task.getResult();
                gotoLocation(location.getLatitude(), location.getLongitude());
                latLongText.setText(location.getLatitude() + ", " + location.getLongitude());
            }
        });
    }

    private void gotoLocation(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        mGoogleMap.moveCamera(cameraUpdate);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    private void checkPermissions() {
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                isPermissionGranted = true;
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), "");
                intent.setData(uri);
                startActivity(intent);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    @SuppressLint("MissingPermission")
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMyLocationEnabled(true);
    }
}