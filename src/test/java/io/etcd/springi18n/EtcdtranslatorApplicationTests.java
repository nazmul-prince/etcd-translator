package io.etcd.springi18n;

import io.etcd.springi18n.service.EtcdMessageSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

@SpringBootTest
class EtcdtranslatorApplicationTests {

	@Autowired
	private EtcdMessageSource etcdMessageSource;
	@Test
	void contextLoads() {
	}

	@Test
	void testCorrectMessage() {
		String message = etcdMessageSource.getMessage("service.greet.hello", null, new Locale("en"));
		assertThat(message).isEqualTo("Hello");
	}

}
