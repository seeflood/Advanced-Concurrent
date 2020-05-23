package io.github.seeflood.advanced.concurrent.executor.dag;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultDAGTask<R> implements DAGTask<R> {

    Callable<R> callable;
    List<DAGTask<R>> nextTasks = new ArrayList<>();
    AtomicInteger indegree = new AtomicInteger(0);

    protected DefaultDAGTask(Callable<R> callable) {
        this.callable = callable;
    }

    @Override
    public List<DAGTask<R>> then() {
        return nextTasks;
    }

    public void then(DAGTask<R> then) {
        nextTasks.add(then);
        then.from(this);
    }

    @Override
    public int from(DAGTask<R> from) {
        return indegree.incrementAndGet();
    }

    @Override
    public int inDegree() {
        return indegree.get();
    }

    @Override
    public int removeFrom(DAGTask<R> task) {
        return indegree.decrementAndGet();
    }

    public R call() throws Exception {
        return callable.call();
    }
}
