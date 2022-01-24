package com.scslab.indoorpositioning;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class CollectRSSIDataActivity extends AppCompatActivity {

    private EditText reference_x_edit_text;
    private EditText reference_y_edit_text;
    private EditText angle_edit_text;
    private Button checkLocationButton;
    private Button submitButton;
    private Button upArrowButton;
    private Button downArrowButton;
    private Button leftArrowButton;
    private Button rightArrowButton;

    private ListView networkListView;
    private WifiManager wifiManager;
    private List<ScanResult> wifiList;
    private DirectionManager directionManager;

    private final float x_increment = 0.5f;
    private final float y_increment = 0.5f;

    private DatabaseWrapper database = new DatabaseWrapper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);

        //Find all views
        this.networkListView = findViewById(R.id.networkListView);
        this.checkLocationButton = findViewById(R.id.check_location_button);
        this.submitButton = findViewById(R.id.submit_button);
        this.reference_x_edit_text = findViewById(R.id.reference_x);
        this.reference_y_edit_text = findViewById(R.id.reference_y);
        this.angle_edit_text = findViewById(R.id.angle);
        this.upArrowButton = findViewById(R.id.up_arrow);
        this.downArrowButton = findViewById(R.id.down_arrow);
        this.leftArrowButton = findViewById(R.id.left_arrow);
        this.rightArrowButton = findViewById(R.id.right_arrow);

        //Initialisations
        initUI();
        initNetwork();
        this.directionManager = new DirectionManager(this);

        //Update the network info
        getLocationData();
    }

    private void initUI() {
        checkLocationButton.setOnClickListener(v -> this.getLocationData());
        submitButton.setOnClickListener(v -> this.uploadFingerprintData());
        upArrowButton.setOnClickListener(v -> addFloatToEditText(reference_y_edit_text, y_increment));
        downArrowButton.setOnClickListener(v -> addFloatToEditText(reference_y_edit_text, -y_increment));
        leftArrowButton.setOnClickListener(v -> addFloatToEditText(reference_x_edit_text, -x_increment));
        rightArrowButton.setOnClickListener(v -> addFloatToEditText(reference_x_edit_text, x_increment));
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

    private void getLocationData() {
        //WIFI Data
        wifiManager.startScan();
        wifiList = wifiManager.getScanResults();
        ListAdapter listAdapter = new ListAdapter(getApplicationContext(), wifiList);
        networkListView.setAdapter(listAdapter);

        //Direction Data
        angle_edit_text.setText(String.valueOf(directionManager.getCurrentDegreesFromNorth()));
    }

    private void uploadFingerprintData() {
        float ref_x = Float.parseFloat(reference_x_edit_text.getText().toString());
        float ref_y = Float.parseFloat(reference_y_edit_text.getText().toString());
        float angle = Float.parseFloat(angle_edit_text.getText().toString());
        database.addFingerprintRecord(ref_x, ref_y, angle, wifiList);
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

