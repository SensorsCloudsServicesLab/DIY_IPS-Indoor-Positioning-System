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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.lmu.ifi.dbs.elki.math.statistics.distribution.SkewGeneralizedNormalDistribution;

public class IndoorPositioningRSSIModel {

    private WeakReference<Activity> activityReference;

    private final Map<String, Map<String, RoomMatrix<SkewGeneralizedNormalDistribution>>> distributions = new HashMap<>();
    private final double thresholdProbabilityPercentage = 0.4; //The percentage of the highest probability that is required for the point to be treated as valid

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
        Map<String, Double> rssiValues = getRssiValues(false);

        //Get the associated maps
        Map<String, RoomMatrix<SkewGeneralizedNormalDistribution>> xDirectionData = distributions.get(DatabaseWrapper.DIRECTION_NAMES[directions[0]]);
        Map<String, RoomMatrix<SkewGeneralizedNormalDistribution>> yDirectionData = distributions.get(DatabaseWrapper.DIRECTION_NAMES[directions[1]]);

        RoomMatrix<Double> xProbabilities = calculateProbabilityMap(rssiValues, xDirectionData);
        RoomMatrix<Double> yProbabilities = calculateProbabilityMap(rssiValues, yDirectionData);

        double xScale = Math.pow(Math.cos(degreesFromNorth + 90), 2);
        double yScale = Math.pow(Math.sin(degreesFromNorth + 90), 2);
        RoomMatrix<Double> probabilities = xProbabilities.operate((int row, int col) -> {
            return (xScale * xProbabilities.getValueAtIndex(row, col)) + (yScale * yProbabilities.getValueAtIndex(row, col));
        });

        Double maxProbability = probabilities.getMaxValue(Comparator.comparingDouble(a -> a));
        Double thresholdProbability = maxProbability*(1-thresholdProbabilityPercentage);
        for (int row = 0; row < probabilities.yArrayLength; row++) {
            StringBuilder string = new StringBuilder();
            for (int col = 0; col < probabilities.xArrayLength; col++) {
                string.append(probabilities.getValueAtIndex(row, col) > thresholdProbability ? "#," : " ,");
            }
            Log.d("Riccardo", string.toString());
        }

        return calcCentroid(probabilities, thresholdProbability);
    }

    public RoomMatrix<Double> calculateProbabilityMap(Map<String, Double> rssiValues, Map<String, RoomMatrix<SkewGeneralizedNormalDistribution>> accessPointData) {
        //Get all the access point names without the GHZ part
        List<String> accessPointNames = new ArrayList<>();
        for (String accessPointName : accessPointData.keySet()) {
            accessPointName = accessPointName.replace("_2GHZ", "");
            accessPointName = accessPointName.replace("_5GHZ", "");
            if (!accessPointNames.contains(accessPointName)) {
                accessPointNames.add(accessPointName);
            }
        }

        int cols = accessPointData.get(accessPointData.keySet().iterator().next()).xArrayLength;
        int rows = accessPointData.get(accessPointData.keySet().iterator().next()).yArrayLength;

        //Calculate
        Double[][] probabilities = new Double[rows][cols];
        for (String accessPointName : accessPointNames) {
            RoomMatrix<SkewGeneralizedNormalDistribution> current2GHZAccessPointDistributions = accessPointData.get(accessPointName+"_2GHZ");
            RoomMatrix<SkewGeneralizedNormalDistribution> current5GHZAccessPointDistributions = accessPointData.get(accessPointName+"_5GHZ");
            for (int row = 0; row < current2GHZAccessPointDistributions.yArrayLength; row++) {
                for (int col = 0; col < current2GHZAccessPointDistributions.xArrayLength; col++) {
                    SkewGeneralizedNormalDistribution distribution2GHZ = current2GHZAccessPointDistributions.getValueAtIndex(row, col);
                    SkewGeneralizedNormalDistribution distribution5GHZ = current5GHZAccessPointDistributions.getValueAtIndex(row, col);

                    if (!rssiValues.containsKey(accessPointName+"_2GHZ") && !rssiValues.containsKey(accessPointName+"_5GHZ")) {
                        continue;
                    }

                    double probability;
                    if (distribution2GHZ == null || distribution5GHZ == null) {
                        probability = 0;
                    } else {
                        double probability2GHZ = rssiValues.containsKey(accessPointName+"_2GHZ") ? distribution2GHZ.pdf(rssiValues.get(accessPointName+"_2GHZ")) : 1;
                        double probability5GHZ = rssiValues.containsKey(accessPointName+"_5GHZ") ? distribution5GHZ.pdf(rssiValues.get(accessPointName+"_5GHZ")) : 1;
                        probability = probability2GHZ * probability5GHZ;
                    }

                    if (probabilities[row][col] == null) {
                        probabilities[row][col] = probability;
                    } else {
                        probabilities[row][col] += probability;
                    }
                }
            }
        }

        RoomMatrix<Double> test = new RoomMatrix<>(probabilities);
        return test;
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

    public Position calcCentroid(RoomMatrix<Double> map, Double threshold) {
        double x = 0;
        double y = 0;
        int numAboveThreshold = 0;
        for (int row = 0; row < map.yArrayLength; row++) {
            for (int col = 0; col < map.xArrayLength; col++) {
                if (map.getValueAtIndex(row, col) > threshold) {
                    x += col*0.2;
                    y += row*0.2;
                    numAboveThreshold++;
                }
            }
        }
        return new Position(x/numAboveThreshold, y/numAboveThreshold);
    }

    public void onPause() {
        directionManager.onPause();
    }

    public void onResume() {
        directionManager.onResume();
    }
}
