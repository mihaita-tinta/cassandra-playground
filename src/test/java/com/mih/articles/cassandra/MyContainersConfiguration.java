package com.mih.articles.cassandra;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraConnectionDetails;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;

import java.io.IOException;
import java.util.List;

@Configuration
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

    /**
     * TODO add ui:
     * <pre>
     *   toxiproxy-ui:
     *     image: "buckle/toxiproxy-frontend"
     *     ports:
     *       - "8470:8080"
     *     environment:
     *       TOXIPROXY_URL: http://toxiproxy:8474
     *     depends_on:
     *      - toxiproxy
     *   toxiproxy:
     *     image: "shopify/toxiproxy"
     *     ports:
     *       - "8474:8474"
     *       - "9242:9142"
     *     # We created proxy configuration from another ephermal container
     *   toxiproxy-config:
     *       image: "shopify/toxiproxy"
     *       entrypoint: >
     *         sh -c "/go/bin/toxiproxy-cli -h toxiproxy:8474 create cassandra --listen 0.0.0.0:9142 --upstream db:9142;"
     * </pre>
     * @param network
     * @return
     * @throws IOException
     */
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
    @DynamicPropertySource
    Proxy toxiCassandra(DynamicPropertyRegistry dynamicPropertyRegistry,
                        CassandraContainer db,
                        ToxiproxyContainer toxiproxy, ToxiproxyClient toxiproxyClient) throws IOException {

        var cassandra = toxiproxyClient.createProxy("cassandra", "0.0.0.0:8667", "cassandra:9042");

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
