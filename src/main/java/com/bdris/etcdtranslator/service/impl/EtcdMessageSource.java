package com.bdris.etcdtranslator.service.impl;

import com.bdris.etcdtranslator.service.EtcdClient;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.AbstractMessageSource;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * EtcdMessageSource is responsible for managing translation resources
 * fetched from an etcd key-value store. It watches for changes in the
 * translation keys and updates the translation cache accordingly.
 */
@Slf4j
public class EtcdMessageSource extends AbstractMessageSource {
    /**
     * Executor service for long-running etcd tasks.
     */
    private final ExecutorService etcdLongBlockingThreadPoolTaskExecutor;

    /**
     * Client interface to interact with etcd.
     */
    private final EtcdClient client;

    /**
     * Set of available locales for translations.
     */
    private final Set<Locale> availableLocales = new HashSet<>();

    /**
     * Map for storing base directories for each locale.
     */
    private final Map<Locale, String> localeWiseBaseDirs = new ConcurrentHashMap<>();

    /**
     * Cache for storing translation messages, organized by locale.
     */
    private final Map<Locale, Map<String, String>> messagesCache = new ConcurrentHashMap<>();

    /**
     * Root directory for messages or translations.
     */
    private final String baseDir;

    /**
     * Key prefix to watch in etcd for changes.
     */
    private final String baseDirToWatch;

    /**
     * Key where the locales are defined as a comma-separated string.
     */
    private final String localesKey;

    /**
     * Default locales string, used if localesKey is not set.
     */
    private final String defaultLocalesStr = "bn,en";

    /**
     * Flag indicating whether to use an asynchronous approach for loading messages.
     */
    private boolean loadMessageWithAsyncApproach = true;

    //for demo purposes
    AtomicInteger atomicInteger = new AtomicInteger(0);

    /**
     * Constructs an instance of EtcdMessageSource.
     *
     * <p>This constructor initializes the EtcdMessageSource with the specified parameters to fetch and watch translation resources from an etcd key-value store.</p>
     *
     * @param client                                 the {@link EtcdClient} interface to interact with etcd for key-value fetching and watching through a watcher
     * @param baseDir                                the root directory in etcd where message or translation resources are stored
     * @param localesKey                             the key in etcd where locales are defined, typically as a comma-separated string
     * @param baseDirToWatch                         the key prefix in etcd to watch for changes in translation resources
     * @param etcdLongBlockingThreadPoolTaskExecutor the {@link ExecutorService} used for long-running tasks, such as fetching or watching keys
     * @param loadMessageWithAsyncApproach           a boolean flag indicating whether to load messages using an asynchronous approach (true) or a synchronous approach (false)
     */
    public EtcdMessageSource(EtcdClient client, String baseDir, String localesKey, String baseDirToWatch, ExecutorService etcdLongBlockingThreadPoolTaskExecutor, boolean loadMessageWithAsyncApproach) {
        this.client = client;
        this.baseDirToWatch = baseDirToWatch;
        this.localesKey = localesKey;
        this.baseDir = baseDir;
        this.etcdLongBlockingThreadPoolTaskExecutor = etcdLongBlockingThreadPoolTaskExecutor;
        this.loadMessageWithAsyncApproach = loadMessageWithAsyncApproach;

        initiateLoadingMessagesAsync();
    }

    @PostConstruct
    private void startWatching() {
        final Consumer<WatchResponse> consumer = generateConsumer();
        log.info("Will start the watcher on dir: " + baseDirToWatch);
        client.watchByKeyPrefix(baseDirToWatch, consumer);
        //for demo purposes
//        client.testWatchAndGet();
    }

    private Consumer<WatchResponse> generateConsumer() {
        return watchResponse -> {
            boolean anyMatch = watchResponse.getEvents()
                    .stream()
                    .anyMatch(watchEvent -> Objects.equals(watchEvent.getEventType(), WatchEvent.EventType.PUT)
                            || Objects.equals(watchEvent.getEventType(), WatchEvent.EventType.DELETE));

            if (anyMatch) {
                log.info("reloading messages for: " + atomicInteger.getAndIncrement());
                if (loadMessageWithAsyncApproach) {
                    reloadMessagesAsync();
                } else {
                    CompletableFuture.runAsync(this::reloadMessages, etcdLongBlockingThreadPoolTaskExecutor);
                }

            }
        };
    }

