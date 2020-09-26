package io.github.seeflood.advanced.concurrent.executor.dag;

import java.util.*;
import java.util.concurrent.Callable;

public class DAGTaskGroup<R> {
    //    private List<DAGTask<R>> adjacencyList = new ArrayList<>();
    private final Map<Callable<R>, DAGTask<R>> callable2Task = new HashMap<>();

    public DAGTask<R> newDAGTask(Callable<R> callable) {
        if (callable2Task.containsKey(callable)) {
            return callable2Task.get(callable);
        }
        DAGTask<R> task = new DefaultDAGTask<>(callable);
        callable2Task.put(callable, task);
//        adjacencyList.add(task);
        return task;
    }

    public void link(DAGTask<R> from, DAGTask<R> to) {
        if (from == null || to == null) {
            throw new NullPointerException();
        }
        Callable<R> callableFrom = from.getRawCallable();
        if (!callable2Task.containsKey(callableFrom)) {
            callable2Task.put(callableFrom, from);
        }
        Callable<R> callableTo = to.getRawCallable();
        if (!callable2Task.containsKey(callableTo)) {
            callable2Task.put(callableTo, to);
        }
        from.then(to);
    }

    public void link(Callable<R> from, Callable<R> to) {
        if (from == null || to == null) {
            throw new NullPointerException();
        }
        DAGTask<R> fromTask;
        if (callable2Task.containsKey(from)) {
            fromTask = callable2Task.get(from);
        } else {
            fromTask = newDAGTask(from);
        }
        DAGTask<R> toTask;
        if (callable2Task.containsKey(to)) {
            toTask = callable2Task.get(to);
        } else {
            toTask = newDAGTask(to);
        }
        fromTask.then(toTask);
    }

    public boolean isEmpty() {
        return callable2Task.isEmpty();
    }

    public SortedDAGTaskGroup<R> topologicalSort() {
        Queue<DAGTask<R>> q = new ArrayDeque<>();
        Map<DAGTask<R>, Integer> in = new HashMap<>();
//        1. init indegree
        Collection<DAGTask<R>> adjacencyList = callable2Task.values();
        for (DAGTask<R> task : adjacencyList) {
            int indegree = task.inDegree();
            in.put(task, indegree);
            if (indegree == 0) {
                q.add(task);
            }
        }
        List<DAGTask<R>> head = new ArrayList<>(q);
        int visited = 0;
        while (!q.isEmpty()) {
//            2. visit
            DAGTask<R> poll = q.poll();
            visited++;
//           3. check neighbours' indegree
            List<DAGTask<R>> nbs = poll.then();
            for (DAGTask<R> nb : nbs) {
                int nbIn = in.get(nb) - 1;
                in.put(nb, nbIn);
                if (nbIn == 0) {
                    q.add(nb);
                }
            }
        }
//        4. check has cycle
        boolean hasCycle = visited < adjacencyList.size();
        return new SortedDAGTaskGroup<>(hasCycle, head, adjacencyList.size());
    }
}
