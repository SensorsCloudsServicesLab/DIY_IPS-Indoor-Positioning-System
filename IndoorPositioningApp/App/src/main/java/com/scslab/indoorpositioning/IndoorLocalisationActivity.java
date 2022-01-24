package com.scslab.indoorpositioning;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import smile.stat.distribution.LogNormalDistribution;

public class IndoorLocalisationActivity extends AppCompatActivity {

    private Button checkLocationButton;

    private ListView networkListView;
    private WifiManager wifiManager;
    private List<ScanResult> wifiList;
    private DirectionManager directionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indoor_localisation);

        //Find all views
        this.networkListView = findViewById(R.id.networkListView);
        this.checkLocationButton = findViewById(R.id.check_location_button);

        //Initialisations
        initNetwork();
        initUI();
        this.directionManager = new DirectionManager(this);
    }

    private void initUI() {
        checkLocationButton.setOnClickListener(v -> this.getNetworkLocation());
        networkListView.setOnItemClickListener((parent, view, position, id) -> {

        });
    }

    private void initNetwork() {
        this.wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private void getNetworkLocation() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return;
        }

        wifiManager.startScan();
        wifiList = wifiManager.getScanResults();
        ListAdapter listAdapter = new ListAdapter(getApplicationContext(), wifiList);
        networkListView.setAdapter(listAdapter);

        getLocation();
    }

    private void getLocation() {
        //look up the closest x and y directions
        float degreesFromNorth = directionManager.getCurrentDegreesFromNorth();
        int[] directions = Helpers.getClosestDirections(degreesFromNorth);

        //Get the associated maps
        Map<String, Map<Position, LogNormalDistribution>> xDirectionData = importDistributions(directions[0]);
        //TODO: Process this one, then unset xDirectionData. may also need to do this on a different thread + multithreads maybe

        Map<String, Map<Position, LogNormalDistribution>> yDirectionData = importDistributions(directions[1]);
        //TODO:  Process this one, then unset yDirectionData. may also need to do this on a different thread + multithreads maybe

        Log.d("Riccardo", xDirectionData.toString());
    }

    public Map<String, Map<Position, LogNormalDistribution>> importDistributions(int direction) {
        try {
            //Read data from file
            String directionName = DatabaseWrapper.DIRECTION_NAMES[direction];
            File path = getApplicationContext().getExternalFilesDir(null);
            File distributionDataFile = new File(path, directionName+"_distributions.json");

            int length = (int) distributionDataFile.length();
            byte[] bytes = new byte[length];
            FileInputStream in = new FileInputStream(distributionDataFile);
            in.read(bytes);
            in.close();

            String distributionDataString = new String(bytes);
            JSONObject RSSIDistributionJSON = new JSONObject(distributionDataString);
            Map<String, Map<Position, LogNormalDistribution>> RSSIDistributions = new HashMap<>();
            for (Iterator<String> it = RSSIDistributionJSON.keys(); it.hasNext(); ) {
                String accessPointName = it.next();
                JSONArray accessPointDataJSON = RSSIDistributionJSON.getJSONArray(accessPointName);
                Map<Position, LogNormalDistribution> accessPointData = new HashMap<>();
                for (int i = 0; i < accessPointDataJSON.length(); i++) {
                    JSONObject positionDistributionJSON = accessPointDataJSON.getJSONObject(i);

                    Position position = new Position(
                            positionDistributionJSON.getDouble("ref_x"),
                            positionDistributionJSON.getDouble("ref_y")
                    );

                    LogNormalDistribution distribution = new LogNormalDistribution(
                        Math.max(positionDistributionJSON.getDouble("mu"), 0),
                        Math.max(positionDistributionJSON.getDouble("sigma"), 0.01)
                    );

                    accessPointData.put(position, distribution);
                }
                RSSIDistributions.put(accessPointName, accessPointData);
            }
            return RSSIDistributions;

        } catch (JSONException e) {
            Log.d("Riccardo | JSONException", e.getMessage());
        } catch (IOException e) {
            Log.d("Riccardo | IOException", e.getMessage());
        }

        return null;
    }

    protected void onResume() {
        super.onResume();
        directionManager.onResume();
    }

    protected void onPause() {
        super.onPause();
        directionManager.onPause();
    }

}