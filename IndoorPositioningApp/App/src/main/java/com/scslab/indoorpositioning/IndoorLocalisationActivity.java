package com.scslab.indoorpositioning;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import indoorpositioningmodel.IndoorPositioningModel;
import indoorpositioningmodel.IndoorPositioningVisualiser;
import indoorpositioningmodel.Position;

public class IndoorLocalisationActivity extends AppCompatActivity implements IndoorPositioningModel.UpdatePositionCallback {

    private IndoorPositioningModel indoorPositioningModel;
    private IndoorPositioningVisualiser indoorVisualiser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indoor_localisation);

        //Init UI
        this.indoorVisualiser = new IndoorPositioningVisualiser(this);

        //Initialisations
        this.indoorPositioningModel = new IndoorPositioningModel(this, this);
    }

    protected void onResume() {
        super.onResume();
        indoorPositioningModel.onResume();
    }

    protected void onPause() {
        super.onPause();
        indoorPositioningModel.onPause();
    }

    @Override
    public void onPositionUpdate(Position position) {
        if (indoorVisualiser != null) {
            indoorVisualiser.setMarkerPosition(position.x,position.y);
        }
    }
}