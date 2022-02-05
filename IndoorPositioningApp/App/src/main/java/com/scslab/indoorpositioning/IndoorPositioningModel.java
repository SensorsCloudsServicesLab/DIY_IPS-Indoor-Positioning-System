package com.scslab.indoorpositioning;

import android.app.Activity;
import java.lang.ref.WeakReference;

public class IndoorPositioningModel {

    private WeakReference<Activity> activityReference;
    private IndoorPositioningRSSIModel rssiModel;
    private IndoorPositioningPDRModel pdrModel;

    public IndoorPositioningModel(Activity activity) {
        this.activityReference = new WeakReference<>(activity);
        this.rssiModel = new IndoorPositioningRSSIModel(activity);
        this.pdrModel = new IndoorPositioningPDRModel(activity);
    }

    public Position getCurrentPosition() {
        Position rssiPosition = rssiModel.getLocation();
        return rssiPosition;
    }

    public void onResume() {
        rssiModel.onResume();
        pdrModel.onResume();
    }

    public void onPause() {
        rssiModel.onPause();
        pdrModel.onPause();
    }
}

