package indoorpositioningmodel;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.scslab.indoorpositioning.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoeModel {
    private WeakReference<Activity> activityReference;
    private WifiManager wifiManager;
    private DirectionManager directionManager;
    private Map<String, Integer> indexMap;
    double[] max = new double[]{-34., -47., -29., -44., -28., -43., -13., -33.};
    double[] min = new double[]{-91., -97., -94., -97., -85., -96., -79., -96.};

    public JoeModel(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        this.activityReference = new WeakReference<>(activity);
        this.wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.directionManager = new DirectionManager(activity, null);
        indexMap = new HashMap<>();
        indexMap.put("SCSLAB_AP_1_2GHZ", 1);
        indexMap.put("SCSLAB_AP_1_5GHZ", 2);
        indexMap.put("SCSLAB_AP_2_2GHZ", 3);
        indexMap.put("SCSLAB_AP_2_5GHZ", 4);
        indexMap.put("SCSLAB_AP_3_2GHZ", 5);
        indexMap.put("SCSLAB_AP_3_5GHZ", 6);
        indexMap.put("SCSLAB_AP_4_2GHZ", 7);
        indexMap.put("SCSLAB_AP_4_5GHZ", 8);
    }

    public Position getLocation(double degreesFromNorth) {
        //Read RSSI values
        Map<String, Double> rssiValues = getRssiValues();
        ByteBuffer byteBuffer = extractFeature(degreesFromNorth, rssiValues);
        float[] floats = estimateLocation(byteBuffer);
        return new Position(floats[0], floats[1]);
    }

    public ByteBuffer extractFeature(double degreesFromNorth, Map<String, Double> rssiValues) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 * 8);
        byteBuffer.order(ByteOrder.nativeOrder());
        float[] rssValues = new float[8];
//        byteBuffer.putFloat((float) degreesFromNorth);

        for (String wifiName : rssiValues.keySet()) {
            double rssi = rssiValues.get(wifiName);
            int index = indexMap.get(wifiName);
            // normalize
            double v = rssi - min[index - 1];
            v = v / (max[index - 1] - min[index - 1]);
            rssValues[index - 1] = (float) v;
        }
        for (int i = 0; i < rssValues.length; i++) {
            byteBuffer.putFloat(rssValues[i]);
        }
        return byteBuffer;
    }

    public float[] estimateLocation(ByteBuffer byteBuffer) {
        Activity activity = activityReference.get();
        if (activity == null || activity.isFinishing()) {
            return null;
        }
        try {
            Model model = Model.newInstance(activity.getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 8}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float[] result = outputFeature0.getFloatArray();
            // Releases model resources if no longer used.
            model.close();
            return result;
        } catch (IOException e) {
            return null;
        }
    }

    public Map<String, Double> getRssiValues() {
        wifiManager.startScan();
        List<ScanResult> wifiList = wifiManager.getScanResults();
        Map<String, Double> rssiValues = new HashMap<>();
        for (ScanResult scanResult : wifiList) {
            if (scanResult.SSID.contains("SCSLAB_AP")) {
                rssiValues.put(scanResult.SSID, (double) scanResult.level);
            }
        }
        return rssiValues;
    }
}
