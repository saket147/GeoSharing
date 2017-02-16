package com.example.socket.geosharing;

/**
 * Created by socket on 16/2/17.
 */
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = "GeofenceTransitions";

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(TAG, "onHandleIntent");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            //String errorMessage = GeofenceErrorMessages.getErrorString(this,
            //      geofencingEvent.getErrorCode());
            Log.e(TAG, "Goefencing Error " + geofencingEvent.getErrorCode());
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        Log.i(TAG, "geofenceTransition = " + geofenceTransition + " Enter : " + Geofence.GEOFENCE_TRANSITION_ENTER + "Exit : " + Geofence.GEOFENCE_TRANSITION_EXIT);
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL){
            showNotification("Entered", "Entered the Location");
        }
        else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.i(TAG, "Showing Notification...");
            showNotification("Exited", "Exited the Location");
        } else {
            // Log the error.
            showNotification("Error", "Error");
            Log.e(TAG, "Error ");
        }
    }

    public void showNotification(String text, String bigText) {

        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Title")
                .setContentText(text)
                .setContentIntent(pendingNotificationIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
        notificationManager.notify(0, notification);
    }
}
