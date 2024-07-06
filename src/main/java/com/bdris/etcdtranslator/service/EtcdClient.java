package com.bdris.etcdtranslator.service;

import io.etcd.jetcd.Client;
import io.etcd.jetcd.watch.WatchResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public interface EtcdClient {

    /**
     * Retrieves the value associated with the specified key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value associated with the specified key, or {@code null} if the key does not exist
     * @throws ExecutionException   if the computation threw an exception
     * @throws InterruptedException if the current thread was interrupted while waiting
     */
    String getByKey(String key) throws ExecutionException, InterruptedException;

    /**
     * Retrieves the key-value pairs associated with the specified key asynchronously.
     *
     * @param key      the key whose associated key-value pairs are to be returned
     * @param isPrefix whether the key is a prefix
     * @return a {@link CompletableFuture} that will be completed with the key-value pairs
     */
    CompletableFuture<Map<String, String>> getByKeyAsync(String key, boolean isPrefix);

    /**
     * Retrieves the key-value pairs associated with the specified key prefix asynchronously.
     *
     * @param key the key prefix whose associated key-value pairs are to be returned
     * @return a {@link CompletableFuture} that will be completed with the key-value pairs
     */
    CompletableFuture<Map<String, String>> getByKeyPrefixAsync(String key);

    /**
     * Watches for changes on the specified key prefix and processes the changes using the provided consumer.
     *
     * @param keyToWatch the key prefix to watch
     * @param consumer   the consumer to process the watch response
     */
    void watchByKeyPrefix(String keyToWatch, Consumer<WatchResponse> consumer);

    void testWatchAndGet();

    void stopWatcher();

    EtcdClient create(String[] hosts, String port, ExecutorService taskExecutor, ExecutorService etcdLongBlockingThreadPoolTaskExecutor);
}
