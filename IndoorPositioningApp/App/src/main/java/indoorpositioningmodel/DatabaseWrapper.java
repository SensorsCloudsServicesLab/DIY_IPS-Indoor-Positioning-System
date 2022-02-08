package indoorpositioningmodel;

import android.app.Activity;
import android.net.wifi.ScanResult;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseWrapper {

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

    private static String TAG = "Database";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private WeakReference<Activity> activityWeakReference;

    public DatabaseWrapper(Activity activity) {
        FirebaseApp.initializeApp(activity);
        this.activityWeakReference = new WeakReference<>(activity);
    }

    /*
    {
        reference_x : 0.25,
        reference_y : 0.25,
        angle : 170,
        rssi_observations: [
            {
                SSID: "SCSLAB_AP_1_2GHZ",
                RSSI : -50,
                frequency: 2180
            },
            {
                SSID: "SCSLAB_AP_2_2GHZ",
                RSSI : -50,
                frequency: 2190
            },
            {
                SSID: "SCSLAB_AP_3_2GHZ",
                RSSI : -50,
                frequency: 2200
            },
            {
                SSID: "SCSLAB_AP_1_5GHZ",
                RSSI : -50,
                frequency: 5780
            },
            {
                SSID: "SCSLAB_AP_2_5GHZ",
                RSSI : -50,
                frequency: 5790
            },
            {
                SSID: "SCSLAB_AP_3_5GHZ",
                RSSI : -50,
                frequency: 5800
            }
        ]
    }
     */
    public void addFingerprintRecord(float ref_x, float ref_y, float angle, List<ScanResult> wifiList) {
        Map<String, Object> record = new HashMap<>();
        record.put("reference_x", ref_x);
        record.put("reference_y", ref_y);
        record.put("angle", angle);

        List<Object> rssi_observation_list = new ArrayList<Object>();

        for (ScanResult scanResult : wifiList) {
            if (!scanResult.SSID.isEmpty() && scanResult.SSID.contains("SCSLAB_AP")) {
                Map<String, Object> rssi_record = new HashMap<>();
                rssi_record.put("SSID", scanResult.SSID);
                rssi_record.put("RSSI", scanResult.level);
                rssi_record.put("frequency", scanResult.frequency);
                rssi_observation_list.add(rssi_record);
            }
        }

        record.put("rssi_observations", rssi_observation_list);

        db.collection("rssi_records").document("("+ref_x+","+ref_y+")").collection("records")
                .add(record)
                .addOnSuccessListener((DocumentReference documentReference) -> {
                    Activity activity = activityWeakReference.get();
                    if (activity != null && !activity.isFinishing()) {
                        ToastManager.showToast(activity, "Recorded with ID: " + documentReference.getId());
                    }
                    Log.d(TAG, "Recorded with ID: " + documentReference.getId());
                })
                .addOnFailureListener((@NonNull Exception e) -> {
                    Activity activity = activityWeakReference.get();
                    if (activity != null && !activity.isFinishing()) {
                        ToastManager.showToast(activity, "Error adding record");
                    }
                    Log.d(TAG, e.getMessage());
                    e.printStackTrace();
                });
    }

    static int num = 0;

    public void getRSSIDataFromDatabase(OnCompleteListener onCompleteListener) {

        //Prepare data structure
        List<Map<String, Map<Position, List<Double>>>> parsedDirectionalRSSIData = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Map<String, Map<Position, List<Double>>> parsedRSSIData = new HashMap<>();
            parsedRSSIData.put("SCSLAB_AP_1_2GHZ", new HashMap<>());
            parsedRSSIData.put("SCSLAB_AP_1_5GHZ", new HashMap<>());
            parsedRSSIData.put("SCSLAB_AP_2_2GHZ", new HashMap<>());
            parsedRSSIData.put("SCSLAB_AP_2_5GHZ", new HashMap<>());
            parsedRSSIData.put("SCSLAB_AP_3_2GHZ", new HashMap<>());
            parsedRSSIData.put("SCSLAB_AP_3_5GHZ", new HashMap<>());
            parsedRSSIData.put("SCSLAB_AP_4_2GHZ", new HashMap<>());
            parsedRSSIData.put("SCSLAB_AP_4_5GHZ", new HashMap<>());
            parsedDirectionalRSSIData.add(parsedRSSIData);
        }

        //Read from database
        db.collectionGroup("records")
            .get()
            .addOnCompleteListener((task1) -> {
                if (task1.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task1.getResult()) {

                        Map<String, Object> data = document.getData();
                        Double angle = (Double) data.get("angle");

                        //Find the map corresponding to the observation's direction
                        int direction = Helpers.getDirection(angle);
                        Map<String, Map<Position, List<Double>>> relevantDataMap = parsedDirectionalRSSIData.get(direction);

                        //Add the data to this map:
                        Double reference_x = (Double) data.get("reference_x");
                        Double reference_y = (Double) data.get("reference_y");
                        List<Map<String, Object>> RSSIObservations = (List<Map<String, Object>>) data.get("rssi_observations");
                        if (reference_x == null || reference_y == null || RSSIObservations == null) {
                            continue;
                        }

                        Position position = new Position(reference_x, reference_y);
                        for (Map<String, Object> observation : RSSIObservations) {
                            String accessPointName = (String) observation.get("SSID");
                            Double RSSI = ((Long) observation.get("RSSI")).doubleValue();

                            if (!relevantDataMap.containsKey(accessPointName)) {
                                continue;
                            }

                            Map<Position, List<Double>> accessPointDataMap = relevantDataMap.get(accessPointName);
                            List<Double> rssiList;
                            if (accessPointDataMap.containsKey(position)) {
                                rssiList = accessPointDataMap.get(position);
                            } else {
                                rssiList = new ArrayList<>();
                            }
                            rssiList.add(RSSI);

                            accessPointDataMap.put(position, rssiList);
                        }
                    }

                    onCompleteListener.onComplete(parsedDirectionalRSSIData);
                } else {
                    Log.d("Riccardo", "Error getting documents: ", task1.getException());
                }
            });
    }

    @FunctionalInterface
    public interface OnCompleteListener {
        void onComplete(List<Map<String, Map<Position, List<Double>>>> result);
    }

}
