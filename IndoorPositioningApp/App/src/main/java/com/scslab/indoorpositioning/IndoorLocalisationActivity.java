package com.scslab.indoorpositioning;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import indoorpositioningmodel.IndoorPositioningModel;
import indoorpositioningmodel.IndoorPositioningVisualiser;
import indoorpositioningmodel.Position;

public class IndoorLocalisationActivity extends AppCompatActivity {

    private Button checkLocationButton;

    private IndoorPositioningModel indoorPositioningModel;
    private IndoorPositioningVisualiser indoorVisualiser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indoor_localisation);

        //Init UI
        this.checkLocationButton = findViewById(R.id.check_location_button);
        this.indoorVisualiser = new IndoorPositioningVisualiser(this);
        checkLocationButton.setOnClickListener(v -> {
            Position position = indoorPositioningModel.getCurrentPosition();
            indoorVisualiser.setMarkerPosition(position.x,position.y);
        });

        //Initialisations
        indoorPositioningModel = new IndoorPositioningModel(this);
    }

    protected void onResume() {
        super.onResume();
        indoorPositioningModel.onResume();
    }

    protected void onPause() {
        super.onPause();
        indoorPositioningModel.onPause();
    }

}