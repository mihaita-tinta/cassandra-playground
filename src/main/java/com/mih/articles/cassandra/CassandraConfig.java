package com.mih.articles.cassandra;

import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.autoconfigure.cassandra.DriverConfigLoaderBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Configuration
public class CassandraConfig {

    @Bean
    public CollectorRegistry buildCollectorRegistry() {
        return new CollectorRegistry();
    }

    @Bean
    public PrometheusMeterRegistry buildPrometheusMeterRegistry(CollectorRegistry collectorRegistry) {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT, collectorRegistry, Clock.SYSTEM);
    }

    @Bean
    CqlSessionBuilderCustomizer customizer(MeterRegistry registry) {
        return c -> {
            c.withMetricRegistry(registry);
        };
    }

    @Bean
    DriverConfigLoaderBuilderCustomizer moreConfig() {
        return c -> {
            c.withString(DefaultDriverOption.METRICS_ID_GENERATOR_PREFIX, "cassandra")
                    .withString(DefaultDriverOption.METRICS_FACTORY_CLASS, "MicrometerMetricsFactory")
                    .withString(DefaultDriverOption.METRICS_ID_GENERATOR_CLASS, "TaggingMetricIdGenerator")
                    .withStringList(DefaultDriverOption.METRICS_SESSION_ENABLED, new ArrayList<>() {
                        {
                            this.add("bytes-sent");
                            this.add("bytes-received");
                            this.add("connected-nodes");
                            this.add("cql-requests");
                            this.add("cql-client-timeouts");
                        }
                    })
                    .withStringList(DefaultDriverOption.METRICS_NODE_ENABLED, new ArrayList<String>() {
                        {
                            this.add("bytes-sent");
                            this.add("bytes-received");
                            this.add("cql-messages");
                            this.add("pool.open-connections");
                            this.add("pool.available-streams");
                            this.add("pool.in-flight");
                            this.add("pool.orphaned-streams");
                        }
                    });
        };
    }
}
