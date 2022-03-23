package com.example.aidl;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;


import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    IMyAidlInterface iMyAidlInterface;
    private boolean connected;


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
            connected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connected = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.click).setOnClickListener(this);
        findViewById(R.id.click1).setOnClickListener(this);
        findViewById(R.id.click2).setOnClickListener(this);

        bindService();


    }

    private void bindService() {
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
                    Book book = new Book("C++");
                    book.setName("java=" + v.getId());
                    iMyAidlInterface.addBookInOut(book);
                    Log.d(TAG, "onClick: addBookInOut");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.click1:
                String str = null;
                try {
                    str = iMyAidlInterface.getString();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "onClick: getString=" + str);
                break;
            case R.id.click2:
                List<Book> bookList = null;
                try {
                    bookList = iMyAidlInterface.getBookList();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                for (Book book : bookList) {
                    Log.d(TAG, "   book=" + book.getName());

                }
                Log.d(TAG, "onClick: getBookList");
                break;
        }
    }
}