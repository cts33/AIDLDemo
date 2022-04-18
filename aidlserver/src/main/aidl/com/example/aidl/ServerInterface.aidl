// ServerInterface.aidl
package com.example.aidl;

import com.example.aidl.ClientCallback;

interface ServerInterface {

    /** client调用server ,传递数据json**/
    boolean sendMsgToServer(String packageName,String json);
    /** 注册一个callback ,用于回调给client数据**/
    void registerCallbackToServer(String packageName,in ClientCallback clientCallback);

    void unRegisterCallbackToServer(in ClientCallback clientCallback);

}