package com.scslab.indoorpositioning;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Find all views
        this.collectRSSIDataButton = findViewById(R.id.collect_rsi_button);
        this.processDistributionsButton = findViewById(R.id.process_distributions_button);
        this.processRegressionButton = findViewById(R.id.process_regression_data_button);
        this.indoorLocalisationButton = findViewById(R.id.indoor_localisation_button);

        //Initialisations
        initUI();
    }

    private void initUI() {
        collectRSSIDataButton.setOnClickListener(v -> {
            Intent myIntent = new Intent(getApplicationContext(), CollectRSSIDataActivity.class);
            startActivity(myIntent);
        });

        processDistributionsButton.setOnClickListener(v -> {
            processDataDistributions();
        });

        processRegressionButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setMessage("Please run the python script in the repository to continue.")
                .setPositiveButton("OK", null)
                .show();
        });

        indoorLocalisationButton.setOnClickListener(v -> {
            Intent myIntent = new Intent(getApplicationContext(), IndoorLocalisationActivity.class);
            startActivity(myIntent);
        });
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