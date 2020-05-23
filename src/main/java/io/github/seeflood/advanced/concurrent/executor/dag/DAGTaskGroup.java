package io.github.seeflood.advanced.concurrent.executor.dag;

import java.util.*;
import java.util.concurrent.Callable;

public class DAGTaskGroup<R> {
    private List<DAGTask<R>> adjacencyList = new ArrayList<>();

    public DAGTask<R> newDAGTask(Callable<R> callable) {
        DAGTask<R> task = new DefaultDAGTask<>(callable);
        adjacencyList.add(task);
        return task;
    }

    public void link(DAGTask<R> from, DAGTask<R> to) {
        if (from == null || to == null) {
            throw new NullPointerException();
        }
        from.then(to);
    }

    public boolean isEmpty() {
        return adjacencyList.isEmpty();
    }

    public SortedDAGTaskGroup<R> topologicalSort() {
        Queue<DAGTask<R>> q = new ArrayDeque<>();
        Map<DAGTask<R>, Integer> in = new HashMap<>();
//        1. init indegree
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
