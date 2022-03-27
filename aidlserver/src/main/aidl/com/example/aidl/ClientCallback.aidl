// ServerInterface.aidl
package com.example.aidl;

interface ClientCallback {
    /** 发送json格式数据给client**/
     boolean sendMsgToClient(String json);
}