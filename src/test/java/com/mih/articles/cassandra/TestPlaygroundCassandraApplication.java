package com.mih.articles.cassandra;

import org.springframework.boot.SpringApplication;

public class TestPlaygroundCassandraApplication {

    public static void main(String[] args) {

        SpringApplication.from(PlaygroundApplication::main)
                .with(TestContainersConfiguration.class)
                .run(args);
    }
}
