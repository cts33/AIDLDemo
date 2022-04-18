package com.example.testaidlsdkdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    lateinit var viewHolder: LinearLayout
    lateinit var viewHolder1: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewHolder = findViewById(R.id.view_holder)
        viewHolder1 = findViewById(R.id.view_holder_1)

        findViewById<Button>(R.id.click1).setOnClickListener {
            var appletManager = AppletManager.init(
                this.application, object : com.gwm.applet.sdk.IAppletCallback() {
                    override fun connected() {
                        Log.d(TAG, "---------connected: ")
                    }

                    override fun unConnected() {
                        Log.d(TAG, "----------unConnected: ")
                    }

                    override fun onAppletAction(json: String?): Boolean {
                        Log.d(TAG, "---------onAppletAction: $json")
                        return true
                    }
                })
            AppletManager.initRemoteManager(RemoteManager(this, appletManager))
        }

        findViewById<Button>(R.id.click2).setOnClickListener {
// AppletManager.startApplet("demo", "page/page1/index.html", "")
// AppletManager.sendMsgToApplet("packageName", "json")
            AppletManager.startLocalApplet(viewHolder, "ticketDemo", "", "")
// AppletManager.startLocalApplet(viewHolder1, "washDemo", "", "")
        }
        findViewById<Button>(R.id.click3).setOnClickListener {
            AppletManager.disConnect()
// AppletManager.sendMsgToApplet("{'msg-from-client':'success'}")
        }

    }
}

