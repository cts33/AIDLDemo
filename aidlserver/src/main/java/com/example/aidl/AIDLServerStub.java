package com.example.aidl;

import android.os.RemoteException;

/**
 * @description
 * @mail chentaishan@aliyun.com
 * @date 2022/3/24
 */
public class AIDLServerStub extends ServerInterface.Stub {
    private ClientCallback clientCallback;

    @Override
    public String getMsgFromServer() throws RemoteException {
        return "hello ,我是 server的数据";
    }

    @Override
    public void registerClientCallback(ClientCallback clientCallback) throws RemoteException {

        this.clientCallback = clientCallback;
    }

    public ClientCallback getClientCallback() {
        return clientCallback;
    }
}
