package io.etcd.springi18n.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Configuration class for setting up thread pools for etcd operations.
 */
@Configuration
public class EtcdThreadPoolConfiguration {

    /**
     * Core pool size for the main etcd thread pool.
     */
    @Value("${etcd.server.connection.corePoolSize:2500}")
    private int corePoolSize;

    /**
     * Maximum pool size for the main etcd thread pool.
     */
    @Value("${etcd.server.connection.maxPoolSize:3000}")
    private int maxPoolSize;

    /**
     * Queue capacity for the main etcd thread pool.
     */
    @Value("${etcd.server.connection.queueCapacity:1000}")
    private int queueCapacity;

    /**
     * Thread name prefix for the main etcd thread pool.
     */
    @Value("${etcd.server.connection.threadNamePrefix:etcd-conn-}")
    private String threadNamePrefix;

    /**
     * Core pool size for the blocking etcd thread pool.
     */
    @Value("${etcd.server.connection.corePoolSize:10}")
    private int blockingCorePoolSize;

    /**
     * Maximum pool size for the blocking etcd thread pool.
     */
    @Value("${etcd.server.connection.maxPoolSize:20}")
    private int blockingMaxPoolSize;

    /**
     * Queue capacity for the blocking etcd thread pool.
     */
    @Value("${etcd.server.connection.queueCapacity:20}")
    private int blockingQueueCapacity;

    /**
     * Thread name prefix for the blocking etcd thread pool.
     */
    @Value("${etcd.server.connection.threadNamePrefix:etcd-blocking-}")
    private String blockingThreadNamePrefix;

    /**
     * Creates a bean for the main etcd thread pool executor service.
     *
     * @return an {@link ExecutorService} for the main etcd operations
     */
    @Bean(name = "etcdThreadPoolTaskExecutor")
    public ExecutorService etcdThreadPoolTaskExecutor() {
        ThreadFactory threadFactory = new CustomThreadFactory(threadNamePrefix);
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                10L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                threadFactory
        );
    }

    /**
     * Creates a bean for the blocking etcd thread pool executor service.
     *
     * @return an {@link ExecutorService} for the long-running blocking etcd operations
     */
    @Bean(name = "etcdLongBlockingThreadPoolTaskExecutor")
    public ExecutorService etcdLongBlockingThreadPoolTaskExecutor() {
        ThreadFactory threadFactory = new CustomThreadFactory(blockingThreadNamePrefix);
        return new ThreadPoolExecutor(
                blockingCorePoolSize,
                blockingMaxPoolSize,
                10L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(blockingQueueCapacity),
                threadFactory
        );
    }

    /**
     * Custom thread factory for naming threads in the thread pool.
     */
    static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        /**
         * Constructs a CustomThreadFactory with the given name prefix.
         *
         * @param namePrefix the prefix for thread names
         */
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
