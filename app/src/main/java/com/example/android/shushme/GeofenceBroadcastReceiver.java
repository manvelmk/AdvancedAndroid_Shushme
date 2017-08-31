package com.example.android.shushme;

/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build.VERSION;
import android.support.v7.app.NotificationCompat;
import android.support.v7.app.NotificationCompat.Builder;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = GeofenceBroadcastReceiver.class.getSimpleName();

    /***
     * Handles the Broadcast message sent when the Geofence Transition is triggered
     * Careful here though, this is running on the main thread so make sure you start an AsyncTask for
     * anything that takes longer than say 10 second to run
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive called");

        // DONE (4) Use GeofencingEvent.fromIntent to retrieve the GeofencingEvent that caused the transition
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, String.format("Error code : %d", geofencingEvent.getErrorCode()));
            return;
        }

        // DONE (5) Call getGeofenceTransition to get the transition type and use AudioManager to set the
        // phone ringer mode based on the transition type. Feel free to create a helper method (setRingerMode)
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        // Check the type of transition triggering this event
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            setRingerMode(context, AudioManager.RINGER_MODE_SILENT);
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            setRingerMode(context, AudioManager.RINGER_MODE_NORMAL);
        } else {
            Log.e(TAG, String.format("Unknown transition: %d", geofenceTransition));
            return;
        }

        sendNotification(context, geofenceTransition);
    }


    // Helper method to switch ringer mode
    private void setRingerMode(Context context, int mode) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (VERSION.SDK_INT < 24 || (VERSION.SDK_INT >= 24 && !notificationManager.isNotificationPolicyAccessGranted())) {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(mode);
        }
    }


    // DONE (6) Show a notification to alert the user that the ringer mode has changed.
    // Feel free to create a helper method (sendNotification)
    private void sendNotification(Context context, int transitionType) {
        // Explicit Intent for MainActivity
        Intent notificationIntent = new Intent(context, MainActivity.class);

        // Make a task stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add MainActivity as parent to TaskStack
        stackBuilder.addParentStack(MainActivity.class);

        // Push content Intent on the stack
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent with the entire back stack
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Make a notification builder
        NotificationCompat.Builder builder = new Builder(context);

        // Check transition type and display the correct icon
        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            builder.setSmallIcon(R.drawable.ic_volume_off_white_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.ic_volume_off_white_24dp))
                    .setContentTitle(context.getString(R.string.silent_mode_activated));
        } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
            builder.setSmallIcon(R.drawable.ic_volume_up_white_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.ic_volume_up_white_24dp))
                    .setContentTitle(context.getString(R.string.back_to_normal));
        }

        builder.setContentText(context.getString(R.string.touch_to_relaunch));
        builder.setContentIntent(notificationPendingIntent);

        // Dismiss the notification after touching it
        builder.setAutoCancel(true);

        // Get an instance of Notification manager
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, builder.build());
    }
}
