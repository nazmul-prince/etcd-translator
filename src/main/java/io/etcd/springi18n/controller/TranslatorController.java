package io.etcd.springi18n.controller;

import io.etcd.springi18n.AppCmdRunner;
import io.etcd.springi18n.service.EtcdClient;
import io.etcd.springi18n.service.EtcdMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
public class TranslatorController {
    private final Logger log = LoggerFactory.getLogger(TranslatorController.class);
    @Autowired
    private AppCmdRunner appCmdRunner;

    @Autowired
    private EtcdMessageSource etcdMessageSource;

    @Autowired
    private EtcdClient etcdClient;

    @GetMapping("/translate/{key}")
    public String viewProperties(
            @PathVariable(name = "key") String key,
            @RequestParam(name = "args", defaultValue = "") List<String> args
    ) throws Exception {

        String message1 = "";
        String message2 = "";

        log.info("Got request for sending message to: ");
//        message1 = etcdMessageSource.getMessage("/messages/bn/bris.greet.hello", null, new Locale("bn"));
        log.info("/message1: " + message1);
        message2 = etcdMessageSource.getMessage("bris.greet.hello", new String[]{"111", "222"}, new Locale("en"));
        log.info("/message2: " + message2);
//        log.info("watcher closed: " + etcdClient.watcher.isClosed());
        return message1 + " : " + message2;
    }
}
