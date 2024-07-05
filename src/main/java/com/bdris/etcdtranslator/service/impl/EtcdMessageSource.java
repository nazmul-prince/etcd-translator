package com.bdris.etcdtranslator.service.impl;

import com.bdris.etcdtranslator.service.EtcdClient;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractMessageSource;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EtcdMessageSource extends AbstractMessageSource {
    private Logger log = LoggerFactory.getLogger(EtcdMessageSource.class);
    private final ExecutorService etcdLongBlockingThreadPoolTaskExecutor;
    private final EtcdClient client;
    private final Set<Locale> availableLocales = new HashSet<>();
    private final Map<Locale, Map<String, String>> messagesCache = new ConcurrentHashMap<>();
    private final Map<Locale, String> localeWiseBaseDirs = new ConcurrentHashMap<>();
    private final Locale defaultLocale = Locale.US;
    private final String[] dirs;
    private final String baseDir;
    private final String baseDirToWatch;
    private final String localesKey;
    private final String defaultLocalesStr = "bn,en";

    public EtcdMessageSource(String[] hosts, String port, EtcdClient client, String baseDir, String localesKey, String baseDirToWatch, ExecutorService etcdLongBlockingThreadPoolTaskExecutor) {
        this.client = client;
        this.baseDirToWatch = baseDirToWatch;
        this.localesKey = localesKey;
        this.dirs = new String[0];
        this.baseDir = baseDir;
        this.etcdLongBlockingThreadPoolTaskExecutor = etcdLongBlockingThreadPoolTaskExecutor;

        initiateLoadingMessages();
    }

    private void initiateLoadingMessages() {
        loadAvailableLocals();
        loadLocalWiseBaseDirs();
        loadMessages();
//        watchForChanges();
    }

    @PostConstruct
    public void startWatching() {
        final Consumer<WatchResponse> consumer = watchResponse -> {
            boolean anyMatch = watchResponse.getEvents()
                    .stream()
                    .anyMatch(watchEvent -> Objects.equals(watchEvent.getEventType(), WatchEvent.EventType.PUT)
                            || Objects.equals(watchEvent.getEventType(), WatchEvent.EventType.DELETE));

            if(anyMatch) {
                log.info("reload messages");
                CompletableFuture.runAsync(() -> {
                    reloadMessages();
                }, etcdLongBlockingThreadPoolTaskExecutor);
            }
        };
        log.info("Will start the watcher on dir: " + baseDir);
        client.watchByKeyPrefix(baseDir, consumer);
        //for demo purposes
//        client.testWatchAndGet();
    }

    @PreDestroy
    public void stopWatching() {
        log.info("Will stop the watcher on dir: " + baseDir);
        client.stopWatcher();
    }

    private void loadLocalWiseBaseDirs() {
        availableLocales.forEach(locale -> {
            String localeBase = baseDir + "/" + locale.getLanguage();
            localeWiseBaseDirs.put(locale, localeBase);
        });
    }

    private void loadMessages() {
        localeWiseBaseDirs.forEach((locale, dir) -> {
            Map<String, String> translations = client.getByKeyPrefix(dir);

            translations = translations.entrySet()
                    .stream()
                    .filter(e -> (!(e.getValue().isBlank() || Objects.equals("\'\'", e.getValue()))))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue
                    ));
            messagesCache.put(locale, translations);
        });
        log.info("successfully loaded translations");
    }

    private void watchForChanges() {
//        client.watchByKeyPrefix("/messages", consumer);
        client.testWatchAndGet();

    }

    private void loadAvailableLocals() {
        String locales = client.getByKey(localesKey);
        locales = (locales == null || locales.isBlank()) ? defaultLocalesStr :locales;
        Arrays.stream(locales.split(",")).forEach(lang -> availableLocales.add(new Locale(lang)));
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        Locale messageLocale = availableLocales.stream()
                .filter(al -> Objects.equals(al.getLanguage(), locale.getLanguage()))
                .findFirst()
                .get();

        String message = messagesCache.get(messageLocale)
                .get(code);

        return message != null ? new MessageFormat(message, locale) : null;
    }

    public void reloadMessages() {
        log.info("Reloading messages will clear all cache first");
        availableLocales.clear();
        localeWiseBaseDirs.clear();
        messagesCache.clear();
        client.stopWatcher();
        initiateLoadingMessages();
    }
}
