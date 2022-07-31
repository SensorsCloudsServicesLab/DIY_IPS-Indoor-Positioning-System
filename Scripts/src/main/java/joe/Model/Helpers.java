package joe.Model;

import java.util.ArrayList;
import java.util.List;

public class Helpers {
    public static int DIRECTION_NORTH = 0;
    public static int DIRECTION_EAST = 1;
    public static int DIRECTION_SOUTH = 2;
    public static int DIRECTION_WEST = 3;
    public static String[] DIRECTION_NAMES = new String[] {
            "north",
            "east",
            "south",
            "west"
    };

    public static List<Double> linearArray(double start, double end, double gap) {
        List<Double> list = new ArrayList<>();
        for (double i = start; i <= end; i += gap) {
            list.add(i);
        }
        return list;
    }

    public static int getDirection(Double angleFromNorth) {
        if (angleFromNorth > 315 || angleFromNorth <= 45) {
            return DIRECTION_NORTH;
        } else if (angleFromNorth > 45 && angleFromNorth <= 135) {
            return DIRECTION_EAST;
        } else if (angleFromNorth > 135 && angleFromNorth < 225) {
            return DIRECTION_SOUTH;
        } else if (angleFromNorth <= 315 && angleFromNorth > 225) {
            return DIRECTION_WEST;
        }
        return DIRECTION_NORTH;
    }


//    public static int getDirection(Double angleFromNorth) {
//        if (angleFromNorth > -45 && angleFromNorth <= 45) {
//            return DIRECTION_NORTH;
//        } else if (angleFromNorth > 45 && angleFromNorth <= 135) {
//            return DIRECTION_EAST;
//        } else if (angleFromNorth > 135 && angleFromNorth <= 180 || angleFromNorth <= -135 && angleFromNorth >= -180) {
//            return DIRECTION_SOUTH;
//        } else if (angleFromNorth <= -45 && angleFromNorth > -135) {
//            return DIRECTION_WEST;
//        }
//        return DIRECTION_NORTH;
//    }

    public static int[] getClosestDirections(float angleFromNorth) {
        if (angleFromNorth >= 0 && angleFromNorth < 90) {
            return new int[] {DIRECTION_EAST, DIRECTION_NORTH};
        } else if (angleFromNorth >= 90 && angleFromNorth <= 180) {
            return new int[] {DIRECTION_EAST, DIRECTION_SOUTH};
        } else if (angleFromNorth <= -90 && angleFromNorth >= -180) {
            return new int[] {DIRECTION_WEST, DIRECTION_SOUTH};
        } else if (angleFromNorth < 0 && angleFromNorth >= -90) {
            return new int[] {DIRECTION_WEST, DIRECTION_NORTH};
        }
        return new int[] {DIRECTION_NORTH, DIRECTION_NORTH};
    }
}
