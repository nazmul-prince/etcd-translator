package io.etcd.springi18n;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.kv.GetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class AppCmdRunner implements CommandLineRunner {
    private final Logger log = LoggerFactory.getLogger(AppCmdRunner.class);
    private final MessageSource etcdMessageSource;

    public AppCmdRunner(MessageSource etcdMessageSource) {
        this.etcdMessageSource = etcdMessageSource;
    }

    public String test() {
        return testJetcd();
    }

    @Override
    public void run(String... args) throws Exception {
        testJetcd();
//        String message = etcdMessageSource.getMessage("/messages/bris/bn/greet.hello", null, Locale.ENGLISH);
//        System.out.println("message: " + message);
    }

    private String testJetcd() {
        String result = "not not";
        String etcdEndpoint = "http://localhost:2379";
        ByteSequence key = ByteSequence.from("/messages/bn/bris.greet.hello".getBytes());
        ByteSequence value = ByteSequence.from("Hello, etcd!".getBytes());

        try (Client client = Client.builder().endpoints(etcdEndpoint).build()) {
            KV kvClient = client.getKVClient();
//            kvClient.
            // Put a key-value pair
//            kvClient.put(key, value).get();

            // Retrieve the value using CompletableFuture
            CompletableFuture<GetResponse> getFuture = kvClient.get(key).exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
            GetResponse response = getFuture.get();
            result = response.getKvs().get(0).getValue().toString();
            log.info("message issssssssssss: " + result);

            // Delete the key
//            kvClient.delete(key).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
