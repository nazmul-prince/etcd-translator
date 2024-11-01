package io.etcd.springi18n.service.impl;

import io.etcd.springi18n.service.EtcdClient;

import java.util.concurrent.ExecutorService;

public class EtcdClientFactory {

    public static EtcdClient createEtcdClient(String[] hosts, String port, ExecutorService taskExecutor, ExecutorService longBlockingTaskExecutor) {
        return EtcdClientImpl.create(hosts, port, taskExecutor, longBlockingTaskExecutor);
    }
}
