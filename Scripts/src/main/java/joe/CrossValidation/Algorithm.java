package joe.CrossValidation;

import de.lmu.ifi.dbs.elki.math.statistics.distribution.SkewGeneralizedNormalDistribution;
import joe.Model.*;
import joe.Utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Path;
import java.util.*;


public class Algorithm {

    private RoomMatrix<Double> probabilitySums;
    private int numSamples = 0;
    private final Map<String, Map<String, RoomMatrix<SkewGeneralizedNormalDistribution>>> distributions = new HashMap<>();
    private Path basePath;

    public static int DIRECTION_NORTH = 0;
    public static int DIRECTION_EAST = 1;
    public static int DIRECTION_SOUTH = 2;
    public static int DIRECTION_WEST = 3;
    public static String[] DIRECTION_NAMES = new String[]{
            "north",
            "east",
            "south",
            "west"
    };

    /**
     *
     * @param path path of x_distributions.json file
     */
    public Algorithm(Path path) {
        basePath = path;
        distributions.put(DIRECTION_NAMES[DIRECTION_NORTH], importDistributions(DIRECTION_NORTH));
        distributions.put(DIRECTION_NAMES[DIRECTION_EAST], importDistributions(DIRECTION_EAST));
        distributions.put(DIRECTION_NAMES[DIRECTION_SOUTH], importDistributions(DIRECTION_SOUTH));
        distributions.put(DIRECTION_NAMES[DIRECTION_WEST], importDistributions(DIRECTION_WEST));
    }

    private Map<String, RoomMatrix<SkewGeneralizedNormalDistribution>> importDistributions(int direction) {
        try {
            //Read data from file
            String directionName = DIRECTION_NAMES[direction];
            String f = directionName + "_distributions.json";
            Path distributionPath = basePath.resolve(f);
            JSONObject RSSIDistributionJSON = JSONUtils.loadJSONObject(distributionPath);

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

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Position getLocation(Fingerprint fingerprint) {
        //Look up the closest x and y directions
        double degreesFromNorth = fingerprint.getDegreesFromNorth();
        int direction = Helpers.getDirection(degreesFromNorth);

        //Read RSSI values
        Map<String, Double> rssiValues = fingerprint.getRssiValues();

        //Get the associated maps
        Map<String, RoomMatrix<SkewGeneralizedNormalDistribution>> directionData = distributions.get(DIRECTION_NAMES[direction]);

        RoomMatrix<Double> probabilities = calculateProbabilityMap(rssiValues, directionData);

        probabilitySums = probabilities;

//        probabilitySums = probabilities.operate((int row, int col) -> {
//            Double probability = probabilities.getValueAtIndex(row, col);
//            if (probability != null) {
//                if (numSamples == 0) {
//                    return probability;
//                } else {
//                    return (probabilitySums.getValueAtIndex(row, col) + probability);
//                }
//            }
//            return probabilitySums.getValueAtIndex(row, col);
//        });
//        numSamples++;

//        probabilitySums = probabilities.operate((int row, int col) -> {
//            double probability = probabilities.getValueAtIndex(row, col);
//            if (numSamples == 0) {
//                return probability;
//            } else {
//                return (probabilitySums.getValueAtIndex(row, col) + probability);
//            }
//        });
//        numSamples++;

        Double maxProbability = probabilitySums.getMaxValue(Comparator.comparingDouble(a -> a));
        Double thresholdProbability = maxProbability * (1 - IndoorPositioningSettings.THRESHOLD_PROBABILITY_PERCENTAGE);
//        for (int row = 0; row < probabilitySums.yArrayLength; row++) {
//            StringBuilder string = new StringBuilder();
//            for (int col = 0; col < probabilitySums.xArrayLength; col++) {
//                string.append(probabilitySums.getValueAtIndex(row, col) > thresholdProbability ? "#," : " ,");
//            }
//        }

        return calcCentroid(probabilitySums, thresholdProbability);
    }

    private RoomMatrix<Double> calculateProbabilityMap(Map<String, Double> rssiValues, Map<String, RoomMatrix<SkewGeneralizedNormalDistribution>> accessPointData) {
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
//        Double[][] probabilities = new Double[rows][cols];
        Double[][] probabilities = new Double[56][56];

        for (String accessPointName : accessPointNames) {
            RoomMatrix<SkewGeneralizedNormalDistribution> current2GHZAccessPointDistributions = accessPointData.get(accessPointName + "_2GHZ");
            RoomMatrix<SkewGeneralizedNormalDistribution> current5GHZAccessPointDistributions = accessPointData.get(accessPointName + "_5GHZ");
            for (int row = 0; row < current2GHZAccessPointDistributions.yArrayLength; row++) {
                for (int col = 0; col < current2GHZAccessPointDistributions.xArrayLength; col++) {
                    SkewGeneralizedNormalDistribution distribution2GHZ = current2GHZAccessPointDistributions.getValueAtIndex(row, col);
                    SkewGeneralizedNormalDistribution distribution5GHZ = current5GHZAccessPointDistributions.getValueAtIndex(row, col);

                    if (!rssiValues.containsKey(accessPointName + "_2GHZ") && !rssiValues.containsKey(accessPointName + "_5GHZ")) {
                        continue;
                    }

                    double probability;
                    if (distribution2GHZ == null || distribution5GHZ == null) {
                        probability = 0;
                    } else {
                        double probability2GHZ = rssiValues.containsKey(accessPointName + "_2GHZ") ? distribution2GHZ.pdf(rssiValues.get(accessPointName + "_2GHZ")) : 1;
                        double probability5GHZ = rssiValues.containsKey(accessPointName + "_5GHZ") ? distribution5GHZ.pdf(rssiValues.get(accessPointName + "_5GHZ")) : 1;
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

    private Position calcCentroid(RoomMatrix<Double> map, Double threshold) {
        double x = 0;
        double y = 0;
        int numAboveThreshold = 0;
        for (int row = 0; row < map.yArrayLength; row++) {
            for (int col = 0; col < map.xArrayLength; col++) {
                if (map.getValueAtIndex(row, col) == null) continue;
                if (map.getValueAtIndex(row, col) > threshold) {
                    x += col * IndoorPositioningSettings.REFERENCE_POINT_DISTANCE;
                    y += row * IndoorPositioningSettings.REFERENCE_POINT_DISTANCE;
                    numAboveThreshold++;
                }
            }
        }
        return new Position(x / numAboveThreshold, y / numAboveThreshold);
    }

}
