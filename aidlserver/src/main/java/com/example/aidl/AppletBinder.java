package com.example.aidl;

import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

/**
 * @description
 * @mail chentaishan@aliyun.com
 * @date 2022/3/25
 */
public class AppletBinder extends ServerInterface.Stub {
    private static final String TAG = AppletBinder.class.getSimpleName();
    private BindManager mBindManager;
    public AppletBinder(BindManager mBindManager) {
        this.mBindManager = mBindManager;
    }

    @Override
    public void sendMsgToServer(String json) throws RemoteException {
        Log.d(TAG, "-------------sendMsgToServer: ");
        if (!TextUtils.isEmpty(json)) {
            mBindManager.receiverClientMsg(json);
        }
    }

    @Override
    public void registerCallbackToServer(String packageName, ClientCallback clientCallback) throws RemoteException {
        Log.d(TAG, "----------------registerCallbackToServer: ");
        if (!TextUtils.isEmpty(packageName) && clientCallback != null) {
            mBindManager.registerClientCallback(packageName, clientCallback);
        }
    }
}
