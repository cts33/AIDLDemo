// ServerInterface.aidl
package com.lll.libserver;
interface ClientCallback {
    /** 来自server的调用**/
     boolean onServerAction(String json);
}