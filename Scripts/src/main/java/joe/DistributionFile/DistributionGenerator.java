package joe.DistributionFile;

import de.lmu.ifi.dbs.elki.math.statistics.distribution.SkewGeneralizedNormalDistribution;
import de.lmu.ifi.dbs.elki.math.statistics.distribution.estimator.SkewGNormalLMMEstimator;
import de.lmu.ifi.dbs.elki.utilities.datastructures.arraylike.DoubleArrayAdapter;
import joe.Model.Helpers;
import joe.Model.Position;
import joe.Utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistributionGenerator {

    /**
     * Get RSSI data from JSONArray(local_records.json)
     * @return
     */
    public static List<Map<String, Map<Position, List<Double>>>> getRSSIData(JSONArray records) {
        //Prepare data structure
        List<Map<String, Map<Position, List<Double>>>> parsedDirectionalRSSIData = new ArrayList<>();
        // Every direction has a map
        for (int i = 0; i < 4; i++) {
            Map<String, Map<Position, List<Double>>> parsedRSSIData = new HashMap<>();
            parsedRSSIData.put("SCSLAB_AP_1_2GHZ", new HashMap<>());
            parsedRSSIData.put("SCSLAB_AP_1_5GHZ", new HashMap<>());
            parsedRSSIData.put("SCSLAB_AP_2_2GHZ", new HashMap<>());
            parsedRSSIData.put("SCSLAB_AP_2_5GHZ", new HashMap<>());
            parsedRSSIData.put("SCSLAB_AP_3_2GHZ", new HashMap<>());
            parsedRSSIData.put("SCSLAB_AP_3_5GHZ", new HashMap<>());
            parsedRSSIData.put("SCSLAB_AP_4_2GHZ", new HashMap<>());
            parsedRSSIData.put("SCSLAB_AP_4_5GHZ", new HashMap<>());
            parsedDirectionalRSSIData.add(parsedRSSIData);
        }
        for (int i = 0; i < records.length(); i++) {
            JSONObject data = records.getJSONObject(i);
            Double angle = data.getDouble("angle");
            //Find the map corresponding to the observation's direction
            int direction = Helpers.getDirection(angle);
            Map<String, Map<Position, List<Double>>> relevantDataMap = parsedDirectionalRSSIData.get(direction);

            //Add the data to this map:
            Double reference_x = data.getDouble("ref_x");
            Double reference_y = data.getDouble("ref_y");
            JSONArray RSSIObservations = data.getJSONArray("rssi_observations");
            Position position = new Position(reference_x, reference_y);
            for (int j = 0; j < RSSIObservations.length(); j++) {
                JSONObject observation = RSSIObservations.getJSONObject(j);
                String accessPointName = observation.getString("SSID");
                Double RSSI = observation.getDouble("RSSI");
                if (!relevantDataMap.containsKey(accessPointName)) {
                    continue;
                }
                Map<Position, List<Double>> accessPointDataMap = relevantDataMap.get(accessPointName);
                List<Double> rssiList;
                if (accessPointDataMap.containsKey(position)) {
                    rssiList = accessPointDataMap.get(position);
                } else {
                    rssiList = new ArrayList<>();
                }
                rssiList.add(RSSI);

                accessPointDataMap.put(position, rssiList);
            }
        }
        return parsedDirectionalRSSIData;
    }

    /**
     * Process the distributions given parsedDirectionalRSSIData
     * @param RSSIDirectionData parsedDirectionalRSSIData
     * @return
     */
    public static List<Map<String, Map<Position, List<Double>>>> processDistributions(List<Map<String, Map<Position, List<Double>>>> RSSIDirectionData) {
        ArrayList<Map<String, Map<Position, List<Double>>>> directionalDistributionData = new ArrayList<>();
        for (Map<String, Map<Position, List<Double>>> RSSIData : RSSIDirectionData) {
            Map<String, Map<Position, List<Double>>> distributionData = new HashMap<>();
            for (String accessPointName : RSSIData.keySet()) {
                Map<Position, List<Double>> accessPointDistributionData = new HashMap<>();
                Map<Position, List<Double>> accessPointData = RSSIData.get(accessPointName);
                for (Position position : accessPointData.keySet()) {
                    double[] positionRSSIData = accessPointData.get(position).stream().mapToDouble(i -> i).toArray();
                    if (positionRSSIData.length < 9) {
                        continue; //not enough data to fit a regression model -> throws an exception
                    }

                    SkewGeneralizedNormalDistribution distribution = SkewGNormalLMMEstimator.STATIC.estimate(positionRSSIData, DoubleArrayAdapter.STATIC);
                    List<Double> distributionParameters = new ArrayList<>();
                    distributionParameters.add(distribution.getLocation());
                    distributionParameters.add(distribution.getScale());
                    distributionParameters.add(distribution.getSkew());
                    accessPointDistributionData.put(position, distributionParameters);
                }
                distributionData.put(accessPointName, accessPointDistributionData);
            }
            directionalDistributionData.add(distributionData);
        }

        return directionalDistributionData;
    }

    public static void saveDataJSON(List<Map<String, Map<Position, List<Double>>>> RSSIDirectionalDistributions, Path path) {
        try {
            JSONObject RSSIDirectionalDistributionsJSON = new JSONObject();
            int index = 0;
            for (Map<String, Map<Position, List<Double>>> directionaldistributionData : RSSIDirectionalDistributions) {
                JSONObject directionalDistributionDataJSON = new JSONObject();
                for (String accessPointName : directionaldistributionData.keySet()) {
                    Map<Position, List<Double>> accessPointData = directionaldistributionData.get(accessPointName);
                    JSONArray positionDataJSON = new JSONArray();
                    for (Position position : accessPointData.keySet()) {
                        List<Double> distributionData = accessPointData.get(position);
                        JSONObject distributionJSON = new JSONObject();
                        distributionJSON.put("x", position.x);
                        distributionJSON.put("y", position.y);
                        distributionJSON.put("loc", distributionData.get(0));
                        distributionJSON.put("scale", distributionData.get(1));
                        distributionJSON.put("skew", distributionData.get(2));
                        positionDataJSON.put(distributionJSON);
                    }
                    directionalDistributionDataJSON.put(accessPointName, positionDataJSON);
                }

                RSSIDirectionalDistributionsJSON.put(Helpers.DIRECTION_NAMES[index], directionalDistributionDataJSON);
                index++;
            }

            //Write this JSON to a file
            String distributionData = RSSIDirectionalDistributionsJSON.toString();
            JSONUtils.writeFile(path, distributionData);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generateDistributionFile(Path localRecordsPath, Path distributionPath) {
        // Retrieving RSSI Data from local_records.json
        JSONArray records = JSONUtils.loadJSONArray(localRecordsPath);
        List<Map<String, Map<Position, List<Double>>>> RSSIData = getRSSIData(records);
        // Processing RSSI Distributions
        List<Map<String, Map<Position, List<Double>>>> RSSIDistributions = processDistributions(RSSIData);
        // Saving RSSI Distributions
        saveDataJSON(RSSIDistributions, distributionPath);
    }

    public static void main(String[] args) {
        Path localRecordsPath = Paths.get("data", "local_records.json");
        Path distributionPath = Paths.get("data", "distributions.json");
        generateDistributionFile(localRecordsPath, distributionPath);
    }
}
