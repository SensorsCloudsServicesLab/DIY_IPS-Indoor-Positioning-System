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
import android.widget.Toast;

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

import de.lmu.ifi.dbs.elki.math.statistics.distribution.SkewGeneralizedNormalDistribution;

public class IndoorLocalisationActivity extends AppCompatActivity {

    private Button checkLocationButton;

    private ListView networkListView;
    private WifiManager wifiManager;
    private List<ScanResult> wifiList;
    private DirectionManager directionManager;
    private final Map<String, Map<String, RoomMatrix<SkewGeneralizedNormalDistribution>>> distributions = new HashMap<>();

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
        initDistributions();
        this.directionManager = new DirectionManager(this);
    }

    private void initUI() {
        checkLocationButton.setOnClickListener(v -> this.getNetworkLocation());
    }

    private void initNetwork() {
        this.wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private void initDistributions() {
        new Thread(() -> {
            runOnUiThread(() -> {Toast.makeText(IndoorLocalisationActivity.this, "Importing Data...", Toast.LENGTH_SHORT).show();});
            distributions.put(DatabaseWrapper.DIRECTION_NAMES[DatabaseWrapper.DIRECTION_NORTH], importDistributions(DatabaseWrapper.DIRECTION_NORTH));
            distributions.put(DatabaseWrapper.DIRECTION_NAMES[DatabaseWrapper.DIRECTION_EAST], importDistributions(DatabaseWrapper.DIRECTION_EAST));
            distributions.put(DatabaseWrapper.DIRECTION_NAMES[DatabaseWrapper.DIRECTION_SOUTH], importDistributions(DatabaseWrapper.DIRECTION_SOUTH));
            distributions.put(DatabaseWrapper.DIRECTION_NAMES[DatabaseWrapper.DIRECTION_WEST], importDistributions(DatabaseWrapper.DIRECTION_WEST));
            runOnUiThread(() -> {Toast.makeText(IndoorLocalisationActivity.this, "Init Complete.", Toast.LENGTH_SHORT).show();});
        }).start();
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
        //Look up the closest x and y directions
        float degreesFromNorth = directionManager.getCurrentDegreesFromNorth();
        int[] directions = Helpers.getClosestDirections(degreesFromNorth);

        //Read RSSI values
        RoomSimulator sim = new RoomSimulator(8, 8, 100);
        Map<String, Double> rssiValues = sim.sampleRSSI(new Position(3, 4));

        //Get the associated maps
        Map<String, RoomMatrix<SkewGeneralizedNormalDistribution>> xDirectionData = distributions.get(DatabaseWrapper.DIRECTION_NAMES[directions[0]]);
        Map<String, RoomMatrix<SkewGeneralizedNormalDistribution>> yDirectionData = distributions.get(DatabaseWrapper.DIRECTION_NAMES[directions[1]]);

        //Length of arrays:
        int xLen = xDirectionData.get("SCSLAB_AP_1_2GHZ").xArrayLength;;
        int yLen = xDirectionData.get("SCSLAB_AP_1_2GHZ").yArrayLength;

        Double[][] xProbabilities = new Double[yLen][xLen];
        for (String accessPointName : xDirectionData.keySet()) {
            RoomMatrix<SkewGeneralizedNormalDistribution> currentAccessPointDistributions = xDirectionData.get(accessPointName);
            for (int row = 0; row < currentAccessPointDistributions.yArrayLength; row++) {
                for (int col = 0; col < currentAccessPointDistributions.xArrayLength; col++) {
                    SkewGeneralizedNormalDistribution distributionAtPoint = currentAccessPointDistributions.getValueAtIndex(row, col);
                    double probability = distributionAtPoint.pdf(rssiValues.get(accessPointName));

//                    if (xProbabilities[row][col] == null) {
                        xProbabilities[row][col] = probability;
//                    } else {
//                        xProbabilities[row][col] += probability;
//                    }
                }
            }

            Log.d("Riccardo", "-------------------------");
            for (Double[] row : xProbabilities) {
                String string = "";
                for (Double prob : row) {
                    string += (prob > 0.015 ? "#" : " ") + ",";
                }
                Log.d("Riccardo", string);
            }
        }
    }

    public Map<String, RoomMatrix<SkewGeneralizedNormalDistribution>> importDistributions(int direction) {
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
            Map<String, RoomMatrix<SkewGeneralizedNormalDistribution>> RSSIDistributions = new HashMap<>();
            for (Iterator<String> it = RSSIDistributionJSON.keys(); it.hasNext(); ) {
                String accessPointName = it.next();
                JSONArray accessPointDataJSON = RSSIDistributionJSON.getJSONArray(accessPointName);
                Map<Position, SkewGeneralizedNormalDistribution> accessPointData = new HashMap<>();
                for (int i = 0; i < accessPointDataJSON.length(); i++) {
                    JSONObject positionDistributionJSON = accessPointDataJSON.getJSONObject(i);

                    Position position = new Position(
                        positionDistributionJSON.getDouble("x"),
                        positionDistributionJSON.getDouble("y")
                    );

                    SkewGeneralizedNormalDistribution distribution = new SkewGeneralizedNormalDistribution(
                        positionDistributionJSON.getDouble("loc"),
                        positionDistributionJSON.getDouble("scale"),
                        positionDistributionJSON.getDouble("skew")
                    );

                    accessPointData.put(position, distribution);
                }
                RSSIDistributions.put(accessPointName, new RoomMatrix<SkewGeneralizedNormalDistribution>(accessPointData, SkewGeneralizedNormalDistribution.class));
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