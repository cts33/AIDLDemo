package com.example.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AidlService extends Service {
    private static final String TAG = "AidlService";
    ArrayList list = new ArrayList<Book>();
    private final IMyAidlInterface.Stub stub = new IMyAidlInterface.Stub() {
        @Override
        public String getString() throws RemoteException {
            Log.d(TAG, "getString: ");
            return " this is string";
        }

        @Override
        public List<Book> getBookList() throws RemoteException {


            Log.d(TAG, "getBookList: ");
            return list;
        }


        @Override
        public void addBookInOut(Book book) throws RemoteException {
            Log.d(TAG, "addBookInOut: "+book);
            list.add(book);
        }
    };

    public AidlService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }
}