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
import java.util.List;

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
                    .withStringList(DefaultDriverOption.METRICS_SESSION_ENABLED, List.of("cql-requests"))
            ;
        };
    }
}
