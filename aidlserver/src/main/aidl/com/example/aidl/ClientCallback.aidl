// ServerInterface.aidl
package com.example.aidl;

interface ClientCallback {
    /** 来自server的调用**/
     boolean onServerAction(String json);
}