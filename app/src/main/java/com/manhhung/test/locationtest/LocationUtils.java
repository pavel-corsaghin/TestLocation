package com.manhhung.test.locationtest;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import java.util.Timer;
import java.util.TimerTask;

public class LocationUtils {

    private static final String LOG = "LocationUtils";
    private static final int PENDING_INTENT_LOCATION_REPORTING_ID = 6996;
    private static final int TEN_MIN_MILLIES = 10 * 60 * 1000; // 10 minutes
    public static final int FIVE_MIN_MILLIES = 5 * 60 * 1000; // 5 minutes
    public static final int ONE_MIN_MILLIES = 60 * 1000; // 1 minutes to auto check location
    public static final String UPDATE_LOCATION_ACTION = "com.manhhung.test.locationtest.action.update.location";
    public static final String LOCATION_DATA = "location_data";


    private static boolean sKeepSearching;

    public interface LocationCallback {
        void onNewLocationAvailable(Location location);
    }


    public static boolean isLocationServiceEnabled() {
        LocationManager lm = (LocationManager) App.getContext().getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        return isGpsEnabled || isNetworkEnabled;
    }

    public static void enableLocationService(final Context context) {
        // notify user
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage("Please enable gps");
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(myIntent);
                //get gps
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                if (context instanceof MainActivity) {
                    ((Activity) context).finish();
                }
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    // callsback to calling thread, note this is for low grain: if you want higher precision, swap the
    // contents of the else and if. Also be sure to check gps permission/settings are allowed.
    // call usually takes <10ms
    public static boolean requestSingleUpdate(final Context context, final boolean inForeground,
                                              final LocationCallback callback, final long timeLimit) {
        sKeepSearching = true;
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        final Timer timer = new Timer();

        final boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        final boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGPSEnabled && !isNetworkEnabled) {
            if (inForeground) {
                enableLocationService(context);
            }
            return false;
        }

        if (isGPSEnabled && checkPermission(context)) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            locationManager.requestSingleUpdate(criteria, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (!sKeepSearching) return;
                    sKeepSearching = false;
                    timer.cancel();
                    callback.onNewLocationAvailable(location);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            }, null);
        }

        if (isNetworkEnabled && checkPermission(context)) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            locationManager.requestSingleUpdate(criteria, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (!sKeepSearching) return;
                    sKeepSearching = false;
                    timer.cancel();
                    callback.onNewLocationAvailable(location);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            }, null);
        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!sKeepSearching) return;
                sKeepSearching = false;
                callback.onNewLocationAvailable(getLatestSavedLocation(context));
            }
        }, timeLimit);
        return true;
    }


    public static void startReportLocationPeriodically(Context context, long intervalTime) {
        Intent alarmIntent = new Intent(context, LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context,
                PENDING_INTENT_LOCATION_REPORTING_ID,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                intervalTime,
                pendingIntent);
    }


    public static void stopReportLocationPeriodically(Context context){
        Intent alarmIntent = new Intent(context, LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context,
                PENDING_INTENT_LOCATION_REPORTING_ID,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private static boolean checkPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }


    private static Location getLatestSavedLocation(Context context) {
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (checkPermission(context)) {
            Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (gpsLocation != null
                    && System.currentTimeMillis() > gpsLocation.getTime() + TEN_MIN_MILLIES) {
                gpsLocation = null;
            }

            if (networkLocation != null
                    && System.currentTimeMillis() > networkLocation.getTime() + TEN_MIN_MILLIES) {
                networkLocation = null;
            }

            //if there are both values use the newer one
            if (gpsLocation != null && networkLocation != null) {
                return gpsLocation.getTime() > networkLocation.getTime() ? gpsLocation : networkLocation;
            }

            if (gpsLocation != null) {
                return gpsLocation;
            }

            if (networkLocation != null) {
                return networkLocation;
            }

            // Cannot get location in anyway

            return null;
        } else {
            return null;
        }
    }


    public static String locationToString(Location location) {
        if (location != null) {
            return "lat: " + location.getLongitude() + ", lng: " + location.getLatitude();
        } else {
            return "0 0";
        }
    }
}