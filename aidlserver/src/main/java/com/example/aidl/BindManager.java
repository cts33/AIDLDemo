package com.example.aidl;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @description
 * @mail chentaishan@aliyun.com
 * @date 2022/3/25
 */
public class BindManager {
    private static final String TAG = "BindManager";

    public static final Object oo = new Object();
    private static BindManager mBindManager = null;

    public static final String ACTION = "qqq.aaa.zzz";
    public static final String PACKAGE = "com.example.aidlserver";
    private static HashMap<String, ClientCallback> callbackHashMap = new HashMap<>();
    private static Application context;

    private BindManager(Application context) {
        this.context = context;
    }

    public static BindManager getInstance(Application context) {
        if (mBindManager == null) {
            synchronized (oo) {
                if (mBindManager == null)
                    mBindManager = new BindManager(context);
            }
        }

        return mBindManager;
    }

    private static boolean connected;

    public static void unRegister() {
        Log.d(TAG, "------------------------------unRegister: ");
        callbackHashMap.clear();
    }

    private static IBinder.DeathRecipient deadthRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG, "------------------------------------------------------run: binderDied");
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // TODO 启动服务
                    startServer();
                }
            }, 3000);

            while (connected) {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
            }
        }
    };
    private static ServerInterface serverStub;
    private static ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            serverStub = ServerInterface.Stub.asInterface(service);
            try {
                serverStub.asBinder().linkToDeath(deadthRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            connected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connected = false;
            startServer();
        }
    };

    public static void registerClientCallback(String packageName, ClientCallback clientCallback) {
        Log.d(TAG, "------------------------------registerClientCallback: ");
        callbackHashMap.put(packageName, clientCallback);
    }

    /**
     * 服务端接受客户端的信息
     *
     * @param json
     */
    public static void receiverClientMsg(String json) {

        // TODO 接受到客户端发来的信息，未来要通知小程序框架，执行某操作
        Log.d(TAG, "receiverClientMsg: ");


    }

    /**
     * 服务端发送信息给客户端
     *
     * @param json
     */
    public static void sendMsgToClient(String json) {
        ClientCallback callback;
        try {
            Set<Map.Entry<String, ClientCallback>> entries = callbackHashMap.entrySet();
            Iterator<Map.Entry<String, ClientCallback>> iterator = entries.iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, ClientCallback> next = iterator.next();
                callback = next.getValue();
                if (callback==null){
                    continue;
                }
                callback.sendMsgToClient(json);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private static void startServer() {
        Log.d(TAG, "-----------------------------------------------------bindService: ");
        Intent intent = new Intent();
        intent.setPackage(PACKAGE);
        intent.setAction(ACTION);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
}
