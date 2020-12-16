# Advanced-Concurrent
一个java的并发库，刚开的坑，逐步完善中。


## Feature
### 图线程池
#### 任务集合可以抽象成图
并发编程中，经常面对一些可以并发执行的任务集合，我们可以把这些任务集合抽象成图，以便简化编程。
例如某pipeline中，任务之间没有互相先后顺序依赖，可以抽象成以下的任务关系图，图的点代表任务，边代表依赖关系。对于这种任务之间没有任何依赖关系的图，直接把所有任务放到线程池ExecutorService执行即可。

![image.png](https://upload-images.jianshu.io/upload_images/8926363-ed6f2b9dddcc8c94.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


java的线程池ExecutorService适合提交互相之间没有依赖的任务，如果任务之间有依赖，就不能简单的调用ExecutorService.submit()了，可能出现死锁、饥饿等情况。例如下图线性pipeline，图中有4个任务,彼此之间互相依赖，如果把4个任务扔进一个只有3个线程的ExecutorService，那么可能出现任务2、任务3、任务4占用了3个线程，但都需要等待前面的任务完成；而任务1还在ExecutorService的任务队列里放着，永远没有空闲线程能把他取出来执行，因此整个线程池就会永远阻塞、无法前进。

![image.png](https://upload-images.jianshu.io/upload_images/8926363-fd5c608cf28c5ecb.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


如果任务之间的依赖很简单，靠CompletionService+future.get()可以搞定，比如下图的非线性pipeline；但是写起来很麻烦，每一个阶段需要拿到前面阶段的future对象，感兴趣可以尝试写一下，个人感觉不好写。

![image.png](https://upload-images.jianshu.io/upload_images/8926363-562b15920811215b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

如果是分治任务，可以使用Fork/join，任务图如下（网上抄的图）；

![image.png](https://upload-images.jianshu.io/upload_images/8926363-c56653b78eb3ca63.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

而如果任务之间的依赖图不规则、有点复杂，比如下图，该如何写任务编排呢？

![image.png](https://upload-images.jianshu.io/upload_images/8926363-cc87590fa8f3d361.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

有很多编程模式可以选择，包括以下（详细可以看[并发编程模型](http://ifeve.com/并发编程模型/)和[并发编程时，如何写复杂的任务编排？](https://www.jianshu.com/p/64cc6c0706f3)，这里不细聊）

A. 水平切分：Parallel Workers
每个线程串行跑全套任务，不管任务图多复杂，我们就老老实实的单线程按顺序一个一个执行，通过添加更多线程来提高并发度

B. 垂直切分：pipeline模式
类似于CPU流水线，每个任务由独立的线程（池）执行，任务与任务之间靠queue异步通信，个人理解go中的csp模型就是这种方案

C. 生产消费模式

D. Actor模式

E. 函数式并行

办法很多，但写起来还是有些麻烦的，有没有更好的办法？

#### 图线程池
为了简化上述任务图的并发编程，我写了个图线程池，见https://github.com/seeflood/Advanced-Concurrent/blob/master/src/test/java/io/github/seeflood/advanced/concurrent/executor/dag/DAGTaskExecutorImplTest.java

假设有一组做菜任务，任务图如下,其中烧水/洗菜/听歌可以并行，烧水/洗菜/听歌都做好了才能切菜，切菜完成了才能炒菜
 ![image.png](https://upload-images.jianshu.io/upload_images/8926363-83b487d5c655f3e1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
 
那么使用图线程池执行这些任务，只需要构造好DAG，扔到线程池里执行即可，代码如下：
```
        DAGTaskGroup<String> dag = new DAGTaskGroup<>();
        Callable<String> one_one = () -> handle("烧水");
        Callable<String> one_two = () -> handle("洗菜");
        Callable<String> one_three = () -> handle("听歌");
        Callable<String> two_one = () -> handle("切菜");
        Callable<String> three_one = () -> handle("炒菜");
        dag.link(one_one, two_one);
        dag.link(one_two, two_one);
        dag.link(one_three, two_one);
        dag.link(two_one, three_one);
        dag.link(one_three, three_one);
        DAGTaskExecutorImpl executor = new DAGTaskExecutorImpl(Executors.newFixedThreadPool(3));
        Map<Callable, String> submit = executor.submit(dag);
        submit.forEach((k, v) -> System.out.println("result map value:" + v));

```

## TODO

- 添加超时中断
- 添加熔断功能
