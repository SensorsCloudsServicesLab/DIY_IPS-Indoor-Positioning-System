package indoorpositioningmodel;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Button;

import com.scslab.indoorpositioning.R;

public class DirectionManager implements SensorEventListener {

    private float[] mGravity;
    private float[] mGeomagnetic;
    private float currentDegreesFromNorth = 0f;
    private double calibrationAngle = 180;

    private final SensorManager sensorManager;
    private final Sensor accelerometerSensor;
    private final Sensor magnetometerSensor;
    private final OnDirectionChangedCallback onDirectionChangedCallback;

    public DirectionManager(Activity activity, OnDirectionChangedCallback onDirectionChangedCallback) {
        this.sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        this.accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        this.onDirectionChangedCallback = onDirectionChangedCallback;

        //Check if a calibration button exists
        Button calibrationButton = activity.findViewById(R.id.calibrate_north);
        if (calibrationButton != null) {
            calibrationButton.setOnClickListener((v) -> {
                this.calibrateNorth();
            });
        }
    }

//    public float getCurrentDegreesFromNorth() {
//        return (float) (IndoorPositioningSettings.PDR_X_AXIS_FLIP * (this.currentDegreesFromNorth - calibrationAngle));
//    }

    public float getCurrentDegreesFromNorth() {
        float v = (float) (IndoorPositioningSettings.PDR_X_AXIS_FLIP * (this.currentDegreesFromNorth - calibrationAngle));
        return (v + 180) % 360;
    }

    public void onPause() {
        sensorManager.unregisterListener(this);
    }

    public void onResume() {
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
            float[] R = new float[9];
            float[] I = new float[9];

            if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {

                // orientation contains azimuth, pitch and roll
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);

                float angle = orientation[0];
                currentDegreesFromNorth = -angle * 360 / (2 * 3.14159f);

                if (onDirectionChangedCallback != null) {
                    onDirectionChangedCallback.onDirectionChanged(getCurrentDegreesFromNorth());
                }
            }
        }
    }

    private void calibrateNorth() {
        this.calibrationAngle = 180 + this.currentDegreesFromNorth;
        onDirectionChangedCallback.onDirectionChanged(getCurrentDegreesFromNorth());
    }

    @FunctionalInterface
    public interface OnDirectionChangedCallback {
        void onDirectionChanged(double angleFromNorth);
    }
}
