package io.github.seeflood.advanced.concurrent.executor.dag;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public interface DAGTaskExecutor {

    <R> Map<Callable, R> submit(DAGTaskGroup<R> dag) throws InterruptedException, ExecutionException;

}
