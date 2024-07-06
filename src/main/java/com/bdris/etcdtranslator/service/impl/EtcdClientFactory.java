package com.bdris.etcdtranslator.service.impl;

import com.bdris.etcdtranslator.service.EtcdClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

public class EtcdClientFactory {

    public static EtcdClient createEtcdClient(String[] hosts, String port, ExecutorService taskExecutor, ExecutorService longBlockingTaskExecutor) {
        return new EtcdClientImpl(hosts, port, taskExecutor, longBlockingTaskExecutor);
    }
}
