package com.scslab.indoorpositioning;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.lmu.ifi.dbs.elki.math.statistics.distribution.SkewGeneralizedNormalDistribution;

public class IndoorPositioningRSSIModel {

    private WeakReference<Activity> activityReference;

    private final Map<String, Map<String, RoomMatrix<SkewGeneralizedNormalDistribution>>> distributions = new HashMap<>();
    private final double thresholdProbabilityPercentage = 0.15; //The percentage of the highest probability that is required for the point to be treated as valid

    private WifiManager wifiManager;
    private DirectionManager directionManager;

    public IndoorPositioningRSSIModel(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        this.activityReference = new WeakReference<>(activity);
        this.wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.directionManager = new DirectionManager(activity);

        new Thread(() -> {
            activity.runOnUiThread(() -> Toast.makeText(activity, "Importing Data...", Toast.LENGTH_SHORT).show());
            distributions.put(DatabaseWrapper.DIRECTION_NAMES[DatabaseWrapper.DIRECTION_NORTH], importDistributions(DatabaseWrapper.DIRECTION_NORTH));
            distributions.put(DatabaseWrapper.DIRECTION_NAMES[DatabaseWrapper.DIRECTION_EAST], importDistributions(DatabaseWrapper.DIRECTION_EAST));
            distributions.put(DatabaseWrapper.DIRECTION_NAMES[DatabaseWrapper.DIRECTION_SOUTH], importDistributions(DatabaseWrapper.DIRECTION_SOUTH));
            distributions.put(DatabaseWrapper.DIRECTION_NAMES[DatabaseWrapper.DIRECTION_WEST], importDistributions(DatabaseWrapper.DIRECTION_WEST));
            activity.runOnUiThread(() -> Toast.makeText(activity, "Init Complete.", Toast.LENGTH_SHORT).show());
        }).start();
    }

    public Map<String, RoomMatrix<SkewGeneralizedNormalDistribution>> importDistributions(int direction) {
        Activity activity = activityReference.get();
        if (activity == null || activity.isFinishing()) {
            return null;
        }

        try {
            //Read data from file
            String directionName = DatabaseWrapper.DIRECTION_NAMES[direction];
            File path = activity.getApplicationContext().getExternalFilesDir(null);
            File distributionDataFile = new File(path, directionName + "_distributions.json");

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
                RSSIDistributions.put(accessPointName, new RoomMatrix<>(accessPointData, SkewGeneralizedNormalDistribution.class));
            }
            return RSSIDistributions;

        } catch (JSONException e) {
            Log.d("Riccardo | JSONException", e.getMessage());
        } catch (IOException e) {
            Log.d("Riccardo | IOException", e.getMessage());
        }

        return null;
    }

    public Position getLocation() {
        Activity activity = activityReference.get();
        if (activity == null || activity.isFinishing()) {
            return null;
        }

        //Check Permissions
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return null;
        }

        //Look up the closest x and y directions
        float degreesFromNorth = directionManager.getCurrentDegreesFromNorth();
        int[] directions = Helpers.getClosestDirections(degreesFromNorth);

        //Read RSSI values
        Map<String, Double> rssiValues = getRssiValues(true);

        //Get the associated maps
        Map<String, RoomMatrix<SkewGeneralizedNormalDistribution>> xDirectionData = distributions.get(DatabaseWrapper.DIRECTION_NAMES[directions[0]]);
        Map<String, RoomMatrix<SkewGeneralizedNormalDistribution>> yDirectionData = distributions.get(DatabaseWrapper.DIRECTION_NAMES[directions[1]]);

        //Length of arrays:
        int xLen = xDirectionData.get("SCSLAB_AP_1_2GHZ").xArrayLength;
        int yLen = xDirectionData.get("SCSLAB_AP_1_2GHZ").yArrayLength;

        //Get all the access point names without the GHZ part
        List<String> accessPointNames = new ArrayList<>();
        for (String accessPointName : xDirectionData.keySet()) {
            accessPointName = accessPointName.replace("_2GHZ", "");
            accessPointName = accessPointName.replace("_5GHZ", "");
            if (!accessPointNames.contains(accessPointName)) {
                accessPointNames.add(accessPointName);
            }
        }

        Double[][] xProbabilities = new Double[yLen][xLen];
        for (String accessPointName : accessPointNames) {
            RoomMatrix<SkewGeneralizedNormalDistribution> current2GHZAccessPointDistributions = xDirectionData.get(accessPointName+"_2GHZ");
            RoomMatrix<SkewGeneralizedNormalDistribution> current5GHZAccessPointDistributions = xDirectionData.get(accessPointName+"_2GHZ");
            for (int row = 0; row < current2GHZAccessPointDistributions.yArrayLength; row++) {
                for (int col = 0; col < current2GHZAccessPointDistributions.xArrayLength; col++) {
                    SkewGeneralizedNormalDistribution distribution2GHZ = current2GHZAccessPointDistributions.getValueAtIndex(row, col);
                    SkewGeneralizedNormalDistribution distribution5GHZ = current5GHZAccessPointDistributions.getValueAtIndex(row, col);
                    double probability = distribution2GHZ.pdf(rssiValues.get(accessPointName+"_2GHZ")) * distribution5GHZ.pdf(rssiValues.get(accessPointName+"_5GHZ"));

                    if (xProbabilities[row][col] == null) {
                        xProbabilities[row][col] = probability;
                    } else {
                        xProbabilities[row][col] += probability;
                    }
                }
            }
        }

        Double maxProbability = 0.0;
        for (Double[] row : xProbabilities) {
            for (Double prob : row) {
                if (prob > maxProbability) {
                    maxProbability = prob;
                }
            }
        }

        Double thresholdProbability = maxProbability*(1-thresholdProbabilityPercentage);
        for (Double[] row : xProbabilities) {
            StringBuilder string = new StringBuilder();
            for (Double prob : row) {
                string.append(prob > thresholdProbability ? "#" : " ").append(",");
            }
            Log.d("Riccardo", string.toString());
        }

        return new Position(0, 0);
    }

    public Map<String, Double> getRssiValues(boolean shouldSimulateData) {
        if (shouldSimulateData) {
            RoomSimulator sim = new RoomSimulator(8, 8, 50);
            return sim.sampleRSSI(new Position(3, 4));
        }

        wifiManager.startScan();
        List<ScanResult> wifiList = wifiManager.getScanResults();
        Map<String, Double> rssiValues = new HashMap<>();
        for (ScanResult scanResult : wifiList) {
            if (scanResult.SSID.contains("SCSLAB_AP")) {
                rssiValues.put(scanResult.SSID, (double) scanResult.level);
            }
        }
        return rssiValues;
    }

    public void onResume() {
        directionManager.onResume();
    }

    public void onPause() {
        directionManager.onPause();
    }
}
