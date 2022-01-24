package com.scslab.indoorpositioning;

import java.util.ArrayList;
import java.util.List;

public class Helpers {
    public static List<Double> linearArray(double start, double end, double gap) {
        List<Double> list = new ArrayList<>();
        for (double i = start; i <= end; i += gap) {
            list.add(i);
        }
        return list;
    }

    public static int getDirection(Double angleFromNorth) {
        if (angleFromNorth > -45 && angleFromNorth <= 45) {
            return DatabaseWrapper.DIRECTION_NORTH;
        } else if (angleFromNorth > 45 && angleFromNorth <= 135) {
            return DatabaseWrapper.DIRECTION_EAST;
        } else if (angleFromNorth > 135 && angleFromNorth <= 180 || angleFromNorth <= -135 && angleFromNorth >= -180) {
            return DatabaseWrapper.DIRECTION_SOUTH;
        } else if (angleFromNorth <= -45 && angleFromNorth > -135) {
            return DatabaseWrapper.DIRECTION_WEST;
        }
        return DatabaseWrapper.DIRECTION_NORTH;
    }

    public static int[] getClosestDirections(float angleFromNorth) {
        if (angleFromNorth >= 0 && angleFromNorth < 90) {
            return new int[] {DatabaseWrapper.DIRECTION_EAST, DatabaseWrapper.DIRECTION_NORTH};
        } else if (angleFromNorth >= 90 && angleFromNorth <= 180) {
            return new int[] {DatabaseWrapper.DIRECTION_EAST, DatabaseWrapper.DIRECTION_SOUTH};
        } else if (angleFromNorth <= -90 && angleFromNorth >= -180) {
            return new int[] {DatabaseWrapper.DIRECTION_WEST, DatabaseWrapper.DIRECTION_SOUTH};
        } else if (angleFromNorth < 0 && angleFromNorth >= -90) {
            return new int[] {DatabaseWrapper.DIRECTION_WEST, DatabaseWrapper.DIRECTION_NORTH};
        }
        return new int[] {DatabaseWrapper.DIRECTION_NORTH, DatabaseWrapper.DIRECTION_NORTH};
    }
}
