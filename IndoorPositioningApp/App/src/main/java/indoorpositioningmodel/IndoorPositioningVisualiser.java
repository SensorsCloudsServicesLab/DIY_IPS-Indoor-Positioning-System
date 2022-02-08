package indoorpositioningmodel;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.scslab.indoorpositioning.R;

public class IndoorPositioningVisualiser {

    private TextView positionTextView;
    private ImageView floorPlanImageView;
    private ImageView positionMarkerImageView;

    private final double roomWidth = 13.5;
    private final double roomHeight = 13.5;
    private final Position roomTopLeft = new Position(0.009, 0.009);  //in percentage
    private final Position roomBottomRight = new Position(0.964, 0.977); //in percentage

    public IndoorPositioningVisualiser(Activity activity) {
        this.positionTextView = activity.findViewById(R.id.position);
        this.floorPlanImageView = activity.findViewById(R.id.floor_plan);
        this.positionMarkerImageView = activity.findViewById(R.id.position_marker);
        this.setMarkerPosition(0, 0);
    }

    public void setMarkerPosition(double x, double y) {
        this.positionTextView.setText("(" + (Math.round(x*100)/100.0) + ", " + (Math.round(y*100)/100.0) + ")");

        float roomWidthPixels = floorPlanImageView.getWidth();
        float roomHeightPixels = floorPlanImageView.getHeight();

        double leftPadding = roomTopLeft.x * roomWidthPixels;
        double topPadding = roomTopLeft.y * roomHeightPixels;

        int xPos = (int) (leftPadding + ((x/roomWidth) * roomBottomRight.x * roomWidthPixels));
        int yPos = (int) (topPadding + ((y/roomHeight) * roomBottomRight.y * roomHeightPixels));

        ConstraintLayout.LayoutParams markerLayoutParams = (ConstraintLayout.LayoutParams) positionMarkerImageView.getLayoutParams();
        markerLayoutParams.setMargins(xPos - positionMarkerImageView.getWidth()/2, yPos - positionMarkerImageView.getHeight()/2, 0, 0);
        positionMarkerImageView.setLayoutParams(markerLayoutParams);
    }



}
