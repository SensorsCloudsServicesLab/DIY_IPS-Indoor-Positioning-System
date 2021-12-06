package com.scslab.indoorpositioning;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.DialogInterface;
import android.app.AlertDialog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.opencensus.tags.Tag;

import static androidx.constraintlayout.motion.widget.Debug.getLocation;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    boolean isPermissionGranted;
    GoogleMap mGoogleMap;

    WifiManager wifiManager;
    WifiReceiver wifiReceiver;
    ListAdapter listAdapter;
    ListView networkListView;
    List<ScanResult> wifiList;
    Button buttonClick;
    TextInputEditText intersectionPoint;
    Button refresh;
    AlertDialog.Builder builder;

    private FusedLocationProviderClient fusedLocationProviderClient;
    TextView Mac_address;
    TextView LatLng;
    StringBuilder stringBuilder = new StringBuilder();

    Map<String, Object> networkData = new HashMap<>();
    Double[] geoPoint = new Double[2];
    private SharedPreferences sharedPreferences;
    ArrayList<String> savedCoordinate = new ArrayList<>();
    ArrayList<double[]> savedIntersection = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Mac_address = findViewById(R.id.Mac_address);
        LatLng = findViewById(R.id.Latlng);
        networkListView = (ListView)findViewById(R.id.networkListView);
        Intent newIntent = new Intent(MainActivity.this, NetworkActivity.class);

        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();
        builder = new AlertDialog.Builder(this);

        buttonClick = (Button)findViewById(R.id.click);
        refresh = (Button)findViewById(R.id.refresh);

        sharedPreferences = getSharedPreferences("coordinate_save", Context.MODE_PRIVATE);
        intersectionPoint = findViewById(R.id.intersectionPoints);
        intersectionPoint.setText(null);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intersectionPoint.getText().clear();
                stringBuilder.setLength(0);

            }
        });

        buttonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                savedCoordinate.clear();

                // intersectionPoint.getText().clear();
                Map<String, ?> allEntries = sharedPreferences.getAll();
                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
                    String[] startConvert = entry.getValue().toString().split(",");

                    Log.d("0", startConvert[0]);
                    Log.d("1", startConvert[1]);
                    Log.d("2", startConvert[2]);

                    savedIntersection.add(Intersection.getIntersectionPoint(Double.parseDouble(LatLng.getText().toString().split(",")[0].trim()), Double.parseDouble(LatLng.getText().toString().split(",")[1].trim()), Double.parseDouble(startConvert[2]), Double.parseDouble(startConvert[0]), Double.parseDouble(startConvert[1]), Double.parseDouble(startConvert[2])));

                    savedCoordinate.add(entry.getValue().toString());

                }

                Log.d("result", savedCoordinate.toString());
                Log.d("coordinate",LatLng.getText().toString());

                Double A = 0.0;
                Double B = 0.0;
                Double C = 0.0;
                Double D = 0.0;

                LinkedList<Double> linkedListA = new LinkedList<>();
                LinkedList<Double> linkedListB = new LinkedList<>();
                LinkedList<Double> linkedListC = new LinkedList<>();
                LinkedList<Double> linkedListD = new LinkedList<>();

//                List<Double> listA = new List<Double>();
//                List<Double> listB;
//                List<Double> listC;
//                List<Double> listD;


                //  StringBuilder stringBuilder = new StringBuilder();
                for (int i=0; i<savedIntersection.size();i++){
                    Log.d("intersection", Arrays.toString(savedIntersection.get(i)));
                    stringBuilder.append(Arrays.toString(savedIntersection.get(i)));
                    linkedListA.add(savedIntersection.get(i)[0]);
                    linkedListB.add(savedIntersection.get(i)[1]);
                    linkedListC.add(savedIntersection.get(i)[2]);
                    linkedListD.add(savedIntersection.get(i)[3]);
                }

                Log.d("Allintersection", stringBuilder.toString());
                intersectionPoint.setText(stringBuilder.toString());
                Log.d("linkedListA", linkedListA.toString());
                Log.d("linkedListB", linkedListB.toString());
                Log.d("linkedListC", linkedListC.toString());
                Log.d("linkedListD", linkedListD.toString());
                for(double each: linkedListA){
                    A += each;
                    Log.d("eachA",String.valueOf(each));
                    //    A = A/linkedListA.size();
                }

                for(double each: linkedListB){
                    B += each;
                    Log.d("eachB",String.valueOf(each));

                    //   B = B/linkedListB.size();
                }

                for(double each: linkedListC){
                    C += each;
                    Log.d("eachC",String.valueOf(each));

                    //  C = C/linkedListC.size();
                }

                for(double each: linkedListD){
                    D += each;
                    Log.d("eachD",String.valueOf(each));

                    //   D = D/linkedListD.size();
                }

                builder.setMessage(R.string.dialog_message) .setTitle(R.string.dialog_title);
                builder.setMessage("This is the average point of intersection points: " + String.valueOf(B/linkedListB.size() +D/linkedListD.size() ) + " " + String.valueOf(A/linkedListA.size() +C/linkedListC.size() ))
                        .setCancelable(false)
                        .setNegativeButton("close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //  Action for 'NO' Button
                                dialog.cancel();
                                //   Toast.makeText(getApplicationContext(),"you choose no action for alertbox",
                                //          Toast.LENGTH_SHORT).show();
                            }
                        });

                AlertDialog alert = builder.create();
                //Setting the title manually
                alert.setTitle("average point");
                alert.show();

                Log.d("X", String.valueOf(A/linkedListA.size() +C/linkedListC.size() ));
                Log.d("Y", String.valueOf(B/linkedListB.size() + D/linkedListD.size()));
                //   Log.d("C", String.valueOf(C/linkedListC.size()));
                //  Log.d("D", String.valueOf(D/linkedListD.size()));

