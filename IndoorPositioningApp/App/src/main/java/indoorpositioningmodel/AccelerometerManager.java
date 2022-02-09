package indoorpositioningmodel;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class AccelerometerManager implements SensorEventListener {

    private final SensorManager sensorManager;
    private final Sensor accelerometerSensor;
    private final Sensor gravitySensor;
    private final Sensor magneticSensor;
    private final Sensor rotationSensor;

    private float[] mRotationMatrixFromVector = new float[] {
            1,0,0,0,
            0,1,0,0,
            0,0,1,0,
            0,0,0,1,
    };

    private float[] mRemappedRotationMatrix = new float[] {
            1,0,0,0,
            0,1,0,0,
            0,0,1,0,
            0,0,0,1,
    };

    private float[] gravityValues = null;
    private float[] magneticValues = null;

    public AccelerometerManager(Activity activity) {
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public void onPause() {
        sensorManager.unregisterListener(this);
    }

    public void onResume() {
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            if (gravityValues != null && magneticValues != null) {
                float[] deviceRelativeAcceleration = new float[4];
                deviceRelativeAcceleration[0] = event.values[0];
                deviceRelativeAcceleration[1] = event.values[1];
                deviceRelativeAcceleration[2] = event.values[2];
                deviceRelativeAcceleration[3] = 0;

                // Change the device relative acceleration values to earth relative values
                // X axis -> East
                // Y axis -> North Pole
                // Z axis -> Sky

                float[] R = new float[16];
                float[] I = new float[16];
                float[] earthAcc = new float[16];

                SensorManager.getRotationMatrix(R, I, gravityValues, magneticValues);

                float[] inv = new float[16];
                android.opengl.Matrix.invertM(inv, 0, R, 0);
                android.opengl.Matrix.multiplyMV(earthAcc, 0, inv, 0, deviceRelativeAcceleration, 0);
                Log.d("Acceleration", "Values: (" + earthAcc[0] + ", " + earthAcc[1] + ", " + earthAcc[2] + ")");
            }

        } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            gravityValues = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticValues = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {

            //This last sensor measures the rotation of the phone. I'm not sure if it's needed yet.
            float[] eulerAngles = new float[3];
            SensorManager.getRotationMatrixFromVector(mRotationMatrixFromVector,
                    event.values);
            SensorManager.remapCoordinateSystem(mRotationMatrixFromVector,
                    SensorManager.AXIS_X, SensorManager.AXIS_Z, mRemappedRotationMatrix);
            SensorManager.getOrientation(mRemappedRotationMatrix, eulerAngles);

            eulerAngles[0] = (float) (eulerAngles[0] * 180 / Math.PI);
            eulerAngles[1] = (float) (eulerAngles[1] * 180 / Math.PI);
            eulerAngles[2] = (float) (eulerAngles[2] * 180 / Math.PI);

            Log.d("FacingDirection", eulerAngles[0] + ", " + eulerAngles[1] + ", " + eulerAngles[2]);

        }
    }
}
