
# 1.特点

AIDL定义`客户端与服务均认可的编程接`口，以便二者使用进程间通信 (IPC) 进行相互通信。在 Android 中，一个进程通常无法访问另一个进程的内存。因此，为进行通信，进程需将其对象分解成可供操作系统理解的原语，并将其编组为可供您操作的对象。编写执行该编组操作的代码较为繁琐，因此 Android 会使用 AIDL 为您处理此问题。

> 注意：
> 只有在不同应用的客户端通过 IPC 方式访问服务，且在服务中进行多线程处理时，才有必要使用 AIDL。
>
> 如果您无需跨不同应用执行并发 IPC，则应通过实现 Binder来创建接口
>
> 如果您想执行 IPC，但*不*需要处理多线程，请[使用 Messenger 来实现接口。无论如何，在实现 AIDL 之前，请您务必理解[绑定服务

在开始设计 AIDL 接口之前，请注意，AIDL 接口的调用是直接函数调用。您无需对发生调用的线程做任何假设。实际情况的差异取决于调用是来自本地进程中的线程，还是远程进程中的线程。具体而言：

- 来自本地进程的调用和发起调用在同一线程内。如果该线程是您的主界面线程，则其将继续在 AIDL 接口中执行。如果该线程是其他线程，则其便是在服务中执行代码的线程。因此，只有在本地线程访问服务时，您才能完全控制哪些线程在服务中执行（但若出现此情况，您根本无需使用 AIDL，而应通过[实现 Binder 类](https://developer.android.com/guide/components/bound-services#Binder)来创建接口）。
- 远程进程的调用分配来自线程池，且平台会在您自己的进程内部维护该线程池。您必须为来自未知线程，且多次调用同时发生的传入调用做好准备。换言之，AIDL 接口的实现必须基于完全的线程安全。如果调用来自同一远程对象上的某个线程，则该调用将**依次**抵达接收器端。
- `oneway` 关键字用于修改远程调用的行为。使用此关键字后，远程调用不会屏蔽，而只是发送事务数据并立即返回。最终接收该数据时，接口的实现会将其视为来自 `Binder` 线程池的常规调用（普通的远程调用）。如果 `oneway` 用于本地调用，则不会有任何影响，且调用仍为同步调用。


## 1.AIDL可以传递哪些数据

- Java 编程语言中的所有原语类型（如 `int`、`long`、`char`、`boolean` 等）

- `String`  CharSequence List

  `List` 中的所有元素必须是以上列表中支持的数据类型，或者您所声明的由 AIDL 生成的其他接口或 Parcelable 类型。

  Map

  `Map` 中的所有元素必须是以上列表中支持的数据类型，或者您所声明的由 AIDL 生成的其他接口或 Parcelable 类型。不支持泛型 Map（如 `Map<String,Integer>` 形式的 Map）

如果你要使用上方未列出的附加类型，如Book对象，要添加一条 `import` 语句。也是为什么创建Book.aidl文件的原因

请注意：

- 方法可有参数和返回值或空值。

- 所有非原语参数均需要指示数据走向的方向标记（in out inout）。这类标记可以是原语类型默认为 `in`，不能是其他方向。

  `注意：您应将方向限定为真正需要的方向，因为编组参数的开销较大`。

- 生成的 `IBinder` 接口内包含 `.aidl` 文件中的所有代码注释（import 和 package 语句之前的注释除外）。

- 您可以在 ADL 接口中定义 String 常量和 int 字符串常量。例如：`const int VERSION = 1;`。

- 方法调用由 transact() 代码分派，该代码通常基于接口中的方法索引。由于这会增加版本控制的难度，因此您可以向方法手动配置事务代码：`void method() = 10;`。

- 使用 `@nullable` 注释可空参数或返回类型。

## 2.in out  inout
所有非基本类型的参数都需要一个定向tag来表明数据是如何走向的，要不是in，out或者inout。基本数据类型默认是in，而且不能是其他tag。
其中 in 表示数据只能由客户端流向服务端， out 表示数据只能由服务端流向客户端，而 inout 则表示数据可以在服务端与客户端之间双向流通。

# 2.服务端

## a. 创建 module_server,然后右键添加aidl文件，注意包名

ServerInterface.aidl
```java
interface ServerInterface {

    /** client调用server ,传递数据json**/
    boolean sendMsgToServer(String packageName,String json);
    /** 注册一个callback ,用于回调给client数据**/
    void registerCallbackToServer(String packageName,in ClientCallback clientCallback);

}
```
ClientCallback.aidl
```java
import com.example.aidl.Book;

interface ClientCallback {
    /** 发送json格式数据给client**/
     boolean sendMsgToClient(String json);
   //boolean sendMsgToClient(Book book);
}
```
如果内部需要传递对象Book,就需要序列化。注意导包Book

Book.java
```java
public class Book implements Parcelable {
    String name;
...
```

同时还需要创建Book.aidl文件，注意要和aidl文件放到一起


Book.aidl

```java
// Book.aidl
package com.example.aidl;

// Declare any non-default types here with import statements

parcelable Book;
```

创建完毕aidl文件，记得执行build 编译一下.会自动构建相对应的接口文件。ServerInterface.aidl` 生成的文件名是 `ServerInterface.java

## b. 创建一个ServerService

ServerService.java

```java
 public class ServerService extends Service {
    private static final String TAG = "AidlService";

    private AppletBinder appletBinder = null;

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = this.getApplicationContext();

        appletBinder = new AppletBinder(BindManager.getInstance((Application) context));
        Log.d(TAG, "------------ ----------------onCreate: ");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "------------------------------onBind: ");
        return appletBinder;
    }
```

```java
package com.example.aidl;

import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

/**
 * @description
 * @mail chentaishan@aliyun.com
 * @date 2022/3/25
 */
public class AppletBinder extends ServerInterface.Stub {
    private static final String TAG = AppletBinder.class.getSimpleName();
    private BindManager mBindManager;
    public AppletBinder(BindManager mBindManager) {
        this.mBindManager = mBindManager;
    }

    @Override
    public boolean sendMsgToServer(String packageName,String json) throws RemoteException {
        Log.d(TAG, "-------------sendMsgToServer: "+json);
        if (!TextUtils.isEmpty(json)) {
            mBindManager.receiverClientMsg(json);
            return true;
        }
        return false;
    }

    @Override
    public void registerCallbackToServer(String packageName, ClientCallback clientCallback) throws RemoteException {
        Log.d(TAG, "----------------registerCallbackToServer: ");
        if (!TextUtils.isEmpty(packageName) && clientCallback != null) {
            mBindManager.registerClientCallback(packageName, clientCallback);
        }
    }
}

```

然后注册清单文件

```xml
        <service
            android:name="com.example.aidl.AidlService"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="qqq.aaa.zzz"></action>
            </intent-filter>
        </service>
```
为了有一个统一对接外部的接口，需要创建一个Manager。

BindManager.java
```java

public class BindManager {
    private static final String TAG = "BindManager";

    public static final Object oo = new Object();
    private static BindManager mBindManager = null;

    public static final String ACTION = "qqq.aaa.zzz";
    public static final String PACKAGE = "com.example.aidlserver";
    private static HashMap<String, ClientCallback> callbackHashMap = new HashMap<>();
    private static Application context;

    private BindManager(Application context) {
        this.context = context;
    }

    public static BindManager getInstance(Application context) {
        if (mBindManager == null) {
            synchronized (oo) {
                if (mBindManager == null)
                    mBindManager = new BindManager(context);
            }
        }

        return mBindManager;
    }

    private static boolean connected;

    public static void clearCallbacks() {
        Log.d(TAG, "------------------------------unRegister: ");
        callbackHashMap.clear();
    }

    private static IBinder.DeathRecipient deadthRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            clearCallbacks();
            Log.d(TAG, "------------------------------------------------------run: binderDied");
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // TODO 启动服务
                    startServer();
                }
            }, 3000);

            while (connected) {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
            }
        }
    };
    private static ServerInterface serverStub;
    private static ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Log.d(TAG, "---------------------------onServiceConnected: ");
            serverStub = ServerInterface.Stub.asInterface(service);
            try {
                serverStub.asBinder().linkToDeath(deadthRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            connected = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "---------------------------onServiceDisconnected: ");
            connected = false;
            clearCallbacks();
            startServer();
        }
    };

    public static void registerClientCallback(String packageName, ClientCallback clientCallback) {
        Log.d(TAG, "------------------------------registerClientCallback: ");
        callbackHashMap.put(packageName, clientCallback);
    }

    /**
     * 服务端接受客户端的信息
     *
     * @param json
     */
    public static void receiverClientMsg(String json) {

        // TODO 接受到客户端发来的信息，未来要通知小程序框架，执行某操作
        Log.d(TAG, "receiverClientMsg: " + json);


    }

    /**
     * 服务端发送信息给客户端
     *
     * @param json
     */
    public static void sendMsgToClient(String json) {

        Log.d(TAG, "------------------sendMsgToClient: " + json);
        ClientCallback callback;
        try {
            Set<Map.Entry<String, ClientCallback>> entries = callbackHashMap.entrySet();
            Iterator<Map.Entry<String, ClientCallback>> iterator = entries.iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, ClientCallback> next = iterator.next();
                callback = next.getValue();
                if (callback == null) {
                    continue;
                }
                callback.sendMsgToClient(json);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private static void startServer() {
        Log.d(TAG, "-----------------------------------------------------startServer: ");
        Intent intent = new Intent();
        intent.setPackage(PACKAGE);
        intent.setAction(ACTION);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
}

```




# 3.客户端

## 1.创建客户端的module，并且创建aidl文件，注意此处aidl的`包名和文件名要和服务端`保持一致。

```java
package com.example.aidl;

import com.example.aidl.ClientCallback;

interface ServerInterface {

    /** client调用server ,传递数据json**/
    boolean sendMsgToServer(String packageName,String json);
    /** 注册一个callback ,用于回调给client数据**/
    void registerCallbackToServer(String packageName,in ClientCallback clientCallback);

}



// ServerInterface.aidl
package com.example.aidl;

interface ClientCallback {
    /** 发送json格式数据给client**/
     boolean sendMsgToClient(String json);
}
```

## 2.开启绑定服务

创建一个界面，首先绑定service ，然后创建serviceConnection负责连接服务对象，同时创建一个callback对象和一个DeathRecipient对象，还有对接服务端的接口对象。

ClientCallback：负责数据的回调，接收服务端的数据
DeathRecipient：当服务端被杀死的，该方法会触发，然后再次启动服务。其实就是避免服务端停止。

ServerInterface：接口对象，里面包含接口的api,负责客户端和服务端交互

```java
 
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private ServerInterface serverInterface;
    private boolean connected;
    private TextView result;

    ClientCallback clientCallback = new ClientCallback.Stub() {

        @Override
        public boolean sendMsgToClient(String json) throws RemoteException {
            Log.d(TAG, "---------------------sendMsgToClient: "+json);
            return true;
        }
    };
    IBinder.DeathRecipient deadthRecipient = new IBinder.DeathRecipient() {

        @Override
        public void binderDied() {
            Log.d(TAG, "run----------------------------------------:binderDied ");
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    bindService();
                }
            }, 3000);

            while (connected) {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
            }
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                Log.d(TAG, "------------------------------onServiceConnected: ");
                serverInterface = ServerInterface.Stub.asInterface(service);

                serverInterface.registerCallbackToServer("com.example.client",clientCallback);
                service.linkToDeath(deadthRecipient, 0);

            } catch (RemoteException e) {
                e.printStackTrace();
            }
            connected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connected = false;
            Log.d(TAG, "------------------------------onServiceDisconnected: ");
            bindService();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.click).setOnClickListener(this);
        result = findViewById(R.id.result);

        bindService();


    }

    private void bindService() {
        Log.d(TAG, "------------------------------bindService: ");
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
                    Log.d(TAG, "------------------------------onClick: ");
                    serverInterface.sendMsgToServer("com.package.test","this is client msg,server please receiver");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;

        }
    }
}
```

# 常见问题
客户端和服务端的包名要保持一致。不然找不到对应的资源。

```
java.lang.SecurityException: Binder invocation to an incorrect interface
```