//                Log.d("B", B.toString());
//                Log.d("C", C.toString());
//                Log.d("D", D.toString());






                //   intersectionPoint.setText(savedIntersection.toString());

//                Log.d("result", result);


//                networkData.put("manualCoordinate", data);
//
            }
        });

        networkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                newIntent.putExtra("SSID", wifiList.get(position).SSID);
                newIntent.putExtra("RSSI", String.valueOf(wifiList.get(position).level));
                newIntent.putExtra("Frequency", String.valueOf(wifiList.get(position).frequency));
                //  newIntent.putExtra("Frequency", wifiList.get(position).frequency);
//                newIntent.putExtra()
                Toast.makeText(MainActivity.this,  String.valueOf(wifiList.get(position).frequency), Toast.LENGTH_SHORT).show();


                startActivity(newIntent);
            }
        });

        checkMyPermission();

        initMap();
        fusedLocationProviderClient = new FusedLocationProviderClient(this);


        getMacAddress();

        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);

        } else {
            ScanWifiList();
            getCurrentLocation();

        }


    }



    private void ScanWifiList() {
        wifiManager.startScan();
        wifiList = wifiManager.getScanResults();
        setAdapter();
    }

    private String getData(String key){
        return sharedPreferences.getString(key, "");
    }

    private void setAdapter() {

        listAdapter = new ListAdapter(getApplicationContext(), wifiList);

        networkListView.setAdapter(listAdapter);
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
                        //  newIntent.putExtra("macAddress", macAddress);
                        //  newIntent.putExtra("macAddress", networkData.get("macAddress").toString());

                        //    networkData.put("GeoPoint", LatLng.getText().toString());

                    }
                    break;
                }
            }
            Mac_address.setText(macAddress);

        } catch (SocketException e){
            e.printStackTrace();
        }
    }


    private void initMap() {
        if(isPermissionGranted){
            SupportMapFragment supportMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
            supportMapFragment.getMapAsync(this::onMapReady);

        }
    }
    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Location location = task.getResult();
                gotoLocation(location.getLatitude(), location.getLongitude());



                LatLng.setText(location.getLatitude() + ", " + location.getLongitude());


//                geoPoint[0] = location.getLongitude();
//                geoPoint[1] = location.getLongitude();




            }
        });
    }

    private void gotoLocation(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        mGoogleMap.moveCamera(cameraUpdate);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //GeoPoint geoPoint = new GeoPoint(latitude, longitude);
        networkData.put("GeoPoint", new GeoPoint(latLng.latitude, latLng.longitude));
//        newIntent.putExtra("Geolocation", new GeoPoint(latLng.latitude, latLng.longitude).toString());

//        wifiList = wifiManager.getScanResults();
//
//        System.out.println("this is wifiList");
//
//        System.out.println(wifiList);


        // saveGeoToMap(latitude, longitude);

        //   networkData.put("GeoPoint", geoPoint);


        //     networkData.put("GeoPoint", new GeoPoint(latLng.latitude, latLng.longitude));
        //   System.out.println(networkData);

        // mGoogleMap.setMapStyle(GoogleMap.MAP_TYPE_NORMAL);
    }



    public static GeoPoint saveGeoToMap(double latitude, double longitude) {
        System.out.println("this is latitude"+ Double.toString(latitude));
        System.out.println("this is longitude"+ Double.toString(longitude));



        GeoPoint geoPoint = new GeoPoint(latitude, longitude);

        System.out.println("this is longitude"+ Double.toString(geoPoint.getLatitude()));
        System.out.println("this is longitude"+ Double.toString(geoPoint.getLongitude()));

        // networkData.put("GeoPoint", new GeoPoint(geoPoint.getLatitude(), geoPoint.getLongitude()));

        double coordinate[] = new double[2];
        coordinate[0] = geoPoint.getLatitude();
        coordinate[1] = geoPoint.getLongitude();
        System.out.println("-----");
        System.out.println(coordinate);
        return geoPoint;
    }

    private void checkMyPermission() {
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
    @Override
    public void onMapReady( GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMyLocationEnabled(true);

    }

    class WifiReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

}