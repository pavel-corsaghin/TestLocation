package com.manhhung.test.locationtest;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;

public class LocationService extends Service {


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LocationUtils.requestSingleUpdate(this, false, new LocationUtils.LocationCallback() {
            @Override
            public void onNewLocationAvailable(final Location location) {
                Intent updateLocationIntent = new Intent(LocationUtils.UPDATE_LOCATION_ACTION);
                updateLocationIntent.putExtra(LocationUtils.LOCATION_DATA, LocationUtils.locationToString(location));
                sendBroadcast(updateLocationIntent);
            }
        }, LocationUtils.FIVE_MIN_MILLIES);
        return START_STICKY;
    }
}
