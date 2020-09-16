

package io.jingwei.wallet.biz.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * An service that should execute a task in an Asynchronous manner.
 *
 */
public interface AsyncTaskService {

    void execute(String executorName, Runnable task);

    CompletableFuture<Void> executeWithCompletableFuture(String executorName, Runnable task);

    <T> Future<T> submit(String executorName, Callable<T> task);
}
