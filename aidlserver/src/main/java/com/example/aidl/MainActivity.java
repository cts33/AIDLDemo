package com.example.aidl;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
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
    private boolean connected;
    private TextView mResult;
    private ServerInterface serverStub;
    private ServerService mService;
    BindManager bindManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        bindManager = BindManager.getInstance((Application) MainActivity.this.getApplicationContext());


    }

    private void initViews() {
        mClick = findViewById(R.id.click);
        mResult = findViewById(R.id.result);
        mClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String json = "{\"method\":\"invoke\",\"params\":[{\"key1\":\"value1\"}],\"Boolean\":\"false\"}";

                bindManager.sendMsgToClient(json);

//                mResult.setText(clientMsg);
                Log.d(TAG, "------------------------ ------------onClick: main thread=" + (Looper.getMainLooper() == Looper.myLooper()));
            }
        });
    }

    private static final String TAG = "mainActivity";


}