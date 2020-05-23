package io.github.seeflood.advanced.concurrent.executor.dag;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class DAGTaskExecutorImplTest {

    @org.junit.Test
    public void submit() throws ExecutionException, InterruptedException {
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
        submit.forEach((k, v) -> System.out.println("result map value:"+v));
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