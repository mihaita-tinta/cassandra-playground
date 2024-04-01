package com.mih.articles.cassandra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataCassandraTest
@Import(TestContainersConfiguration.class)
class AccountRepositoryDataTest {

    @Autowired
    AccountRepository accountRepository;

    @Test
    void testSave() {
        var account = new Account();
        account.setId(UUID.randomUUID());
        account.setName("junit");
        Account saved = accountRepository.save(account);

        assertNotNull(saved);

    }

}
