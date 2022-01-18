package distributionprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smile.stat.distribution.LogNormalDistribution;

public class RoomSimulator {

    private double roomWidth;
    private double roomHeight;
    private double observationsAtEachPoint;
    private Map<String, Position> accessPointPositions;
    private LogNormalDistribution noiseDistribution = new LogNormalDistribution(0, 2.5);

    public RoomSimulator(double roomWidth, double roomHeight, int observationsAtEachPoint) {
        this.roomWidth = roomWidth;
        this.roomHeight = roomHeight;
        this.observationsAtEachPoint = observationsAtEachPoint;
        this.accessPointPositions = new HashMap<>();
        this.accessPointPositions.put("SCSLAB_AP_1_2GHZ", new Position(0, 0));
        this.accessPointPositions.put("SCSLAB_AP_1_5GHZ", new Position(0, 0));
        this.accessPointPositions.put("SCSLAB_AP_2_2GHZ", new Position(0, roomHeight));
        this.accessPointPositions.put("SCSLAB_AP_2_5GHZ", new Position(0, roomHeight));
        this.accessPointPositions.put("SCSLAB_AP_3_2GHZ", new Position(roomWidth, 0));
        this.accessPointPositions.put("SCSLAB_AP_3_5GHZ", new Position(roomWidth, 0));
        this.accessPointPositions.put("SCSLAB_AP_4_2GHZ", new Position(roomWidth, roomHeight));
        this.accessPointPositions.put("SCSLAB_AP_4_5GHZ", new Position(roomWidth, roomHeight));
    }

    public Map<String, Map<Position, List<Double>>> simulate() {
        //Fill in access point names and empty position maps
        Map<String, Map<Position, List<Double>>> data = new HashMap<>();
        data.put("SCSLAB_AP_1_2GHZ", new HashMap<>());
        data.put("SCSLAB_AP_1_5GHZ", new HashMap<>());
        data.put("SCSLAB_AP_2_2GHZ", new HashMap<>());
        data.put("SCSLAB_AP_2_5GHZ", new HashMap<>());
        data.put("SCSLAB_AP_3_2GHZ", new HashMap<>());
        data.put("SCSLAB_AP_3_5GHZ", new HashMap<>());
        data.put("SCSLAB_AP_4_2GHZ", new HashMap<>());
        data.put("SCSLAB_AP_4_5GHZ", new HashMap<>());

        //Fill in all the position maps
        for (String accessPointName : data.keySet()) {
            Map<Position, List<Double>> accessPointData = data.get(accessPointName);
            for (Double x : Helpers.linearArray(0, this.roomWidth, 0.5)) {
                for (Double y : Helpers.linearArray(0, this.roomHeight, 0.5)) {
                    Position position = new Position(x, y);
                    List<Double> observations = new ArrayList<>();
                    for (int i = 0; i < this.observationsAtEachPoint; i++) {
                        double distance = position.distanceFrom(this.accessPointPositions.get(accessPointName));
                        observations.add(this.distanceToRSSI(distance) + this.getDistributionNoise());
                    }
                    accessPointData.put(position, observations);
                }
            }
        }

        return data;
    }

    public double getDistributionNoise() {
        return this.noiseDistribution.rand();
    }

    public double distanceToRSSI(double distance) {
        return Math.round(-6 * distance - 20);
    }
}
