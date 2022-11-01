package com.lll.libserver;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

/**
 * @description
 * @mail chentaishan@aliyun.com
 * @date 2022/3/25
 */
public class BindManager {
    private static String TAG = "";
    private static BindManager mBindManager = null;
    private static Context context;
    private static RemoteCallbackList<ClientCallback> remoteCallbackList = new RemoteCallbackList<>();
    private  static IUpdateUI iUpdateUI;
    private IBinder mService;

    public static BindManager getInstance( ) {
        if (mBindManager == null) {
            synchronized (BindManager.class) {
                if (mBindManager == null)
                    mBindManager = new BindManager( );
            }
        }
        return mBindManager;
    }
    public void setService(IBinder iBinder) {
        Log.d(TAG, "setService() called with: iBinder = [" + iBinder + "]");
        this.mService = iBinder;
    }
    public void setUIObj(IUpdateUI iUpdateUI) {
        Log.d(TAG, "setUIObj() called with: iUpdateUI = [" + iUpdateUI + "]");
        this.iUpdateUI = iUpdateUI;
    }

    public static void killRemoteBackList() {
        Log.d(TAG, "------------------------------clearCallbacks: ");
        remoteCallbackList.kill();
    }

    public static void registerClientCallback(String packageName, ClientCallback clientCallback) {
        Log.d(TAG, "------------------------------registerClientCallback: " + packageName);
        remoteCallbackList.register(clientCallback, packageName);

    }

    public static void unRegisterClientCallback(String packageName, ClientCallback clientCallback) {
        Log.d(TAG, "------------------------------unregisterClientCallback: ");
        remoteCallbackList.unregister(clientCallback);
        remoteCallbackList.onCallbackDied(clientCallback, packageName);
    }

    /**
     * 服务端接受客户端的信息
     *
     * @param json
     */
    public static void receiverClientMsg(String json) {
        // TODO 接受到客户端发来的信息，未来要通知小程序框架，执行某操作
        Log.d(TAG, "receiverClientMsg: " + json);
        int pid = android.os.Process.myPid();
        Log.d(TAG, "receiverClientMsg: pid=" + pid);

//        if (iUpdateUI instanceof IUpdateUI) {
//            iUpdateUI.updateUI(json);
//        }
    }

    /**
     * 服务端发送信息给客户端
     *
     * @param json
     */
    public static void sendMsgToClient(String json) {
        try {
            int i = remoteCallbackList.beginBroadcast();
            Log.d(TAG, "------------------sendMsgToClient: (i=" + i + ") json:" + json);
            while (i > 0) {
                i--;
                String cookie = (String) remoteCallbackList.getBroadcastCookie(i);
                ClientCallback callback = remoteCallbackList.getBroadcastItem(i);
                callback.onServerAction(json);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            remoteCallbackList.finishBroadcast();
        }
    }
}
