package com.bdris.etcdtranslator.service.impl;

import com.bdris.etcdtranslator.service.EtcdClient;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import io.etcd.jetcd.watch.WatchEvent.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EtcdClientImpl implements EtcdClient {
    private final Logger log = LoggerFactory.getLogger(EtcdClientImpl.class);
    private Client etcdClient;
    Watch.Watcher watcher;

    private final ExecutorService etcdLongBlockingThreadPoolTaskExecutor;

    public EtcdClientImpl(String[] hosts, String port, ExecutorService taskExecutor, ExecutorService etcdLongBlockingThreadPoolTaskExecutor) {
        this.etcdLongBlockingThreadPoolTaskExecutor = etcdLongBlockingThreadPoolTaskExecutor;
        this.etcdClient = Client.builder().endpoints(hosts).executorService(taskExecutor).build();
    }

    @Override
    public String getByKey(String key) {
        GetOption option = GetOption.builder()
                .isPrefix(false)
                .build();
        List<KeyValue> kvs = get(option, key);

        return ( kvs.isEmpty() ? null : kvs.get(0).toString());
    }

    @Override
    public Map<String, String> getByKeyPrefix(String key) {
        GetOption option = GetOption.builder()
                .isPrefix(true)
                .build();
        List<KeyValue> kvs = get(option, key);

        Map<String, String> keyValues = kvs.stream()
                .collect(Collectors.toMap(
                        keyValue -> keyValue.getKey().toString(),
                        keyValue1 -> keyValue1.getValue().toString())
                );
        return keyValues;
    }

    @Override
    public void watchByKeyPrefix(String keyToWatch) {

    }

    @Override
    public void testWatchAndGet() {
        final ByteSequence key = ByteSequence.from("rand".getBytes());
        final ByteSequence value = ByteSequence.from("rand".getBytes());
        WatchOption watchOption = WatchOption.builder().build();
        // Configure Vert.x to use the custom executor service
        final Consumer<WatchResponse> consumer = response -> {
            for (WatchEvent event : response.getEvents()) {
                if (event.getEventType() == EventType.PUT) {
                    executeLongBlockingTasks();
//                    client.getKVClient().get(key1).whenComplete((r, t) -> {
//                        if (!r.getKvs().isEmpty()) {
//                            ref.set(r.getKvs().get(0));
//                        }
//                    });
                }
            }
        };

        watcher = etcdClient.getWatchClient().watch(key, consumer);

    }

    private void executeLongBlockingTasks() {
        CompletableFuture.runAsync(() -> {

            try {
                log.info("sleeping for few secs in another thread");
                Thread.sleep(50000);
            } catch (InterruptedException e) {
                log.info("inturrppted");
//                        throw new RuntimeException(e);
            }
        }, etcdLongBlockingThreadPoolTaskExecutor);
    }

    @Override
    public void watchByKeyPrefix(String keyToWatch, Consumer<WatchResponse> consumer) {
        WatchOption watchOption = WatchOption.builder()
                .isPrefix(true)
                .build();
        watch(keyToWatch, watchOption, consumer);
    }

    @Override
    public void watchByKey(String keyToWatch, Consumer<WatchResponse> consumer) {
        WatchOption watchOption = WatchOption.builder()
                .isPrefix(false)
                .build();
        watch(keyToWatch, watchOption, consumer);
    }

    @Override
    public void stopWatcher() {
        if(watcher != null) {
            watcher.close();
        }
    }

    private void watch(String key, WatchOption watchOption, Consumer<WatchResponse> consumer) {
        Watch watchClient = etcdClient.getWatchClient();
        watcher = watchClient.watch(ByteSequence.from(key.getBytes(StandardCharsets.UTF_8)),
                watchOption,
                consumer);
    }

    private List<KeyValue> get(GetOption option, String key) {
        List<KeyValue> kvs= new ArrayList<>();
        KV kvClient = etcdClient.getKVClient();

        byte[] bytes = key.getBytes();

        ByteSequence keyByteSequence = ByteSequence.from(bytes);
        try {
            GetResponse response = kvClient.get(keyByteSequence, option)
                    .exceptionally(e -> {
                        log.error("error while getting key with : " + key);
                        return null;
                    })
                    .get();
            kvs = response.getKvs();
        } catch (InterruptedException e) {
            log.error("Jetcd interrupted");
        } catch (ExecutionException e) {
            log.error("Jetcd execution got problem");
        }

        return kvs;
    }
}
