package indoorpositioningmodel;

public class IndoorPositioningSettings {

    public static final String RSSI_OBSERVATIONS_COLLECTION_NAME = "rssi_records_200";

    /**
     * The highest x value of all reference points
     */
    public static final float ROOM_WIDTH = 5;

    /**
     * The highest y value of all reference points
     */
    public static final float ROOM_HEIGHT = 5;

    /**
     * The difference between reference points when measuring (For the simulator)
     */
    public static final double REFERENCE_POINT_MEASURED_DISTANCE = 1;

    /**
     * The difference between reference points after GPR has been applied
     */
    public static final double REFERENCE_POINT_DISTANCE = 0.2;

    /**
     * The number of samples taken at each reference point
     */
    public static final int NUM_OBSERVATIONS = 5;

    /**
     * The percentage of the highest probability that is required for the point to be treated as valid
     */
    public static final double THRESHOLD_PROBABILITY_PERCENTAGE = 0.7;

    /**
     * If true:
     * a) The Process Distributions function generates simulated data instead of pulling it from the database
     * b) The observed RSSI values are simulated in the Indoor Localisation Activity
     */
    public static final boolean SHOULD_SIMULATE = false;

    /**
     * If the x axis has been defined as the opposite as the normal direction, you change this to -1
     */
    public static final double PDR_X_AXIS_FLIP = -1;

    /**
     * The below settings help map positions to the image in the indoor positioning activity.
     * This must only be changed if the floor plan image changes.
     * VISUALISER_ROOM_WIDTH and VISUALISER_ROOM_HEIGHT represent the dimensions of the room as seen in the image
     * roomTopLeft is the x and y coordinates of the room's top left corner in a percentage of the image width/height
     * roomBottomRight is the x and y coordinates of the room's bottom right corner in a percentage of he image width/height
     */
    public static final double VISUALISER_ROOM_WIDTH = 0;
    public static final double VISUALISER_ROOM_HEIGHT = 0;
    public static final Position ROOM_TOP_LEFT = new Position(0.0, 0.0);  //in percentage
    public static final Position ROOM_BOTTOM_RIGHT = new Position(1.0, 1.0); //in percentage

    /**
     * The number of test taken for each direction at each reference point
     */
    public static final int NUM_TESTS = 5;
}
