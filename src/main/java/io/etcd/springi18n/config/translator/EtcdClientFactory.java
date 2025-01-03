package io.etcd.springi18n.config.translator;

import java.util.concurrent.ExecutorService;

public class EtcdClientFactory {

    static EtcdClient createEtcdClient(String[] hosts, String port, ExecutorService taskExecutor, ExecutorService longBlockingTaskExecutor) {
        return EtcdClientImpl.create(hosts, port, taskExecutor, longBlockingTaskExecutor);
    }
}
