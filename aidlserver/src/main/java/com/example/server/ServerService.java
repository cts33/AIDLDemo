package com.example.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.example.aidl.ClientCallback;
import com.example.aidl.ServerInterface;

public class ServerService extends Service {
    private static final String TAG = "AidlService";
    private BindManager mBindManager;
    private ServerInterface.Stub serverInterface = new ServerInterface.Stub() {
        @Override
        public boolean sendMsgToServer(String packageName, String json) throws RemoteException {
            Log.d(TAG, "-------------sendMsgToServer: " + json);
            if (!TextUtils.isEmpty(json)) {
                mBindManager.receiverClientMsg(json);
                return true;
            }
            return false;
        }

        @Override
        public void registerCallbackToServer(String packageName, ClientCallback clientCallback) throws RemoteException {
            Log.d(TAG, "----------------registerCallbackToServer: ");
            if (!TextUtils.isEmpty(packageName) && clientCallback != null) {
                mBindManager.registerClientCallback(packageName, clientCallback);
            }
        }

        @Override
        public void unRegisterCallbackToServer(String packageName, ClientCallback clientCallback) throws RemoteException {
            if (clientCallback != null) {
                mBindManager.unRegisterClientCallback(getPackageName(),clientCallback);
            }
        }

    };

    @Override
    public void onCreate() {
        super.onCreate();
        mBindManager = BindManager.getInstance(this);
        Log.d(TAG, "------------ ----------------onCreate: ");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "------------------------------onBind: ");
        return serverInterface;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBindManager.killRemoteBackList();
    }
}