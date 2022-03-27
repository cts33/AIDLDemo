package com.example.aidl;

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

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private ServerInterface serverInterface;
    private boolean connected;
    private TextView result;

    ClientCallback clientCallback = new ClientCallback.Stub() {

        @Override
        public boolean sendMsgToClient(String json) throws RemoteException {
            Log.d(TAG, "---------------------sendMsgToClient: "+json);
            return true;
        }
    };
    IBinder.DeathRecipient deadthRecipient = new IBinder.DeathRecipient() {

        @Override
        public void binderDied() {
            Log.d(TAG, "run----------------------------------------:binderDied ");
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    bindService();
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

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                Log.d(TAG, "------------------------------onServiceConnected: ");
                serverInterface = ServerInterface.Stub.asInterface(service);

                serverInterface.registerCallbackToServer("com.example.client",clientCallback);
                service.linkToDeath(deadthRecipient, 0);

            } catch (RemoteException e) {
                e.printStackTrace();
            }
            connected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connected = false;
            Log.d(TAG, "------------------------------onServiceDisconnected: ");
            bindService();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.click).setOnClickListener(this);
        result = findViewById(R.id.result);

        bindService();


    }

    private void bindService() {
        Log.d(TAG, "------------------------------bindService: ");
        Intent intent = new Intent();
        intent.setPackage("com.example.aidlserver");
        intent.setAction("qqq.aaa.zzz");
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.click:
                try {
                    Log.d(TAG, "------------------------------onClick: ");
                    serverInterface.sendMsgToServer("this is client msg,server please receiver");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;

        }
    }
}