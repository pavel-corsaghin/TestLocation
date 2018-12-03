package com.manhhung.test.locationtest;


import android.app.Application;
import android.content.Context;


public class App extends Application {

    private static App sApp;


    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;


    }

    public static Application getInstance() {
        return sApp;
    }

    public static Context getContext() {
        return sApp.getApplicationContext();
    }


}
