package com.scslab.indoorpositioning;

import android.app.Activity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smile.stat.distribution.LogNormalDistribution;

class DistributionProcessor {

    public static void getDataFromDatabase(Activity activity, boolean should_simulate_data, DatabaseWrapper.OnCompleteListener onCompleteListener) {
        if (should_simulate_data) {
            RoomSimulator simulator = new RoomSimulator(8, 8, 100);
            onCompleteListener.onComplete(simulator.simulateForAllDirections());
            return;
        }

        DatabaseWrapper db = new DatabaseWrapper(activity);
        db.getRSSIDataFromDatabase(onCompleteListener);
    }

    public static List<Map<String, Map<Position, List<Double>>>> processDistributions(List<Map<String, Map<Position, List<Double>>>> RSSIDirectionData) {
        ArrayList<Map<String, Map<Position, List<Double>>>> directionalDistributionData = new ArrayList<>();
        for (Map<String, Map<Position, List<Double>>> RSSIData : RSSIDirectionData) {
            Map<String, Map<Position, List<Double>>> distributionData = new HashMap<>();
            for (String accessPointName : RSSIData.keySet()) {
                Map<Position, List<Double>> accessPointDistributionData = new HashMap<>();
                Map<Position, List<Double>> accessPointData = RSSIData.get(accessPointName);
                for (Position position : accessPointData.keySet()) {
                    double[] positionRSSIData = prepareDataForDistribution(accessPointData.get(position));
                    if (positionRSSIData.length < 2) {
                        continue; //not enough data to fit a regression model -> throws an exception
                    }

                    LogNormalDistribution distribution = LogNormalDistribution.fit(positionRSSIData);
                    List<Double> distributionParameters = new ArrayList<>();
                    distributionParameters.add(distribution.mu);
                    distributionParameters.add(distribution.sigma);
                    accessPointDistributionData.put(position, distributionParameters);
                }
                distributionData.put(accessPointName, accessPointDistributionData);
            }
            directionalDistributionData.add(distributionData);
        }

        return directionalDistributionData;
    }

    public static double[] prepareDataForDistribution(List<Double> list) {
        double[] array = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = Math.abs(list.get(i));
        }
        return array;
    }

    public static void saveDataJSON(Activity activity, List<Map<String, Map<Position, List<Double>>>> RSSIDirectionalDistributions) {
        try {
            JSONObject RSSIDirectionalDistributionsJSON = new JSONObject();
            int index = 0;
            for (Map<String, Map<Position, List<Double>>> directionaldistributionData : RSSIDirectionalDistributions) {
                JSONObject directionalDistributionDataJSON = new JSONObject();
                for (String accessPointName : directionaldistributionData.keySet()) {
                    Map<Position, List<Double>> accessPointData = directionaldistributionData.get(accessPointName);
                    JSONArray positionDataJSON = new JSONArray();
                    for (Position position : accessPointData.keySet()) {
                        List<Double> distributionData = accessPointData.get(position);
                        JSONObject distributionJSON = new JSONObject();
                        distributionJSON.put("ref_x", position.x);
                        distributionJSON.put("ref_y", position.y);
                        distributionJSON.put("mu", distributionData.get(0));
                        distributionJSON.put("sigma", distributionData.get(1));
                        positionDataJSON.put(distributionJSON);
                    }
                    directionalDistributionDataJSON.put(accessPointName, positionDataJSON);
                }

                RSSIDirectionalDistributionsJSON.put(DatabaseWrapper.DIRECTION_NAMES[index], directionalDistributionDataJSON);
                index++;
            }

            //Write this JSON to a file
            String distributionData = RSSIDirectionalDistributionsJSON.toString();
            File path = activity.getApplicationContext().getExternalFilesDir(null);
            File distributionDataFile = new File(path, "distributions.json");

            FileOutputStream stream = new FileOutputStream(distributionDataFile);
            stream.write(distributionData.getBytes());
            stream.close();

        } catch (JSONException e) {
            Log.d("Riccardo | JSONException", e.getMessage());
        } catch (IOException e) {
            Log.d("Riccardo | IOException", e.getMessage());
        }
    }
}
