package indoorpositioningmodel;

import android.app.Activity;
import android.util.Log;

import com.scslab.indoorpositioning.R;

import java.lang.ref.WeakReference;

public class IndoorPositioningModel implements IndoorPositioningPDRModel.NewStepCallback, DirectionManager.OnDirectionChangedCallback {

    private final WeakReference<Activity> activityReference;
    private final UpdatePositionCallback updatePositionCallback;
    private final DirectionManager.OnDirectionChangedCallback onDirectionChangedCallback;
    private final IndoorPositioningRSSIModel rssiModel;
    private final IndoorPositioningPDRModel pdrModel;

    private Position currentPosition;

    public IndoorPositioningModel(Activity activity, UpdatePositionCallback updatePositionCallback, DirectionManager.OnDirectionChangedCallback onDirectionChangedCallback) {
        this.activityReference = new WeakReference<>(activity);
        this.updatePositionCallback = updatePositionCallback;
        this.onDirectionChangedCallback = onDirectionChangedCallback;
        this.rssiModel = new IndoorPositioningRSSIModel(activity);
        this.pdrModel = new IndoorPositioningPDRModel(activity, this, this);

        activity.findViewById(R.id.init_button).setOnClickListener((v) -> {
            this.currentPosition = rssiModel.getLocation();
            this.updatePositionCallback.onPositionUpdate(this.currentPosition);
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

    @Override
    public void onDirectionChanged(double angleFromNorth) {
        this.onDirectionChangedCallback.onDirectionChanged(angleFromNorth);
    }

    @FunctionalInterface
    public interface UpdatePositionCallback {
        void onPositionUpdate(Position position);
    }

}

