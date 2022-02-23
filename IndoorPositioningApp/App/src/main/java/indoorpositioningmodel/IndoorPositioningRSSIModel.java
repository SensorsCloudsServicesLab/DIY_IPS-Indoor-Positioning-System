package indoorpositioningmodel;

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

    private WifiManager wifiManager;
    private DirectionManager directionManager;
    private final Map<String, Map<String, RoomMatrix<SkewGeneralizedNormalDistribution>>> distributions = new HashMap<>();
    private RoomMatrix<Double> probabilitySums;
    private int numSamples = 0;

    public IndoorPositioningRSSIModel(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        this.activityReference = new WeakReference<>(activity);
        this.wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.directionManager = new DirectionManager(activity, null);

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
            Log.d("IndoorPositioning | JSONException", e.getMessage());
        } catch (IOException e) {
            Log.d("IndoorPositioning | IOException", e.getMessage());
        }

        return null;
    }

    public Position getLocation() {
        Activity activity = activityReference.get();
        if (activity == null || activity.isFinishing()) {
            return null;
        }

        //Look up the closest x and y directions
        double degreesFromNorth = directionManager.getCurrentDegreesFromNorth();
        int direction = Helpers.getDirection(degreesFromNorth);

        //Read RSSI values
        Map<String, Double> rssiValues = getRssiValues();

        //Get the associated maps
        Map<String, RoomMatrix<SkewGeneralizedNormalDistribution>> directionData = distributions.get(DatabaseWrapper.DIRECTION_NAMES[direction]);

        RoomMatrix<Double> probabilities = calculateProbabilityMap(rssiValues, directionData);

        probabilitySums = probabilities.operate((int row, int col) -> {
            double probability = probabilities.getValueAtIndex(row, col);
            if (numSamples == 0) {
                return probability;
            } else {
                return (probabilitySums.getValueAtIndex(row, col) + probability);
            }
        });
        numSamples++;

        Double maxProbability = probabilitySums.getMaxValue(Comparator.comparingDouble(a -> a));
        Double thresholdProbability = maxProbability*(1-IndoorPositioningSettings.THRESHOLD_PROBABILITY_PERCENTAGE);
        for (int row = 0; row < probabilitySums.yArrayLength; row++) {
            StringBuilder string = new StringBuilder();
            for (int col = 0; col < probabilitySums.xArrayLength; col++) {
                string.append(probabilitySums.getValueAtIndex(row, col) > thresholdProbability ? "#," : " ,");
            }
            Log.d("IndoorPositioning", string.toString());
        }

        return calcCentroid(probabilitySums, thresholdProbability);
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

    public Map<String, Double> getRssiValues() {
        if (IndoorPositioningSettings.SHOULD_SIMULATE) {
            RoomSimulator sim = new RoomSimulator(IndoorPositioningSettings.ROOM_WIDTH, IndoorPositioningSettings.ROOM_HEIGHT, IndoorPositioningSettings.NUM_OBSERVATIONS);
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
                    x += col * IndoorPositioningSettings.REFERENCE_POINT_DISTANCE;
                    y += row * IndoorPositioningSettings.REFERENCE_POINT_DISTANCE;
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
