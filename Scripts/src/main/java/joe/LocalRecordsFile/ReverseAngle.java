package joe.LocalRecordsFile;

import joe.Utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReverseAngle {

    public static void reverseAngle(Path localRecordsPath, Path distributionPath) {
        // load data
        JSONArray records = JSONUtils.loadJSONArray(localRecordsPath);
        for (int i = 0; i < records.length(); i++) {
            JSONObject data = records.getJSONObject(i);
            double angle = data.getDouble("angle");
            // reverse the angle by 180 degree
            Double reverse = (angle + 180) % 360;
            data.put("angle", reverse);
        }
        String s = records.toString();
        // save it to file
        JSONUtils.writeFile(distributionPath, s);
    }

    public static void main(String[] args) {
        Path localRecordsPath = Paths.get("data", "local_records.json");
        Path distributionPath = Paths.get("data", "reversed_angle.json");
        reverseAngle(localRecordsPath, distributionPath);
    }
}
