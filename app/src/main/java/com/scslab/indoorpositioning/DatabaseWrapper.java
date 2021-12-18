package com.scslab.indoorpositioning;

import android.app.Activity;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseWrapper {

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
        RSSI_2GHZ: [
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
            }
        ],
        RSSI_5GHZ: [
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

        List<Object> rssi_2GHz_list = new ArrayList<Object>();
        List<Object> rssi_5GHz_list = new ArrayList<Object>();

        for (ScanResult scanResult : wifiList) {
            if (!scanResult.SSID.isEmpty() && scanResult.SSID.contains("SCSLAB_AP")) {
                Map<String, Object> rssi_record = new HashMap<>();
                rssi_record.put("SSID", scanResult.SSID);
                rssi_record.put("RSSI", scanResult.level);
                rssi_record.put("frequency", scanResult.frequency);

                if (scanResult.frequency >= 5000) {
                    rssi_5GHz_list.add(rssi_record);
                } else {
                    rssi_2GHz_list.add(rssi_record);
                }
            }
        }

        record.put("RSSI_2GHZ", rssi_2GHz_list);
        record.put("RSSI_5GHZ", rssi_5GHz_list);

        db.collection("fingerprint_records")
                .add(record)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Activity activity = activityWeakReference.get();
                        if (activity != null && !activity.isFinishing()) {
                            Toast.makeText(activity, "Recorded with ID: " + documentReference.getId(), Toast.LENGTH_SHORT).show();
                        }
                        Log.d(TAG, "Recorded with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Activity activity = activityWeakReference.get();
                        if (activity != null && !activity.isFinishing()) {
                            Toast.makeText(activity, "Error adding record", Toast.LENGTH_SHORT).show();
                        }
                        Log.d(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                });
    }

}
