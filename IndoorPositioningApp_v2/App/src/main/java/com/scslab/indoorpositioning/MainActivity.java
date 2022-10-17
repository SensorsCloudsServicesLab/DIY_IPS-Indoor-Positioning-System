package com.scslab.indoorpositioning;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.widget.Button;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import indoorpositioningmodel.DistributionProcessor;
import indoorpositioningmodel.Position;

public class MainActivity extends AppCompatActivity {

    private Button collectRSSIDataButton;
    private Button processDistributionsButton;
    private Button processRegressionButton;
    private Button indoorLocalisationButton;
    private Button testAccuracy;
//    private Button joeModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Find all views
        this.collectRSSIDataButton = findViewById(R.id.collect_rsi_button);
        this.processDistributionsButton = findViewById(R.id.process_distributions_button);
        this.processRegressionButton = findViewById(R.id.process_regression_data_button);
        this.indoorLocalisationButton = findViewById(R.id.indoor_localisation_button);
        this.testAccuracy = findViewById(R.id.test_accuracy);
//        this.joeModel = findViewById(R.id.joe_model);
        //Initialisations
        initUI();
    }

    private void initUI() {
        collectRSSIDataButton.setOnClickListener(v -> {
            if (!requestPermissions()) return;

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {}, 0);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {}, 0);
            }

            Intent myIntent = new Intent(getApplicationContext(), CollectRSSIDataActivity.class);
            startActivity(myIntent);
        });

        processDistributionsButton.setOnClickListener(v -> {
            if (!requestPermissions()) return;

            processDataDistributions();
        });

        processRegressionButton.setOnClickListener(v -> {
            if (!requestPermissions()) return;

            new AlertDialog.Builder(this)
                .setMessage("Please run the python script in the repository to continue.")
                .setPositiveButton("OK", null)
                .show();
        });

        indoorLocalisationButton.setOnClickListener(v -> {
            if (!requestPermissions()) return;

            Intent myIntent = new Intent(getApplicationContext(), IndoorLocalisationActivity.class);
            startActivity(myIntent);
        });

        testAccuracy.setOnClickListener(v -> {
            Intent intent = new Intent(this, TestAccuracy.class);
            startActivity(intent);
        });

//        joeModel.setOnClickListener(v -> {
//            Intent intent = new Intent(this, JoeModelActivity.class);
//            startActivity(intent);
//        });
    }

    private boolean requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACTIVITY_RECOGNITION,
                    Manifest.permission.ACCESS_WIFI_STATE
            }, 0);
            return false;
        }
        return true;
    }

    public void processDataDistributions() {
        Toast.makeText(MainActivity.this, "Retrieving RSSI Data from database...", Toast.LENGTH_SHORT).show();
        DistributionProcessor.getDataFromDatabase(this, (List<Map<String, Map<Position, List<Double>>>> RSSIData) -> {

            Toast.makeText(MainActivity.this, "Processing RSSI Distributions...", Toast.LENGTH_SHORT).show();
            List<Map<String, Map<Position, List<Double>>>> RSSIDistributions = DistributionProcessor.processDistributions(RSSIData);

            Toast.makeText(MainActivity.this, "Saving RSSI Distributions...", Toast.LENGTH_SHORT).show();
            DistributionProcessor.saveDataJSON(this, RSSIDistributions);
        });
    }
}