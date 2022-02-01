package com.scslab.indoorpositioning;

import android.app.Activity;
import android.widget.Toast;

public class ToastManager {

    private static Toast currentToast;

    public static void showToast(Activity activity, String message) {
        if (currentToast != null) {
            currentToast.cancel();
        }

        currentToast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
        currentToast.show();
    }
}
