package com.bdris.etcdtranslator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class EtcdThreadPoolConfiguration {
    @Value("${etcd.server.connection.corePoolSize:2500}")
    private int corePoolSize;
    @Value("${etcd.server.connection.maxPoolSize:3000}")
    private int maxPoolSize;
    @Value("${etcd.server.connection.queueCapacity:1000}")
    private int queueCapacity;
    @Value("${etcd.server.connection.threadNamePrefix:etcd-conn-}")
    private String threadNamePrefix;

    @Value("${etcd.server.connection.corePoolSize:10}")
    private int blockingCorePoolSize;
    @Value("${etcd.server.connection.maxPoolSize:20}")
    private int blockingMaxPoolSize;
    @Value("${etcd.server.connection.queueCapacity:20}")
    private int blockingQueueCapacity;
    @Value("${etcd.server.connection.threadNamePrefix:etcd-blocking-}")
    private String blockingThreadNamePrefix;

    @Bean(name = "etcdThreadPoolTaskExecutor")
    public ExecutorService etcdThreadPoolTaskExecutor() {
        ThreadFactory threadFactory = new EtcdThreadPoolConfiguration.CustomThreadFactory(threadNamePrefix);
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                10L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                threadFactory
        );
    }

    @Bean(name = "etcdLongBlockingThreadPoolTaskExecutor")
    public ExecutorService etcdLongBlockingThreadPoolTaskExecutor() {
        ThreadFactory threadFactory = new EtcdThreadPoolConfiguration.CustomThreadFactory(threadNamePrefix);
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                10L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                threadFactory
        );
    }

    static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        CustomThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
