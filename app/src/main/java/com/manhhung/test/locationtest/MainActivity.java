package com.manhhung.test.locationtest;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private boolean reportingPeriodically = false;

    final BroadcastReceiver updateLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getStringExtra(LocationUtils.LOCATION_DATA) != null) {
                Toast.makeText(MainActivity.this, "Location updated", Toast.LENGTH_LONG).show();
                ((TextView) findViewById(R.id.txtLocationShow)).setText(intent.getStringExtra(LocationUtils.LOCATION_DATA));
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnRequestLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationUtils.requestSingleUpdate(MainActivity.this, true, new LocationUtils.LocationCallback() {
                    @Override
                    public void onNewLocationAvailable(Location location) {
                        Toast.makeText(MainActivity.this, "Location updated", Toast.LENGTH_LONG).show();
                        ((TextView) findViewById(R.id.txtLocationShow)).setText(LocationUtils.locationToString(location));
                    }
                }, LocationUtils.FIVE_MIN_MILLIES);
            }
        });

        findViewById(R.id.btnRequestLocationPeriodically).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reportingPeriodically){
                    ((Button)v).setText("Start request location every minute");
                    LocationUtils.stopReportLocationPeriodically(MainActivity.this);
                } else {
                    ((Button)v).setText("Stop request location every minute");
                    LocationUtils.startReportLocationPeriodically(MainActivity.this, LocationUtils.ONE_MIN_MILLIES);
                }

                reportingPeriodically = !reportingPeriodically;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(updateLocationReceiver, new IntentFilter(LocationUtils.UPDATE_LOCATION_ACTION));
        NetworkUtils.checkNetworkAndGps(this);
        checkPermission();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(updateLocationReceiver);
        super.onPause();
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, request it from user. When user responds, your activity will call method "onRequestPermissionResult", which you should override
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // PERMISSION WAS GRANTED
                } else {
                    Toast.makeText(this, "You need grant permission to use the app", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }


}
