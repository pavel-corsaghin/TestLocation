package com.manhhung.test.locationtest;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;

import static com.manhhung.test.locationtest.LocationUtils.enableLocationService;
import static com.manhhung.test.locationtest.LocationUtils.isLocationServiceEnabled;

public class NetworkUtils {

    private static boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private static void connect(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("No internet connection")
                .setCancelable(false)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkNetworkAndGps(context);
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((Activity) context).finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static boolean checkNetworkAndGps(Context context) {
        if (!isConnected(context)) {
            connect(context);
            return false;
        } else {
            if (!isLocationServiceEnabled()) {
                enableLocationService(context);
            }
            return true;
        }
    }
}
