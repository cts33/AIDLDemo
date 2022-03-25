package com.example.aidl;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class ServerService extends Service {
    private static final String TAG = "AidlService";

    private AppletBinder appletBinder = null;

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = this.getApplicationContext();

        appletBinder = new AppletBinder(BindManager.getInstance((Application) context));
        Log.d(TAG, "------------ ----------------onCreate: ");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "------------------------------onBind: ");
        return appletBinder;
    }


}