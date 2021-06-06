# Advanced-Concurrent
一个java的并发库。


## Feature
### 一、图线程池
#### 任务集合可以抽象成图
并发编程中，经常面对一些可以并发执行的任务集合，我们可以把这些任务集合抽象成图，以便简化编程。
例如某pipeline中，任务之间没有互相先后顺序依赖，可以抽象成以下的任务关系图，图的点代表任务，边代表依赖关系。对于这种任务之间没有任何依赖关系的图，直接把所有任务放到线程池ExecutorService执行即可。

![image.png](https://upload-images.jianshu.io/upload_images/8926363-ed6f2b9dddcc8c94.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


java的线程池ExecutorService适合提交互相之间没有依赖的任务，如果任务之间有依赖，就不能简单的调用ExecutorService.submit()了，可能出现死锁、饥饿等情况。
例如有一组做菜任务，任务图如下,其中烧水/洗菜/听歌可以并行，烧水/洗菜/听歌都做好了才能切菜，切菜完成了才能炒菜。

 ![image.png](https://upload-images.jianshu.io/upload_images/8926363-83b487d5c655f3e1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
 
如果用ExecutorService处理这组任务会很痛苦。

#### 图线程池
为了任务图的并发编程，我写了个图线程池，见https://github.com/seeflood/Advanced-Concurrent/blob/master/src/test/java/io/github/seeflood/advanced/concurrent/executor/dag/DAGTaskExecutorImplTest.java

例如使用图线程池执行上述做菜任务，只需要构造好DAG，扔到线程池里执行即可，代码如下：
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
