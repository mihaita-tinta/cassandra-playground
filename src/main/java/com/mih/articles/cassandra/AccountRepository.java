package com.mih.articles.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

public interface AccountRepository extends CassandraRepository<Account, UUID> {
}
