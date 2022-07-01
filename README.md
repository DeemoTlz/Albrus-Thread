# Albrus-Tlz
(:3[▓▓▓▓▓▓▓▓]
> Systematically study the idea of multi-threaded concurrency and practice and record to deepen understanding.
>
> 参考书籍；《图解 Java 多线程设计模式》《Java 安全编码标准》

## 一、JUC概述和进程线程概念

### 1.1 JUC

`java.util.concurrent`

### 1.2 进程与线程概念

#### 1.2.1 进程与线程

进程：指在系统中正在运行的一个应用程序；进程 - 资源分配的最小单位。

线程：系统分配处理器时间资源的基本单元；线程 - 进程运行的最小单位。

#### 1.2.2 线程的状态

```java
public enum State {
    /**
     * Thread state for a thread which has not yet started.
     */
    NEW,

    /**
     * Thread state for a runnable thread.  A thread in the runnable
     * state is executing in the Java virtual machine but it may
     * be waiting for other resources from the operating system
     * such as processor.
     */
    RUNNABLE,

    /**
     * Thread state for a thread blocked waiting for a monitor lock.
     * A thread in the blocked state is waiting for a monitor lock
     * to enter a synchronized block/method or
     * reenter a synchronized block/method after calling
     * {@link Object#wait() Object.wait}.
     */
    BLOCKED,

    /**
     * Thread state for a waiting thread.
     * A thread is in the waiting state due to calling one of the
     * following methods:
     * <ul>
     *   <li>{@link Object#wait() Object.wait} with no timeout</li>
     *   <li>{@link #join() Thread.join} with no timeout</li>
     *   <li>{@link LockSupport#park() LockSupport.park}</li>
     * </ul>
     *
     * <p>A thread in the waiting state is waiting for another thread to
     * perform a particular action.
     *
     * For example, a thread that has called <tt>Object.wait()</tt>
     * on an object is waiting for another thread to call
     * <tt>Object.notify()</tt> or <tt>Object.notifyAll()</tt> on
     * that object. A thread that has called <tt>Thread.join()</tt>
     * is waiting for a specified thread to terminate.
     */
    WAITING,

    /**
     * Thread state for a waiting thread with a specified waiting time.
     * A thread is in the timed waiting state due to calling one of
     * the following methods with a specified positive waiting time:
     * <ul>
     *   <li>{@link #sleep Thread.sleep}</li>
     *   <li>{@link Object#wait(long) Object.wait} with timeout</li>
     *   <li>{@link #join(long) Thread.join} with timeout</li>
     *   <li>{@link LockSupport#parkNanos LockSupport.parkNanos}</li>
     *   <li>{@link LockSupport#parkUntil LockSupport.parkUntil}</li>
     * </ul>
     */
    TIMED_WAITING,

    /**
     * Thread state for a terminated thread.
     * The thread has completed execution.
     */
    TERMINATED;
}
```

#### 1.2.3 `wait & sleep`

`wait`：`Thread.sleep()`，会释放锁，前提是在同步代码块中执行

`sleep`：`Object.wait()`，不会释放锁也不需要占有锁

在哪儿睡在哪儿醒，都能被 `interrupted()` 中断。

#### 1.2.4 并发和并行

并发 - 同一时刻多个线程在访问同一资源

并行 - 多项工作一起执行，之后再汇总

#### 1.2.5 管程

管程 - Monitor 监视器

是一种同步机制，保证同一时刻只有一个线程对受保护的数据进行访问。

==**JVM中的同步，基于进入和退出过程，使用管程对象来实现（加锁、释放锁）**==，也就是说，每一个对象都有一个管程对象（随着Java对象创建而创建）

**执行同步操作时，需要先获取管程对象，当持有管程对象后，才能进入被加锁的操作。当一个线程持有管程对象后，其他线程不能再获取同一个管程对象。当操作执行完成后，再释放管程对象其他线程才能尝试访问（获取锁）。**

#### 1.2.6 用户线程 & 守护线程

用户线程 - `new Thread` 自定义线程

守护线程 - 例如垃圾回收

- `Thread.isDaemon()` 判断是否是守护线程。

- 主线程结束，用户线程还存活 - JVM存活

  ```java
  public static void main(String[] args) {
      Thread t = new Thread(() -> {
          // false
          Thread.currentThread().isDaemon();
          while (true) {}
      }, "deemo-thread-01");
      
      t.start();
  }
  // 此时运行 `main()` 方法，JVM将一直等待用户线程结束
  ```

- 没有用户线程之后，JVM结束

  ```java
  public static void main(String[] args) {
      Thread t = new Thread(() -> {
          // true
          Thread.currentThread().isDaemon();
          while (true) {}
      }, "deemo-thread-01");
      
      t.setDaemon(true);
      t.start();
  }
  // 此时运行 `main()` 方法，JVM将结束
  ```

## 二、Lock接口

```java
interface Lock
    ReentrantLock implements Lock
    ReentrantReadWriteLock.ReadLock implements Lock
    ReentrantReadWriteLock.WriteLock implements Lock
```

`Lock` 与 `synchronized`：

- `synchronized` 是Java中的关键字
- `synchronized` 隐式锁，不需要手动释放锁
- `Lock` 使用更灵活（`wait & notifyAll`），但需要手动释放锁

## 三、集合的线程安全

> `java.util.ConcurrentModificationException`

### 3.1 `List`

`Vector`

`ArrayList`

- 构造方法

  ```java
  /**
   * Shared empty array instance used for default sized empty instances. We
   * distinguish this from EMPTY_ELEMENTDATA to know how much to inflate when
   * first element is added.
   */
  private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
  
  /**
   * The array buffer into which the elements of the ArrayList are stored.
   * The capacity of the ArrayList is the length of this array buffer. Any
   * empty ArrayList with elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA
   * will be expanded to DEFAULT_CAPACITY when the first element is added.
   */
  transient Object[] elementData; // non-private to simplify nested class access
  
  /**
   * The size of the ArrayList (the number of elements it contains).
   *
   * @serial
   */
  private int size;
  
  /**
   * Constructs an empty list with the specified initial capacity.
   *
   * @param  initialCapacity  the initial capacity of the list
   * @throws IllegalArgumentException if the specified initial capacity
   *         is negative
   */
  public ArrayList(int initialCapacity) {
      if (initialCapacity > 0) {
          this.elementData = new Object[initialCapacity];
      } else if (initialCapacity == 0) {
          this.elementData = EMPTY_ELEMENTDATA;
      } else {
          throw new IllegalArgumentException("Illegal Capacity: "+
                                             initialCapacity);
      }
  }
  
  /**
   * Constructs an empty list with an initial capacity of ten.
   */
  public ArrayList() {
      this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
  }
  
  /**
   * Constructs a list containing the elements of the specified
   * collection, in the order they are returned by the collection's
   * iterator.
   *
   * @param c the collection whose elements are to be placed into this list
   * @throws NullPointerException if the specified collection is null
   */
  public ArrayList(Collection<? extends E> c) {
      elementData = c.toArray();
      if ((size = elementData.length) != 0) {
          // c.toArray might (incorrectly) not return Object[] (see 6260652)
          if (elementData.getClass() != Object[].class)
              elementData = Arrays.copyOf(elementData, size, Object[].class);
      } else {
          // replace with empty array.
          this.elementData = EMPTY_ELEMENTDATA;
      }
  }
  ```

  - 无参构造方法：默认是空对象，在添加元素时扩容：不见鬼子不挂弦

  - `int initialCapacity` 构造方法：

    - 初始容量 >0：直接创建指定容量的数组
    - 初始容量 =0：同无参构造方法
    - 初始容量 <0：抛异常

  - `Collection<? extends E> c`：拷贝创建

    - 在 `ArrayList` 中 `toArray()`：

      ```java
      public Object[] toArray() {
          return Arrays.copyOf(elementData, size);
      }
      ```

`Collections.synchronizedList()`

`CopyOnWriteArrayList`

- 构造方法

  ```java
  public class CopyOnWriteArrayList<E>
      implements List<E>, RandomAccess, Cloneable, java.io.Serializable {
      private static final long serialVersionUID = 8673264195747942595L;
  
      /** The lock protecting all mutators */
      final transient ReentrantLock lock = new ReentrantLock();
  
      /** The array, accessed only via getArray/setArray. */
      private transient volatile Object[] array;
  
      /**
       * Gets the array.  Non-private so as to also be accessible
       * from CopyOnWriteArraySet class.
       */
      final Object[] getArray() {
          return array;
      }
  
      final void setArray(Object[] a) {
          array = a;
      }
  
      /**
       * Creates an empty list.
       */
      public CopyOnWriteArrayList() {
          setArray(new Object[0]);
      }
  
      /**
       * Creates a list containing the elements of the specified
       * collection, in the order they are returned by the collection's
       * iterator.
       *
       * @param c the collection of initially held elements
       * @throws NullPointerException if the specified collection is null
       */
      public CopyOnWriteArrayList(Collection<? extends E> c) {
          Object[] elements;
          if (c.getClass() == CopyOnWriteArrayList.class)
              elements = ((CopyOnWriteArrayList<?>)c).getArray();
          else {
              elements = c.toArray();
              // c.toArray might (incorrectly) not return Object[] (see 6260652)
              if (elements.getClass() != Object[].class)
                  elements = Arrays.copyOf(elements, elements.length, Object[].class);
          }
          setArray(elements);
      }
      
      // ...
  }
  ```

  - 底层是一个 `volatile` 数组 `private transient volatile Object[] array;`
  - 初始时赋值创建一个长度为 0 的数组

- 在 `add` 等数据修改操作才会加锁 `ReentrantLock.lock()`

  ```java
  /**
       * Appends the specified element to the end of this list.
       *
       * @param e element to be appended to this list
       * @return {@code true} (as specified by {@link Collection#add})
       */
  public boolean add(E e) {
      final ReentrantLock lock = this.lock;
      lock.lock();
      try {
          Object[] elements = getArray();
          int len = elements.length;
          Object[] newElements = Arrays.copyOf(elements, len + 1);
          newElements[len] = e;
          setArray(newElements);
          return true;
      } finally {
          lock.unlock();
      }
  }
  ```

  - 先**拷贝**一份原数组，添加元素后再**替换**
  - 每次添加，将长度 +1

- 遍历时，将现有的数组**赋值创建一个新的迭代器**

  ```java
  /**
   * Returns an iterator over the elements in this list in proper sequence.
   *
   * <p>The returned iterator provides a snapshot of the state of the list
   * when the iterator was constructed. No synchronization is needed while
   * traversing the iterator. The iterator does <em>NOT</em> support the
   * {@code remove} method.
   *
   * @return an iterator over the elements in this list in proper sequence
   */
  public Iterator<E> iterator() {
      return new COWIterator<E>(getArray(), 0);
  }
  ```

### 3.2 `Set`

`HashSet`

- 底层是一个加载因子 0.75 的 `HashMap`

- 添加元素：

  ```java
  // Dummy value to associate with an Object in the backing Map
  private static final Object PRESENT = new Object();
  
  /**
   * Adds the specified element to this set if it is not already present.
   * More formally, adds the specified element <tt>e</tt> to this set if
   * this set contains no element <tt>e2</tt> such that
   * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>.
   * If this set already contains the element, the call leaves the set
   * unchanged and returns <tt>false</tt>.
   *
   * @param e element to be added to this set
   * @return <tt>true</tt> if this set did not already contain the specified
   * element
   */
  public boolean add(E e) {
      return map.put(e, PRESENT)==null;
  }
  ```

  - V 是一个 `static final Object` 对象

`Collections.synchronizedSet()`

`CopyOnWriteArraySet`

### 3.3 `Map`

`HashMap`

`Collections.synchronizedMap()`

**`ConcurrentHashMap`**

- 分段锁
- TODO

## 四、多线程锁

### 4.1 synchronized

修饰不同的地方的不同效果

非同一个对象时的效果（非同一把锁）

### 4.2 公平锁、非公平锁

> 公平锁按照申请锁的顺序来获取锁。
>
> 非公平锁，吞吐量更大，`synchroinzed` 是非公平锁。

```java
/**
 * Creates an instance of {@code ReentrantLock}.
 * This is equivalent to using {@code ReentrantLock(false)}.
 */
public ReentrantLock() {
    sync = new NonfairSync();
}

/**
 * Creates an instance of {@code ReentrantLock} with the
 * given fairness policy.
 *
 * @param fair {@code true} if this lock should use a fair ordering policy
 */
public ReentrantLock(boolean fair) {
    sync = fair ? new FairSync() : new NonfairSync();
}
```

- 公平锁减少**“饥饿”**现象

  里面维护了一个队列，用于判断 `head` 是否是当前线程

  ```java
  public final boolean hasQueuedPredecessors() {
      // The correctness of this depends on head being initialized
      // before tail and on head.next being accurate if the current
      // thread is first in queue.
      Node t = tail; // Read fields in reverse initialization order
      Node h = head;
      Node s;
      return h != t &&
          ((s = h.next) == null || s.thread != Thread.currentThread());
  }
  ```

- 非公平锁减少线程切换，效率更高，会造成**“饥饿”**现象

### 4.3 可重入锁

> 可重入锁、递归锁。
>
> 线程可进入任何一个它**已经拥有的锁所同步着的代码块**。

`synchroinzed & ReentrantLock & ReentrantReadWriteLock` 都是可重入锁。

### 4.4 自旋锁

> 用循环取代阻塞，减少线程上下文切换，缺点是循环耗费 CPU。

```java
int var;
do {
    var = get();
} while (!this.cas(obj, var, newVal));
```

### 4.5 读写锁

`ReentrantReadWriteLock`

- 读 - 读 共享
- 读 - 写 互斥
- 写 - 写 互斥

### 4.6 死锁

死锁：两个或两个以上的线程在执行过程中，同时取争夺对方的资源形成循环造成的一直等待。

- 互斥

- 占有且等待

  在持有共享资源A的同时在等待共享资源B且不释放共享资源A

- 不可抢占

  其他线程不能抢占

- 循环等待

  T1 > T2 > T1

分析：`jps & jstack`

解决：**只要破坏四大条件中的其中一个，便能解决死锁问题：**

- 互斥：不可碰

- 占有且等待

  一次性申请所有的资源（引入一个公共颁发资源的角色）

- 不可抢占

  在占用部分共享资源的线程进一步申请其他共享资源时，如果申请不到，则主动释放持有的共享资源

- 循环等待

  按顺序申请资源，例如按ID排序后申请共享资源

## 五、辅助类

### 5.1 CountDownLatch

计数器，初始化设置一个计数量，每次调用 `countDown` 方法将计数量**减一**，使用 `await` 方法等待，当计数量恢复到0时线程被唤醒。

```java
CountDownLatch countDownLatch = new CountDownLatch(6);

for (int i = 0; i < 6; i++) {
    new Thread(() -> {
        try {
            TimeUnit.SECONDS.sleep(new Random().nextInt(5));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("小小" + Thread.currentThread().getName().split("-")[1] + "离开了教室。。。");
        countDownLatch.countDown();
    }, "thread-0" + i).start();
}

try {
    countDownLatch.await();
} catch (InterruptedException e) {
    System.out.println("女朋友叫班长不等了。。。");
    throw new RuntimeException(e);
}

System.out.println("人都走完了，班长该锁门了。");
```

### 5.2 CyclicBarrier

与 `CountDownLatch` 类似，初始化设置一个目标信号量，每次 `await` 时信号量将**加一**并等待，当达到目标信号量时，所有等待的线程都将被唤醒。

```java
// CyclicBarrier 执行完回调后，会自动重置计数器
CyclicBarrier cyclicBarrier = new CyclicBarrier(7, () -> {
    System.out.println("召~唤$神%龙！！！！！！");
});

for (int i = 0; i < 7; i++) {
    new Thread(() -> {
        // 循环利用
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(new Random().nextInt(5));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            System.out.println(Thread.currentThread().getName().split("-")[1] + " 星龙珠找到了！");
            try {
                cyclicBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                System.out.println("不等待召唤神龙了。。。");
                throw new RuntimeException(e);
            }
            System.out.println(Thread.currentThread().getName().split("-")[1] + " 星龙珠又消失了^~");
        }
    }, "thread-0" + i).start();
}
```

- `CyclicBarrier` 执行完回调后，会自动重置计数器
- 循环利用

### 5.3 小总结

> 阻塞操作，建议设置超时时间。

前两个辅助类怎么理解它的实用性呢？

列举一个场景：**用户下单为订单库，物流派单为派单库，为防止出现漏发错发的问题，需要每天进行订单库和派单库对账。**

1. 方案一：串行处理

   ```java
   while (存在未对账的订单) {
       // 查询未对账订单
       pos = getPOrders();
       // 查询派送单
       dos = getDOrders();
       // 对账
       diff = check(pos, dos);
       // 差异写库
       save(diff);
   }
   ```

   存在的问题：由于订单量和派单量数量都很大，查询效率很低，导致对账很慢。![image-20220430182140674](images/image-20220430182140674.png)

2. 方案二：多线程粗粗步拆解

   对方案一进行分析，瓶颈在于查询未对账订单和查询派送单两个操作，那么能不能把这两个操作改为并行处理行不行呢？答案是可以的，因为这两个操作没有先后依赖关系。将这两个操作拆分为并行后，执行效果如图：![image-20220430182513033](images/image-20220430182513033.png)

   可以发现，相同的等待时间内，并行执行的吞吐量近乎单线程的2倍。

   所优化的点：两个耗时查询并行查询，在忽略线程切换等环节带来的耗时时，并行执行节省了一次订单查询时间（节省相对较短的查询时间），也就是说只需要等待一次最长的查询时间即可。

   思路有了，接下来代码实现。新开两个线程T1 T2分别查询两种订单，线程T3便执行对账和差异写入操作，关键便是T3需要等待T1和T2执行结束才能执行。

   ```java
   while (存在未对账的订单) {
       // 查询未对账订单
       Thread t1 = new Thread(() -> {
           pos = getPOrders();
       });
       t1.start();
       // 查询派送单
       Thread t1 = new Thread(() -> {
           dos = getDOrders();
       });
       t2.start();
       
       // 等待
       t1.join();
       t2.join();
       
       // 对账
       diff = check(pos, dos);
       // 差异写库
       save(diff);
   }
   ```

3. 方案三：`CountDownLatch`

   在方案二已经提升了大部分性能，但美中不足的是每次都要创建新的线程，是一个比较重且浪费线程的操作，那么首先想到的便是线程池！是的，线程池的确可以解决这个问题，但问题是，使用线程池后怎样保证执行顺序问题？线程池没有提供 `join()` 方法，如何检测T1 T2执行结束呢 —— 计数器，每个查询执行完成后将计数器减一，当计数器为0时便是T3执行的时间，但无需重复造轮子了，呐：`CountDownLatch`

   ```java
   // 创建 2 个线程的线程池
   Executor executor = Executors.newFixedThreadPool(2);
   while (存在未对账的订单) {
       // 计数器初始化：2
       CountDownLatch latch = new CountDownLatch(2);
       
       // 查询未对账订单
       executor.execute(() -> {
           pos = getPOrders();
           latch.countDown();
       });
       // 查询派送单
       executor.execute(() -> {
           dos = getDOrders();
           latch.countDown();
       });
       
       // 等待
       latch.asait();
       
       // 对账
       diff = check(pos, dos);
       // 差异写库
       save(diff);
   }
   ```

4. 进一步优化性能——==**完全并行**==：`CyclicBarrier`

   进一步钻进去看，其实，两个查询操作和 `check() & save()` 操作之间也是可以并行的：当在 `check() & save()` 时又已经可以进行下一次查询了：![image-20220430184125124](images/image-20220430184125124.png)

   完全并行，那么需要将T1 T2的查询结果用队列存储起来，T3从队列中获取订单 `check() & save()` 即可，那不就是生产者- 消费者模式了吗。

   队列设计：简单的来说，使用两个队列分别存储，但需要两个队列一一对应：![image-20220430184434378](images/image-20220430184434378.png)

   双队列并行执行：T1执行订单查询，T2执行派单查询，都查询结束后通知T3执行对账检查。这个想法看似简单，但T1和T2的步调应该一致，不能一个太快一个太慢，需要T1和T2都查询一次结束后再通知T3进行对账检查：![image-20220430185006740](images/image-20220430185006740.png)

   有了想法便来实现。上述的方案两个难点：

   1. T1和T2需要步调一致
   2. 通知T3对账检查

   依然可以使用方案三的计数器来解决：循环利用计数器，将计数器后的对账检查提取为异步操作。但是同样的，不需要重复造轮子 —— 循环计数器 `CyclicBarrier`。

   `CyclicBarrier` 工具需要指定一个计数器，并传入一个回调函数，当计数器减到 0 时，将会触发回调函数，粗看与 `CountDownLatch` 并无差异，但重点便是 `CyclicBarrier` 有自动重置计数器的功能：当减到 0 时，待回调函数执行完成后重设初始值。

   `CountDownLatch` 的计数器是不能循环利用的，也就是说一旦计数器减到 0，再有线程调用 `await()`，该线程会直接通过。但 `CyclicBarrier` 的计数器是可以循环利用的，而且具备自动重置的功能，一旦计数器减到 0 会自动重置到你设置的初始值。

   ```java
   // 订单队列
   Vector<P> pos;
   // 派送单队列
   Vector<D> dos;
   // 执行回调的线程池
   Executor executor = Executors.newFixedThreadPool(1);
   final CyclicBarrier barrier = new CyclicBarrier(2, () -> {
       executor.execute(() -> check());
   });
   
   void check() {
       P p = pos.remove(0);
       D d = dos.remove(0);
       // 执行对账操作
       diff = check(p, d);
       // 差异写入差异库
       save(diff);
   }
   
   void checkAll() {
       // 循环查询订单库
       Thread T1 = new Thread(() -> {
           while (存在未对账订单) {
               // 查询订单库
               pos.add(getPOrders());
               // 等待
               barrier.await();
           }
       });
       T1.start();
       
       // 循环查询派单库
       Thread T2 = new Thread(() -> {
           while (存在未对账订单) {
               // 查询派单库
               dos.add(getDOrders());
               // 等待
               barrier.await();
           }
       });
       T2.start();
   }
   ```

   - 为什么需要一个 `Executors.newFixedThreadPool(1);` 线程池？
     - 异步啊！不异步不就同步了吗
     - 一个线程是因为两个队列的获取操作存在竞态条件

   - 循环计数器何时循环？当回调函数执行完成后，`CyclicBarrier.await()` 处都会被唤醒。

   - T3执行的时候是哪个线程执行？==**`CyclicBarrier` 的回调函数执行在一个回合的最后执行 `await()` 的线程上**==。
   
     ==**请遇到回调函数的时候一定要多想一想执行回调函数的是谁！！！**==

### 5.4 Semaphore

初始化设置一个许可集大小，每次 `acquire` 申请一个许可集（可一次性申请多个），如果没有则阻塞等待；每次 `release` 释放一个许可集（可一次性释放多个）。

**通常用于限制可以访问某些资源（物理或逻辑）的线程数目。**

```java
 // 三个许可证
Semaphore semaphore = new Semaphore(3);

for (int i = 0; i < 6; i++) {
    int finalI = i;
    new Thread(() -> {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("第 " + finalI + " 辆车抢到了车位");

        try {
            TimeUnit.SECONDS.sleep(new Random().nextInt(5));
            System.out.println("第 " + finalI + " 辆车离开了车位");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }, "thread-0" + i).start();
}
```

## 六、读写锁

### 6.1 ReentrantReadWriteLock

> - 读共享
> - 写独占
> - 支持**==锁降级（同一个线程获取到写锁时也可以获取读锁）==**
> - **读写锁均可重入**
> - `ReadLock` 不支持 Condition：`java.lang.UnsupportedOperationException`

```java
// 读锁
ReentrantReadWriteLock.readLock().lock();
ReentrantReadWriteLock.readLock().unlock();
// 写锁
ReentrantReadWriteLock.writeLock().lock();
ReentrantReadWriteLock.writeLock().unlock();
```

锁降级：

```java
public void test() {
    ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
    ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();

    writeLock.lock();
    System.out.println("write...");

    // 0
    System.out.println(readWriteLock.getReadLockCount());
    // true
    System.out.println(readWriteLock.isWriteLocked());

    readLock.lock();
    System.out.println("read...");

    // 1
    System.out.println(readWriteLock.getReadLockCount());
    // true
    System.out.println(readWriteLock.isWriteLocked());

    writeLock.unlock();
    readLock.unlock();
}
```

- 可以成功运行结束，说明同一个线程获取到写锁时，也可以获取读锁
- 如果交换读写锁获取顺序，线程将**阻塞**

### 6.2 读写锁演变

> 一个资源可以被多个**读**线程访问或者被一个**写**线程访问，但不能同时存在读写操作，读写互斥、读读共享。

无锁 > 全锁 > 读写锁

线程不安全 > 性能差 > 写独占提高性能

### 6.3 StampedLock

> 写锁、悲观读锁、乐观读（不是乐观读锁，==**乐观读是无锁的**==）
>
> - 不支持直接锁升级、锁降级==**（同一个线程获取到写锁时，不能直接再获取读锁）**==，可通过 `tryConvertToReadLock & tryConvertToWriteLock` 降级或升级锁
> - **==写锁不可重入==**，悲观读锁可重入
> - 不支持 Condition
> - 具体使用时可参考JDK源码中的示例

```java
/**
 * Returns a stamp that can later be validated, or zero
 * if exclusively locked.
 *
 * @return a stamp, or zero if exclusively locked
 */
public long tryOptimisticRead() {
    long s;
    return (((s = state) & WBIT) == 0L) ? (s & SBITS) : 0L;
}

/**
 * Returns true if the lock has not been exclusively acquired
 * since issuance of the given stamp. Always returns false if the
 * stamp is zero. Always returns true if the stamp represents a
 * currently held lock. Invoking this method with a value not
 * obtained from {@link #tryOptimisticRead} or a locking method
 * for this lock has no defined effect or result.
 *
 * @param stamp a stamp
 * @return {@code true} if the lock has not been exclusively acquired
 * since issuance of the given stamp; else false
 */
public boolean validate(long stamp) {
    U.loadFence();
    return (stamp & SBITS) == (state & SBITS);
}

/**
 * If the lock state matches the given stamp, performs one of
 * the following actions. If the stamp represents holding a write
 * lock, returns it.  Or, if a read lock, if the write lock is
 * available, releases the read lock and returns a write stamp.
 * Or, if an optimistic read, returns a write stamp only if
 * immediately available. This method returns zero in all other
 * cases.
 *
 * @param stamp a stamp
 * @return a valid write stamp, or zero on failure
 */
public long tryConvertToWriteLock(long stamp) {
    long a = stamp & ABITS, m, s, next;
    while (((s = state) & SBITS) == (stamp & SBITS)) {
        if ((m = s & ABITS) == 0L) {
            if (a != 0L)
                break;
            if (U.compareAndSwapLong(this, STATE, s, next = s + WBIT))
                return next;
        }
        else if (m == WBIT) {
            if (a != m)
                break;
            return stamp;
        }
        else if (m == RUNIT && a != 0L) {
            if (U.compareAndSwapLong(this, STATE, s,
                                     next = s - RUNIT + WBIT))
                return next;
        }
        else
            break;
    }
    return 0L;
}

/**
 * If the lock state matches the given stamp, performs one of
 * the following actions. If the stamp represents holding a write
 * lock, releases it and obtains a read lock.  Or, if a read lock,
 * returns it. Or, if an optimistic read, acquires a read lock and
 * returns a read stamp only if immediately available. This method
 * returns zero in all other cases.
 *
 * @param stamp a stamp
 * @return a valid read stamp, or zero on failure
 */
public long tryConvertToReadLock(long stamp) {
    long a = stamp & ABITS, m, s, next; WNode h;
    while (((s = state) & SBITS) == (stamp & SBITS)) {
        if ((m = s & ABITS) == 0L) {
            if (a != 0L)
                break;
            else if (m < RFULL) {
                if (U.compareAndSwapLong(this, STATE, s, next = s + RUNIT))
                    return next;
            }
            else if ((next = tryIncReaderOverflow(s)) != 0L)
                return next;
        }
        else if (m == WBIT) {
            if (a != m)
                break;
            state = next = s + (WBIT + RUNIT);
            if ((h = whead) != null && h.status != 0)
                release(h);
            return next;
        }
        else if (a != 0L && a < WBIT)
            return stamp;
        else
            break;
    }
    return 0L;
}
```

一般使用乐观读的时候，先是获取乐观读，再执行业务，再判断乐观读之后的时间里是否有写操作 `validate`，如果有则升级为悲观读锁。**这里是升级为悲观读锁而不是循环执行乐观读，感觉是避免循环读浪费CPU**？但是本来就是写少读多？可能是如果某一时间点写较多，读的请求将一直被循环阻塞。一般建议便是使用该流程模板来使用乐观读。

理解这个乐观读可用对比数据库的乐观锁（版本号：`UPDATE x set version = version + 1 WHERE xxxx AND version = #{version};`）。这里的乐观读也是会生成一个 `stamp`，与数据库中的 `version` 有异曲同工之妙。

另外注意：==**如果线程阻塞在 `StampedLock.readLock() || StampedLock.writeLock()` 时调用该线程的 `interrupt()` 方法，将导致CPU飙升。**==所以如果需要支持中断操作，一定使用可终端的悲观读锁和写锁（`readLockInterruptibly & writeLockInterruptibly`）。

`StampedLock` 性能相比 `ReadWriteLock` 性能更好的原因是：前者支持乐观读。原理便是从实际业务出发认定业务系统中，某一部分数据的操作是读多写少，因此没必要每次读的时候都去消耗时间加读锁和释放读锁。于是在读取数据时，便从读取数据前记录一个 `stamp` （与数据库的 `version` 异曲同工之妙），假设没有人来写数据，在操作完之后便验证一次是否真的没人来写数据，如果真的没有便返回，如果有则升级为悲观读锁处理。**相比之下，性能提升的便是将每次的读锁处理优化为操作数据时是否有人来写过数据，是一个无锁操作。**

## 七、内置工具类

### 7.1 并发容器

![image-20220501173849145](images/image-20220501173849145.png)

#### 7.1.1 List

`CopyOnWriteArrayList`：内部维护一个数组，在写数据时将使用重入锁 `ReentrantLock` 加锁，并且会先将原 array 数组拷贝一份，在新数组上操作写后再将 array 指向这个新数组。

因此可以总结出使用场景：

- ==**应适用于写操作很少的场景**==，因为每次写操作都会拷贝原数组
- **能够容忍短暂的读写不一致**
- **迭代器仅可读，不支持增删改**，因为迭代器提供的仅仅是一个快照，对快照进行增删改是没有意义的

#### 7.1.2 Map

Map 接口的两个实现是 `ConcurrentHashMap` 和 `ConcurrentSkipListMap`，它们从应用的角度来看，**主要区别在于`ConcurrentHashMap` 的 key 是无序的，而 `ConcurrentSkipListMap` 的 key 是有序的**。所以如果你需要保证 key 的顺序，就只能使用 `ConcurrentSkipListMap`。

使用 `ConcurrentHashMap` 和 `ConcurrentSkipListMap` 需要注意的地方是，**它们的 key 和 value 都不能为空**，否则会抛出 `NullPointerException` 这个运行时异常。

![image-20220501174145397](images/image-20220501174145397.png)

#### 7.1.3 Set

`Set` 接口的两个实现是 `CopyOnWriteArraySet` 和 `ConcurrentSkipLiskSet`，使用场景可以参考前文描述。

#### 7.1.4 Queue

可以从两个角度来分类：

- 阻塞与非阻塞，关键字：`Blocking`

  所谓阻塞指的是当队列已满时，入队操作阻塞；当队列已空时，出队操作阻塞

- 单端与双端，关键字：`Queue & Deque`

  单端指的是只能队尾入队，队首出队；而双端指的是队首队尾皆可入队出队

两个维度组合后，可以细分为四大类：

1. 单端阻塞队列

   `ArrayBlockingQueue`、`LinkedBlockingQueue`、`SynchronousQueue`、`LinkedTransferQueue`、`PriorityBlockingQueue` 和 `DelayQueue`

2. 双端阻塞队列

   `LinkedBlockingDeque`

3. 单端非阻塞队列

   `ConcurrentLinkedQueue`

4. 双端非阻塞队列

   `ConcurrentLinkedDeque`

==**使用时，应注意队列是否支持有界（一般不建议使用无界，容易OOM），只有 `ArrayBlockingQueue` 和 `LinkedBlockingQueue` 是支持有界的**==

#### 7.1.5 阻塞队列额外篇

> 在某些情况下挂起线程（阻塞），一旦条件满足时自动唤醒被挂起的线程。不用重复造轮子啦~~

##### 7.1.5.0 认识 BlockingQueue

![image-20220427222259590](images/image-20220427222259590.png)

```java
interface BlockingQueue<E>
    -> extends Queue<E>
        -> extends Collection<E>
            -> extends Iterable<E>
```

`BlockingQueue`：

- 获取元素时等待队列变为非空

- 存储元素时等待空间变得可用

- 对于不能立即满足但可能在将来某一时刻可以满足的操作，以四种形式出现：
  1. 抛出异常
  2. 返回特殊值，`null || false`
  3. 在操作成功前，无限期阻塞当前线程
  4. 在给定的最大时间内阻塞当前线程
  
- **==核心方法==**

  ![image-20220428192830646](images/image-20220428192830646.png)

##### 7.1.5.1 ArrayBlockingQueue

> 由数组结构组成的**==有界==**阻塞队列。

基于数组的阻塞队列实现，在 `ArrayBlockingQueue` 内部，维护了一个定长数组，以便缓存队列中的数据对象，这是一个常用的阻塞队列，除了一个定长数 组外，`ArrayBlockingQueue` 内部还保存着两个整形变量，分别标识着队列的 头部和尾部在数组中的位置。

**==`ArrayBlockingQueue` 在生产者放入数据和消费者获取数据，都是共用同一个 锁对象，由此也意味着两者无法真正并行运行==**，这点尤其不同于 `LinkedBlockingQueue`；按照实现原理来分析，`ArrayBlockingQueue` 完全可以采用分离锁，从而实现生产者和消费者操作的完全并行运行。Doug Lea 之 所以没这样去做，也许是因为 `ArrayBlockingQueue` 的数据写入和获取操作已经足够轻巧，以至于引入独立的锁机制，除了给代码带来额外的复杂性外，其 在性能上完全占不到任何便宜。 

##### 7.1.5.2 LinkedBlockingQueue 

>  由链表结构组成的**==有界（但大小默认值为 integer.MAX_VALUE）==**阻塞队列。

`LinkedBlockingQueue` 之所以能够高效的处理并发数据，还因为**==其对于生产者端和消费者端分别采用了独立的锁来控制数据同步，这也意味着在高并发的情况下生产者和消费者可以并行地操作队列中的数据，以此来提高整个队列 的并发性能==**。

##### 7.1.5.3 DelayQueue

> 使用优先级队列实现的延迟**==无界==**阻塞队列。

`DelayQueue` 中的元素只有当其指定的延迟时间到了，才能够从队列中获取到该元素。`DelayQueue` 是一个没有大小限制的队列，因此往队列中插入数据的操作**==（生产者）永远不会被阻塞，而只有获取数据的操作（消费者）才会被阻塞==**。

##### 7.1.5.4 PriorityBlockingQueue

> 支持优先级排序的**==无界==**阻塞队列。

基于优先级的阻塞队列（优先级的判断通过构造函数传入的 `Compator` 对象来决定），但需要注意的是 `PriorityBlockingQueue` 并**==不会阻塞数据生产者==**，而只会在没有可消费的数据时，阻塞数据的消费者。

在实现 `PriorityBlockingQueue` 时，内部控制线程同步的锁采用的是公平锁。

##### 7.1.5.5 SynchronousQueue

> 不存储元素的阻塞队列，也即单个元素的队列。

##### 7.1.5.6 LinkedTransferQueue

> 由链表组成的**==无界==**阻塞队列。

`LinkedTransferQueue` 是一个由链表结构组成的无界阻塞 `TransferQueue` 队 列。相对于其他阻塞队列，`LinkedTransferQueue` 多了 `tryTransfer` 和 `transfer `方法。

##### 7.1.5.7 LinkedBlockingDeque

>  由链表组成的**==有界（但大小默认值为 integer.MAX_VALUE）==**双向阻塞队列。

`LinkedBlockingDeque` 是一个由链表结构组成的双向阻塞队列，即可以从队列的两端插入和移除元素。

### 7.2 volatile

> `volatile` 是 Java 虚拟机提供的轻量级的同步机制：
>
> 1. 保证可见性
> 2. ==**不保证原子性**==
> 3. 禁止指令重排

#### 7.2.1 JMM Java 内存模型

> Java Memory Model 本身是一种抽象的概念，并不真实存在，描述的是一组规则或规范，定义了程序中各个变量（实例字段、静态字段、构成数组对象的元素）的访问方式。

**JMM 关于同步的规定：**

1. 线程解锁前：必须将共享变量的值刷新回主内存
2. 线程加锁前：必须从主内存读取最新的共享变量到自己的工作内存
3. 加锁、解锁是同一把锁

由于 JVM 允许程序的实体是**线程**，而每个线程创建时 JVM 都会**为其创建一个工作内存（栈空间）**，工作内存是每个线程的私有数据区域，而 **JMM 规定所有变量都==存储==在主内存**（主内存是共享内存区域，所有线程都可以访问），**但线程对变量的==操作（读取、赋值等）==必须在各自的工作内存中进行**：==**线程首先将变量从主内存拷贝到自己的工作空间，然后对变量进行操作，操作完成后再将变量写回主内存。**==不能直接操作主内存的变量，各个线程的工作空间中存储的都是**主内存中变量的副本拷贝**，因此线程间无法访问对方的工作内存，==**线程间的通信必须通过主内存完成**==：

![image-20220626103839246](images/image-20220626103839246.png)

#### 7.2.2 保证可见性

```java
public class DeemoVolatile {

    public static void main(String[] args) {
        Data data = new Data();

        new Thread(() -> {
            System.out.println("Thread: " + Thread.currentThread().getName() + "\t come in...");
            DeemoUtils.sleep(3);
            data.add(66);
            System.out.println("Thread: " + Thread.currentThread().getName() + "\t added " + 66 + " is over.");
        }, "Thread-Volatile").start();

        while (data.getData() == 0) {
            // System.out.println(data.getData());
        }

        System.out.println("main thread is over.");
    }

    private static class Data {
        int data = 0;
        // volatile int data = 0;

        public void add(int value) {
            this.data += value;
        }

        public int getData() {
            return data;
        }

        public void setData(int data) {
            this.data = data;
        }
    }
}
```

#### 7.2.3 不保证原子性

> 不可分割、完整性，中间不能被加塞或被分割，要么同时成功，要么同时失败。

也即：多个线程对 `volatile` 变量的修改**存在线程冲突**：

```java
public static void main(String[] args) {
    Data data = new Data();

    for (int i = 0; i < 20; i++) {
        new Thread(() -> {
            for (int j = 0; j < 1000; j++) {
                data.add(1);
            }
        }, "Thread-Volatile-" + i).start();
    }

    // 默认存在 main 线程和 GC 线程
    while (Thread.activeCount() > 2) {
        Thread.yield();
    }

    System.out.println("main thread is over, the value is: " + data.getData());
}
```

**`++` 指令：**

```java
public class DeemoPlusPlus {
    private volatile int n;

    public void add() {
        /*
            LINENUMBER 7 L0
            ALOAD 0
            DUP
            GETFIELD com/deemo/thread/DeemoPlusPlus.n : I
            ICONST_1 入栈
            IADD
            PUTFIELD com/deemo/thread/DeemoPlusPlus.n : I
         */
        n++;
    }
}
```

- `++` 指令底层会被拆解：`GETFIELD、IADD、PUTFIELD`
- [字节码](https://www.jianshu.com/p/47de87deeb33)

**解决原子性：**

加锁、原子类（CAS）。

#### 7.2.4 禁止指令重排

为了提高性能，编译器和处理器常常会对指令做重拍：源代码 -> 编译器优化的重排 -> 指令并行的重排 -> 内存系统的重排 -> 最终执行的指令。

- 确保在**单线程**中，重排后的指令执行结果与源代码执行结果一致
- 处理器在进行重排时必须考虑指令之间**数据依赖性**

**DCL（Double Check Lock）单例模式：**

> 指令重排问题

`instance = new Instance();` 可以分为以下 3 步完成：

1. `memory = allocate();`：分配对象内存空间
2. `instance(memory);`：初始化对象
3. `instance = memory;`：指向内存地址

如果指令重排执行顺序为：1 -> 3 -> 2 便会存在问题：**对象还未完成初始化**。

**内存屏障（Memory Barrier），是一个 CPU 指令（了解）：**

- 保证特定操作的执行顺序
- 保证某些变量的内存可见性

![image-20220626120911748](images/image-20220626120911748.png)

### 7.3 Happens-Before

![img](images/v2-e1078c60d3c7cf74de4e77f1694545b2_720w.jpg)

**【原则一】程序次序规则**

在一个线程中，按照代码的顺序，前面的操作 Happens-Before 于后面的任意操作。

例如【示例一】中的程序 `x=42` 会在 `v=true` 之前执行。

这个规则比较符合单线程的思维：在同一个线程中，程序在前面对某个变量的修改一定是对后续操作可见的。

**【原则二】volatile 变量规则**

对一个 `volatile` 变量的写操作，Happens-Before 于后续对这个变量的读操作。

也就是说，对一个使用了 `volatile` 变量的写操作，先行发生于后面对这个变量的读操作。这个需要大家重点理解。

**【原则三】传递规则**

如果A Happens-Before B，并且B Happens-Before C，则A Happens-Before C。

我们结合【原则一】、【原则二】和【原则三】再来看【示例一】程序，此时，我们可以得出如下结论：

1. `x = 42` Happens-Before 写变量 `v = true`，符合【原则一】程序次序规则。
2. 写变量 `v = true` Happens-Before 读变量 `v = true`，符合【原则二】volatile 变量规则。

再根据【原则三】传递规则，我们可以得出结论：`x = 42` Happens-Before 读变量 `v=true`。

也就是说，如果线程B读取到了 `v=true` ，那么，线程A设置的 `x = 42` 对线程B就是可见的。换句话说，就是此时的线程B能够访问到 `x=42`。

**其实，Java 1.5版本的 `java.util.concurrent` 并发工具就是靠 volatile 语义来实现可见性的。**

**【原则四】锁定规则**

对一个锁的解锁操作 Happens-Before 于后续对这个锁的加锁操作。

例如，下面的代码，在进入 `synchronized` 代码块之前，会自动加锁，在代码块执行完毕后，会自动释放锁。

![img](images/v2-fce0d63f529cab7a9180102ca8987d33_720w.jpg)示例2

我们可以这样理解这段程序：假设变量x的值为10，线程A执行完synchronized代码块之后将x变量的值修改为10，并释放synchronized锁。

当线程B进入synchronized代码块时，能够获取到线程A对x变量的写操作，也就是说，线程B访问到的x变量的值为10。

**【原则五】线程启动规则**

如果线程A调用线程B的 `start()` 方法来启动线程B，则 `start()` 操作 Happens-Before 于线程B中的任意操作。

我们也可以这样理解线程启动规则：线程A启动线程B之后，线程B能够看到线程A在启动线程B之前的操作。

我们来看下面的代码。

![img](images/v2-3e331eb30a6cc2e2d235a23167a79f04_720w.jpg)示例三

上述代码是在线程A中执行的一个代码片段，根据【原则五】线程的启动规则，线程A启动线程B之后，线程B能够看到线程A在启动线程B之前的操作，在线程B中访问到的x变量的值为100。

**【原则六】线程终结规则**

线程A等待线程B完成（在线程A中调用线程B的 `join()` 方法实现），当线程B完成后（线程A调用线程B的 `join()` 方法返回），则线程A能够访问到线程B对共享变量的操作。

例如，在线程A中进行的如下操作。

![img](images/v2-3155dd0ed9b24b62c0bf6009f7961403_720w.jpg)示例四

**【原则七】线程中断规则**

**对线程 `interrupt()` 方法的调用 Happens-Before 于被中断线程的代码检测到中断事件的发生。**

例如，下面的程序代码。在线程A中中断线程B之前，将共享变量x的值修改为100，则当线程B检测到中断事件时，访问到的x变量的值为100。

![img](images/v2-5df9614a403c649d47c6daa5fc92b5a0_720w.jpg)示例五

**【原则八】对象终结原则**

**一个对象的初始化完成 Happens-Before 于它的 `finalize()` 方法的开始。**

例如，下面的程序代码：

![img](images/v2-ff54a30652b105c105918bdecf0aabf0_720w.jpg)

运行结果如下所示：

1. 构造方法
2. 对象销毁

### 7.4 原子类

`AtomicLong` 等原子类是无锁的方案实现，最大的好处就是==**性能**==，无需加解锁、无需阻塞等待锁的线程。

#### 7.4.1 无锁方案的实现原理

硬件支持。CPU为了解决并发问题，提供了 CAS 指令（ Compare And Swap）。CAS 指令包含 3 个参数：共享变量的内存地址 A、用于比较的值 B 和共享变量的新值 C；并且只有当内存中地 址 A 处的值等于 B 时，才能将内存中地址 A 处的值更新为新值 C。**作为一条 CPU 指令， CAS 指令本身是能够保证原子性的。**

使用 CAS 来解决并发问题，一般都会伴随着自旋（循环尝试）：

```java
do {
    oldVal = ...; // 1
    newVal = ...; // 2
} while (
    val != cas(oldVal, newVal) // 3
);
```

- ==**切记在循环中获取 `oldVal`**==

  在循环外获取的话，如果刚好在获取到值但进入循环前被其他线程修改了之后，会陷入死循环

- `cas()` 中，将当前值 `val` 与 `oldVal` 比较，如果相同则替换新值 `newVal`，否则返回当前值 `val`

  ```java
  cas(T expect, T newVal) {
      // 读取目前值
      int cur = val;
      
      // 比较期望值
      if (cur == expect) {
          // 如果是，则更新
          val = newVal;
      }
      
      return cur;
  }
  ```

**但是在 CAS 方案中，存在一个 ABA 问题：“如果 `cas()` 返回的值不等于 `val`，意味着线程在执行完代码1之后3之前，有其他线程更新过 `val`”，那么“如果 `cas()` 返回的值等于 `val`，是否就能认定 `val` 没有被其他线程修改过吗？”，显然是不能的！**

假设原本 `val = A`，T1 在执行完代码1后执行3之前，有可能被 T2 修改为B，后续又被 T3 修改回了A，这样 T1 虽然一直看到的是A，但 `val` 已经是被更新过了的。原子化更新对象很可能需要关心这个问题，因为虽然两个对象相等，但其中的属性可能已经变化了。

==**ABA 问题如何解决？增加版本号维度。**==

#### 7.4.2 原子类

> Java SDK 并发包里提供的原子类内容很丰富，我们可以将它们分为五个类别：**原子化的基本数据类型、原子化的对象引用类型、原子化数组、原子化对象属性更新器和原子化的累加器**。![image-20220501184308337](images/image-20220501184308337.png)

Java SDK 并发包里提供的原子类，使用的 `compareAndSet()` 来实现 `cas()`。

注意：原子化的对象引用中，`AtomicStampedReference` 和 `AtomicMarkableReference` 这两个原子类可以解决 ABA 问题。解决方案便是增加一个**版本号**维度。

#### 7.4.3 CAS

> CAS -> Compare-And-Swap，==**是一条 CPU 并发原语**==。
>
> 原语属于操作系统用语范畴，是由若干条指令组成，且原语的执行必须是连续、不能被中断。
>
> 它的功能是判断内存中某个位置的值是否为预期值，如果是则更新为新的值，这个过程是原子操作。

CAS 并发原语体现在 Java 语言中就是通过 `sun.misc.Unsafe` 类中的各个方法（大部分都是 `native` 方法来操作系统底层命令）。

![image-20220627202411837](images/image-20220627202411837.png)

![image-20220627202110663](images/image-20220627202110663.png)

- `var5`：期望值
- `var5 + var4`：更新值

**CAS 缺点：**

1. 自旋锁，更新失败会循环尝试，耗费 CPU
2. 只能保证一个共享变量的原子操作
3. ABA 问题

#### 7.4.4 ABA 问题

> **首尾**相同，中间可能会存在数据更新，但 CAS 同样会更新成功。

解决方案：额外增加一种唯一性机制，用于感知中间是否发生过变动，即使首尾相同 -> 乐观锁：版本号 stamp -> `AtomicStampedReference`。

```java
/**
 * An {@code AtomicStampedReference} maintains an object reference
 * along with an integer "stamp", that can be updated atomically.
 *
 * <p>Implementation note: This implementation maintains stamped
 * references by creating internal objects representing "boxed"
 * [reference, integer] pairs.
 *
 * @since 1.5
 * @author Doug Lea
 * @param <V> The type of object referred to by this reference
 */
public class AtomicStampedReference<V> {

    private static class Pair<T> {
        final T reference;
        final int stamp;
        private Pair(T reference, int stamp) {
            this.reference = reference;
            this.stamp = stamp;
        }
        static <T> Pair<T> of(T reference, int stamp) {
            return new Pair<T>(reference, stamp);
        }
    }

    private volatile Pair<V> pair;

    /**
     * Creates a new {@code AtomicStampedReference} with the given
     * initial values.
     *
     * @param initialRef the initial reference
     * @param initialStamp the initial stamp
     */
    public AtomicStampedReference(V initialRef, int initialStamp) {
        pair = Pair.of(initialRef, initialStamp);
    }
    
    // ...
}
```

**问题演示：**

```java
private static final AtomicReference<Integer> atomicReference = new AtomicReference<>(100);
```

- 注意 `AtomicReference` 是引用级别检测，`Integer` 自带缓存
- `Integer` 自带缓存部分有 ABA 问题，未缓存部分其实没有 ABA 问题

## 八、线程池

一种线程使用模式。线程过多会带来调度开销，进而影响缓存局部性和整体性能，而线程池维护着多个线程，等待着监督管理者分配可并发执行的任务。这样**==避免了在处理短时间任务时创建与销毁线程的代价==**。

- 降低资源消耗：重复利用线程
- 提高响应速度：任务到达便能立即执行，无需等待线程创建
- 提高线程可管理性：线程是稀缺资源，统一管理能够提升系统稳定性，还能统一分配、监控

![image-20220428195520381](images/image-20220428195520381.png)

###  8.1 newFixedThreadPool

线程数量固定的线程池。如果因为在关闭前的执行期间出现异常导致线程终止，如果需要，一个新线程将代理它执行后续的任务。

```java
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                  0L, TimeUnit.MILLISECONDS,
                                  new LinkedBlockingQueue<Runnable>());
}
```

### 8.2 newSingleThreadExecutor

单线程线程池，以无界队列的方式来运行该线程。如果因为在关闭前的执行期间出现异常导致线程终止，如果需要，一个新线程将代理它执行后续的任务。

```java
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService
        (new ThreadPoolExecutor(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>()));
}
```

### 8.3 newCachedThreadPool

创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程。（随着任务的并发程度越高，创建的线程越多）

```java
public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                  60L, TimeUnit.SECONDS,
                                  new SynchronousQueue<Runnable>());
}
```

任务队列是 `SynchronousQueue`

### 8.4 newScheduleThreadPool

线程池支持定时以及周期性执行任务，创建一个 corePoolSize 为传入参 数，最大线程数为整形的最大数的线程池。

```java
public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
    return new ScheduledThreadPoolExecutor(corePoolSize);
}
```

### 8.5 newWorkStealingPool

JDK 1.8提供的线程池，底层使用 `ForkJoinPool` 实现，创建一个拥有多个任务队列的线程池，可以减少连接数，创建当前可用 CPU 核数的线程来并行执行任务。

```java
/**
 * Creates a thread pool that maintains enough threads to support
 * the given parallelism level, and may use multiple queues to
 * reduce contention. The parallelism level corresponds to the
 * maximum number of threads actively engaged in, or available to
 * engage in, task processing. The actual number of threads may
 * grow and shrink dynamically. A work-stealing pool makes no
 * guarantees about the order in which submitted tasks are
 * executed.
 *
 * @param parallelism the targeted parallelism level
 * @return the newly created thread pool
 * @throws IllegalArgumentException if {@code parallelism <= 0}
 * @since 1.8
 */
public static ExecutorService newWorkStealingPool(int parallelism) {
    return new ForkJoinPool
        (parallelism,
         ForkJoinPool.defaultForkJoinWorkerThreadFactory,
         null, true);
}

/**
 * Creates a work-stealing thread pool using all
 * {@link Runtime#availableProcessors available processors}
 * as its target parallelism level.
 * @return the newly created thread pool
 * @see #newWorkStealingPool(int)
 * @since 1.8
 */
public static ExecutorService newWorkStealingPool() {
    return new ForkJoinPool
        (Runtime.getRuntime().availableProcessors(),
         ForkJoinPool.defaultForkJoinWorkerThreadFactory,
         null, true);
}
```

- parallelism：并行级别，通常默认为 JVM 可用的处理器个数
- factory：用于创建 `ForkJoinPool` 中使用的线程
- handler：用于处理工作线程未处理的异常，默认为 null
- asyncMode：用于控制 WorkQueue 的工作模式：队列---反队列

### 8.6 七个参数

1. 核心线程数量 - 正式工

2. 最大线程数量 - 包含临时工的最大数

   **==什么时候创建临时工？==**

   1. 如果正在运行的线程数量小于 corePoolSize，那么马上创建线程运行这个任务
   2. 如果正在运行的线程数量大于或等于 corePoolSize，那么将这个任务放入队列
   3. 如果这个时候队列满了且正在运行的线程数量还小于 maximumPoolSize，那么创建非核心线程立刻运行这个任务
   4. 如果队列满了且正在运行的线程数量大于或等于 maximumPoolSize，那么线程池会启动饱和拒绝策略来执行

3. 空闲线程存活时间 - 临时工最大空闲时间

4. 存活时间单位

5. workQueue - 存放提交但未执行的任务

6. 线程创建工厂 - 创建线程（可省略）

7. handler - 等待队列满后的拒绝策略（可省略）

### 8.7 拒绝策略

![image-20220428204321251](images/image-20220428204321251.png)

1. `AbortPolicy` - 抛出异常

   ```java
   public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
       throw new RejectedExecutionException("Task " + r.toString() +
                                            " rejected from " +
                                            e.toString());
   }
   ```

2. `CallerRunsPolicy` - 调用者运行

   ```java
   /**
    * Executes task r in the caller's thread, unless the executor
    * has been shut down, in which case the task is discarded.
    *
    * @param r the runnable task requested to be executed
    * @param e the executor attempting to execute this task
    */
   public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
       if (!e.isShutdown()) {
           r.run();
       }
   }
   ```

3. `DiscardOldestPolicy` - 抛弃等待最久的任务

   ```java
   /**
    * Obtains and ignores the next task that the executor
    * would otherwise execute, if one is immediately available,
    * and then retries execution of task r, unless the executor
    * is shut down, in which case task r is instead discarded.
    *
    * @param r the runnable task requested to be executed
    * @param e the executor attempting to execute this task
    */
   public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
       if (!e.isShutdown()) {
           e.getQueue().poll();
           e.execute(r);
       }
   }
   ```

4. `DiscardPolicy` - 对新任务不做任何处理

   ```java
   /**
    * Does nothing, which has the effect of discarding task r.
    *
    * @param r the runnable task requested to be executed
    * @param e the executor attempting to execute this task
    */
   public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
   }
   ```

### 8.8 自定义线程池

1. `FixedThreadPool & SingleThreadPool` 允许的请求队列（`LinkedBlockingQueue`）长度为 `Integer.MAX_VALUE`，可能会堆积大量任务导致OOM
2. `FixedThreadPool & CachedThreadPool & ScheduledThreadPool` 允许创建的线程数量最大为 `Integer.MAX_VALUE`，可能会创建大量线程导致OOM

因此建议自定义线程池![image-20220428205438804](images/image-20220428205438804.png)

## 九、Future

### 11.1 Future

`Future` 接口有 5 个方法：

```java
// 取消任务
boolean cancel(boolean mayInterruptIfRunning);
// 判断任务是否已取消
boolean isCancelled();
// 判断任务是否已结束
boolean isDone();
// 获得任务执行结果
get();
// 获得任务执行结果，支持超时
get(long timeout, TimeUnit unit);
```

- 两个 `get()` 方法都是阻塞式

线程池使用 `execute(Runnable)` 提交任务，但是**无法获取任务的执行结果**，线程池提供了其他接口来获取任务的执行结果：

```java
// 提交 Runnable 任务
Future<?> submit(Runnable task);
// 提交 Callable 任务
<T> Future<T> submit(Callable<T> task);
// 提交 Runnable 任务及结果引用
<T> Future<T> submit(Runnable task, T result);
```

这三个 `submit()` 接口方法的返回值都是 `Future` 类型，三个方法简要介绍：

1. `submit(Runnable task)`

   方法参数是一个 `Runnable ` 接口，该接口是没有返回值的，所有这个方法返回的 `Future` 仅可以用来断言任务已经结束，类似于 `Thread.join()`

2. `submit(Callable<T> task)`

   方法参数是一个 `Callable ` 接口，该接口拥有一个带有返回值的方法：`V call() throws Exception;`，可以通过 `Future.get()` 来获取任务的执行结果

3. `submit(Runnable task, T result)`

   这个方法提供了两个参数，一个是不带返回结果的 `Runnable` 接口，另一个就是用于主线程与子线程沟通的桥梁 `T result`，通过 `Future.get()` 获取到的返回值便是传递给 `submit()` 方法的参数 `T result`，这样便得以使主线程通过它来与子线程共享数据

### 11.2 FutureTask

==**相比 `Runnable`，`Callable` 接口可以拥有返回值！**==

由于 `Thread` 不能直接接收 `Callable` 接口，因此需要一个辅助类：`FutureTask` 完成转换：

```java
public class FutureTask<V>
    implements RunnableFuture<V>
        extends Runnable, Future<V>

public void test() throws ExecutionException, InterruptedException {
    FutureTask<Integer> futureTask = new FutureTask<>(() -> {
        TimeUnit.SECONDS.sleep(5);
        return 10;
    });

    new Thread(futureTask, "deemo-thread-01").start();

    while (!futureTask.isDone()) {
        System.out.println("wait...");
    }
    System.out.println(futureTask.get());
    System.out.println("done.");
}
```

### 11.3 泡茶（一）

> 实现泡茶程序：==**分工、同步和互斥**==![image-20220502085911479](images/image-20220502085911479.png)

1. 分工

   使用两个线程来完成泡茶任务，T1 负责洗水壶、烧开水和泡茶，T2 负责洗茶壶、洗茶杯和拿茶叶

2. 同步

   T1 泡茶之前需要等待 T2 拿茶叶

3. 互斥

   暂无

使用 `Thread.join() || CountDownLatch` 都可以解决，尝试使用 `Future` 特性来实现：

- 两个线程对应两个 `FutureTask`
- 由于 T1 需要等待 T2 拿茶叶，所以 T1 中需要持有 T2 的 `FutureTask` 对象
- ![image-20220502093346401](images/image-20220502093346401.png)

```java
// T1Task 需要执行的任务：
// 洗水壶、烧开水、泡茶
class T1Task implements Callable<String> {
    FutureTask<String> ft2;
    // T1 任务需要 T2 任务的 FutureTask
    T1Task(FutureTask<String> ft2){
        this.ft2 = ft2;
    }
    
    @Override
    String call() throws Exception {
        System.out.println("T1: 洗水壶...");
        TimeUnit.SECONDS.sleep(1);

        System.out.println("T1: 烧开水...");
        TimeUnit.SECONDS.sleep(15);
        // 获取 T2 线程的茶叶
        String tf = ft2.get();
        System.out.println("T1: 拿到茶叶:"+tf);
        System.out.println("T1: 泡茶...");
        return " 上茶:" + tf;
    }
}

// T2Task 需要执行的任务:
// 洗茶壶、洗茶杯、拿茶叶
class T2Task implements Callable<String> {
    @Override
    String call() throws Exception {
        System.out.println("T2: 洗茶壶...");
        TimeUnit.SECONDS.sleep(1);
        System.out.println("T2: 洗茶杯...");
        TimeUnit.SECONDS.sleep(2);
        System.out.println("T2: 拿茶叶...");
        TimeUnit.SECONDS.sleep(1);
        return " 龙井 ";
    }
}

// 一次执行结果：
T1: 洗水壶...
T2: 洗茶壶...
T1: 烧开水...
T2: 洗茶杯...
T2: 拿茶叶...
T1: 拿到茶叶: 龙井
T1: 泡茶...
上茶: 龙井
```

### 11.4 CompletableFuture

> ==**建议根据不同的业务创建不同的线程池，避免互相干扰**==
>
> **`CompletableFuture` 已经满足简单的异步编程需求，可以关注 `RxJava` 这个项目（`ReactiveX` 的 Java 实现），即便在 Java 1.6 版本也能享受异步编程的乐趣。**

创建 `CompletableFuture` 主要靠以下四个静态方法：

```java
// 使用默认线程池
static CompletableFuture<Void> runAsync(Runnable runnable) {}
static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {}

// 可以指定线程池
static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor) {}
static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor) {}
```

- `runAsync` 和 `supplyAsync` 的区别在于前者没有返回值
- 前两个方法和后两个方法的区别在于后置可以指定线程池
- 默认情况下 `CompletableFuture` 会使用公共的 `ForkJoinPool` 线程池，这个线程池默认创建的线程数是 CPU 的核数（也可以通过 ` JVM option:- Djava.util.concurrent.ForkJoinPool.common.parallelism` 来设置线程数）
- 如果所有的 `CompletableFuture` 都共享一个线程池，那么一旦有线程执行一些很慢的 I/O 操作，就会导致线程池中的其他任务被阻塞执行造成**线程饥饿**，进而影响整个系统的性能。==**因此，建议根据不同的业务创建不同的线程池，避免互相干扰**==

### 11.5 泡茶（二）

```java
// 任务一
CompletableFuture<Void> cf1 = CompletableFuture.runAsync(() -> {
    System.out.println("T1: 洗水壶...");
    DeemoCompletableFuture.sleep(TimeUnit.SECONDS, 1);

    System.out.println("T1: 烧开水...");
    DeemoCompletableFuture.sleep(TimeUnit.SECONDS, 15);
});

// 任务二
CompletableFuture<String> cf2 = CompletableFuture.supplyAsync(() -> {
    System.out.println("T2: 洗茶壶...");
    DeemoCompletableFuture.sleep(TimeUnit.SECONDS, 1);

    System.out.println("T2: 洗茶杯...");
    DeemoCompletableFuture.sleep(TimeUnit.SECONDS, 2);

    System.out.println("T2: 拿茶叶...");
    DeemoCompletableFuture.sleep(TimeUnit.SECONDS, 1);

    return "红茶";
});

// 任务三：等待任务一和任务二完成后执行
CompletableFuture<String> cf3 = cf1.thenCombine(cf2, (__, name) -> {
    System.out.println("拿到茶叶：" + name);
    System.out.println("泡茶...");

    return "上等好茶：" + name;
});

// 等待任务一执行结束
System.out.println(cf3.get());
```

### 11.6 CompletableFuture 之 CompletionStage

> `CompletableFuture` 类还实现了 `CompletionStage` 接口，在 1.8 版本提供了40个方法！！如何理解？

站在分工的角度类比一下工作流：任务是由**时序关系**的，比如有==**串行、并行和汇聚**==等。类比烧水泡茶，其中洗水壶和烧开水是串行关系，洗水壶、烧开水和洗茶壶、洗茶杯这两组任务之间是并行关系，而烧开水、拿茶叶和泡茶就是汇聚关系。![image-20220502095939068](images/image-20220502095939068.png)

`CompletionStage` 接口可以清晰地描述任务之间的这种时许关系，例如 `cf1.thenCombine(cf2, (__, name) -> {})` 就是一种汇聚关系。烧水泡茶中的汇聚关系是一种==** AND 聚合关系**==（指依赖任务都完成后才开始执行当前任务）。既然有 AND 聚合关系，就一定还有 ==**OR 聚合关系**==（指依赖任务只要有一个完成就可以执行当前任务）。

在编程领域，还有一个绕不开的山头，那就是==**异常处理**==，`CompletionStage` 接口也能优雅地描述异常处理。

#### 11.6.1 串行关系

> `CompletionStage` 接口里描述串行关系主要是：`thenApply`、`thenAccept`、`thenRun` 和 `thenCompose` 这四个系列的接口。

- `thenApply` 系列函数里参数 `fn` 的类型是接口 `Function`，这个接口里与 `CompletionStage` 相关的方法是 `R apply(T t)`，这个方法既能接收参数也支持返回值，所以 `thenApply` 系列方法返回的是 `CompletionStage<R>`

-  `thenAccept` 系列方法里参数 `consumer` 的类型是接口 `Consumer`，这个接口里与 `CompletionStage` 相关的方法是 `void accept(T t)`，这个方法虽然支持参数，但却不支持回值，所以 `thenAccept`系列方法返回的是 `CompletionStage<R>`

- `thenRun` 系列方法里 `action` 的参数是 `Runnable`，所以 `action` 既不能接收参数也不支持返回值，所以 `thenRun` 系列方法返回的也是 `CompletionStage<Void>`

- ==**这些方法里面 `Async` 代表的是异步执行。**==其中，`thenCompose` 系列方法会新创建一个子流程，最终结果和 `thenApply` 系列相同

- 示例：

  ```java
  CompletableFuture<String> f0 =
   CompletableFuture.supplyAsync(
   () -> "Hello World") // ①
   .thenApply(s -> s + " QQ") // ②
   .thenApply(String::toUpperCase); // ③
  System.out.println(f0.join());
  // 输出结果
  HELLO WORLD QQ
  // 虽说是一个异步流程，但任务是①②③串行执行，③依赖②依赖①
  ```

```java
CompletionStage<R> thenApply(fn);
CompletionStage<R> thenApplyAsync(fn);
CompletionStage<Void> thenAccept(consumer);
CompletionStage<Void> thenAcceptAsync(consumer);
CompletionStage<Void> thenRun(action);
CompletionStage<Void> thenRunAsync(action);
CompletionStage<R> thenCompose(fn);
CompletionStage<R> thenComposeAsync(fn);
```

#### 11.6.2 AND 汇聚

> `CompletionStage` 接口里描述 AND 汇聚关系，主要是 `thenCombine`、`thenAcceptBoth`和 `runAfterBoth` 系列接口，这些接口的区别也是源自 `fn`、`consume` 和 `action` 这三个核心参数不同。

```java
CompletionStage<R> thenCombine(other, fn);
CompletionStage<R> thenCombineAsync(other, fn);
CompletionStage<Void> thenAcceptBoth(other, consumer);
CompletionStage<Void> thenAcceptBothAsync(other, consumer);
CompletionStage<Void> runAfterBoth(other, action);
CompletionStage<Void> runAfterBothAsync(other, action);
```

#### 11.6.3 OR 汇聚

> `CompletionStage` 接口里描述 AND 汇聚关系，主要是 `applyToEither`、`acceptEither`和 `runAfterEither` 系列接口，这些接口的区别也是源自 `fn`、`consume` 和 `action` 这三个核心参数不同。

```java
CompletionStage applyToEither(other, fn);
CompletionStage applyToEitherAsync(other, fn);
CompletionStage acceptEither(other, consumer);
CompletionStage acceptEitherAsync(other, consumer);
CompletionStage runAfterEither(other, action);
CompletionStage runAfterEitherAsync(other, action);
```

示例：

```java
CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
    int t = getRandom(5, 10);
    sleep(TimeUnit.SECONDS, t);
    return String.valueOf(t);
});
CompletableFuture<String> f2 =CompletableFuture.supplyAsync(()->{
    int t = getRandom(5, 10);
    sleep(TimeUnit.SECONDS, t);
    return String.valueOf(t);
});
CompletableFuture<String> f3 = f1.applyToEither(f2, s -> s);

System.out.println(f3.join());
```

#### 11.6.4 异常处理

虽然上面提到的 `fn`、`consume` 和 `action` 它们的核心方法都**不允许抛出可检查异常，但不能限制它们抛出运行时异常**。非异步编程使用 `try-catch-finally` 来捕获处理异常，那么在异步编程里面，异常如何处理？

==**`CompletionStage` 提供的异常处理很简单，支持链式编程。**==

```java
CompletionStage exceptionally(fn);
CompletionStage<R> whenComplete(consumer);
CompletionStage<R> whenCompleteAsync(consumer);
CompletionStage<R> handle(fn);
CompletionStage<R> handleAsync(fn);
```

- `exceptionally()` 的使用类似于 `catch(e) {}`，支持链式编程，所以使用起来非常简单

  ```java
  CompletableFuture<Integer> f0 = CompletableFuture
      .supplyAsync(() -> 7 / 0))
      .thenApply(r -> r * 10)
      .exceptionally(e -> 0);
  
  System.out.println(f0.join());
  ```

- `whenComplete()` 和 `handle()` 系列方法就类似于 `finally() {}`，无论是否发生异常都会执行

### 11.7 CompletionService

> **`CompletionService` 可以优雅地批量提交异步任务，内部维护了一个阻塞队列，当任务执行结束就把任务的执行结果加入到阻塞队列中。**注意，是将 ==**`Future<V>` 对象**==加入到队列中。

`CompletionService` 接口的实现类是 `ExecutorCompletionService`，该类的构造方法：

- `ExecutorCompletionService(Executor executor)`
- `ExecutorCompletionService(Executor executor, BlockingQueue<Future<V>> completionQueue)`

这两个构造方法都需要传入一个线程池，如果不指定 `completionQueue` ，将会使用默认的无界 `LinkedBlockingQueue`

`CompletionService` 接口提供 5 个方法：

```java
Future<V> submit(Callable<V> task);
Future<V> submit(Runnable task, V result);

Future<V> take() throws InterruptedException;
Future<V> poll();
Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException;
```

- `submit` 方法有二，区别在于 `Callable` 和 `Runnable` 的特性
- 其余三个方法都是和阻塞队列相关，`take()` 和 `poll()` 的区别在于当队列为空时，前者会被阻塞，后者返回 `NULL`

> 利用 `CompletionService` 实现 Dubbo 中的 Forking Cluster

Dubbo 中有一种叫做 Forking 的集群模式，这种集群模式下，支持并行地调用多个查询服 务，只要有一个成功返回结果，整个服务就可以返回了。

```java
CompletionService<Integer> cs = new ExecutorCompletionService<>(executor);
// 获取最快返回的任务执行结果
Integer r = 0;
try {
   // 只要有一个成功返回，则 break
   for (int i = 0; i < 3; ++i) {
       r = cs.take().get();
       // 简单地通过判空来检查是否成功返回
       if (r != null) {
           // 返回结果
           return r;
       }
   }
} finally {
   // 取消所有任务
   for (Future<Integer> f : futures) {
       f.cancel(true);
   }
}
```

## 十、Fork/Join

注意：`a.fork() & b.fork() & b.join() & a.join()` 调用顺序！

## 十一、设计模式

### 11.1 Immutability模式

**将一个类所有的属性都设置成 final 的，并且只允许存在只读方法，那么这个类基本上就具备不可变性了。更严格的做法是这个类本身也是 final 的，也就是不允许继承。**

Java SDK 里很多类都具备不可变性，例如经常用到的 String 和 Long、Integer、Double 等基础类型的包装类都具备不可变性， 这些对象的线程安全性都是靠不可变性来保证的（String 的 `replace()` 方法返回的也是新字符串）。

如果具备不可变性的类，**需要提供类似修改的功能**，具体该怎么操作呢？==**做法很简单，那就是创建一个新的不可变对象。**==

享元模式（Flyweight Pattern）：==**利用享元模式可以减少创建对象的数量（本质是一个对象池）**==，从而减少内存占用。Java 语言里面 Long、Integer、Short、Byte 等这些基本数据类型的包装类都用到了享元模式。**因此，这些类也不适合当作互斥锁来使用，因为内部缓存得有数据导致对象被重用。**

**在使用 Immutability 模式的时候一定要确认保持不变性的边界在哪里，是否要求属性对象也具备不可变性。**

具备不变性的对象，只有一种状态，这个状态由对象内部所有的不变属性共同决定，其实还有一种更简单的不变性对象 —— **无状态**，只有方法没有属性。

### 11.2  Copy-on-Write模式

> Linux 中的 fork() 函数就聪明得多 了，fork() 子进程的时候，并不复制整个进程的地址空间，而是让父子进程共享同一个地址 空间；只用在父进程或者子进程需要写入的时候才会复制地址空间，从而使父子进程拥有各 自的地址空间。
>
> 使用 Copy-on-Write 更多地体 现的是一种==**延时策略，只有在真正需要复制的时候才复制，而不是提前复制好**==，同时 Copy-on-Write 还支持**按需复制**，所以 Copy-on-Write 在操作系统领域是能够提升性能 的。

现有的 `CopyOnWriteArrayList` 和 `CopyOnWriteArraySet` 都比较傻，一旦修改需要全量复制，因此建议仅仅是读多写非常少的场景使用，例如RPC的路由表（接口方法与服务方的路由信息）。

### 11.3 线程本地存储模式

> 没有共享，就没有伤害。
>
> getEntry(): 0x61c88647，解决hash碰撞的一个神奇的数？

Java 中对 `ThreadLocal` 的实现：

由 Thread 持有 `ThreadLocal.ThreadLocalMap`，`ThreadLocalMap` 的 key 是 `ThreadLocal`（就是说，Thread 的所有线程安全变量全部由 `ThreadLocalMap` 存储，每种不同的 `ThreadLocal` 类型数据都是一个不同的 key，也就是集中管理）：![image-20220503183812982](images/image-20220503183812982.png)

```java
public class Thread() {
    /* ThreadLocal values pertaining to this thread. This map is maintained
     * by the ThreadLocal class. */
    ThreadLocal.ThreadLocalMap threadLocals = null;
}

public class ThreadLocal<T> {
    void createMap(Thread t, T firstValue) {
        // this -> ThreadLocal
        t.threadLocals = new ThreadLocalMap(this, firstValue);
    }
    
    public T get() {
        // 首先获取线程持有的
        // ThreadLocalMap
        ThreadLocalMap map = Thread.currentThread().threadLocals;
        // 在 ThreadLocalMap 中 查找变量 this -> ThreadLocal
        Entry e = map.getEntry(this);
        return e.value;
    }
    
    static class ThreadLocalMap {
        // 内部是数组而不是 Map
        Entry[] table;
        // 根据 ThreadLocal 查找 Entry
        Entry getEntry(ThreadLocal key) {
            // 省略查找逻辑
        }
        // Entry 定义
        static class Entry extends WeakReference<ThreadLocal> {
            Object value;
            Entry(ThreadLocal<?> k, Object v) {
                // 弱引用
                super(k);
                value = v;
            }
        }
    }
}
```

而不是说 `ThreadLocalMap` 持有 Thread，Java 中的设计更合理一些，Java 设计中，`ThreadLocal` 仅仅是一个代理工具类，内部不持有任何与线程相关的数据，所有和线程相关的数据都存储在 Thread 里面，这样的设计更容易理解，从数据的亲缘性来将，`ThreadLocalMap` 属于 Thread 也更加合理。

更深层次的原因是==**不容易产生内存泄漏**==：如果是由 `ThreadLocal` 持有的 Map 持有 Thread 对象的引用，那么意味着只要 `ThreadLocal` 对象存在，那么 Map 中的 Thread 对象就永远不会被回收，而 `ThreadLocal` 的生命周期往往都比线程长。而 Java 实现中 Thread 持有 `ThreadLocalMap`，而且 `ThreadLocalMap` 里对 `ThreadLocal` 的引用还是弱引用，所以，只要 Thread 对象可以被回收，那么 `ThreadLocalMap` 就能被回收。

- ==**`ThreadLocal` 与内存泄漏**==：

  **在线程池中或者不会销毁的线程中使用 `ThreadLocal` 可能会导致内存泄漏！**原因是因为线程的存活时间太长，如果跟程序共同生死，这就意味着 Thread 持有的 `ThreadLocalMap` 一直都不会被回收。虽然 `ThreadLocalMap` 对 `ThreadLocal` 是弱引用，只要 `ThreadLocal` 结束了自己的生命周期就可以被回收掉，但是其中的 `Object value` 确实强引用，所以即便 Value 的生命周期结束了，Value 也是无法被回收的！

  那么怎么解决呢？==**`try {} finally {} ` 手动释放。**==

- `InheritableThreadLocal` 与继承性

  通过 `ThreadLocal` 创建的线程变量，子线程无法继承。而 `InheritableThreadLocal` 支持了这种特性，但是不建议使用，一是容易内存泄漏，而是容易导致继承关系错乱导致数据错误

- ==**`ThreadLocal` 与 局部变量**==

  局部变量会随着调用量而创建新对象，`ThreadLocal` 新建对象是线程数，两者差距很大

### 11.4 Guarded Suspension模式

> 多线程版等待唤醒机制的规范实现，Guarded Suspension 模式也常被称作 Guarded Wait 模式、Spin Lock 模式（因为使用 了 while 循环去等待）。

设想一个现实场景：朋友聚众取餐厅聚餐，发现包间没收拾好，等待服务员收拾结束后**大堂经理**再通知就餐。

设想一个实际场景：线程 T1 发送一个异步请求到 MQ，等 MQ 响应后再返回结果，但消费 MQ 结果的线程并不是线程 T1，此时如何通知线程 T1 等待并拿到 MQ 的返回结果呢？

**其实本质上都是“等待 - 唤醒”机制，但此时由于不在同一个线程，因此需要增加一个类似于“大堂经理”的角色完成调度，并且“大堂经理”需要很聪明，能够知道 T1 与 返回结果 的对应关系。**

前人种树，后人乘凉。前人将其总结为了一个设计模式：Guarded Suspension，保护性暂停。下图就是 Guarded Suspension 模式的结构图，一个对象 `GuardedObject`，内部有一个成员变量 —— 受保护对象，两个成员方法 —— `get(Predicate<T> p) & onChange(T obj)`。 `GuardedObject` 类似于大堂经理角色，==**受保护对象便是包间（需要通信传递的数据对象）**==，`get()` 方法的参数 p 便是前提条件，`onChange() ` 方法可以 fire 一个事件，这个事件往往能改变前提条件 p 的计算结果。

 ![image-20220503194120159](images/image-20220503194120159.png)

通用模板：

```java
class GuardedObject<T>{
    // 受保护的对象
    volatile T obj;
    final Lock lock = new ReentrantLock();
    final Condition done = lock.newCondition();
    final int timeout = 1;
    
    // 获取受保护对象
    T get(Predicate<T> p) {
        lock.lock();
        try {
            //MESA 管程推荐写法
            while(!p.test(obj)){
                done.await(timeout, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e){
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
        
        // 返回非空的受保护对象
         return obj;
    }
    
    // 事件通知方法
    void onChanged(T obj) {
        lock.lock();
        try {
            this.obj = obj;
            done.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
```

- ==**注意收保护的对象的可见性问题：`volatile`**==

这个模板仍需要改进，因为它并不像大堂经理一样知道谁在等待哪个包间，因此，需要增加一个通信机制识别谁在等待谁：

此时需要找出一个可以联系的点，比如实际问题中的 T1 等待 MQ 中的返回结果，可以通过消息的唯一 ID 确认双方谁在等待谁，有了这个关系，我们便可以维护一个 MQ 消息 ID 和 `GuardedObject` 对象实例的关系：在 `GuardedObject` 中维护一个 Map，key 是 MQ 消息 ID，value 是 `GuardedObject` 对象实例，同时增加静态方法 `create() & fireEvent()`：

```java
// 保存所有 GuardedObject
final static Map<Object, GuardedObject> gos= new ConcurrentHashMap<>();

// 静态方法创建 GuardedObject
static <K> GuardedObject create(K key) {
    GuardedObject go = new GuardedObject();
    gos.put(key, go);
    return go;
}

// 静态方法找到谁（GuardedObject）在等并触发 onChanged
static <K, T> void fireEvent(K key, T obj) {
    GuardedObject go = gos.remove(key);
    if (go != null) {
        go.onChanged(obj);
    }
}
```

### 11.5 Balking模式

> 本质上也是一种规范化地解决“多线程版本 if”的方案。

Balking模式相比 Guarded Suspension模式，后者需要一直等一直等，必须等到结果才会返回，而前者便是为了解决此现象而设计的。

### 11.6 Thread-Per-Message模式

我们曾经把并发编程领域的问题总结为三个核心问题：分工、同步和互斥。其中，同步和互 斥相关问题更多地源自微观，而分工问题则是源自宏观。我们解决问题，往往都是从宏观入 手，在编程领域，软件的设计过程也是先从概要设计开始，而后才进行详细设计。==**同样，解 决并发编程问题，首要问题也是解决宏观的分工问题。**==并发编程领域的分工问题，指的是如何高效地拆解任务并分配给线程。

> 并发编程领域里，解决分工问题也有一系列的设计模式，比较常用的主要有 Thread-Per-Message 模式、Worker Thread 模式、生产者 - 消费者模式等等。

==理解 Thread-Per-Message模式，可以想象网络编程中的服务端实现，主线程只接收客户端请求，将客户端请求的处理任务交由子线程去处理。==

而在 Java 中由于创建-销毁线程太过于**重量级**，因此**直接创建-销毁线程开销很大**，不切实际，因此解决方案会想到用线程池，这也算是 Java 语言的特性原因（Java 线程与操作系统线程一一对应，好处是稳定、可靠，缺点是创建成本高）导致，而像 Go、Lua 语言里的协程，有一种叫**轻量级线程**的解决方案，其创建的成本很低，基本上和创建一个普通对象的成本相似，并且创建的速度和内存占用相比操作系统至少有一个数量级的提升，因此基于轻量级线程实现的 Thread-Per-Message模式就完成没问题。

==**在 OpenJDK 中有个 Loom 项目，其中的 Fiber 就是用于解决 Java 语言的轻量级线程问题**==，对比 Thread 的实现，使用上改动非常小：只需将 `new Thread(() -> {}).start()` 修改为 `Fiber.schedule(() -> {]})`。

### 11.7 Worker Thread模式

同样也是一种面向分工的设计模式。

==Worker Thread模式可以类比现实世界中车间的工作模式，有活了大家一起干没活就一起闲聊，对应 Java 语言中的线程池。==

- 使用线程池一定要注意任务队列是否是无界队列
- 最好根据业务需求实现线程工厂方便调试和诊断
- 当线程池任务队列满时的拒绝策略也一定要清晰
- **避免死锁**：工厂里只有一个工人，但他的工作是等待工厂中的其他人给他提供东西！恰到好处的比喻。

Worker Thread模式与 Thread-Per-Message模式不同的是，前者是不关心任务被哪个线程执行，后者是主线程可以直接与子线程进行沟通。

### 11.8 两阶段终止模式

Java 语言中 Thread 曾经提供了一个 `stop()` 方法来终止线程，但是早已经不建议使用了，原因便是因为这个方法会一剑封喉，被终止的线程没有任何机会“料理后事”。

两阶段终止模式：![image-20220504100600433](images/image-20220504100600433.png)

1. 第一阶段发送终止指令
2. 第二阶段响应终止指令

回顾 Java 线程的生命周期：<img src="images/image-20220504100624184.png" alt="image-20220504100624184" style="zoom:50%;" />

==**Java 线程进入终止状态的前提状态是 RUNNABLE 状态**==，而实际上线程也可以处于休眠状态，那么如何将休眠状态的线程切换到 RUNNABLE 状态呢？`interrupt()`

**使用注意事项：**

- `Thread.currentThread().isInterrupted()` 判断线程的中断线程
- 手动 `Thread.interrupt()` 中断线程时，会抛出 `InterruptedException`，需要注意的是，在捕获该异常后，==**JVM 的异常处理会清除线程的中断状态**==，如有需要，需要手动重新设置线程的中断状态 `Thread.currentThread().interrupt() `
- 基于以上原因，当引入第三方库提供的业务方法时，我们没有办法保证第三方库正确处理了线程的中断异常，因此==**建议设置自己的线程中断标志位**==

**优雅地终止线程池：**

- `shutdown()`

  拒绝接收新的任务，会等待线程池中阻塞队列的任务（包括正在被线程执行的任务）都执行完毕之后再关闭线程池

- `shutdownNow()`

  更激进一些，不但会拒绝接收新的任务，同时还会中断线程池中正在执行的任务，阻塞队列中的任务也会被剥夺执行的机会，但**这些被剥夺执行机会的任务会作为该方法的返回值**

### 11.9 生产者 - 消费者模式

> Java 线程池本质上就是用生产者 - 消费者模式实现的。

生产者 - 消费者模式的核心是一个**任务队列**，生产者生产任务并将任务添加到任务队列，消费者从任务队列中获取任务并执行。![image-20220504105950723](images/image-20220504105950723.png)

**从架构设计来看，该模式的优点：**

- 解耦

  解耦的关键是组件之间的依赖关系和通信方式必须受限。**在生产者 - 消费者模式中，生产者和消费者没有任何依赖关系，彼此间的通信只能通过任务队列。**

- 异步

  生产者只需将任务提交到任务队列中无需等待任务被消费者线程执行完

- 平衡生产者和消费者的速度差异

  当两者间速度存在差异时，通过此模式来平衡二者之间的速度差异，减少线程创建、减少上下文切换提高效率。在 Java 中，Java 的线程和操作系统线程是一一对应的，并不是线程越多越好。

**批量执行：**

批量执行的核心要点是如何优雅地获取批量任务？

```java
// 任务队列（LinkedBlockingQueue 读写使用不同的锁提高性能）
BlockingQueue<Task> bq = new LinkedBlockingQueue<>(2000);
List<Task> pollTasks() throws InterruptedException {
    List<Task> ts = new LinkedList<>();
    
    // 阻塞式获取第一条任务
    Task t = bq.take();
    while (t != null) {
        ts.add(t);
        // 非阻塞式获取任务
        t = bq.poll();
    }
    
    return ts;
}
```

- `LinkedBlockingQueue` 读写使用不同的锁提高性能
- 首先以阻塞式获取第一条任务，而后以非阻塞式获取任务，避免没必要的循环

**分阶段提交：**

> 写文件如果同步刷盘性能会很慢，所以对于不是很重要的数据，我们可以采用异步刷盘的方式。

设计一个日志组件，其刷盘机制如下：

1. ERROR 级别的日志需要立即刷盘
2. 数据积累到 500 条需要立即刷盘
3. 存在未刷盘数据，且 5 秒钟内未曾刷盘，需要立即刷盘

该需求本质上是一种**分阶段提交**，具体看看怎么设计：

```java
// 任务队列
final BlockingQueue<LogMsg> bq = new BlockingQueue<>();
// flush 批量
static final int batchSize = 500;
// 只需要一个线程写日志
ExecutorService es = Executors.newFixedThreadPool(1);

void start() {
    final FileWriter writer = new FileWriter(file);
    this.es.execute(() -> {
        try {
            // 批量标记
            int idx = 0;
            long preFT = System.currentTimeMillis();
            while (!terminated) {
                LogMsg log = bq.poll(5, TimeUnit.SECONDS);
                if (log == null) {
                    continue;
                }
                
                writer.write();
                ++idx;
                
                // 根据规则刷盘
                if (log.level==LEVEL.ERROR ||
                   curIdx == batchSize ||
                    System.currentTimeMillis() - preFT > 5000)) {
                    writer.flush();
                    idx = 0;
                    preFT = System.currentTimeMillis();
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            writer.flush();
            writer.close();
        }
    });
}
```

- 思考如何停止

  日志系统一般是最后退出的环节，因此可以使用“毒丸”消息，等消费队列中的所有任务都被消费后再退出循环。

**优雅地终止生产者 - 消费者服务之“毒丸”：**

==**“毒丸”对象是生产者生产的一条特殊任务，当消费者线程读到“毒丸”任务时，会立即终止自身的执行。**==

```java
// 用于终止日志执行的“毒丸”
final LogMsg poisonPill = new LogMsg(LEVEL.ERROR, "");
// 任务队列
final BlockingQueue<LogMsg> bq = new LinkedBlockingQueue<>();

LogMsg logMsg;
while (!terminated) {
    logMsg = bq.poll(5, TimeUnit.SECONDS);
    if (poisonPill.equals(logMsg)) {
        // 毒丸消息
        break;
    }
    // ...
}
```

- 自定义中断标志 `terminated`
- 判断、处理“毒丸”消息

## 十二、案例分析

### 12.1 高性能限流器 Guava | RateLimiter

### 12.2 高性能网络应用框架 Netty

> https://wangwei.one/tags/Netty/

### 12.3 高性能队列 Disruptor

### 12.4 高性能数据库连接池 HiKariCP





