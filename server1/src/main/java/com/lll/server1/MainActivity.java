package com.lll.server1;

import android.app.Application;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.lll.libserver.BindManager;
import com.lll.libserver.IUpdateUI;
import com.lll.libserver.ServerInterface;
import com.lll.libserver.ServerService;

public class MainActivity extends AppCompatActivity implements IUpdateUI {
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
        int pid = android.os.Process.myPid();
        Log.d(TAG, "receiverClientMsg: pid=" + pid);

        initViews();
        bindManager = BindManager.getInstance();
    }

    private void initViews() {
        mClick = findViewById(R.id.click);
        mResult = findViewById(R.id.result);
        mClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "------------------------ ------------onClick: main thread=" + (Looper.getMainLooper() == Looper.myLooper()));
                String json = "{\"method\":\"invoke\",\"params\":[{\"key1\":\"value1\"}],\"Boolean\":\"false\"}";
                bindManager.sendMsgToClient( json);
            }
        });
    }
    private static final String TAG = "mainActivity";

    @Override
    public void updateUI(String json) {
        Log.d(TAG, "updateUI() called with: json = [" + json + "]");
    }
}