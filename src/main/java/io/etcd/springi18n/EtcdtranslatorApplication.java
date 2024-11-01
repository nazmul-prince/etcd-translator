package io.etcd.springi18n;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableConfigurationProperties
public class EtcdtranslatorApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(EtcdtranslatorApplication.class);
		app.setWebApplicationType(WebApplicationType.NONE);  // Disable web environment
		app.run(args);
	}

}
