package indoorpositioningmodel;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;

public class IndoorPositioningPDRModel implements SensorEventListener {

    private final WeakReference<Activity> activityReference;
    private final DirectionManager directionManager;
    private final SensorManager sensorManager;
    private final Sensor stepDetectorSensor;

    private final NewStepCallback newStepCallback;

    public IndoorPositioningPDRModel(Activity activity, NewStepCallback newStepCallback, DirectionManager.OnDirectionChangedCallback onDirectionChangedCallback) {
        requestPermissions(activity);

        this.activityReference = new WeakReference<>(activity);
        this.sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        this.stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        this.newStepCallback = newStepCallback;
        this.directionManager = new DirectionManager(activity, onDirectionChangedCallback);
    }

    public void requestPermissions(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }
    }

    public void onPause() {
        sensorManager.unregisterListener(this);
        directionManager.onPause();
    }

    public void onResume() {
        sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
        directionManager.onResume();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            double stepDistance = 0.7;
            double currentRadians = (directionManager.getCurrentDegreesFromNorth()*Math.PI/180)+(Math.PI/2);
            Vector direction = new Vector(Math.cos(currentRadians), Math.sin(currentRadians)).normalise();
            newStepCallback.onNewStep(direction.scale(stepDistance));
        }
    }

    @FunctionalInterface
    public interface NewStepCallback {
        void onNewStep(Vector stepVector);
    }

}
