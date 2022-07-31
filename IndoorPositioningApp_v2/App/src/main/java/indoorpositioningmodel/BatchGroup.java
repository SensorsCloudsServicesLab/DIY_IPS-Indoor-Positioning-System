package indoorpositioningmodel;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.j2objc.annotations.Weak;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * A class to manage a batch with more than 500 documents (the limit)
 * This class only implements the batch.set method
 */
public class BatchGroup {

    private final WeakReference<Activity> activityWeakReference;
    private final FirebaseFirestore db;
    private final ArrayList<WriteBatch> batches;
    private int currentBatchCount;
    private int totalDocuments;
    private int completedBatches;
    private boolean running;

    public BatchGroup(Activity activity, FirebaseFirestore db) {
        this.activityWeakReference = new WeakReference<>(activity);
        this.db = db;
        this.batches = new ArrayList<>();
        this.batches.add(db.batch());
        this.currentBatchCount = 0;
        this.totalDocuments = 0;
        this.completedBatches = 0;
        this.running = false;
    }

    public void set(@NonNull DocumentReference documentRef, @NonNull Object data) {
        if (running) {
            Log.e("BatchGroup", "This BatchGroup has already started and cannot be rerun.");
            return;
        }

        if (currentBatchCount >= 500) {
            this.batches.add(db.batch());
            currentBatchCount = 0;
        }

        WriteBatch currentBatch = this.batches.get(this.batches.size()-1);
        currentBatch.set(documentRef, data);

        currentBatchCount++;
        totalDocuments++;
    }

    public void runBatches() {
        running = true;

        for (int i = 0; i < this.batches.size(); i++) {
            WriteBatch currentBatch = this.batches.get(i);
            currentBatch.commit().addOnSuccessListener((result) -> {
                completedBatches++;

                if (completedBatches == this.batches.size()) {
                    Activity currentActivity = activityWeakReference.get();
                    if (currentActivity != null && !currentActivity.isFinishing()) {
                        ToastManager.showToast(currentActivity, "Recorded " + totalDocuments + " observations across " + this.batches.size() + " batches.");
                    }
                }
            }).addOnFailureListener((@NonNull Exception e) -> {
                Activity currentActivity = activityWeakReference.get();
                if (currentActivity != null && !currentActivity.isFinishing()) {
                    ToastManager.showToast(currentActivity, "Error uploading data");
                }
                Log.e("BatchGroup", e.getMessage());
                e.printStackTrace();
            });
        }
    }
}
