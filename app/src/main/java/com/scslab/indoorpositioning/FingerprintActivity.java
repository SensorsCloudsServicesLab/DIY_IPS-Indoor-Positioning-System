package com.scslab.indoorpositioning;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class FingerprintActivity extends AppCompatActivity implements SensorEventListener {

    private EditText reference_x_edit_text;
    private EditText reference_y_edit_text;
    private EditText angle_edit_text;
    private Button checkLocationButton;
    private Button submitButton;
    private Button upArrowButton;
    private Button downArrowButton;
    private Button leftArrowButton;
    private Button rightArrowButton;

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor magnetometerSensor;
    private ListView networkListView;
    private WifiManager wifiManager;
    private List<ScanResult> wifiList;

    private float[] mGravity;
    private float[] mGeomagnetic;

    private final float x_increment = 0.25f;
    private final float y_increment = 0.25f;

    private float current_degrees_from_north = 0f;

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
        initSensors();

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

    private void initSensors() {
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
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
        angle_edit_text.setText(String.valueOf(current_degrees_from_north));
    }

    private void uploadFingerprintData() {
        float ref_x = Float.parseFloat(reference_x_edit_text.getText().toString());
        float ref_y = Float.parseFloat(reference_y_edit_text.getText().toString());
        float angle = Float.parseFloat(angle_edit_text.getText().toString());
        database.addFingerprintRecord(ref_x, ref_y, angle, wifiList);
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {

                // orientation contains azimuth, pitch and roll
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);

                float angle = orientation[0];
                current_degrees_from_north = -angle * 360 / (2 * 3.14159f);
            }
        }
    }
}

