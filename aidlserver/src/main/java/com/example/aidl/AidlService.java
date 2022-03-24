package com.example.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AidlService extends Service {
    private static final String TAG = "AidlService";

    @Override
    public IBinder onBind(Intent intent) {
        return new AIDLServerStub();
    }
}