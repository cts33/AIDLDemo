package com.example.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AidlService extends Service {
    private static final String TAG = "AidlService";
    private boolean connected;
    private final RemoteCallbackList<ClientCallback> mCallbackList = new RemoteCallbackList<ClientCallback>(){
        @Override
        public void onCallbackDied(ClientCallback callback) {
            super.onCallbackDied(callback);
            Log.d(TAG, "--------------------------onCallbackDied: "+callback);


        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "------------ ----------------------onCreate: ");
        connected = true;
    }

    public ClientCallback getClientCallback() {
        Log.d(TAG, "------------------------------getClientCallback: ");
        ClientCallback temp = null;
        int i = mCallbackList.beginBroadcast();
        while (i > 0) {
            i--;
            temp =  mCallbackList.getBroadcastItem(i);
        }
        mCallbackList.finishBroadcast();

        return temp;
    }

    class ServerStub extends ServerInterface.Stub {

        @Override
        public String getMsgFromServer() throws RemoteException {
            Log.d(TAG, "------------------------------getMsgFromServer: ");
            return "hello ,我是 server的数据";
        }

        @Override
        public void registerClientCallback(ClientCallback clientCallback) throws RemoteException {
            Log.d(TAG, "------------------------------registerClientCallback: ");
            if (clientCallback != null) {
                mCallbackList.register(clientCallback);
            }
        }

        public AidlService getService() {
            return AidlService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "------------------------------onBind: ");
        return new ServerStub();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        connected = false;

        Log.d(TAG, "------------------------------onDestroy: ");
    }

    public void unRegister(ClientCallback mclientCallback) {
        Log.d(TAG, "------------------------------unRegister: ");
        if (mclientCallback != null) {
            mCallbackList.unregister(mclientCallback);
        }
    }

}