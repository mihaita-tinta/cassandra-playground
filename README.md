# cassandra-playground

Start the application locally using `TestPlaygroundCassandraApplication`
It uses the `TestContainersConfiguration` class to start cassandra and toxiproxy containers before the application

```plantuml
@startuml
Client -> API : POST /accounts
API --> Toxiproxy: save new account
Toxiproxy --> Cassandra: save new account
Toxiproxy --> Cassandra: account
Toxiproxy --> API: account
API -> Client: Account response
@enduml
```

![Prometheus](docs/prometheus.png "Title")