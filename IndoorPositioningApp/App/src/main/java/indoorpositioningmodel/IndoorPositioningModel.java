package indoorpositioningmodel;

import android.app.Activity;

import com.scslab.indoorpositioning.R;

import java.lang.ref.WeakReference;

public class IndoorPositioningModel implements IndoorPositioningPDRModel.NewStepCallback {

    private final WeakReference<Activity> activityReference;
    private final UpdatePositionCallback updatePositionCallback;
    private final IndoorPositioningRSSIModel rssiModel;
    private final IndoorPositioningPDRModel pdrModel;

    private Position currentPosition;

    public IndoorPositioningModel(Activity activity, UpdatePositionCallback updatePositionCallback) {
        this.activityReference = new WeakReference<>(activity);
        this.updatePositionCallback = updatePositionCallback;
        this.rssiModel = new IndoorPositioningRSSIModel(activity);
        this.pdrModel = new IndoorPositioningPDRModel(activity, this);

        //First position estimation is purely based on RSSI
        //TODO: tell the user to stay still and take 5 rssi samples for a more accurate starting position
        activity.findViewById(R.id.init_button).setOnClickListener((v) -> {
            this.currentPosition = rssiModel.getLocation();
        });
    }

    public void onResume() {
        rssiModel.onResume();
        pdrModel.onResume();
    }

    public void onPause() {
        rssiModel.onPause();
        pdrModel.onPause();
    }

    @Override
    public void onNewStep(Vector stepVector) {
        this.updatePositionCallback.onPositionUpdate((Position) currentPosition.add(stepVector));
    }

    @FunctionalInterface
    public interface UpdatePositionCallback {
        void onPositionUpdate(Position position);
    }
}

