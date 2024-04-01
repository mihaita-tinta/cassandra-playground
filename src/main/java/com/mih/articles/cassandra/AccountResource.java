package com.mih.articles.cassandra;

import org.springframework.context.annotation.DependsOn;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@DependsOn("cassandraSession")
public class AccountResource {

    private final AccountRepository repository;

    public AccountResource(AccountRepository repository) {
        this.repository = repository;
    }

    /**
     * curl -v http://localhost:8080/accounts
     * @return
     */
    @GetMapping("/accounts")
    public List<Account> getAll() {
        return repository.findAll();
    }

    /**
     * curl -v  -d "{\"name\":\"asdasdas\"}" -H "Content-Type: application/json" -X POST http://localhost:8080/accounts
     * @param req
     * @return
     */
    @PostMapping("/accounts")
    public Account save(@RequestBody AccountCreateRequest req) {

        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setName(req.name());
        return repository.save(account);
    }

    record AccountCreateRequest(String name) {}
}
