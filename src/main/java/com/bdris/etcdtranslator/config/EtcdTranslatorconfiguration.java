package com.bdris.etcdtranslator.config;

import com.bdris.etcdtranslator.service.EtcdClient;
import com.bdris.etcdtranslator.service.impl.EtcdClientImpl;
import com.bdris.etcdtranslator.service.impl.EtcdMessageSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Configuration
@ConfigurationProperties(prefix = "etcd.server")
public class EtcdTranslatorconfiguration {

    @Value("${etcd.server.hosts:http://localhost}")
    private List<String> hosts = new ArrayList<>();

    @Value("${etcd.server.port:2379}")
    private String port;

    private List<String> dirs = List.of("/messages/bris/bn", "/messages/bris/en");

    @Value("${etcd.server.baseDir:/messages}")
    private String baseDir;

    @Value("${etcd.server.baseDirTowatch:/messages}")
    private String baseDirTowatch;

    @Value("${etcd.server.localesKey:/locales}")
    private String localesKey;
    @Value("${etcd.server.connection.corePoolSize:2500}")
    private int corePoolSize;
    @Value("${etcd.server.connection.maxPoolSize:3000}")
    private int maxPoolSize;
    @Value("${etcd.server.connection.queueCapacity:1000}")
    private int queueCapacity;
    @Value("${etcd.server.connection.threadNamePrefix:etcd-conn-}")
    private String threadNamePrefix;

    private final ExecutorService etcdLongBlockingThreadPoolTaskExecutor;
    private final ExecutorService etcdThreadPoolTaskExecutor;

    public EtcdTranslatorconfiguration(ExecutorService etcdLongBlockingThreadPoolTaskExecutor, ExecutorService etcdThreadPoolTaskExecutor) {
        this.etcdLongBlockingThreadPoolTaskExecutor = etcdLongBlockingThreadPoolTaskExecutor;
        this.etcdThreadPoolTaskExecutor = etcdThreadPoolTaskExecutor;
    }

    @Bean
    @ConditionalOnMissingBean(EtcdMessageSource.class)
    public EtcdMessageSource etcdMessageSource() {
        return new EtcdMessageSource(hosts.toArray(new String[hosts.size()]),
                port,
                etcdClient(),
                baseDir,
                localesKey,
                baseDirTowatch,
                etcdLongBlockingThreadPoolTaskExecutor);
    }

    @Bean
    @ConditionalOnMissingBean(EtcdClient.class)
    public EtcdClient etcdClient() {
        return new EtcdClientImpl(hosts.toArray(new String[hosts.size()]), port, etcdThreadPoolTaskExecutor, etcdLongBlockingThreadPoolTaskExecutor);
    }
}
