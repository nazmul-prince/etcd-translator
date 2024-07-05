package com.bdris.etcdtranslator;

import com.bdris.etcdtranslator.config.EtcdTranslatorconfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class EtcdtranslatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(EtcdtranslatorApplication.class, args);
	}

}
