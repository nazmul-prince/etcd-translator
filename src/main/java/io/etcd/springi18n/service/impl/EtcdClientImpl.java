package io.etcd.springi18n.service.impl;

import io.etcd.springi18n.service.EtcdClient;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

final class EtcdClientImpl implements EtcdClient {
    private final Logger log = LoggerFactory.getLogger(EtcdClientImpl.class);

    /**
     * Executor service for long-running etcd tasks.
     */
    private final ExecutorService etcdLongBlockingThreadPoolTaskExecutor;

    /**
     * Watcher to monitor etcd key changes.
     */
    public Watch.Watcher watcher;

    /**
     * Client to interact with etcd.
     */
    private Client etcdClient;

    /**
     * Constructs an instance of EtcdClientImpl.
     *
     * @param hosts                                  the etcd hosts to connect to, you can add the ports in hosts too, for example: http://ip:port,
     *                                               in that case port won't be concatenated.
     * @param port                                   the port number for etcd
     * @param taskExecutor                           the executor service for general tasks
     * @param etcdLongBlockingThreadPoolTaskExecutor the executor service for long-running etcd tasks
     */
    EtcdClientImpl(String[] hosts, String port, ExecutorService taskExecutor, ExecutorService etcdLongBlockingThreadPoolTaskExecutor) {
        this.etcdLongBlockingThreadPoolTaskExecutor = etcdLongBlockingThreadPoolTaskExecutor;
        
        if(!port.isEmpty())
            hosts = addPortToHosts(hosts, port);
        
        this.etcdClient = Client.builder().endpoints(hosts).maxInboundMessageSize(8 * 1024 * 1024).executorService(taskExecutor).build();
    }

    public EtcdClient create(String[] hosts, String port, ExecutorService taskExecutor, ExecutorService etcdLongBlockingThreadPoolTaskExecutor) {
        return new EtcdClientImpl(hosts,port,taskExecutor,etcdLongBlockingThreadPoolTaskExecutor);
    }

    private String[] addPortToHosts(String[] hosts, String port) {
        return Arrays.stream(hosts)
                .map(host -> host + ":" + port)
                .toArray(String[]::new);
    }

    @Override
    public String getByKey(String key) throws ExecutionException, InterruptedException {
        Map<String, String> kvPairs = getByKeyAsync(key, false).get();
        return kvPairs.values()
                .stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * Retrieves the key-value pairs associated with the specified key asynchronously.
     *
     * @param key      the key whose associated key-value pairs are to be returned
     * @param isPrefix whether the key is a prefix
     * @return a {@link CompletableFuture} that will be completed with the key-value pairs
     */
    @Override
    public CompletableFuture<Map<String, String>> getByKeyAsync(String key, boolean isPrefix) {
        GetOption option = GetOption.builder()
                .isPrefix(isPrefix)
                .build();
        return get(option, key).thenApplyAsync(getResponse -> {
                    List<KeyValue> kvs = getResponse == null ? List.of() : getResponse.getKvs();

                    return kvs.stream()
                            .collect(Collectors.toMap(
                                    keyValue -> keyValue.getKey().toString(),
                                    keyValue1 -> keyValue1.getValue().toString())
                            );
                }, etcdLongBlockingThreadPoolTaskExecutor)
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    log.error("Error while getting key " + key + " isPrefix: " + isPrefix, throwable);
                    return Map.of();
                });
    }

    @Override
    public CompletableFuture<Map<String, String>> getByKeyPrefixAsync(String key) {
        return getByKeyAsync(key, true);
    }

    /**
     * Retrieves the {@link GetResponse} for the specified key with the given options asynchronously.
     *
     * @param option the options to apply when fetching the key
     * @param key    the key whose associated {@link GetResponse} is to be returned
     * @return a {@link CompletableFuture} that will be completed with the {@link GetResponse}
     */
    private CompletableFuture<GetResponse> get(GetOption option, String key) {
        KV kvClient = etcdClient.getKVClient();

        ByteSequence keyByteSequence = ByteSequence.from(key.getBytes(StandardCharsets.UTF_8));
        return kvClient.get(keyByteSequence, option)
                .exceptionally(e -> {
                    log.error("error while getting key with : " + key + " " + e.getMessage(), e);
                    return null;
                });
    }

    @Override
    public void watchByKeyPrefix(String keyToWatch, Consumer<WatchResponse> consumer) {
        WatchOption watchOption = WatchOption.builder()
                .isPrefix(true)
                .build();
        watch(keyToWatch, watchOption, consumer);
    }

    /**
     * Watches for changes on the specified key with the given options and processes the changes using the provided consumer.
     *
     * @param key         the key to watch
     * @param watchOption the options to apply when watching the key
     * @param consumer    the consumer to process the watch response
     */
    private void watch(String key, WatchOption watchOption, Consumer<WatchResponse> consumer) {
        Consumer<Throwable> onError = e -> {
            log.error("error ");
        };

        Runnable onCompleted = () -> {
            log.info("Completed");
        };
        log.info("getting watcher client");
        Watch watchClient = etcdClient.getWatchClient();
        watcher = watchClient.watch(ByteSequence.from(key.getBytes(StandardCharsets.UTF_8)),
                watchOption,
                consumer,
                onError,
                onCompleted);
        log.info("started watching");
    }

    /**
     * Stops the watcher if it is running.
     */
    @Override
    public void stopWatcher() {
        if (watcher != null) {
            watcher.close();
        }
    }

    /**
     * Demonstrates watching a key and performing an action when the key is updated.
     */
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

    /**
     * Demonstrates the execution of a long blocking task asynchronously using the configured executor service.
     */
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
}
