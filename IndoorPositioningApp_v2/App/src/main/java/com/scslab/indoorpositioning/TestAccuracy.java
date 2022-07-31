package com.scslab.indoorpositioning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import indoorpositioningmodel.DirectionManager;
import indoorpositioningmodel.IndoorPositioningRSSIModel;
import indoorpositioningmodel.IndoorPositioningSettings;
import indoorpositioningmodel.Position;
import indoorpositioningmodel.ToastManager;

public class TestAccuracy extends AppCompatActivity {

    private EditText reference_x_edit_text;
    private EditText reference_y_edit_text;
    private EditText estimate_x_edit_text;
    private EditText estimate_y_edit_text;
    private EditText angle_edit_text;
    private Button checkLocationButton;
    private ToggleButton autoRefreshButton;
    private Button upArrowButton;
    private Button downArrowButton;
    private Button leftArrowButton;
    private Button rightArrowButton;
    private ListView networkListView;
    private WifiManager wifiManager;
    private List<ScanResult> wifiList;
    private DirectionManager directionManager;
    private EditText errorText;
    private IndoorPositioningRSSIModel rssiModel = null;
    private final float x_increment = 1f;
    private final float y_increment = 1f;
    private int currentAutoRefreshIndex = 0;
    private JSONArray accuracyRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_accuracy);

        //Find all views
        this.networkListView = findViewById(R.id.networkListView);
        this.checkLocationButton = findViewById(R.id.check_location_button);
        this.autoRefreshButton = findViewById(R.id.auto_refresh);
        this.reference_x_edit_text = findViewById(R.id.reference_x);
        this.reference_y_edit_text = findViewById(R.id.reference_y);
        this.angle_edit_text = findViewById(R.id.angle);
        this.upArrowButton = findViewById(R.id.up_arrow);
        this.downArrowButton = findViewById(R.id.down_arrow);
        this.leftArrowButton = findViewById(R.id.left_arrow);
        this.rightArrowButton = findViewById(R.id.right_arrow);
        this.errorText = findViewById(R.id.error);
        this.rssiModel = new IndoorPositioningRSSIModel(this);
        this.estimate_x_edit_text = findViewById(R.id.estimate_x);
        this.estimate_y_edit_text = findViewById(R.id.estimate_y);

        //Initialisations
        initUI();
        initNetwork();
        loadAccuracyRecords();
        this.directionManager = new DirectionManager(this, null);
        //Update the network info
        getLocationData();
    }

    private void initUI() {
        checkLocationButton.setOnClickListener(v -> this.refresh());
        autoRefreshButton.setOnCheckedChangeListener((v, isChecked) -> this.autoRefreshAndUpload(isChecked));
        upArrowButton.setOnClickListener(v -> addFloatToEditText(reference_y_edit_text, y_increment));
        downArrowButton.setOnClickListener(v -> addFloatToEditText(reference_y_edit_text, -y_increment));
        leftArrowButton.setOnClickListener(v -> addFloatToEditText(reference_x_edit_text, -x_increment));
        rightArrowButton.setOnClickListener(v -> addFloatToEditText(reference_x_edit_text, x_increment));
    }

    private void loadAccuracyRecords() {
        accuracyRecords = new JSONArray();
        File path = this.getExternalFilesDir(null);
        File localRecordsFile = new File(path, "accuracy_records.json");
        try {
            int length = (int) localRecordsFile.length();
            byte[] bytes = new byte[length];
            FileInputStream in = new FileInputStream(localRecordsFile);
            in.read(bytes);
            in.close();

            String localRecordsString = new String(bytes);
            accuracyRecords = new JSONArray(localRecordsString);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveAccuracyRecordsLocally() {
        String s = accuracyRecords.toString();
        File path = this.getApplicationContext().getExternalFilesDir(null);
        File localRecordsFile = new File(path, "accuracy_records.json");

        FileOutputStream stream;
        try {
            stream = new FileOutputStream(localRecordsFile);
            stream.write(s.getBytes());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addFloatToEditText(EditText edittext, Float value) {
        String current_value_string = edittext.getText().toString();
        if (!current_value_string.equals("")) {
            Float current_value = Float.parseFloat(current_value_string);
            current_value += value;
            edittext.setText(current_value.toString());
        }
    }

    private void initNetwork() {
        this.wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * Update network information
     */
    private void getLocationData() {
        //WIFI Data
        wifiManager.startScan();
        wifiList = wifiManager.getScanResults();
        ListAdapter listAdapter = new ListAdapter(getApplicationContext(), wifiList);
        networkListView.setAdapter(listAdapter);

        //Direction Data
        angle_edit_text.setText(String.valueOf(directionManager.getCurrentDegreesFromNorth()));
    }

    /**
     * Display the updated content after refresh
     */
    private void displayFreshContent() {
        //Wifi Data
        wifiList = wifiManager.getScanResults();
        ListAdapter listAdapter = new ListAdapter(getApplicationContext(), wifiList);
        networkListView.setAdapter(listAdapter);

        //Direction Data
        angle_edit_text.setText(String.valueOf(directionManager.getCurrentDegreesFromNorth()));

        Position estimateLocation = rssiModel.getLocation();
        Double estimateX = estimateLocation.x;
        Double estimateY = estimateLocation.y;

        // show estimate location
        estimate_x_edit_text.setText(String.format("%.2f", estimateX));
        estimate_y_edit_text.setText(String.format("%.2f", estimateY));

        Double realX = Double.parseDouble(reference_x_edit_text.getText().toString());
        Double realY = Double.parseDouble(reference_y_edit_text.getText().toString());

        // calculate the error between real and estimate location
        double error = Math.sqrt(Math.pow(estimateX - realX, 2) + Math.pow(estimateY - realY, 2));

        errorText.setText(String.format("%.2f", error));

        ToastManager.showToast(this, "Refreshed");
    }

    private void refresh() {
        //WIFI Data
        wifiManager.startScan();
        displayFreshContent();
    }

    private void saveObservation() {
        float ref_x = Float.parseFloat(reference_x_edit_text.getText().toString());
        float ref_y = Float.parseFloat(reference_y_edit_text.getText().toString());
        float angle = Float.parseFloat(angle_edit_text.getText().toString());
        float est_x = Float.parseFloat(estimate_x_edit_text.getText().toString());
        float est_y = Float.parseFloat(estimate_y_edit_text.getText().toString());
        float error = Float.parseFloat(errorText.getText().toString());
        JSONObject newRecord = new JSONObject();
        try {
            newRecord.put("ref_x", ref_x);
            newRecord.put("ref_y", ref_y);
            newRecord.put("angle", angle);
            newRecord.put("est_x", est_x);
            newRecord.put("est_y", est_y);
            newRecord.put("error", error);
            accuracyRecords.put(newRecord);
            ToastManager.showToast(this, "Recorded Accuracy Data (Time: " + System.currentTimeMillis() + ")");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            MediaPlayer music = MediaPlayer.create(getApplicationContext(), R.raw.notification);
            // This condition is not necessary if you listen to only one action
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                if (currentAutoRefreshIndex >= IndoorPositioningSettings.NUM_TESTS) {
                    autoRefreshButton.setChecked(false);
                    // save the accuracy data to local file on the phone
                    saveAccuracyRecordsLocally();
                    music.start();
                    return;
                }
                displayFreshContent();
                //save observation data
                saveObservation();
                //State
                currentAutoRefreshIndex++;
                wifiManager.startScan();
            }
        }
    };

    private void autoRefreshAndUpload(boolean isOn) {
        if (isOn) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

            registerReceiver(broadcastReceiver, intentFilter);

            wifiManager.startScan();
        } else {
            try {
                unregisterReceiver(broadcastReceiver);
                currentAutoRefreshIndex = 0;
            } catch (IllegalArgumentException e) {
                Log.d("IndoorPositioning", "receiver already unregistered");
            }
        }
    }

    protected void onResume() {
        super.onResume();
        directionManager.onResume();
    }

    protected void onPause() {
        super.onPause();
        directionManager.onPause();
    }
}
