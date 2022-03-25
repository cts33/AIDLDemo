总结



# 1.特点

AIDL定义客户端与服务均认可的编程接口，以便二者使用进程间通信 (IPC) 进行相互通信。在 Android 中，一个进程通常无法访问另一个进程的内存。因此，为进行通信，进程需将其对象分解成可供操作系统理解的原语，并将其编组为可供您操作的对象。编写执行该编组操作的代码较为繁琐，因此 Android 会使用 AIDL 为您处理此问题。

> **注意：**只有在不同应用的客户端通过 IPC 方式访问服务，且在服务中进行多线程处理时，才有必要使用 AIDL。
>
> 如果您无需跨不同应用执行并发 IPC，则应通过[实现 Binder](https://developer.android.com/guide/components/bound-services#Binder) 来创建接口
>
> 如果您想执行 IPC，但*不*需要处理多线程，请[使用 Messenger ](https://developer.android.com/guide/components/bound-services#Messenger)来实现接口。无论如何，在实现 AIDL 之前，请您务必理解[绑定服务](https://developer.android.com/guide/components/bound-services)。

在开始设计 AIDL 接口之前，请注意，AIDL 接口的调用是直接函数调用。您无需对发生调用的线程做任何假设。实际情况的差异取决于调用是来自本地进程中的线程，还是远程进程中的线程。具体而言：

- 来自本地进程的调用和发起调用在同一线程内。如果该线程是您的主界面线程，则其将继续在 AIDL 接口中执行。如果该线程是其他线程，则其便是在服务中执行代码的线程。因此，只有在本地线程访问服务时，您才能完全控制哪些线程在服务中执行（但若出现此情况，您根本无需使用 AIDL，而应通过[实现 Binder 类](https://developer.android.com/guide/components/bound-services#Binder)来创建接口）。
- 远程进程的调用分配来自线程池，且平台会在您自己的进程内部维护该线程池。您必须为来自未知线程，且多次调用同时发生的传入调用做好准备。换言之，AIDL 接口的实现必须基于完全的线程安全。如果调用来自同一远程对象上的某个线程，则该调用将**依次**抵达接收器端。
- `oneway` 关键字用于修改远程调用的行为。使用此关键字后，远程调用不会屏蔽，而只是发送事务数据并立即返回。最终接收该数据时，接口的实现会将其视为来自 `Binder` 线程池的常规调用（普通的远程调用）。如果 `oneway` 用于本地调用，则不会有任何影响，且调用仍为同步调用。

 

# 2.服务端

a. 创建 module_server,然后右键添加aidl文件，注意包名

```java
// IMyAidlInterface.aidl
package com.example.aidl;

import com.example.aidl.Book;

interface IMyAidlInterface {

    String getString();

    List<Book> getBookList();
    
    void addBookInOut(in Book book);
}
```

内部可以传递对象Book,但是需要序列化。注意导包Book

Book.java

```java
public class Book implements Parcelable {
    String name;
...
```

同时还需要创建Book.aidl文件，注意aidl文件放到一起

> 如果不传递自定义类型，就可以不用创建对象.aidl文件

Book.aidl

```java
// Book.aidl
package com.example.aidl;

// Declare any non-default types here with import statements

parcelable Book;
```

创建完毕aidl文件，记得执行build 编译一下.会自动构建相对应的接口文件。IRemoteService.aidl` 生成的文件名是 `IRemoteService.java

b. 创建一个AidlService

AidlService.java

```java

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
```

然后注册清单文件

```xml
        <service
            android:name="com.example.aidl.ServerService"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="qqq.aaa.zzz"></action>
            </intent-filter>
        </service>
```

## 1.AIDL可以传递哪些数据

- Java 编程语言中的所有原语类型（如 `int`、`long`、`char`、`boolean` 等）

- `String`  CharSequence`

- ```
  List
  ```

  `List` 中的所有元素必须是以上列表中支持的数据类型，或者您所声明的由 AIDL 生成的其他接口或 Parcelable 类型。您可选择将 `List` 用作“泛型”类（例如，`List<String>`）。尽管生成的方法旨在使用 `List` 接口，但另一方实际接收的具体类始终是 `ArrayList`。

- ```
  Map
  ```

  `Map` 中的所有元素必须是以上列表中支持的数据类型，或者您所声明的由 AIDL 生成的其他接口或 Parcelable 类型。不支持泛型 Map（如 `Map<String,Integer>` 形式的 Map）。尽管生成的方法旨在使用 `Map` 接口，但另一方实际接收的具体类始终是 `HashMap`。

如果你要使用上方未列出的附加类型，如Book对象，要添加一条 `import` 语句。也是为什么创建Book.aidl文件的原因

请注意：

- 方法可带零个或多个参数，返回值或空值。

- 所有非原语参数均需要指示数据走向的方向标记（in out inout）。这类标记可以是

  原语类型默认为 `in`，不能是其他方向。

  

  **注意：**您应将方向限定为真正需要的方向，因为编组参数的开销较大。

- 生成的 `IBinder` 接口内包含 `.aidl` 文件中的所有代码注释（import 和 package 语句之前的注释除外）。

- 您可以在 ADL 接口中定义 String 常量和 int 字符串常量。例如：`const int VERSION = 1;`。

- 方法调用由 [transact() 代码](https://developer.android.com/reference/android/os/IBinder#transact(int, android.os.Parcel, android.os.Parcel, int))分派，该代码通常基于接口中的方法索引。由于这会增加版本控制的难度，因此您可以向方法手动配置事务代码：`void method() = 10;`。

- 使用 `@nullable` 注释可空参数或返回类型。



# 3.客户端

客户端和服务端的包名要保持一致。不然找不到对应的资源。

eg:

```
java.lang.SecurityException: Binder invocation to an incorrect interface
```



1.创建module，创建aidl文件，注意此处aidl的包名和文件名要和服务端保持一致。

```

```

2.同时也创建Book.java对象



3.创建acitivity,绑定service

实例化ServiceConnection对象，重写onServiceConnected ，通过 iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);获取接口的实例对象。这样就可以调用接口的方法了。

这三个方法就是接口内部定义的功能。

```
  iMyAidlInterface.addBookInOut(book);

  str = iMyAidlInterface.getString();

  bookList = iMyAidlInterface.getBookList();
```



```java

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    IMyAidlInterface iMyAidlInterface;
    private boolean connected;


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
            connected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connected = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.click).setOnClickListener(this);
        findViewById(R.id.click1).setOnClickListener(this);
        findViewById(R.id.click2).setOnClickListener(this);

        bindService();


    }
	/** 绑定服务，通过action ,同时构建一个serviceConnection**/
    private void bindService() {
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
                    Book book = new Book("C++");
                    book.setName("java=" + v.getId());
                    iMyAidlInterface.addBookInOut(book);
                    Log.d(TAG, "onClick: addBookInOut");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.click1:
                String str = null;
                try {
                    str = iMyAidlInterface.getString();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "onClick: getString=" + str);
                break;
            case R.id.click2:
                List<Book> bookList = null;
                try {
                    bookList = iMyAidlInterface.getBookList();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                for (Book book : bookList) {
                    Log.d(TAG, "   book=" + book.getName());

                }
                Log.d(TAG, "onClick: getBookList");
                break;
        }
    }
}
```

