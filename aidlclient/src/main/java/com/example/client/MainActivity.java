package com.example.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.lll.libserver.ClientCallback;
import com.lll.libserver.ServerInterface;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private ServerInterface serverInterface;
    private boolean connected;
    private TextView result;

    private ClientCallback clientCallback = new ClientCallback.Stub() {
        @Override
        public boolean onServerAction(String json) throws RemoteException {
            Log.d(TAG, "-----------1----------sendMsgToClient: " + json);
            return true;
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                Log.d(TAG, "------------------------------onServiceConnected: " + getPackageName());
                serverInterface = ServerInterface.Stub.asInterface(service);
                serverInterface.registerCallbackToServer(getPackageName(), clientCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            connected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connected = false;
            try {
                Log.d(TAG, "------------------------------onServiceDisconnected: ");
                serverInterface.unRegisterCallbackToServer(getPackageName(), clientCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.click1).setOnClickListener(this);
        findViewById(R.id.click2).setOnClickListener(this);
        findViewById(R.id.click3).setOnClickListener(this);
        result = findViewById(R.id.result);

        bindService();
    }

    private void bindService() {
        Log.d(TAG, "------------------------------bindService: ");
        Intent intent = new Intent("qqq.aaa.zzz");
        String pp = "com.lll.libserver";
//        intent.setComponent(new ComponentName(, pp + ".ServerService"));
        intent.setPackage(pp);
//        intent.setType(pp);
        boolean status =  bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "------------------------------bindService: " + status);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.click1:

                Log.d(TAG, "---------1---------------------onClick: ");
                bindService();
                break;
            case R.id.click2:
                Log.d(TAG, "---------2---------------------onClick: ");
                try {
                    serverInterface.sendMsgToServer(getPackageName(), "this is client msg,server please receiver");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.click3:
                Log.d(TAG, "-----------3-------------------onClick: ");
                throw new NullPointerException("client 崩溃");
        }
    }
}