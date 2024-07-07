package io.etcd.springi18n.config;

import io.etcd.springi18n.service.EtcdClient;
import io.etcd.springi18n.service.impl.EtcdClientFactory;
import io.etcd.springi18n.service.impl.EtcdMessageSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Configuration class for setting up etcd client and message source beans.
 */
@Configuration
//@ConfigurationProperties(prefix = "etcd.server")
public class EtcdTranslatorconfiguration {

    /**
     * Executor service for long-running blocking etcd tasks.
     */
    private final ExecutorService etcdLongBlockingThreadPoolTaskExecutor;
    /**
     * Executor service for regular etcd tasks.
     */
    private final ExecutorService etcdThreadPoolTaskExecutor;
    /**
     * List of etcd server hosts.
     */
    @Value("${etcd.server.hosts:http://localhost}")
    private List<String> hosts = new ArrayList<>();
    /**
     * Port for the etcd server.
     */
    @Value("${etcd.server.port:2379}")
    private String port;
    /**
     * List of directories for messages.
     */
    private List<String> dirs = List.of("/messages/bris/bn", "/messages/bris/en");
    /**
     * Base directory for messages or translations.
     */
    @Value("${etcd.server.baseDir:/messages}")
    private String baseDir;
    /**
     * Base directory to watch in etcd.
     */
    @Value("${etcd.server.baseDirTowatch:/messages}")
    private String baseDirTowatch;
    /**
     * Key where locales are defined.
     */
    @Value("${etcd.server.localesKey:/locales}")
    private String localesKey;
    /**
     * Core pool size for the etcd connection thread pool.
     */
    @Value("${etcd.server.connection.corePoolSize:2500}")
    private int corePoolSize;
    /**
     * Maximum pool size for the etcd connection thread pool.
     */
    @Value("${etcd.server.connection.maxPoolSize:3000}")
    private int maxPoolSize;
    /**
     * Queue capacity for the etcd connection thread pool.
     */
    @Value("${etcd.server.connection.queueCapacity:1000}")
    private int queueCapacity;
    /**
     * Thread name prefix for the etcd connection thread pool.
     */
    @Value("${etcd.server.connection.threadNamePrefix:etcd-conn-}")
    private String threadNamePrefix;

    /**
     * Constructs an instance of EtcdTranslatorconfiguration.
     *
     * @param etcdLongBlockingThreadPoolTaskExecutor executor service for long-running blocking etcd tasks
     * @param etcdThreadPoolTaskExecutor             executor service for regular etcd tasks
     */
    public EtcdTranslatorconfiguration(ExecutorService etcdLongBlockingThreadPoolTaskExecutor, ExecutorService etcdThreadPoolTaskExecutor) {
        this.etcdLongBlockingThreadPoolTaskExecutor = etcdLongBlockingThreadPoolTaskExecutor;
        this.etcdThreadPoolTaskExecutor = etcdThreadPoolTaskExecutor;
    }

    /**
     * Creates a bean for the EtcdMessageSource if one is not already defined.
     *
     * @return a new instance of EtcdMessageSource
     */
    @Bean
    @ConditionalOnMissingBean(EtcdMessageSource.class)
    public EtcdMessageSource etcdMessageSource() {
        return new EtcdMessageSource(
                etcdClient(),
                baseDir,
                localesKey,
                baseDirTowatch,
                etcdLongBlockingThreadPoolTaskExecutor,
                true
        );
    }

    /**
     * Creates a bean for the EtcdClient if one is not already defined.
     *
     * @return a new instance of EtcdClient
     */
    @Bean
    @ConditionalOnMissingBean(EtcdClient.class)
    public EtcdClient etcdClient() {
        return EtcdClientFactory.createEtcdClient(
                hosts.toArray(new String[0]),
                port,
                etcdThreadPoolTaskExecutor,
                etcdLongBlockingThreadPoolTaskExecutor
        );
//        return new EtcdClientImpl(
//                hosts.toArray(new String[0]),
//                port,
//                etcdThreadPoolTaskExecutor,
//                etcdLongBlockingThreadPoolTaskExecutor
//        );
    }
}
