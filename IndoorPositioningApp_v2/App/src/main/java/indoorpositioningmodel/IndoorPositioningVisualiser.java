package indoorpositioningmodel;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.scslab.indoorpositioning.R;

public class IndoorPositioningVisualiser {

    private final TextView positionTextView;
    private final ImageView floorPlanImageView;
    private final ImageView positionMarkerImageView;
    private final ImageView rotationMarkerImageView;

    private Position currentPosition;
    private double currentAngle = 0;

    public IndoorPositioningVisualiser(Activity activity) {
        this.positionTextView = activity.findViewById(R.id.position);
        this.floorPlanImageView = activity.findViewById(R.id.floor_plan);
        this.positionMarkerImageView = activity.findViewById(R.id.position_marker);
        this.rotationMarkerImageView = activity.findViewById(R.id.rotation_marker);
        this.setMarkerPosition(new Position(0, 0));
    }

    public void setMarkerPosition(Position position) {
        this.currentPosition = position;
        updateMarker();
    }

    public void setMarkerRotation(double angleInRadians) {
        this.currentAngle = angleInRadians;
        updateMarker();
    }

    private void updateMarker() {
        float roomWidthPixels = floorPlanImageView.getWidth();
        float roomHeightPixels = floorPlanImageView.getHeight();

        double leftPadding = IndoorPositioningSettings.ROOM_TOP_LEFT.x * roomWidthPixels;
        double topPadding = IndoorPositioningSettings.ROOM_TOP_LEFT.y * roomHeightPixels;

        int xPos = (int) (leftPadding + ((currentPosition.x/IndoorPositioningSettings.VISUALISER_ROOM_WIDTH) * IndoorPositioningSettings.ROOM_BOTTOM_RIGHT.x * roomWidthPixels));
        int yPos = (int) (topPadding + ((currentPosition.y/IndoorPositioningSettings.VISUALISER_ROOM_HEIGHT) * IndoorPositioningSettings.ROOM_BOTTOM_RIGHT.y * roomHeightPixels));

        //xPos and yPos are the pixel positions of the marker. now we offset the direction marker
        int rotationMarkerXPos = (int) (xPos + (24 * Math.cos(this.currentAngle)));
        int rotationMarkerYPos = (int) (yPos + (24 * Math.sin(this.currentAngle)));

        //Update UI:
        this.positionTextView.setText("(" + (Math.round(currentPosition.x*100)/100.0) + ", " + (Math.round(currentPosition.y*100)/100.0) + ")");

        ConstraintLayout.LayoutParams markerLayoutParams = (ConstraintLayout.LayoutParams) positionMarkerImageView.getLayoutParams();
        markerLayoutParams.setMargins(xPos - positionMarkerImageView.getWidth()/2, yPos - positionMarkerImageView.getHeight()/2, 0, 0);
        positionMarkerImageView.setLayoutParams(markerLayoutParams);

        ConstraintLayout.LayoutParams rotationMarkerLayoutParams = (ConstraintLayout.LayoutParams) rotationMarkerImageView.getLayoutParams();
        rotationMarkerLayoutParams.setMargins(rotationMarkerXPos - rotationMarkerImageView.getWidth()/2, rotationMarkerYPos - rotationMarkerImageView.getHeight()/2, 0, 0);
        rotationMarkerImageView.setLayoutParams(rotationMarkerLayoutParams);
    }
}
