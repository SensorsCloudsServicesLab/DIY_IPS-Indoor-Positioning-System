package indoorpositioningmodel;

import android.app.Activity;

import java.lang.ref.WeakReference;

public class IndoorPositioningPDRModel {

    private WeakReference<Activity> activityReference;

    private AccelerometerManager accelerometerManager;

    public IndoorPositioningPDRModel(Activity activity) {
        activityReference = new WeakReference<>(activity);
        accelerometerManager = new AccelerometerManager(activity);
    }

    public void onPause() {
        accelerometerManager.onPause();
    }

    public void onResume() {
        accelerometerManager.onResume();
    }

}
