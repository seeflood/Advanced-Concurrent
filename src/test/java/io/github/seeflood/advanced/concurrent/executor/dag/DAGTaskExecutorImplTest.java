package io.github.seeflood.advanced.concurrent.executor.dag;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class DAGTaskExecutorImplTest {

    @org.junit.Test
    public void example1() throws ExecutionException, InterruptedException {
//        Example 1:假设有一组做菜任务，任务之间的拓扑关系如下
//        其中烧水/洗菜/听歌可以并行，烧水/洗菜/听歌都做好了才能切菜，切菜完成了才能炒菜
        /*
         * 烧水---->切菜---->炒菜
         * 洗菜-----|      |
         * 听歌----|------|
         * */
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
    }


    @org.junit.Test
    public void example2() throws ExecutionException, InterruptedException {
//        Example 2:Assume a group of tasks to print strings in such topological order:
        /*
         * 1.1---->2.1---->3.1
         * 1.2-----|      |
         * 1.3----|------|
         * */
        DAGTaskGroup<String> dag = new DAGTaskGroup<>();
        Callable<String> one_one = () -> handle("1.1");
        Callable<String> one_two = () -> handle("1.2");
        Callable<String> one_three = () -> handle("1.3");
        Callable<String> two_one = () -> handle("2.1");
        Callable<String> three_one = () -> handle("3.1");
        dag.link(one_one, two_one);
        dag.link(one_two, two_one);
        dag.link(one_three, two_one);
        dag.link(two_one, three_one);
        dag.link(one_three, three_one);
        DAGTaskExecutorImpl executor = new DAGTaskExecutorImpl(Executors.newFixedThreadPool(3));
        Map<Callable, String> submit = executor.submit(dag);
        submit.forEach((k, v) -> System.out.println("result map value:" + v));
    }


    @org.junit.Test
    public void example3() throws ExecutionException, InterruptedException {
        /*
         * 1.1---->2.1---->3.1
         * 1.2-----|      |
         * 1.3----|------|
         * */
        DAGTaskGroup<String> dag = new DAGTaskGroup<>();
        DAGTask<String> oneone = dag.newDAGTask(() -> handle("1.1"));
        DAGTask<String> onetwo = dag.newDAGTask(() -> handle("1.2"));
        DAGTask<String> onethree = dag.newDAGTask(() -> handle("1.3"));
        DAGTask<String> twoone = dag.newDAGTask(() -> handle("2.1"));
        DAGTask<String> threeone = dag.newDAGTask(() -> handle("3.1"));
        dag.link(oneone, twoone);
        dag.link(onetwo, twoone);
        dag.link(onethree, twoone);
        dag.link(twoone, threeone);
        dag.link(onethree, threeone);
        DAGTaskExecutorImpl executor = new DAGTaskExecutorImpl(Executors.newFixedThreadPool(3));
        Map<Callable, String> submit = executor.submit(dag);
        submit.forEach((k, v) -> System.out.println("result map value:" + v));
    }

    @org.junit.Test
    public void submit1000Times() throws ExecutionException, InterruptedException {
        /*
         * 1.1---->2.1---->3.1
         * 1.2-----|      |
         * 1.3----|------|
         * */
        DAGTaskGroup<String> dag = new DAGTaskGroup<>();
        DAGTask<String> oneone = dag.newDAGTask(() -> handle("1.1"));
        DAGTask<String> onetwo = dag.newDAGTask(() -> handle("1.2"));
        DAGTask<String> onethree = dag.newDAGTask(() -> handle("1.3"));
        DAGTask<String> twoone = dag.newDAGTask(() -> handle("2.1"));
        DAGTask<String> threeone = dag.newDAGTask(() -> handle("3.1"));
        dag.link(oneone, twoone);
        dag.link(onetwo, twoone);
        dag.link(onethree, twoone);
        dag.link(twoone, threeone);
        dag.link(onethree, threeone);
        DAGTaskExecutorImpl executor = new DAGTaskExecutorImpl(Executors.newFixedThreadPool(3));
        for (int i = 0; i < 1000; i++) {
            Map<Callable, String> submit = executor.submit(dag);
        }
    }


    private String handle(String s) {
        System.out.println(s);
        return s;
    }
}