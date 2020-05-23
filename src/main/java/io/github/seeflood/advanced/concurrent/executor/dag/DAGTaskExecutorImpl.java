package io.github.seeflood.advanced.concurrent.executor.dag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class DAGTaskExecutorImpl implements DAGTaskExecutor {
    private ExecutorService executorService;

    public DAGTaskExecutorImpl(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public <R> Map<Callable, R> submit(DAGTaskGroup<R> dag) throws InterruptedException, ExecutionException {
        if (dag == null || dag.isEmpty()) {
            throw new IllegalArgumentException("DAG should not be empty.");
        }
        //  1.topological sort
        SortedDAGTaskGroup<R> sortedDAG=dag.topologicalSort();
        List<DAGTask<R>> head = sortedDAG.head();
        if (head == null || head.isEmpty() || sortedDAG.hasCycle()) {
            throw new IllegalArgumentException("DAG should not have cycle.");
        }
        //  2.execute
        Map<Callable, R> result = execute(sortedDAG, head);
        return result;
    }

    private <R> Map<Callable, R> execute(SortedDAGTaskGroup<R> dag, List<DAGTask<R>> head) throws InterruptedException, ExecutionException {
        Map<Callable, R> result = new ConcurrentHashMap<>();
        CompletionService<ResultWrapper<R>> cs = new ExecutorCompletionService<>(executorService);
        List<Future<ResultWrapper<R>>> futures = new ArrayList<>();
        boolean done = false;

        try {
//            1. add head
            for (DAGTask<R> task : head) {
                Future<ResultWrapper<R>> future = cs.submit(newDagTaskWrapper(task));
                futures.add(future);
            }
            //   2.loop take and find next tasks
            while (!dag.allFinished()) {
                ResultWrapper<R> wrapper = cs.take().get();
                result.put(wrapper.task, wrapper.result);
                // find next tasks
                List<DAGTask<R>> nextTasks = dag.finish(wrapper.task);
                if (nextTasks != null && !nextTasks.isEmpty()) {
                    for (DAGTask<R> nextTask : nextTasks) {
                        Future<ResultWrapper<R>> f = cs.submit(newDagTaskWrapper(nextTask));
                        futures.add(f);
                    }
                }
            }
//            3.1. finish
            done = true;
        } finally {
//            3.2. not finished,cancel all
            if (!done) {
                for (Future<ResultWrapper<R>> f : futures) {
                    f.cancel(true);
                }
            }
        }
        return result;
    }

    private <R> Callable<ResultWrapper<R>> newDagTaskWrapper(DAGTask<R> task) {
        return new DAGTaskWrapper<>(task);
    }

    private static class ResultWrapper<R> {
        public DAGTask<R> task;
        public R result;

        public ResultWrapper(DAGTask<R> task, R result) {
            this.task = task;
            this.result = result;
        }
    }

    private static class DAGTaskWrapper<R> implements Callable<ResultWrapper<R>> {
        private DAGTask<R> task;

        public DAGTaskWrapper(DAGTask<R> task) {
            this.task = task;
        }

        @Override
        public ResultWrapper<R> call() throws Exception {
            R r = task.call();
            ResultWrapper<R> wrapper = new ResultWrapper<>(task, r);
            return wrapper;
        }
    }
}
