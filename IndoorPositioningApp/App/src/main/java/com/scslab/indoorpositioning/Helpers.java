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
}
