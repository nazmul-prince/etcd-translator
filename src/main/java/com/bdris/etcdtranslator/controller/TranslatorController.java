package com.bdris.etcdtranslator.controller;

import com.bdris.etcdtranslator.AppCmdRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
public class TranslatorController {

    @Autowired
    private AppCmdRunner appCmdRunner;

    @GetMapping("/translate/{key}")
    public String viewProperties(
            @PathVariable(name = "key") String key,
            @RequestParam(name = "args", defaultValue = "") List<String> args
    ) throws Exception {

        return appCmdRunner.test();
    }
}
