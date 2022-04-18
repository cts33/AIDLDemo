
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

    void unRegisterCallbackToServer(in ClientCallback clientCallback);

}
```
ClientCallback.aidl
```java
package com.example.aidl;

interface ClientCallback {
    /** 来自server的调用**/
     boolean onServerAction(String json);
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
    private BindManager mBindManager;
    private ServerInterface.Stub serverInterface = new ServerInterface.Stub() {
        @Override
        public boolean sendMsgToServer(String packageName, String json) throws RemoteException {
            Log.d(TAG, "-------------sendMsgToServer: " + json);
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

        @Override
        public void unRegisterCallbackToServer(ClientCallback clientCallback) throws RemoteException {
            Log.d(TAG, "----------------registerCallbackToServer: ");
            if (clientCallback != null) {
                mBindManager.unRegisterClientCallback(clientCallback);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mBindManager = BindManager.getInstance(this);
        Log.d(TAG, "------------ ----------------onCreate: ");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "------------------------------onBind: ");
        return serverInterface;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBindManager.killRemoteBackList();
    }

```

```java

public class BindManager {
    private static final String TAG = "BindManager";

    private static BindManager mBindManager = null;

    private static Context context;
    private static RemoteCallbackList<ClientCallback> remoteCallbackList = new RemoteCallbackList<>();

    private BindManager(Context context) {
        this.context = context;
    }

    public static BindManager getInstance(Context context) {
        if (mBindManager == null) {
            synchronized (BindManager.class) {
                if (mBindManager == null)
                    mBindManager = new BindManager(context);
            }
        }
        return mBindManager;
    }

    public static void killRemoteBackList() {
        Log.d(TAG, "------------------------------clearCallbacks: ");
        remoteCallbackList.kill();
    }

    public static void registerClientCallback(String packageName, ClientCallback clientCallback) {
        Log.d(TAG, "------------------------------registerClientCallback: " + packageName);
        remoteCallbackList.register(clientCallback, packageName);
    }

    public static void unRegisterClientCallback(ClientCallback clientCallback) {
        Log.d(TAG, "------------------------------unregisterClientCallback: ");
        remoteCallbackList.unregister(clientCallback);
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
    public static void sendMsgToClient(String packageName, String json) {
        Log.d(TAG, "------------------sendMsgToClient: " + json);
        try {
            int i = remoteCallbackList.beginBroadcast();
            while (i > 0) {
                i--;
                String cookie = (String) remoteCallbackList.getBroadcastCookie(i);
                if (packageName == cookie) {
                    ClientCallback callback = remoteCallbackList.getBroadcastItem(i);
                    callback.onServerAction(json);
                }
            }
            remoteCallbackList.finishBroadcast();
        } catch (RemoteException e) {
            e.printStackTrace();
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



# 3.客户端

## 1.创建客户端的module，并且创建aidl文件，注意此处aidl的`包名和文件名要和服务端`保持一致。



## 2.开启绑定服务

创建一个界面，首先绑定service ，然后创建serviceConnection负责连接服务对象，同时创建一个callback对象和一个DeathRecipient对象，还有对接服务端的接口对象。

ClientCallback：负责数据的回调，接收服务端的数据
DeathRecipient：当客户端被杀死的，该方法会触发，然后再次启动服务。其实就是避免服务端停止。

ServerInterface：接口对象，里面包含接口的api,负责客户端和服务端交互

```java
 
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private ServerInterface serverInterface;
    private boolean connected;
    private TextView result;

    private ClientCallback clientCallback = new ClientCallback.Stub() {
        @Override
        public boolean onServerAction(String json) throws RemoteException {
            Log.d(TAG, "---------------------sendMsgToClient: " + json);
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
                serverInterface.registerCallbackToServer(getPackageName(), clientCallback);
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
//        intent.setComponent(new ComponentName("com.example.aidl","com.example.aidl.ServerService"));
        intent.setAction("qqq.aaa.zzz");
        intent.putExtra("packageName", getPackageName());
        boolean status = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        Log.d(TAG, "------------------------------bindService: " + status);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.click:

                Log.d(TAG, "------------------------------onClick: ");
                try {
                    serverInterface.sendMsgToServer(getPackageName(), "this is client msg,server please receiver");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                break;

        }
    }

```

# 4 RemoteCallbackList

```java
Takes care of the grunt work of maintaining a list of remote interfaces, typically for the use of performing callbacks from a android.app.Service to its clients. In particular, this:
Keeps track of a set of registered IInterface callbacks, taking care to identify them through their underlying unique IBinder (by calling IInterface.asBinder().
Attaches a IBinder.DeathRecipient to each registered interface, so that it can be cleaned out of the list if its process goes away.
Performs locking of the underlying list of interfaces to deal with multithreaded incoming calls, and a thread-safe way to iterate over a snapshot of the list without holding its lock.
To use this class, simply create a single instance along with your service, and call its register and unregister methods as client register and unregister with your service. To call back on to the registered clients, use beginBroadcast, getBroadcastItem, and finishBroadcast.
If a registered callback's process goes away, this class will take care of automatically removing it from the list. If you want to do additional work in this situation, you can create a subclass that implements the onCallbackDied method.
```

翻译：

负责维护远程接口列表的繁重工作，通常用于执行从 android.app.Service 到其客户端的回调。特别是：
跟踪一组已注册的 IInterface 回调，注意通过其底层唯一的 IBinder 识别它们（通过调用 IInterface.asBinder()。
将 IBinder.DeathRecipient 附加到每个已注册的接口，以便在其进程消失时将其从列表中清除。
执行底层接口列表的锁定以处理多线程传入调用，以及一种线程安全的方式来迭代列表的快照而不持有其锁定。
要使用此类，只需与您的服务一起创建一个实例，并在客户端注册和注销您的服务时调用其注册和注销方法。要回调已注册的客户端，请使用 beginBroadcast、getBroadcastItem 和 finishBroadcast。
如果已注册的回调进程消失，该类将自动将其从列表中删除。如果你想在这种情况下做额外的工作，你可以创建一个实现 onCallbackDied 方法的子类。



register 和unregister

```java

    /**
     * Add a new callback to the list.  This callback will remain in the list
     * until a corresponding call to {@link #unregister} or its hosting process
     * goes away.  If the callback was already registered (determined by
     * checking to see if the {@link IInterface#asBinder callback.asBinder()}
     * object is already in the list), then it will be left as-is.
     * Registrations are not counted; a single call to {@link #unregister}
     * will remove a callback after any number calls to register it.
     *
     * @param callback The callback interface to be added to the list.  Must
     * not be null -- passing null here will cause a NullPointerException.
     * Most services will want to check for null before calling this with
     * an object given from a client, so that clients can't crash the
     * service with bad data.
     *
     * @param cookie Optional additional data to be associated with this
     * callback.
     * 
     * @return Returns true if the callback was successfully added to the list.
     * Returns false if it was not added, either because {@link #kill} had
     * previously been called or the callback's process has gone away.
     *
     * @see #unregister
     * @see #kill
     * @see #onCallbackDied
     */
    public boolean register(E callback, Object cookie) {
        synchronized (mCallbacks) {
            if (mKilled) {
                return false;
            }
            // Flag unusual case that could be caused by a leak. b/36778087
            logExcessiveCallbacks();
            IBinder binder = callback.asBinder();
            try {
                Callback cb = new Callback(callback, cookie);
                unregister(callback);
                binder.linkToDeath(cb, 0);
                mCallbacks.put(binder, cb);
                return true;
            } catch (RemoteException e) {
                return false;
            }
        }
    }

    /**
     * Remove from the list a callback that was previously added with
     * {@link #register}.  This uses the
     * {@link IInterface#asBinder callback.asBinder()} object to correctly
     * find the previous registration.
     * Registrations are not counted; a single unregister call will remove
     * a callback after any number calls to {@link #register} for it.
     *
     * @param callback The callback to be removed from the list.  Passing
     * null here will cause a NullPointerException, so you will generally want
     * to check for null before calling.
     *
     * @return Returns true if the callback was found and unregistered.  Returns
     * false if the given callback was not found on the list.
     *
     * @see #register
     */
    public boolean unregister(E callback) {
        synchronized (mCallbacks) {
            Callback cb = mCallbacks.remove(callback.asBinder());
            if (cb != null) {
                cb.mCallback.asBinder().unlinkToDeath(cb, 0);
                return true;
            }
            return false;
        }
    }
```



服务端调用callback

```java
       int i = callbacks.beginBroadcast();
       while (i > 0) {
           i--;
           try {
               callbacks.getBroadcastItem(i).somethingHappened();
           } catch (RemoteException e) {
               // The RemoteCallbackList will take care of removing
               // the dead object for us.
           }
       }
       callbacks.finishBroadcast();
```



# 常见问题

客户端和服务端的包名要保持一致。不然找不到对应的资源。

```
java.lang.SecurityException: Binder invocation to an incorrect interface
```

