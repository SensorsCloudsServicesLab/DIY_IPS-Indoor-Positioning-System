package com.scslab.indoorpositioning;

public class Intersection {

    public static double[] getIntersectionPoint(double x1, double y1, double r1, double x2, double y2, double r2){
        double distance = Math.sqrt(((x1-x2)*(x1-x2)) + ((y1-y2) * (y1-y2)));

        //     System.out.println(distance);

        double d1 = (r1 * r1 - r2*r2 + distance*distance)/ (2*distance);
        //      System.out.println(d1);

        double h = Math.sqrt((r1*r1 - d1*d1));
        //     System.out.println(h);

        double x3 = x1 + (d1 * (x2-x1)) / distance;

        double y3 = y1 + (d1 * (y2-y1)) / distance;

        //      System.out.println(x3);
        //      System.out.println(y3);

        double x4_i = x3 + (h * (y2 - y1)) / distance;

        double y4_i = y3 - (h * (x2 - x1)) / distance;

        double x4_ii = x3 - (h * (y2 - y1)) / distance;
        double y4_ii = y3 + (h * (x2 - x1)) / distance;

        System.out.println(x4_i);
        System.out.println(y4_i);
        System.out.println(x4_ii);
        System.out.println(y4_ii);

        double[] result = new double[4];
        result[0] = x4_i;
        result[1] = y4_i;
        result[2] = x4_ii;
        result[3] = y4_ii;

        return result;
    }


}
