package io.github.seeflood.advanced.concurrent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureUtils {

    /**
     * 在指定时间内取future结果，超时则取消future并返回。超时的future在返回的map中不存在
     *
     * @param futures
     * @param timeout
     * @param unit
     * @param <R>
     * @return
     */
    public static <R> Map<Future, R> get(List<Future<R>> futures, long timeout, TimeUnit unit) throws ExecutionException, InterruptedException {
        Map<Future, R> result = new HashMap<>();
        long nanos = unit.toNanos(timeout);
        long deadline = System.nanoTime() + nanos;
        boolean done = false;
        try {
            for (Future<R> future : futures) {
                if (future.isDone()) {
                    result.put(future, future.get());
                } else if (nanos > 0L) {
                    try {
                        R r = future.get(nanos, TimeUnit.NANOSECONDS);
                        result.put(future, r);
                    } catch (TimeoutException ignore) {
                    } finally {
                        future.cancel(true);
                    }
                } else {
                    //   won't try to get result if timeout
                    future.cancel(true);
                }
            }
            done = true;
        } finally {
            if (!done) {
                for (Future<R> future : futures) {
                    future.cancel(true);
                }
            }
        }
        return result;
    }
}
