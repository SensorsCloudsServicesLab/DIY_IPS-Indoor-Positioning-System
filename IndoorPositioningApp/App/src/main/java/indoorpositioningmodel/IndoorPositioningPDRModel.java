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
    private final SensorManager sensorManager;
    private final Sensor stepDetectorSensor;
    private final Sensor rotationSensor;

    private float[] facingDirection = new float[3];
    private float lastStepTimestamp = 0;
    private NewStepCallback newStepCallback;

    private final float[] mRotationMatrixFromVector = new float[] {
            1,0,0,0,
            0,1,0,0,
            0,0,1,0,
            0,0,0,1,
    };

    private final float[] mRemappedRotationMatrix = new float[] {
            1,0,0,0,
            0,1,0,0,
            0,0,1,0,
            0,0,0,1,
    };

    public IndoorPositioningPDRModel(Activity activity, NewStepCallback newStepCallback) {
        requestPermissions(activity);

        this.activityReference = new WeakReference<>(activity);
        this.sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        this.stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        this.rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        this.newStepCallback = newStepCallback;
    }

    public void requestPermissions(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }
    }

    public void onPause() {
        sensorManager.unregisterListener(this);
    }

    public void onResume() {
        sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            float timestamp = event.timestamp;

            double stepDistance = 0.8;  //TODO calculate this better
            Vector direction = new Vector(facingDirection[0], facingDirection[1]).normalise();
            newStepCallback.onNewStep(direction.scale(stepDistance));

            lastStepTimestamp = event.timestamp;

        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {

            SensorManager.getRotationMatrixFromVector(mRotationMatrixFromVector,
                    event.values);
            SensorManager.remapCoordinateSystem(mRotationMatrixFromVector,
                    SensorManager.AXIS_X, SensorManager.AXIS_Z, mRemappedRotationMatrix);
            SensorManager.getOrientation(mRemappedRotationMatrix, facingDirection);

            facingDirection[0] = (float) (facingDirection[0] * 180 / Math.PI);
            facingDirection[1] = (float) (facingDirection[1] * 180 / Math.PI);
            facingDirection[2] = (float) (facingDirection[2] * 180 / Math.PI);
        }
    }

    @FunctionalInterface
    public interface NewStepCallback {
        void onNewStep(Vector stepVector);
    }

}
