package com.scslab.indoorpositioning;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class IndoorLocalisationActivity extends AppCompatActivity {

    private Button checkLocationButton;

    private IndoorPositioningModel indoorPositioningModel;
    private IndoorVisualiser indoorVisualiser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indoor_localisation);

        //Init UI
        this.checkLocationButton = findViewById(R.id.check_location_button);
        this.indoorVisualiser = new IndoorVisualiser(this);
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