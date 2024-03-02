package com.mih.articles.cassandra;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;

import java.io.IOException;

public class MyContainersConfiguration {


    @Bean
    CassandraContainer db(Network network) {
        CassandraContainer c = new CassandraContainer<>("cassandra:3.11.2")
                .withNetworkAliases("cassandra")
                .withNetwork(network)
                .withInitScript("schema.cql");

        return c;
    }

    @Bean
    Network network() {
        Network network = Network.newNetwork();
        return network;
    }

    @Bean
    ToxiproxyContainer toxiproxyContainer(Network network) throws IOException {
        ToxiproxyContainer toxiproxy = new ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.5.0")
                .withNetwork(network)
                .withExposedPorts(
                        8474, 8666, 8667
                )
                .withNetworkAliases("toxiproxy");
        return toxiproxy;
    }

    @Bean
    ToxiproxyClient toxiproxyClient(ToxiproxyContainer toxiproxy) {
        final ToxiproxyClient toxiproxyClient = new ToxiproxyClient(toxiproxy.getHost(), toxiproxy.getControlPort());
        return toxiproxyClient;
    }

    @Bean
    Proxy toxiCassandra(DynamicPropertyRegistry dynamicPropertyRegistry,
                        CassandraContainer db,
                        ToxiproxyContainer toxiproxy, ToxiproxyClient toxiproxyClient) throws IOException {

        var cassandra = toxiproxyClient.createProxy("cassandra", "0.0.0.0:8667", "cassandra:9042");

        dynamicPropertyRegistry.add("toxiProxyCassandraPort", () -> toxiproxy.getMappedPort(8667));
        return cassandra;
    }

}
