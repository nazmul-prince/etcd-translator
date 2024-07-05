package com.bdris.etcdtranslator.service;

import io.etcd.jetcd.Client;
import io.etcd.jetcd.watch.WatchResponse;

import java.util.Map;
import java.util.function.Consumer;

public interface EtcdClient {
    String getByKey(String key);
    Map<String, String> getByKeyPrefix(String key);
    void watchByKeyPrefix(String keyToWatch);
    void watchByKeyPrefix(String keyToWatch, Consumer<WatchResponse> consumer);
    void watchByKey(String keyToWatch, Consumer<WatchResponse> consumer);
    void testWatchAndGet();

    void stopWatcher();
}
