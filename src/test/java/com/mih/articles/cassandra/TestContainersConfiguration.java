package com.mih.articles.cassandra;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import eu.rekawek.toxiproxy.model.toxic.Latency;
import org.springframework.boot.autoconfigure.cassandra.CassandraConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;

import java.io.IOException;
import java.util.List;

@Configuration
public class TestContainersConfiguration {


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
    ToxiproxyContainer toxiproxyContainer(Network network) {
        ToxiproxyContainer toxiproxy = new ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.5.0")
                .withNetwork(network)
                .withExposedPorts(
                        8474, 8666, 8667
                )
                .withNetworkAliases("toxiproxy");
        return toxiproxy;
    }

    @Bean
    GenericContainer toxiproxyUI(Network network) {
        GenericContainer ui = new GenericContainer<>("buckle/toxiproxy-frontend")
                .withNetwork(network)
                .withExposedPorts(
                        8080
                )
                .withEnv("TOXIPROXY_URL", "http://toxiproxy:8474")
                .withNetworkAliases("toxiproxy-ui");
        return ui;
    }

    @Bean
    ToxiproxyClient toxiproxyClient(ToxiproxyContainer toxiproxy) {
        final ToxiproxyClient toxiproxyClient = new ToxiproxyClient(toxiproxy.getHost(), toxiproxy.getControlPort());
        return toxiproxyClient;
    }

    @Bean
    Proxy toxiCassandra(CassandraContainer db,
                        ToxiproxyContainer toxiproxy, ToxiproxyClient toxiproxyClient) throws IOException {

        var cassandra = toxiproxyClient.createProxy("cassandra", "0.0.0.0:8667", "cassandra:9042");
//        Latency latency = cassandra.toxics()
//                .latency("latency", ToxicDirection.DOWNSTREAM, 10);

        return cassandra;
    }

    @Bean
    CassandraConnectionDetails cassandraConnectionDetails(Proxy toxiCassandra,
                                                          ToxiproxyContainer toxiproxy) {
        return new CassandraConnectionDetails() {
            @Override
            public List<Node> getContactPoints() {
                return List.of(new Node("localhost", toxiproxy.getMappedPort(8667)));
            }

            @Override
            public String getLocalDatacenter() {
                return "datacenter1";
            }
        };
    }

}