    @PreDestroy
    private void stopWatching() {
        log.info("Will stop the watcher on dir: " + baseDir);
        client.stopWatcher();
    }

    public void reloadMessagesAsync() {
        log.info("Reloading messages with async approach will clear all cache first");
        availableLocales.clear();
        localeWiseBaseDirs.clear();
        messagesCache.clear();
        initiateLoadingMessagesAsync();
    }

    public void reloadMessages() {
        log.info("Reloading messages with synch approach will clear all cache first");
        availableLocales.clear();
        localeWiseBaseDirs.clear();
        messagesCache.clear();
        initiateLoadingMessages();
    }

    private void initiateLoadingMessagesAsync() {
        loadAvailableLocalsAsync().thenRunAsync(() -> loadLocalWiseBaseDirs(), etcdLongBlockingThreadPoolTaskExecutor).whenCompleteAsync((unused, throwable) -> {
            if (throwable == null) {
                loadMessagesAsync();
            }
        }, etcdLongBlockingThreadPoolTaskExecutor);
    }

    private void initiateLoadingMessages() {
        loadAvailableLocales();
        loadLocalWiseBaseDirs();
        loadMessages();
    }


    private CompletableFuture<Void> loadAvailableLocalsAsync() {
        return client.getByKeyAsync(localesKey, false).thenAcceptAsync(kvPairs -> {
            loadLocales(kvPairs);
        }, etcdLongBlockingThreadPoolTaskExecutor);
    }

    private void loadMessagesAsync() {
        localeWiseBaseDirs.forEach((locale, dir) -> {
            client.getByKeyAsync(dir, true).whenCompleteAsync((kvPairs, throwable) -> {
                saveMessagesToCache(kvPairs, locale);
                log.info("successfully loaded translations for locale: " + locale.getLanguage());
            }, etcdLongBlockingThreadPoolTaskExecutor);
        });
    }

    private void loadAvailableLocales() {
        Map<String, String> kvPairs = Map.of();

        try {
            kvPairs = client.getByKeyAsync(localesKey, false).get();
        } catch (InterruptedException | ExecutionException e) {
            errorMessageOnGettingKeyValue(localesKey, false, e);
        }
        loadLocales(kvPairs);
    }

    private void loadLocales(Map<String, String> kvPairs) {
        String locales = kvPairs.values().stream().findFirst().orElse(defaultLocalesStr);
        Arrays.stream(locales.split(",")).forEach(lang -> availableLocales.add(new Locale(lang)));
    }

    private void loadLocalWiseBaseDirs() {
        availableLocales.forEach(locale -> {
            String localeBase = baseDir + "/" + locale.getLanguage();
            localeWiseBaseDirs.put(locale, localeBase);
        });
    }

    private void loadMessages() {
        localeWiseBaseDirs.forEach((locale, dir) -> {
            Map<String, String> kvPairs = Map.of();
            try {
                kvPairs = client.getByKeyAsync(dir, true).get();
            } catch (InterruptedException | ExecutionException e) {
                errorMessageOnGettingKeyValue(dir, true, e);
            }
            saveMessagesToCache(kvPairs, locale);
        });
        log.info("successfully loaded translations");
    }

    private void saveMessagesToCache(Map<String, String> kvPairs, Locale locale) {

        kvPairs = kvPairs.entrySet().stream().filter(e -> (!(e.getValue().isBlank() || Objects.equals("\'\'", e.getValue())))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        messagesCache.put(locale, kvPairs);
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        Locale messageLocale = availableLocales.stream().filter(al -> Objects.equals(al.getLanguage(), locale.getLanguage())).findFirst().get();

        String message = messagesCache.get(messageLocale).get(code);

        return message != null ? new MessageFormat(message, locale) : null;
    }

    private void errorMessageOnGettingKeyValue(String key, boolean isPrefix, Exception e) {
        log.error("Error while getting key " + key + " isPrefix: " + isPrefix, e);
    }

}
