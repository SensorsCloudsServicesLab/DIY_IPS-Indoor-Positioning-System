package indoorpositioningmodel;

public class IndoorPositioningSettings {

    /**
     * The highest x value of all reference points
     */
    public static final float roomWidth = 11;

    /**
     * The highest y value of all reference points
     */
    public static final float roomHeight = 11;

    /**
     * The difference between reference points after GPR has been applied
     */
    public static final double referencePointDistance = 0.2;

    /**
     * The number of samples taken at each reference point
     */
    public static final int numObservations = 50;

    /**
     * The percentage of the highest probability that is required for the point to be treated as valid
     */
    public static final double thresholdProbabilityPercentage = 0.25;

    /**
     * If true:
     * a) The Process Distributions function generates simulated data instead of pulling it from the database
     * b) The observed RSSI values are simulated in the Indoor Localisation Activity
     */
    public static final boolean shouldSimulate = true;
}
