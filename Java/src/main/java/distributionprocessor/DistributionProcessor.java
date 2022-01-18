package distributionprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smile.stat.distribution.LogNormalDistribution;

class DistributionProcessor {
    public static void main(String[] args) {
        System.out.println("Retrieving RSSI Data from database...");
        Map<String, Map<Position, List<Double>>> RSSIData = getDataFromDatabase(true);

        System.out.println("Processing RSSI Distributions...");
        Map<String, Map<Position, List<Double>>> RSSIDistributions = processDistributions(RSSIData);

        System.out.println("Uploading RSSI Distributions...");
        boolean success = uploadDistributions(RSSIDistributions);
        if (success) {
            System.out.println("Upload Successful");
        } else {
            System.out.println("Upload Failed");
        }
    }

    public static Map<String, Map<Position, List<Double>>> getDataFromDatabase(boolean should_simulate_data) {
        if (should_simulate_data) {
            RoomSimulator simulator = new RoomSimulator(8, 8, 100);
            return simulator.simulate();
        }

        //TODO Connect to database...
        return null;
    }

    public static Map<String, Map<Position, List<Double>>> processDistributions(Map<String, Map<Position, List<Double>>> RSSIData) {
        
        Map<String, Map<Position, List<Double>>> distributionData = new HashMap<>();
        for (String accessPointName : RSSIData.keySet()) {
            Map<Position, List<Double>> accessPointDistributionData = new HashMap<>();
            Map<Position, List<Double>> accessPointData = RSSIData.get(accessPointName);
            for (Position position : accessPointData.keySet()) {
                double[] positionRSSIData = prepareDataForDistribution(accessPointData.get(position));
                LogNormalDistribution distribution = LogNormalDistribution.fit(positionRSSIData);
                List<Double> distributionParameters = new ArrayList<>();
                distributionParameters.add(distribution.mu);
                distributionParameters.add(distribution.sigma);
                accessPointDistributionData.put(position, distributionParameters);
            }
            distributionData.put(accessPointName, accessPointDistributionData);
        }
        
        return distributionData;
    }

    public static boolean uploadDistributions(Map<String, Map<Position, List<Double>>> RSSIDistributions) {

        System.out.println(RSSIDistributions.get("SCSLAB_AP_1_5GHZ"));

        return false;
    }

    public static double[] prepareDataForDistribution(List<Double> list) {
        double[] array = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = Math.abs(list.get(i));
        }
        return array;
    }

    //BEGIN TEST CODE
    public static void testFitDistribution() {
        double[] data = new double[] {
            69, 67, 68, 68, 67, 68, 68, 66, 70, 68, 71, 69, 69, 72, 70, 69, 68, 72, 70, 69, 67, 70, 72, 71, 67, 68, 67, 72, 67, 70, 71, 71, 69, 67, 68, 67, 70, 69, 67, 69, 68, 69, 69, 68, 67, 68, 68, 70, 70, 70, 71, 69, 69, 73, 68, 69, 69, 67, 70, 69, 68, 67, 70, 68, 67, 68, 66, 68, 70, 67, 68, 69, 67, 69, 70, 69, 69, 70, 69, 68, 67, 75, 70, 67, 71, 67, 68, 69, 67, 70, 69, 67, 71, 67, 68, 70, 69, 68, 69, 69
        };

        LogNormalDistribution dist = LogNormalDistribution.fit(data);
        dist = new LogNormalDistribution(dist.mu, dist.sigma);
        System.out.println(String.valueOf(dist.mean()));
        System.out.println(String.valueOf(dist.variance()));
        System.out.println(String.valueOf(dist.p(65)));
        System.out.println(String.valueOf(dist.length()));
        plotDistribution(dist);
    }

    public static void plotDistribution(LogNormalDistribution distribution) {
        double[] x = new double[200];
        double[] y = new double[200];

        for (int i = 0; i < 200; i++) {
            x[i] = distribution.mean() + (((double)i)/10);
            y[i] = distribution.p(x[i]);
        }

        Plotter fig = new Plotter();
        fig.plot(x, y, "-r", 2.0f, "AAPL");
        fig.RenderPlot();
        fig.saveas("MyPlot.jpeg",640,480);
    }
}