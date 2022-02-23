package indoorpositioningmodel;

import android.app.Activity;
import android.net.wifi.ScanResult;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private JSONArray localRecords;

    public DatabaseWrapper(Activity activity) {
        FirebaseApp.initializeApp(activity);
        this.activityWeakReference = new WeakReference<>(activity);
        loadLocalRecords();
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
    private void loadLocalRecords() {
        Activity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }

        localRecords = new JSONArray();
        File path = activity.getExternalFilesDir(null);
        File localRecordsFile = new File(path, "local_records.json");

        try {
            int length = (int) localRecordsFile.length();
            byte[] bytes = new byte[length];
            FileInputStream in = new FileInputStream(localRecordsFile);
            in.read(bytes);
            in.close();

            String localRecordsString = new String(bytes);
            localRecords = new JSONArray(localRecordsString);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveLocalRecords() {
        Activity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }

        String localRecordsString = localRecords.toString();
        File path = activity.getApplicationContext().getExternalFilesDir(null);
        File localRecordsFile = new File(path, "local_records.json");

        FileOutputStream stream;
        try {
            stream = new FileOutputStream(localRecordsFile);
            stream.write(localRecordsString.getBytes());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void storeLocalFingerprintRecord(float ref_x, float ref_y, float angle, List<ScanResult> wifiList) {
        JSONObject newRecord = new JSONObject();
        try {
            newRecord.put("ref_x", ref_x);
            newRecord.put("ref_y", ref_y);
            newRecord.put("angle", angle);

            JSONArray rssiObservations = new JSONArray();
            for (ScanResult scanResult : wifiList) {
                if (!scanResult.SSID.isEmpty() && scanResult.SSID.contains("SCSLAB_AP")) {
                    JSONObject rssiRecord = new JSONObject();
                    rssiRecord.put("SSID", scanResult.SSID);
                    rssiRecord.put("RSSI", scanResult.level);
                    rssiRecord.put("frequency", scanResult.frequency);
                    rssiObservations.put(rssiRecord);
                }
            }
            newRecord.put("rssi_observations", rssiObservations);

            localRecords.put(newRecord);

            Activity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            ToastManager.showToast(activity, "Stored Local Fingerprint (Time: " + System.currentTimeMillis() + ")");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        saveLocalRecords();
    }

    public void uploadLocalRecords(String collectionName) {
        Activity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }

        // Get a new write batch
        BatchGroup batchGroup = new BatchGroup(activity, db);

        try {
            for (int i = 0; i < localRecords.length(); i++) {
                JSONObject localRecord = localRecords.getJSONObject(i);
                double ref_x = localRecord.getDouble("ref_x");
                double ref_y = localRecord.getDouble("ref_y");

                Map<String, Object> uploadData = new HashMap<>();
                uploadData.put("reference_x", ref_x);
                uploadData.put("reference_y", ref_y);
                uploadData.put("angle", localRecord.get("angle"));

                List<Object> rssiObservations = new ArrayList<Object>();
                for (int j = 0; j < localRecord.getJSONArray("rssi_observations").length(); j++) {
                    JSONObject currentObservation = localRecord.getJSONArray("rssi_observations").getJSONObject(j);

                    Map<String, Object> rssi_record = new HashMap<>();
                    rssi_record.put("SSID", currentObservation.get("SSID"));
                    rssi_record.put("RSSI", currentObservation.get("RSSI"));
                    rssi_record.put("frequency", currentObservation.get("frequency"));
                    rssiObservations.add(rssi_record);
                }

                uploadData.put("rssi_observations", rssiObservations);

                DocumentReference documentReference = db
                        .collection(collectionName)
                        .document("("+ref_x+","+ref_y+")")
                        .collection(IndoorPositioningSettings.RSSI_OBSERVATIONS_COLLECTION_NAME+"_records")
                        .document();

                batchGroup.set(documentReference, uploadData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            ToastManager.showToast(activity, "JSON Exception");
            return;
        }

        batchGroup.runBatches();
    }

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
        db.collectionGroup(IndoorPositioningSettings.RSSI_OBSERVATIONS_COLLECTION_NAME +"_records")
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
                    Log.d("IndoorPositioning", "Error getting documents: ", task1.getException());
                }
            });
    }

    @FunctionalInterface
    public interface OnCompleteListener {
        void onComplete(List<Map<String, Map<Position, List<Double>>>> result);
    }

}
