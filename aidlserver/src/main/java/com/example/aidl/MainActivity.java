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
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private Button mClick;
    private TextView mResult;
    AIDLServerStub aidlServerStub;
    IBinder.DeathRecipient deadthRecipient = new IBinder.DeathRecipient() {

        @Override
        public void binderDied() {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {

                }
            }, 3000);

            while (connected) {
                timer.cancel();
                timer = null;
            }
        }
    };

    private boolean connected;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                service.linkToDeath(deadthRecipient, 0);
                ServerInterface serverInterface = ServerInterface.Stub.asInterface(service);
                aidlServerStub = (AIDLServerStub) serverInterface.asBinder();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            connected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connected = false;
            bindService();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        bindService();
    }

    private void initViews() {
        mClick = findViewById(R.id.click);
        mResult = findViewById(R.id.result);
        mClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");

                String clientMsg = null;
                try {
                    ClientCallback clientCallback = aidlServerStub.getClientCallback();
                    if (clientCallback==null){
                        Log.d(TAG, "onClick: (clientCallback==null)");
                        return;
                    }
                    clientMsg = clientCallback.getMsgFromClient();
                    Log.d(TAG, "onClick: clientMsg=" + clientMsg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static final String TAG = "mainActivity";

    private void bindService() {
        Intent intent = new Intent();
        intent.setPackage("com.example.aidlserver");
        intent.setAction("qqq.aaa.zzz");
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
}