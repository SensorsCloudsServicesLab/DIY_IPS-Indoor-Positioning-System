package indoorpositioningmodel;

public class IndoorPositioningSettings {

    /**
     * The highest x value of all reference points
     */
    public static final float ROOM_WIDTH = 11;

    /**
     * The highest y value of all reference points
     */
    public static final float ROOM_HEIGHT = 11;

    /**
     * The difference between reference points after GPR has been applied
     */
    public static final double REFERENCE_POINT_DISTANCE = 0.2;

    /**
     * The number of samples taken at each reference point
     */
    public static final int NUM_OBSERVATIONS = 50;

    /**
     * The percentage of the highest probability that is required for the point to be treated as valid
     */
    public static final double THRESHOLD_PROBABILITY_PERCENTAGE = 0.35;

    /**
     * If true:
     * a) The Process Distributions function generates simulated data instead of pulling it from the database
     * b) The observed RSSI values are simulated in the Indoor Localisation Activity
     */
    public static final boolean SHOULD_SIMULATE = true;

    //TODO: use these
    /**
     * If the x axis has been defined as the opposite as the normal direction, you change this to -1
     */
    public static final double PDR_X_AXIS_FLIP = 1;

    /**
     * If the y axis has been defined as the opposite as the normal direction, you change this to -1
     */
    public static final double PDR_Y_AXIS_FLIP = -1;

    /**
     * The below settings help map positions to the image in the indoor positioning activity.
     * This must only be changed if the floor plan image changes.
     * VISUALISER_ROOM_WIDTH and VISUALISER_ROOM_HEIGHT represent the dimensions of the room as seen in the image
     * roomTopLeft is the x and y coordinates of the room's top left corner in a percentage of the image width/height
     * roomBottomRight is the x and y coordinates of the room's bottom right corner in a percentage of he image width/height
     */
    public static final double VISUALISER_ROOM_WIDTH = 13.5;
    public static final double VISUALISER_ROOM_HEIGHT = 13.5;
    public static final Position ROOM_TOP_LEFT = new Position(0.009, 0.009);  //in percentage
    public static final Position ROOM_BOTTOM_RIGHT = new Position(0.964, 0.977); //in percentage
}
