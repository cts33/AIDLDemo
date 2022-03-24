// ServerInterface.aidl
package com.example.aidl;

import com.example.aidl.ClientCallback;

interface ServerInterface {

    /** client调用server ,获取数据json**/
    String getMsgFromServer();
    /** 注册一个callback ,用于回调给client数据**/
    void registerClientCallback(in ClientCallback clientCallback);
}