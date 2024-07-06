package com.bdris.etcdtranslator.controller;

import com.bdris.etcdtranslator.AppCmdRunner;
import com.bdris.etcdtranslator.service.EtcdClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
public class TranslatorController {
    private final Logger log = LoggerFactory.getLogger(TranslatorController.class);
    @Autowired
    private AppCmdRunner appCmdRunner;

    @Autowired
    private EtcdClient etcdClient;

    @GetMapping("/translate/{key}")
    public String viewProperties(
            @PathVariable(name = "key") String key,
            @RequestParam(name = "args", defaultValue = "") List<String> args
    ) throws Exception {

//        log.info("watcher closed: " + etcdClient.watcher.isClosed());
        return appCmdRunner.test();
    }
}
