package io.github.seeflood.advanced.concurrent.executor.dag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SortedDAGTaskGroup<R> extends DAGTaskGroup<R> {
    private boolean hasCycle;
    private List<DAGTask<R>> head;
    private int DAGSize;
    private final ConcurrentHashMap<DAGTask<R>, Object> finished = new ConcurrentHashMap<>();
    private final Map<DAGTask<R>, Integer> indegree = new ConcurrentHashMap<>();
    private final Object dummy = new Object();

    public SortedDAGTaskGroup(boolean hasCycle, List<DAGTask<R>> head, int DAGSize) {
        this.hasCycle = hasCycle;
        this.head = head;
        this.DAGSize = DAGSize;
    }

    public boolean hasCycle() {
        return this.hasCycle;
    }

    public List<DAGTask<R>> head() {
        return this.head;
    }

    public List<DAGTask<R>> finish(DAGTask<R> task) {
        if (finished.contains(task)) {
            throw new IllegalArgumentException("Can not finish same task twice.");
        }
//        do finishing
        finished.put(task, dummy);
        List<DAGTask<R>> result = new ArrayList<>();
//        check neighbours
        List<DAGTask<R>> nextTasks = task.then();
        for (DAGTask<R> nextT : nextTasks) {
                int in;
            if(!indegree.containsKey(nextT)){
                in = nextT.inDegree()-1;
            }else{
                in=indegree.get(nextT)-1;
            }
            indegree.put(nextT,in);
            if (in == 0) {
                result.add(nextT);
            }
        }
        return result;
    }

    public boolean allFinished() {
        return finished.size() == DAGSize;
    }
}
