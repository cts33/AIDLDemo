package com.lll.libserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;


public class ServerService extends Service {
    private BindManager bindManager = BindManager.getInstance();

    private static final String TAG = "ServerService";
    private ServerInterface.Stub serverInterface = new ServerInterface.Stub() {
        @Override
        public boolean sendMsgToServer(String packageName, String json) throws RemoteException {
            Log.d(TAG, "-------------sendMsgToServer: " + json);
            if (!TextUtils.isEmpty(json)) {
                BindManager.getInstance().receiverClientMsg(json);
                return true;
            }
            return false;
        }

        @Override
        public void registerCallbackToServer(String packageName, ClientCallback clientCallback) throws RemoteException {
            Log.d(TAG, "----------------registerCallbackToServer: ");
            if (!TextUtils.isEmpty(packageName) && clientCallback != null) {
                bindManager.registerClientCallback(packageName, clientCallback);
            }
        }

        @Override
        public void unRegisterCallbackToServer(String packageName, ClientCallback clientCallback) throws RemoteException {
            if (clientCallback != null) {
                bindManager.unRegisterClientCallback(getPackageName(), clientCallback);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "------------ ----------------onCreate: ");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "------------------------------onBind: ");
        bindManager.setService(serverInterface);
        return serverInterface;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BindManager.killRemoteBackList();
    }
}