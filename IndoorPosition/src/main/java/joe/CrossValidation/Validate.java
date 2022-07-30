package joe.CrossValidation;

import joe.Model.Fingerprint;
import joe.Model.Position;
import joe.Utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Validate {

    public static void crossValidation() {
        List<Double> allMeanError = new ArrayList<>();
        List<Double> allStdev = new ArrayList<>();
        // test accuracy on every fold
        for (int i = 1; i <= CrossValidationConstant.FOLD; i++) {
            // errors in this round
            List<Double> errorsList = new ArrayList<>();

            String dir = String.valueOf(i);
            Path testDataPath = CrossValidationConstant.preBase
                    .resolve(dir)
                    .resolve(CrossValidationConstant.testDataFileName);
            // load test.json
            JSONArray jsonArray = JSONUtils.loadJSONArray(testDataPath);

            // path of model(x_distributions.json)
            Path modelPath = CrossValidationConstant.modelDataPath.resolve(dir);
            Algorithm algorithm = new Algorithm(modelPath);

            for (int j = 0; j < jsonArray.length(); j++) {
                JSONObject data = jsonArray.getJSONObject(j);
                double reference_x = data.getDouble("ref_x");
                double reference_y = data.getDouble("ref_y");
                Fingerprint fingerprint = jsonObjectToFingerprint(data);
                // use algorithm to estimate location given fingerprint
                Position estimateLocation = algorithm.getLocation(fingerprint);
                double error = calculateError(reference_x, reference_y, estimateLocation.x, estimateLocation.y);
                errorsList.add(error);
            }

            // accuracy and standard deviation
            double meanError = calculateAvg(errorsList);
            double stdev = calculateStdev(errorsList);
            System.out.println("Fold " + i + " Mean Error: " + String.format("%.2f", meanError) + ", Standard Deviation: " +  String.format("%.2f", stdev));
            allMeanError.add(meanError);
            allStdev.add(stdev);
        }
        double mean = calculateAvg(allMeanError);
        double stdev = calculateStdev(allStdev);
        System.out.println("Overall, Mean Error: " +  String.format("%.2f", mean) + ", Standard Deviation: " +  String.format("%.2f", stdev));
    }

    private static double calculateError(double relX, double relY, double estX, double estY) {
        return Math.sqrt(Math.pow(relX - estX, 2) + Math.pow(relY - estY, 2));
    }

    private static Fingerprint jsonObjectToFingerprint(JSONObject data) {
        double angle = data.getDouble("angle");
        JSONArray RSSIObservations = data.getJSONArray("rssi_observations");

        Fingerprint fingerprint = new Fingerprint();
        fingerprint.setDegreesFromNorth(angle);
        fingerprint.setRssiValues(RSSIObservations);
        return fingerprint;
    }

    private static double calculateAvg(List<Double> list) {
        return list.stream().mapToDouble(d -> d).average().orElse(0.0);
    }

    private static double calculateStdev(List<Double> list) {
        double mean = calculateAvg(list);
        double stdev = 0.0;
        for (Double num : list) {
            stdev += Math.pow(num - mean, 2);
        }
        return Math.sqrt(stdev / list.size());
    }

    public static void main(String[] args) {
        crossValidation();
    }
}
