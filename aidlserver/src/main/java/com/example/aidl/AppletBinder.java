package com.example.aidl;

import android.os.Parcel;
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
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {

        Log.d(TAG, "onTransact: " + code);
        if (code == 0x001) {
            String result = data.readString();

            data.enforceInterface("xxx");
            reply.writeNoException();
            Log.d(TAG, "onTransact: result=" + result);
            reply.writeString("hello i am server");
            return true;
        }

        return super.onTransact(code, data, reply, flags);
    }

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
}
