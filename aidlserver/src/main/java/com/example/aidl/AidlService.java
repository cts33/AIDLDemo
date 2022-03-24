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

public class AidlService extends Service {
    private static final String TAG = "AidlService";
    private final RemoteCallbackList<ClientCallback> mCallbackList = new RemoteCallbackList<>();

    public ClientCallback getClientCallback() {
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
            return "hello ,我是 server的数据";
        }

        @Override
        public void registerClientCallback(ClientCallback clientCallback) throws RemoteException {

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

        return new ServerStub();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public void unRegister(ClientCallback mclientCallback) {
        if (mclientCallback != null) {
            mCallbackList.unregister(mclientCallback);
        }
    }

}