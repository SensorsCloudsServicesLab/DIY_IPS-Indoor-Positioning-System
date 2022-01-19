package com.scslab.indoorpositioning;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;

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
//            Intent myIntent = new Intent(getApplicationContext(), IndoorLocalisationActivity.class);
//            startActivity(myIntent);
        });

        processRegressionButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please run the python script in the repository to continue.")
                .setPositiveButton("OK", null);
            builder.show();
        });

        indoorLocalisationButton.setOnClickListener(v -> {
            Intent myIntent = new Intent(getApplicationContext(), IndoorLocalisationActivity.class);
            startActivity(myIntent);
        });
    }
}