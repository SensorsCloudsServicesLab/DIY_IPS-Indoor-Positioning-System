package joe.Model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Fingerprint {
    private double degreesFromNorth;
    private Map<String, Double> rssiValues;

    public double getDegreesFromNorth() {
        return degreesFromNorth;
    }

    public void setDegreesFromNorth(double degreesFromNorth) {
        this.degreesFromNorth = degreesFromNorth;
    }

    public Map<String, Double> getRssiValues() {
        return rssiValues;
    }

    public void setRssiValues(Map<String, Double> rssiValues) {
        this.rssiValues = rssiValues;
    }

    public void setRssiValues(JSONArray jsonArray) {
        Map<String, Double> map = new HashMap<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject data = jsonArray.getJSONObject(i);
            String ssid = data.getString("SSID");
            Double rssi = data.getDouble("RSSI");
            map.put(ssid, rssi);
        }
        rssiValues = map;
    }
}
