//package io.etcd.springi18n.controller;
//
//import io.etcd.springi18n.service.EtcdClient;
//import io.etcd.springi18n.service.EtcdMessageSource;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Locale;
//
//@RestController
//public class TranslatorController {
//    private final Logger log = LoggerFactory.getLogger(TranslatorController.class);
//
//    @Autowired
//    private EtcdMessageSource etcdMessageSource;
//
//    @GetMapping("/translate/{key}")
//    public String viewProperties(
//            @PathVariable(name = "key") String key,
//            @RequestParam(name = "args", defaultValue = "") List<String> args
//    ) throws Exception {
//
//        String message = "";
//
//        log.info("Got request for sending message to: ");
//        message = etcdMessageSource.getMessage("service.greet.hello", null, new Locale("en"));
//        log.info("got localized message: " + message);
//        return message;
//    }
//}
