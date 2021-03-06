package com.example.server;

import android.content.Context;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.example.aidl.ClientCallback;

/**
 * @description
 * @mail chentaishan@aliyun.com
 * @date 2022/3/25
 */
public class BindManager {
    private static final String TAG = "BindManager";
    private static BindManager mBindManager = null;
    private static Context context;
    private static RemoteCallbackList<ClientCallback> remoteCallbackList = new RemoteCallbackList<>();

    private BindManager(Context context) {
        this.context = context;
    }

    public static BindManager getInstance(Context context) {
        if (mBindManager == null) {
            synchronized (BindManager.class) {
                if (mBindManager == null)
                    mBindManager = new BindManager(context);
            }
        }
        return mBindManager;
    }

    public static void killRemoteBackList() {
        Log.d(TAG, "------------------------------clearCallbacks: ");
        remoteCallbackList.kill();

    }

    public static void registerClientCallback(String packageName, ClientCallback clientCallback) {
        Log.d(TAG, "------------------------------registerClientCallback: " + packageName);
        remoteCallbackList.register(clientCallback, packageName);

    }

    public static void unRegisterClientCallback(String packageName,ClientCallback clientCallback) {
        Log.d(TAG, "------------------------------unregisterClientCallback: ");
        remoteCallbackList.unregister(clientCallback);
        remoteCallbackList.onCallbackDied(clientCallback,packageName);
    }

    /**
     * 服务端接受客户端的信息
     *
     * @param json
     */
    public static void receiverClientMsg(String json) {
        // TODO 接受到客户端发来的信息，未来要通知小程序框架，执行某操作
        Log.d(TAG, "receiverClientMsg: " + json);
    }

    /**
     * 服务端发送信息给客户端
     *
     * @param json
     */
    public static void sendMsgToClient(String packageName, String json) {
        try {
            int i = remoteCallbackList.beginBroadcast();
            Log.d(TAG, "------------------sendMsgToClient: (i="+i+") json:" + json+"  packageName:"+packageName);
            while (i > 0) {
                i--;
                String cookie = (String) remoteCallbackList.getBroadcastCookie(i);
                if (packageName.equals(cookie)) {
                    ClientCallback callback = remoteCallbackList.getBroadcastItem(i);
                    callback.onServerAction(json);
                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }finally {
            remoteCallbackList.finishBroadcast();
        }
    }
}
