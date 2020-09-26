package io.github.seeflood.advanced.concurrent.executor.dag;

import java.util.List;
import java.util.concurrent.Callable;

public interface DAGTask<R> extends Callable<R> {
    List<DAGTask<R>> then();

    void then(DAGTask<R> then);

    int from(DAGTask<R> from);

    int inDegree();
//
//    int removeFrom(DAGTask<R> task);

    Callable<R> getRawCallable();
}
