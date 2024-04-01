package com.mih.articles.cassandra;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import eu.rekawek.toxiproxy.model.toxic.Latency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureObservability
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = "logging.level.org.springframework.data.cassandra.core.cql=DEBUG")
@Import(TestContainersConfiguration.class)
class AccountRepositoryTest {
    @LocalManagementPort
    int managementPort;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    Proxy toxiCassandra;

    @Test
    void test() throws IOException {
        System.out.println("Metrics can be checked here: http://localhost:" + managementPort + "/actuator/prometheus" );
        Latency latency = toxiCassandra.toxics()
                .latency("latency2", ToxicDirection.DOWNSTREAM, 400);
//        latency.setJitter(50);
        IntStream.range(0, 100)
                .forEach(i -> {
                    try {
                        int value = 10 * i;
                        System.out.println("Latency: " + value);
                        latency.setLatency(value);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    var account = new Account();
                    account.setId(UUID.randomUUID());
                    account.setName("junit");
                    Account saved = accountRepository.save(account);

                    assertNotNull(saved);
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

    }

}
